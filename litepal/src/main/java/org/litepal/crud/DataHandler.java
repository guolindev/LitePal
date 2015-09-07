/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.crud;

import static org.litepal.util.BaseUtility.changeCase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import android.util.SparseArray;

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
	 * Holds the AssociationsInfo which foreign keys in the current model.
	 */
	private List<AssociationsInfo> fkInCurrentModel;

	/**
	 * Holds the AssociationsInfo which foreign keys in other models.
	 */
	private List<AssociationsInfo> fkInOtherModel;

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
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause. Passing null denotes no LIMIT clause.
	 * @param foreignKeyAssociations
	 *            Associated classes which have foreign keys in the current
	 *            model's table.
	 * @return A model list. The list may be empty.
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> query(Class<T> modelClass, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy, String limit,
			List<AssociationsInfo> foreignKeyAssociations) {
		List<T> dataList = new ArrayList<T>();
		Cursor cursor = null;
		try {
			List<Field> supportedFields = getSupportedFields(modelClass.getName());
			String tableName = getTableName(modelClass);
			String[] customizedColumns = getCustomizedColumns(columns, foreignKeyAssociations);
			cursor = mDatabase.query(tableName, customizedColumns, selection, selectionArgs,
					groupBy, having, orderBy, limit);
			if (cursor.moveToFirst()) {
				do {
					T modelInstance = (T) createInstanceFromClass(modelClass);
					giveBaseObjIdValue((DataSupport) modelInstance,
							cursor.getLong(cursor.getColumnIndexOrThrow("id")));
					setValueToModel(modelInstance, supportedFields, foreignKeyAssociations, cursor);
					if (foreignKeyAssociations != null) {
						setAssociatedModel((DataSupport) modelInstance);
					}
					dataList.add(modelInstance);
				} while (cursor.moveToNext());
			}
			return dataList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataSupportException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Handles the math query of the given table.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param type
	 *            The type of the based on column.
	 * @return The result calculating by SQL.
	 */
	@SuppressWarnings("unchecked")
	protected <T> T mathQuery(String tableName, String[] columns, String[] conditions, Class<T> type) {
		BaseUtility.checkConditionsCorrect(conditions);
		Cursor cursor = null;
		T result = null;
		try {
			cursor = mDatabase.query(tableName, columns, getWhereClause(conditions),
					getWhereArgs(conditions), null, null, null);
			if (cursor.moveToFirst()) {
				Class<?> cursorClass = cursor.getClass();
				Method method = cursorClass.getMethod(genGetColumnMethod(type), int.class);
				result = (T) method.invoke(cursor, 0);
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * Assign the generated id value to {@link DataSupport#baseObjId}. This
	 * value will be used as identify of this model for system use.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @param id
	 *            The value of id.
	 */
	protected void giveBaseObjIdValue(DataSupport baseObj, long id) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (id > 0) {
			DynamicExecutor.setField(baseObj, "baseObjId", id, DataSupport.class);
		}
	}

	/**
	 * Iterate all the fields passed in. Each field calls
	 * {@link #putFieldsValueDependsOnSaveOrUpdate(DataSupport, java.lang.reflect.Field, android.content.ContentValues)}
	 * if it's not id field.
	 * 
	 * @param baseObj
	 *            Current model to persist or update.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	protected void putFieldsValue(DataSupport baseObj, List<Field> supportedFields,
			ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		for (Field field : supportedFields) {
			if (!isIdColumn(field.getName())) {
				putFieldsValueDependsOnSaveOrUpdate(baseObj, field, values);
			}
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
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	protected void putContentValues(DataSupport baseObj, Field field, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Object fieldValue = takeGetMethodValueByField(baseObj, field);
		if ("java.util.Date".equals(field.getType().getName()) && fieldValue != null) {
			Date date = (Date) fieldValue;
			fieldValue = date.getTime();
		}
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
	 * @throws java.lang.reflect.InvocationTargetException
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
	 * @throws java.lang.reflect.InvocationTargetException
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
	 * associated models into baseObj.
	 * 
	 * @param baseObj
	 *            The class of base object.
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
	 * Get the associated model.
	 * 
	 * @param baseObj
	 *            The instance of self model.
	 * @param associationInfo
	 *            To get the associated model.
	 * @return The associated model of self model by analyzing associationInfo.
	 * 
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	protected DataSupport getAssociatedModel(DataSupport baseObj, AssociationsInfo associationInfo)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		return (DataSupport) takeGetMethodValueByField(baseObj,
				associationInfo.getAssociateOtherModelFromSelf());
	}

	/**
	 * Get the associated models collection. When it comes to many2one or
	 * many2many association. A model may have lots of associated models.
	 * 
	 * @param baseObj
	 *            The instance of self model.
	 * @param associationInfo
	 *            To get the associated models collection.
	 * @return The associated models collection of self model by analyzing
	 *         associationInfo.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	protected Collection<DataSupport> getAssociatedModels(DataSupport baseObj,
			AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return (Collection<DataSupport>) takeGetMethodValueByField(baseObj,
				associationInfo.getAssociateOtherModelFromSelf());
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
	 *            An array representing the WHERE part of an SQL statement.
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
	 * When executing {@link #takeGetMethodValueByField(DataSupport, java.lang.reflect.Field)} or
	 * {@link #putSetMethodValueByField(DataSupport, java.lang.reflect.Field, Object)}, the
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
		return BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName()));
	}
	
	/**
	 * Creates an instance from the passed in class. It will always create an
	 * instance no matter how the constructor defines in the class file. A best
	 * suit constructor will be find by calling
	 * {@link #findBestSuitConstructor(Class)} method.
	 * 
	 * @param modelClass
	 *            The class to create instance.
	 * @return An instance by the passed in class.
	 */
	protected Object createInstanceFromClass(Class<?> modelClass) {
		try {
			Constructor<?> constructor = findBestSuitConstructor(modelClass);
			return constructor.newInstance(getConstructorParams(modelClass, constructor));
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Finds the best suit constructor for creating an instance of a class. The
	 * principle is that the constructor with least parameters and has no self
	 * type parameter will be the best suit one to create instance.
	 * 
	 * @param modelClass
	 *            To get constructors from.
	 * @return The best suit constructor.
	 */
	protected Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
		Constructor<?>[] constructors = modelClass.getDeclaredConstructors();
		SparseArray<Constructor<?>> map = new SparseArray<Constructor<?>>();
		int minKey = Integer.MAX_VALUE;
		for (Constructor<?> constructor : constructors) {
			int key = constructor.getParameterTypes().length;
			Class<?>[] types = constructor.getParameterTypes();
			for (Class<?> parameterType : types) {
				if (parameterType == modelClass) {
					key = key + 10000;
				}
			}
			if (map.get(key) == null) {
				map.put(key, constructor);
			}
			if (key < minKey) {
				minKey = key;
			}
		}
		Constructor<?> bestSuitConstructor = map.get(minKey);
		if (bestSuitConstructor != null) {
			bestSuitConstructor.setAccessible(true);
		}
		return bestSuitConstructor;
	}

	/**
	 * Depends on the passed in constructor, creating a parameters array with
	 * initialized values for the constructor.
	 * 
	 * @param modelClass
	 *            The original class the this constructor belongs to.
	 * @param constructor
	 *            The constructor to get parameters for it.
	 * 
	 * @return A parameters array with initialized values.
	 */
	protected Object[] getConstructorParams(Class<?> modelClass, Constructor<?> constructor) {
		Class<?>[] paramTypes = constructor.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			params[i] = getInitParamValue(modelClass, paramTypes[i]);
		}
		return params;
	}

	/**
	 * Get value from database by cursor, then set the value into modelInstance.
	 * 
	 * @param modelInstance
	 *            The model to set into.
	 * @param supportedFields
	 *            Corresponding to each column in database.
	 * @param foreignKeyAssociations
	 *            Associated classes which have foreign keys in the current
	 *            model's table.
	 * @param cursor
	 *            Use to get value from database.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	protected void setValueToModel(Object modelInstance, List<Field> supportedFields,
			List<AssociationsInfo> foreignKeyAssociations, Cursor cursor) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		for (Field field : supportedFields) {
			String getMethodName = genGetColumnMethod(field);
			String columnName = isIdColumn(field.getName()) ? "id" : field.getName();
			int columnIndex = cursor.getColumnIndex(BaseUtility.changeCase(columnName));
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
					} else if (field.getType() == Date.class) {
						long date = (Long) value;
						if (date <= 0) {
							value = null;
						} else {
							value = new Date(date);
						}
					}
					putSetMethodValueByField((DataSupport) modelInstance, field, value);
				}
			}
		}
		if (foreignKeyAssociations != null) {
			for (AssociationsInfo associationInfo : foreignKeyAssociations) {
				String foreignKeyColumn = getForeignKeyColumnName(DBUtility
						.getTableNameByClassName(associationInfo.getAssociatedClassName()));
				int columnIndex = cursor.getColumnIndex(foreignKeyColumn);
				if (columnIndex != -1) {
					long associatedClassId = cursor.getLong(columnIndex);
					try {
						DataSupport associatedObj = (DataSupport) DataSupport.find(
								Class.forName(associationInfo.getAssociatedClassName()),
								associatedClassId);
						if (associatedObj != null) {
							putSetMethodValueByField((DataSupport) modelInstance,
									associationInfo.getAssociateOtherModelFromSelf(), associatedObj);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Get the foreign key associations of the specified class.
	 * 
	 * @param className
	 *            The full class name.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return The foreign key associations of the specified class
	 */
	protected List<AssociationsInfo> getForeignKeyAssociations(String className, boolean isEager) {
		if (isEager) {
			analyzeAssociations(className);
			return fkInCurrentModel;
		}
		return null;
	}

	/**
	 * Get the types of parameters for {@link android.content.ContentValues#put}. Need two
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
	 * @return The types of parameters for {@link android.content.ContentValues#put}.
	 */
	private Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
		Class<?>[] parameterTypes;
		if (isCharType(field)) {
			parameters[1] = String.valueOf(fieldValue);
			parameterTypes = new Class[] { String.class, String.class };
		} else {
			if (field.getType().isPrimitive()) {
				parameterTypes = new Class[] { String.class, getObjectType(field.getType()) };
			} else if ("java.util.Date".equals(field.getType().getName())) {
				parameterTypes = new Class[] { String.class, Long.class };
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
	 * @param modelClass
	 *            The original class the this constructor belongs to.
	 * @param paramType
	 *            Parameter to get initialized value.
	 * @return Default data of basic data type or null.
	 */
	private Object getInitParamValue(Class<?> modelClass, Class<?> paramType) {
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
		if (modelClass == paramType) {
			return null;
		}
		return createInstanceFromClass(paramType);
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
	 * Judge a field is a primitive boolean type or not. Cause it's a little
	 * special when use IDE to generate getter and setter method. The primitive
	 * boolean type won't be like <b>getXxx</b>, it's something like
	 * <b>isXxx</b>.
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
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private void putFieldsValueDependsOnSaveOrUpdate(DataSupport baseObj, Field field,
			ContentValues values) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (isUpdating()) {
			if (!isFieldWithDefaultValue(baseObj, field)) {
				putContentValues(baseObj, field, values);
			}
		} else if (isSaving()) {
            Object value = takeGetMethodValueByField(baseObj, field);
            // put content value only when value is not null. this allows to use defaultValue declared in annotation.
            if (value != null) {
                putContentValues(baseObj, field, values);
            }
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
	 * @throws java.lang.reflect.InvocationTargetException
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
		if (fieldName.matches("^[a-z]{1}[A-Z]{1}.*")) {
			return getterMethodPrefix + fieldName;
		} else {
			return getterMethodPrefix + BaseUtility.capitalize(fieldName);
		}
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
		} else if (field.getName().matches("^[a-z]{1}[A-Z]{1}.*")) {
			setterMethodName = setterMethodPrefix + field.getName();
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
		return genGetColumnMethod(field.getType());
	}

	/**
	 * Generates the getType method for cursor based on field. There're two
	 * unusual conditions. If field type is boolean, generate getInt method. If
	 * field type is char, generate getString method.
	 * 
	 * @param fieldType
	 *            To generate getType method for cursor.
	 * @return The getType method for cursor.
	 */
	private String genGetColumnMethod(Class<?> fieldType) {
		String typeName;
		if (fieldType.isPrimitive()) {
			typeName = BaseUtility.capitalize(fieldType.getName());
		} else {
			typeName = fieldType.getSimpleName();
		}
		String methodName = "get" + typeName;
		if ("getBoolean".equals(methodName)) {
			methodName = "getInt";
		} else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
			methodName = "getString";
		} else if ("getDate".equals(methodName)) {
			methodName = "getLong";
		} else if ("getInteger".equals(methodName)) {
			methodName = "getInt";
		}
		return methodName;
	}

	/**
	 * Customize the passed in columns. If the columns contains an id column
	 * already, just return it. If contains an _id column, rename it to id. If
	 * not, an add id column then return.
	 * 
	 * @param columns
	 *            The original columns that passed in.
	 * @param foreignKeyAssociations
	 *            Associated classes which have foreign keys in the current
	 *            model's table.
	 * @return Customized columns with id column always.
	 */
	private String[] getCustomizedColumns(String[] columns, List<AssociationsInfo> foreignKeyAssociations) {
		if (columns != null) {
			if (foreignKeyAssociations != null && foreignKeyAssociations.size() > 0) {
				String[] tempColumns = new String[columns.length + foreignKeyAssociations.size()];
				System.arraycopy(columns, 0, tempColumns, 0, columns.length);
				for (int i = 0; i < foreignKeyAssociations.size(); i++) {
					String associatedTable = DBUtility
							.getTableNameByClassName(foreignKeyAssociations.get(i)
									.getAssociatedClassName());
					tempColumns[columns.length + i] = getForeignKeyColumnName(associatedTable);
				}
				columns = tempColumns;
			}
			for (int i = 0; i < columns.length; i++) {
				String columnName = columns[i];
				if (isIdColumn(columnName)) {
					if ("_id".equalsIgnoreCase(columnName)) {
						columns[i] = BaseUtility.changeCase("id");
					}
					return columns;
				}
			}
			String[] customizedColumns = new String[columns.length + 1];
			System.arraycopy(columns, 0, customizedColumns, 0, columns.length);
			customizedColumns[columns.length] = BaseUtility.changeCase("id");
			return customizedColumns;
		}
		return null;
	}

	/**
	 * Analyze the associations for the specified class.
	 * 
	 * @param className
	 *            The full class name.
	 */
	private void analyzeAssociations(String className) {
		Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
		if (fkInCurrentModel == null) {
			fkInCurrentModel = new ArrayList<AssociationsInfo>();
		} else {
			fkInCurrentModel.clear();
		}
		if (fkInOtherModel == null) {
			fkInOtherModel = new ArrayList<AssociationsInfo>();
		} else {
			fkInOtherModel.clear();
		}
		for (AssociationsInfo associationInfo : associationInfos) {
			if (associationInfo.getAssociationType() == Const.Model.MANY_TO_ONE
					|| associationInfo.getAssociationType() == Const.Model.ONE_TO_ONE) {
				if (associationInfo.getClassHoldsForeignKey().equals(className)) {
					fkInCurrentModel.add(associationInfo);
				} else {
					fkInOtherModel.add(associationInfo);
				}
			} else if (associationInfo.getAssociationType() == Const.Model.MANY_TO_MANY) {
				fkInOtherModel.add(associationInfo);
			}
		}
	}

	/**
	 * Finds the associated models of baseObj, then set them into baseObj.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setAssociatedModel(DataSupport baseObj) {
		if (fkInOtherModel == null) {
			return;
		}
		for (AssociationsInfo info : fkInOtherModel) {
			Cursor cursor = null;
			String associatedClassName = info.getAssociatedClassName();
			boolean isM2M = info.getAssociationType() == Const.Model.MANY_TO_MANY ? true : false;
			try {
				List<Field> supportedFields = getSupportedFields(associatedClassName);
				if (isM2M) {
					String tableName = baseObj.getTableName();
					String associatedTableName = DBUtility
							.getTableNameByClassName(associatedClassName);
					String intermediateTableName = DBUtility.getIntermediateTableName(tableName,
							associatedTableName);
					StringBuilder sql = new StringBuilder();
					sql.append("select * from ").append(associatedTableName)
							.append(" a inner join ").append(intermediateTableName)
							.append(" b on a.id = b.").append(associatedTableName + "_id")
							.append(" where b.").append(tableName).append("_id = ?");
					cursor = DataSupport.findBySQL(BaseUtility.changeCase(sql.toString()),
							String.valueOf(baseObj.getBaseObjId()));
				} else {
					String foreignKeyColumn = getForeignKeyColumnName(DBUtility
							.getTableNameByClassName(info.getSelfClassName()));
					String associatedTableName = DBUtility
							.getTableNameByClassName(associatedClassName);
					cursor = mDatabase.query(BaseUtility.changeCase(associatedTableName), null,
							foreignKeyColumn + "=?",
							new String[] { String.valueOf(baseObj.getBaseObjId()) }, null, null,
							null, null);
				}
				if (cursor.moveToFirst()) {
					do {
						DataSupport modelInstance = (DataSupport)  createInstanceFromClass(Class.forName(associatedClassName));
						giveBaseObjIdValue(modelInstance,
								cursor.getLong(cursor.getColumnIndexOrThrow("id")));
						setValueToModel(modelInstance, supportedFields, null, cursor);
						if (info.getAssociationType() == Const.Model.MANY_TO_ONE || isM2M) {
							Collection collection = (Collection) takeGetMethodValueByField(baseObj,
									info.getAssociateOtherModelFromSelf());
							collection.add(modelInstance);
						} else if (info.getAssociationType() == Const.Model.ONE_TO_ONE) {
							putSetMethodValueByField(baseObj,
									info.getAssociateOtherModelFromSelf(), modelInstance);
						}
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				throw new DataSupportException(e.getMessage());
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

}