package org.litepal.crud;

import static org.litepal.util.BaseUtility.changeCase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.litepal.LitePalBase;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DataSupportException;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is the base class for CRUD component. All the common actions which can
 * be shared with each function in CURD component will be put here.
 * 
 * @author Tony Green
 * @since 1.1
 */
abstract class DataHandler extends LitePalBase {
	public static final String TAG = "DataHandler";

	/**
	 * Instance of SQLiteDatabase, use to do the CRUD job.
	 */
	SQLiteDatabase mDatabase;

	/**
	 * Store empty model instance. In case to create each time when checking
	 * field is with default value or not.
	 */
	private DataSupport tempEmptyModel;

	/**
	 * Iterate all the fields passed in. Each field calls
	 * {@link #putFieldsValueDependsOnSaveOrUpdate(DataSupport, Field, ContentValues)}
	 * if it's not id field.
	 * 
	 * @param baseObj
	 *            Current model to persist or update.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	protected void putFieldsValue(DataSupport baseObj, List<Field> supportedFields,
			ContentValues values) {
		try {
			for (Field field : supportedFields) {
				if (!isIdColumn(field.getName())) {
					putFieldsValueDependsOnSaveOrUpdate(baseObj, field, values);
				}
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * This method deals with the putting values job into ContentValues. The
	 * ContentValues has <b>put</b> method to set data. But we do not know we
	 * should use which <b>put</b> method cause the field type isn't clear. So
	 * the reflection API is necessary here to put values into ContentValues
	 * with dynamically getting field type to put value.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @param field
	 *            Field to put into ContentValues.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected void putContentValues(DataSupport baseObj, Field field, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Object fieldValue = takeGetMethodValueByField(baseObj, field);
		Object[] parameters = new Object[] { changeCase(field.getName()), fieldValue };
		Class<?>[] parameterTypes = getParameterTypes(field, fieldValue, parameters);
		DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
	}

	/**
	 * It finds the getter method by the field. For example, field name is age,
	 * getter method name will be getAge. Then invoke the getter method and
	 * return the value.
	 * 
	 * @param dataSupport
	 *            The model to get method from.
	 * @param field
	 *            Use to generate getter method name.
	 * @return The value returned by getter method.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected Object takeGetMethodValueByField(DataSupport dataSupport, Field field)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (shouldGetOrSet(dataSupport, field)) {
			String getterMethodPrefix;
			if (isPrimitiveBooleanType(field)) {
				getterMethodPrefix = "is";
			} else {
				getterMethodPrefix = "get";
			}
			String getMethodName = getterMethodPrefix + BaseUtility.capitalize(field.getName());
			return DynamicExecutor.send(dataSupport, getMethodName, null, dataSupport.getClass(),
					null);
		}
		return null;
	}

	/**
	 * It finds the setter method by the field. For example, field name is age,
	 * setter method name will be setAge. Then invoke the setter method with
	 * necessary parameter.
	 * 
	 * @param dataSupport
	 *            The model to set method to.
	 * @param field
	 *            Use to generate setter method name.
	 * @param parameter
	 *            The parameter to invoke setter method.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected void putSetMethodValueByField(DataSupport dataSupport, Field field, Object parameter)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (shouldGetOrSet(dataSupport, field)) {
			String setterMethodPrefix = "set";
			String setMethodName = setterMethodPrefix + BaseUtility.capitalize(field.getName());
			DynamicExecutor.send(dataSupport, setMethodName, new Object[] { parameter },
					dataSupport.getClass(), new Class[] { field.getType() });
		}
	}

	/**
	 * Find all the associated models of currently model. Then add all the
	 * associated models into {@link DataSupport#associatedModels} of baseObj.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @param foreignKeyId
	 *            The id value of foreign key.
	 */
	protected void analyzeAssociatedModels(DataSupport baseObj,
			Collection<AssociationsInfo> associationInfos) {
		try {
			for (AssociationsInfo associationInfo : associationInfos) {
				if (associationInfo.getAssociationType() == Const.Model.MANY_TO_ONE) {
					new Many2OneAnalyzer().analyze(baseObj, associationInfo);
				} else if (associationInfo.getAssociationType() == Const.Model.ONE_TO_ONE) {
					new One2OneAnalyzer().analyze(baseObj, associationInfo);
				} else if (associationInfo.getAssociationType() == Const.Model.MANY_TO_MANY) {
					new Many2ManyAnalyzer().analyze(baseObj, associationInfo);
				}
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Create an empty instance of baseObj if it hasn't created one yet. If
	 * there's already an empty model existed in {@link #tempEmptyModel}, no
	 * need to create a new one.
	 * 
	 * @param baseObj
	 *            Current model to update.
	 * @return An empty instance of baseObj.
	 */
	protected DataSupport getEmptyModel(DataSupport baseObj) {
		if (tempEmptyModel != null) {
			return tempEmptyModel;
		}
		String className = null;
		try {
			className = baseObj.getClassName();
			Class<?> modelClass = Class.forName(className);
			tempEmptyModel = (DataSupport) modelClass.newInstance();
			return tempEmptyModel;
		} catch (ClassNotFoundException e) {
			throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND
					+ className);
		} catch (InstantiationException e) {
			throw new DataSupportException(className + DataSupportException.INSTANTIATION_EXCEPTION);
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Check the number of question mark existed in conditions[0] equals the
	 * number of rest conditions elements or not. If not equals, throws
	 * DataSupportException.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @throws DataSupportException
	 */
	protected void checkConditionsCorrect(String[] conditions) {
		if (conditions != null) {
			int conditionsSize = conditions.length;
			if (conditionsSize > 0) {
				String whereClause = conditions[0];
				int placeHolderSize = BaseUtility.count(whereClause, "?");
				if (conditionsSize != placeHolderSize + 1) {
					throw new DataSupportException(DataSupportException.UPDATE_CONDITIONS_EXCEPTION);
				}
			}
		}
	}

	/**
	 * Get the WHERE clause to apply when updating or deleting multiple rows.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The WHERE clause to apply when updating or deleting multiple
	 *         rows.
	 */
	protected String getWhereClause(String[] conditions) {
		if (isAffectAllLines(conditions)) {
			return null;
		}
		if (conditions != null && conditions.length > 0) {
			return conditions[0];
		}
		return null;
	}

	/**
	 * Get the WHERE arguments to fill into where clause when updating or
	 * deleting multiple rows.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The WHERE arguments to fill into where clause when updating or
	 *         deleting multiple rows.
	 */
	protected String[] getWhereArgs(String[] conditions) {
		if (isAffectAllLines(conditions)) {
			return null;
		}
		if (conditions != null && conditions.length > 1) {
			String[] whereArgs = new String[conditions.length - 1];
			System.arraycopy(conditions, 1, whereArgs, 0, conditions.length - 1);
			return whereArgs;
		}
		return null;
	}
	
	/**
	 * Get the where clause by the passed in id collection to update tables.
	 * 
	 * @param ids
	 *            The id collection.
	 * @return The where clause to execute.
	 */
	protected String getWhereOfIdsWithOr(Collection<Long> ids) {
		StringBuilder whereClause = new StringBuilder();
		boolean needOr = false;
		for (long id : ids) {
			if (needOr) {
				whereClause.append(" or ");
			}
			needOr = true;
			whereClause.append("id = ");
			whereClause.append(id);
		}
		return changeCase(whereClause.toString());
	}

	/**
	 * Do not suggest use this method to find DataSupport class from hierarchy.
	 * Try to use DataSupport.class directly.
	 * 
	 * Detect the baseObj is an instance of DataSupport or not. If true, return
	 * the class of DataSupport. Otherwise throw an exception of
	 * DataSupportException to tell user baseObj is not an instance of
	 * DataSupport.
	 * 
	 * @param baseObj
	 *            The base object model.
	 * @return The class of DataSupport or throw DataSupportException.
	 * @throws DataSupportException
	 */
	@Deprecated
	protected Class<?> findDataSupportClass(DataSupport baseObj) {
		Class<?> superClass = null;
		while (true) {
			superClass = baseObj.getClass().getSuperclass();
			if (superClass == null || DataSupport.class == superClass) {
				break;
			}
		}
		if (superClass == null) {
			throw new DataSupportException(baseObj.getClass().getName()
					+ DataSupportException.MODEL_IS_NOT_AN_INSTANCE_OF_DATA_SUPPORT);
		}
		return superClass;
	}

	/**
	 * When executing {@link #takeGetMethodValueByField(DataSupport, Field)} or
	 * {@link #putSetMethodValueByField(DataSupport, Field, Object)}, the
	 * dataSupport and field passed in should be protected from null value.
	 * 
	 * @param dataSupport
	 *            The object to execute set or get method.
	 * @param field
	 *            The field of generating set and get methods.
	 * @return True if dataSupport and field are not null, false otherwise.
	 */
	protected boolean shouldGetOrSet(DataSupport dataSupport, Field field) {
		return dataSupport != null && field != null;
	}

	/**
	 * Get the name of intermediate join table.
	 * 
	 * @param baseObj
	 *            Current model.
	 * @param associatedTableName
	 *            The name of associated table.
	 * @return The name of intermediate join table.
	 */
	protected String getIntermediateTableName(DataSupport baseObj, String associatedTableName) {
		return changeCase(DBUtility.getIntermediateTableName(baseObj.getTableName(),
				associatedTableName));
	}

	/**
	 * Get the simple name of modelClass. Then change the case by the setting
	 * rule in litepal.xml as table name.
	 * 
	 * @param modelClass
	 *            Class of model to get table name from.
	 * @return The table name of model.
	 */
	protected String getTableName(Class<?> modelClass) {
		return BaseUtility.changeCase(modelClass.getSimpleName());
	}

	/**
	 * Get the types of parameters for {@link ContentValues#put}. Need two
	 * parameters. First is String type for key. Second is depend on field for
	 * value.
	 * 
	 * @param field
	 *            The field to get parameter type.
	 * @param fieldValue
	 *            Value of the field. Only used to convert to String when the
	 *            field is char.
	 * @param parameters
	 *            If the field is char, convert the value to String at index 1.
	 * @return The types of parameters for {@link ContentValues#put}.
	 */
	private Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
		Class<?>[] parameterTypes;
		if (isCharType(field)) {
			parameters[1] = String.valueOf(fieldValue);
			parameterTypes = new Class[] { String.class, String.class };
		} else {
			if (field.getType().isPrimitive()) {
				parameterTypes = new Class[] { String.class, getObjectType(field.getType()) };
			} else {
				parameterTypes = new Class[] { String.class, field.getType() };
			}
		}
		return parameterTypes;
	}

	/**
	 * Each primitive type has a corresponding object type. For example int and
	 * Integer, boolean and Boolean. This method gives a way to turn primitive
	 * type into object type.
	 * 
	 * @param primitiveType
	 *            The class of primitive type.
	 * @return If the passed in parameter is primitive type, return a
	 *         corresponding object type. Otherwise return null.
	 */
	private Class<?> getObjectType(Class<?> primitiveType) {
		if (primitiveType != null) {
			if (primitiveType.isPrimitive()) {
				String basicTypeName = primitiveType.getName();
				if ("int".equals(basicTypeName)) {
					return Integer.class;
				} else if ("short".equals(basicTypeName)) {
					return Short.class;
				} else if ("long".equals(basicTypeName)) {
					return Long.class;
				} else if ("float".equals(basicTypeName)) {
					return Float.class;
				} else if ("double".equals(basicTypeName)) {
					return Double.class;
				} else if ("boolean".equals(basicTypeName)) {
					return Boolean.class;
				} else if ("char".equals(basicTypeName)) {
					return Character.class;
				}
			}
		}
		return null;
	}

	/**
	 * Judge if the field is char or Character type.
	 * 
	 * @param field
	 *            Field to judge type.
	 * @return Return true if it's char or Character. Otherwise return false.
	 */
	private boolean isCharType(Field field) {
		String type = field.getType().getName();
		return type.equals("char") || type.endsWith("Character");
	}

	/**
	 * Judge a field is a primitive boolean type or not. Cause there's something
	 * special when use eclipse to generate getter method. The primitive boolean
	 * type won't be like <b>getXxx</b>, it's something like <b>isXxx</b>.
	 * 
	 * @param field
	 *            Use field to get field type.
	 * @return If it's primitive boolean type return true, else return false.
	 */
	private boolean isPrimitiveBooleanType(Field field) {
		Class<?> fieldType = field.getType();
		if ("boolean".equals(fieldType.getName())) {
			return true;
		}
		return false;
	}

	/**
	 * Put the value of field into ContentValues if current action is saving.
	 * Check the value of field is default value or not if current action is
	 * updating. If it's not default value, put it into ContentValues. Otherwise
	 * ignore it.
	 * 
	 * @param baseObj
	 *            Current model to persist or update.
	 * @param field
	 *            With value to put into ContentValues.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void putFieldsValueDependsOnSaveOrUpdate(DataSupport baseObj, Field field,
			ContentValues values) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (isUpdating()) {
			if (!isFieldWithDefaultValue(baseObj, field)) {
				putContentValues(baseObj, field, values);
			}
		} else if (isSaving()) {
			putContentValues(baseObj, field, values);
		}
	}

	/**
	 * Current action is updating or not. Note that update the record by saving
	 * the already saved record again belongs to save action.
	 * 
	 * @return If current action is updating return true. Otherwise return
	 *         false.
	 */
	private boolean isUpdating() {
		return UpdateHandler.class.getName().equals(getClass().getName());
	}

	/**
	 * Current action is saving or not. Note that update the record by saving
	 * the already saved record again belongs to save action.
	 * 
	 * @return If current action is saving return true. Otherwise return false.
	 */
	private boolean isSaving() {
		return SaveHandler.class.getName().equals(getClass().getName());
	}

	/**
	 * Analyze the passed in field. Judge if this field is with default value.
	 * The baseObj need a default constructor or {@link DataSupportException}
	 * will be thrown.
	 * 
	 * @param baseObj
	 *            Current model to update.
	 * @param field
	 *            To judge if with default value.
	 * @return If the field is with default value, return true. Otherwise return
	 *         false.
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws DatabaseGenerateException
	 * @throws DataSupportException
	 */
	private boolean isFieldWithDefaultValue(DataSupport baseObj, Field field)
			throws IllegalAccessException, SecurityException, IllegalArgumentException,
			NoSuchMethodException, InvocationTargetException {
		DataSupport emptyModel = getEmptyModel(baseObj);
		Object realReturn = takeGetMethodValueByField(baseObj, field);
		Object defaultReturn = takeGetMethodValueByField(emptyModel, field);
		if (realReturn != null && defaultReturn != null) {
			String realFieldValue = takeGetMethodValueByField(baseObj, field).toString();
			String defaultFieldValue = takeGetMethodValueByField(emptyModel, field).toString();
			return realFieldValue.equals(defaultFieldValue);
		}
		return realReturn == defaultReturn;
	}

	/**
	 * Check the passing conditions represent to affect all lines or not. <br>
	 * Here are the supported format means affect all lines.
	 * 
	 * <pre>
	 * null
	 * new String[] {}
	 * new String[] { null }
	 * new String[] { "" }
	 * </pre>
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return Affect all lines or not.
	 */
	private boolean isAffectAllLines(String[] conditions) {
		if (conditions == null) {
			return true;
		}
		if (conditions.length == 0) {
			return true;
		}
		String whereClause = conditions[0];
		if (whereClause == null || "".equals(whereClause.trim())) {
			return true;
		}
		return false;
	}

}
