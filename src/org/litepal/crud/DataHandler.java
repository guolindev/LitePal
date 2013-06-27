package org.litepal.crud;

import static org.litepal.util.BaseUtility.changeCase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import android.database.Cursor;
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
	 * Query the table of the given model, returning a model list over the
	 * result set.
	 * 
	 * @param modelClass
	 *            The model to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param selection
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the
	 *            values from selectionArgs, in order that they appear in the
	 *            selection. The values will be bound as Strings.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @return A model list. The list may be empty.
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> query(Class<T> modelClass, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy) {
		List<T> dataList = new ArrayList<T>();
		Cursor cursor = null;
		try {
			List<Field> supportedFields = getSupportedFields(modelClass.getName());
			String tableName = getTableName(modelClass);
			cursor = mDatabase.query(tableName, columns, selection, selectionArgs, groupBy, having,
					orderBy);
			if (cursor.moveToFirst()) {
				do {
					Constructor<?> constructor = findBestSuitConstructor(modelClass);
					T modelInstance = (T) constructor
							.newInstance(getConstructorParams(constructor));
					setValueToModel(modelInstance, supportedFields, cursor);
					dataList.add(modelInstance);
				} while (cursor.moveToNext());
			}
			return dataList;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

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
			String getMethodName = makeGetterMethodName(field);
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
			String setMethodName = makeSetterMethodName(field);
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
	protected void checkConditionsCorrect(String... conditions) {
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
	protected String getWhereClause(String... conditions) {
		if (isAffectAllLines((Object) conditions)) {
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
	protected String[] getWhereArgs(String... conditions) {
		if (isAffectAllLines((Object) conditions)) {
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
	 * Check the passing conditions represent to affect all lines or not. <br>
	 * Do not pass anything to the conditions parameter means affect all lines.
	 * 
	 * @param conditions
	 *            An array representing the WHERE part of an SQL
	 *            statement.
	 * @return Affect all lines or not.
	 */
	protected boolean isAffectAllLines(Object... conditions) {
		if (conditions != null && conditions.length == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Get the where clause by the passed in id collection to apply multiple
	 * rows.
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
	 * Get the where clause by the passed in id array to apply multiple rows.
	 * 
	 * @param ids
	 *            The id collection.
	 * @return The where clause to execute.
	 */
	protected String getWhereOfIdsWithOr(long... ids) {
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
	 * Finds the best suit constructor for creating an instance of a class. The
	 * principle is that constructor with least parameters will be the best suit
	 * one to create instance. So this method will find the constructor with
	 * least parameters of the passed in class.
	 * 
	 * @param modelClass
	 *            To get constructors from.
	 * @return The best suit constructor with least parameters.
	 */
	protected Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
		Constructor<?> finalConstructor = null;
		Constructor<?>[] constructors = modelClass.getConstructors();
		for (Constructor<?> constructor : constructors) {
			if (finalConstructor == null) {
				finalConstructor = constructor;
			} else {
				int finalParamLength = finalConstructor.getParameterTypes().length;
				int newParamLength = constructor.getParameterTypes().length;
				if (newParamLength < finalParamLength) {
					finalConstructor = constructor;
				}
			}
		}
		finalConstructor.setAccessible(true);
		return finalConstructor;
	}

	/**
	 * Depends on the passed in constructor, creating a parameters array with
	 * initialized values for the constructor.
	 * 
	 * @param constructor
	 *            The constructor to get parameters for it.
	 * 
	 * @return A parameters array with initialized values.
	 */
	protected Object[] getConstructorParams(Constructor<?> constructor) {
		Class<?>[] paramTypes = constructor.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			params[i] = getInitParamValue(paramTypes[i]);
		}
		return params;
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
	 * Gives the passed in parameter an initialized value. If the parameter is
	 * basic data type or the corresponding object data type, return the default
	 * data. Or return null.
	 * 
	 * @param paramType
	 *            Parameter to get initialized value.
	 * @return Default data of basic data type or null.
	 */
	private Object getInitParamValue(Class<?> paramType) {
		String paramTypeName = paramType.getName();
		if ("boolean".equals(paramTypeName) || "java.lang.Boolean".equals(paramTypeName)) {
			return false;
		}
		if ("float".equals(paramTypeName) || "java.lang.Float".equals(paramTypeName)) {
			return 0f;
		}
		if ("double".equals(paramTypeName) || "java.lang.Double".equals(paramTypeName)) {
			return 0.0;
		}
		if ("int".equals(paramTypeName) || "java.lang.Integer".equals(paramTypeName)) {
			return 0;
		}
		if ("long".equals(paramTypeName) || "java.lang.Long".equals(paramTypeName)) {
			return 0l;
		}
		if ("short".equals(paramTypeName) || "java.lang.Short".equals(paramTypeName)) {
			return 0;
		}
		if ("char".equals(paramTypeName) || "java.lang.Character".equals(paramTypeName)) {
			return ' ';
		}
		if ("java.lang.String".equals(paramTypeName)) {
			return "";
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
	 * Generate the getter method name by field, following the eclipse rule.
	 * 
	 * @param field
	 *            The field to generate getter method from.
	 * @return The generated getter method name.
	 */
	private String makeGetterMethodName(Field field) {
		String getterMethodPrefix;
		String fieldName = field.getName();
		if (isPrimitiveBooleanType(field)) {
			if (fieldName.matches("^is[A-Z]{1}.*$")) {
				fieldName = fieldName.substring(2);
			}
			getterMethodPrefix = "is";
		} else {
			getterMethodPrefix = "get";
		}
		return getterMethodPrefix + BaseUtility.capitalize(fieldName);
	}

	/**
	 * Generate the setter method name by field, following the eclipse rule.
	 * 
	 * @param field
	 *            The field to generate setter method from.
	 * @return The generated setter method name.
	 */
	private String makeSetterMethodName(Field field) {
		String setterMethodName;
		String setterMethodPrefix = "set";
		if (isPrimitiveBooleanType(field) && field.getName().matches("^is[A-Z]{1}.*$")) {
			setterMethodName = setterMethodPrefix + field.getName().substring(2);
		} else {
			setterMethodName = setterMethodPrefix + BaseUtility.capitalize(field.getName());
		}
		return setterMethodName;
	}

	/**
	 * Generates the getType method for cursor based on field. There're two
	 * unusual conditions. If field type is boolean, generate getInt method. If
	 * field type is char, generate getString method.
	 * 
	 * @param field
	 *            To generate getType method for cursor.
	 * @return The getType method for cursor.
	 */
	private String genGetColumnMethod(Field field) {
		String typeName;
		Class<?> fieldType = field.getType();
		if (fieldType.isPrimitive()) {
			typeName = BaseUtility.capitalize(fieldType.getName());
		} else {
			typeName = fieldType.getSimpleName();
		}
		String methodName = "get" + typeName;
		if ("getBoolean".equals(methodName)) {
			methodName = "getInt";
		} else if ("getChar".equals(methodName)) {
			methodName = "getString";
		}
		return methodName;
	}

	/**
	 * Get value from database by cursor, then set the value into modelInstance.
	 * 
	 * @param modelInstance
	 *            The model to set into.
	 * @param supportedFields
	 *            Corresponding to each column in database.
	 * @param cursor
	 *            Use to get value from database.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void setValueToModel(Object modelInstance, List<Field> supportedFields, Cursor cursor)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		for (Field field : supportedFields) {
			String getMethodName = genGetColumnMethod(field);
			int columnIndex = cursor.getColumnIndex(BaseUtility.changeCase(field.getName()));
			if (columnIndex != -1) {
				Class<?> cursorClass = cursor.getClass();
				Method method = cursorClass.getMethod(getMethodName, int.class);
				Object value = method.invoke(cursor, columnIndex);
				if (isIdColumn(field.getName())) {
					DynamicExecutor.setField(modelInstance, field.getName(), value,
							modelInstance.getClass());
				} else {
					if (field.getType() == boolean.class || field.getType() == Boolean.class) {
						if ("0".equals(String.valueOf(value))) {
							value = false;
						} else if ("1".equals(String.valueOf(value))) {
							value = true;
						}
					} else if (field.getType() == char.class || field.getType() == Character.class) {
						value = ((String) value).charAt(0);
					}
					putSetMethodValueByField((DataSupport) modelInstance, field, value);
				}
			}
		}
	}

}
