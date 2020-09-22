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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

import org.litepal.LitePalBase;
import org.litepal.Operator;
import org.litepal.annotation.Column;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.cipher.CipherUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.litepal.util.BaseUtility.changeCase;

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
	private LitePalSupport tempEmptyModel;

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
		List<T> dataList = new ArrayList<>();
		Cursor cursor = null;
		try {
            List<Field> supportedFields = getSupportedFields(modelClass.getName());
            List<Field> supportedGenericFields = getSupportedGenericFields(modelClass.getName());
            String[] customizedColumns = DBUtility.convertSelectClauseToValidNames(getCustomizedColumns(columns, supportedGenericFields, foreignKeyAssociations));
            String tableName = getTableName(modelClass);
			cursor = mDatabase.query(tableName, customizedColumns, selection, selectionArgs,
					groupBy, having, orderBy, limit);
			if (cursor.moveToFirst()) {
                SparseArray<QueryInfoCache> queryInfoCacheSparseArray = new SparseArray<>();
                Map<Field, GenericModel> genericModelMap = new HashMap<>();
				do {
					T modelInstance = (T) createInstanceFromClass(modelClass);
					giveBaseObjIdValue((LitePalSupport) modelInstance,
							cursor.getLong(cursor.getColumnIndexOrThrow("id")));
					setValueToModel(modelInstance, supportedFields, foreignKeyAssociations, cursor, queryInfoCacheSparseArray);
                    setGenericValueToModel((LitePalSupport) modelInstance, supportedGenericFields, genericModelMap);
					if (foreignKeyAssociations != null) {
						setAssociatedModel((LitePalSupport) modelInstance);
					}
					dataList.add(modelInstance);
				} while (cursor.moveToNext());
                queryInfoCacheSparseArray.clear();
                genericModelMap.clear();
			}
			return dataList;
		} catch (Exception e) {
			throw new LitePalSupportException(e.getMessage(), e);
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
			throw new LitePalSupportException(e.getMessage(), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * Assign the generated id value to {@link LitePalSupport#baseObjId}. This
	 * value will be used as identify of this model for system use.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @param id
	 *            The value of id.
	 */
	protected void giveBaseObjIdValue(LitePalSupport baseObj, long id) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (id > 0) {
			DynamicExecutor.set(baseObj, "baseObjId", id, LitePalSupport.class);
		}
	}

	/**
	 * Iterate all the fields passed in. Each field calls
	 * {@link #putFieldsValueDependsOnSaveOrUpdate(LitePalSupport, java.lang.reflect.Field, android.content.ContentValues)}
	 * if it's not id field.
	 * 
	 * @param baseObj
	 *            Current model to persist or update.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 */
	protected void putFieldsValue(LitePalSupport baseObj, List<Field> supportedFields,
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
	 */
	protected void putContentValuesForSave(LitePalSupport baseObj, Field field, ContentValues values)
			throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object fieldValue = getFieldValue(baseObj, field);
        if ("java.util.Date".equals(field.getType().getName())) {
        	// handle java.util.Date type for special
			if (fieldValue != null) {
				// If Date field is not null, use date.getTime() value for save.
				Date date = (Date) fieldValue;
				fieldValue = date.getTime();
			} else {
				// If Date field is null, try to use defaultValue on annotation first.
				Column annotation = field.getAnnotation(Column.class);
				if (annotation != null) {
					String defaultValue = annotation.defaultValue();
					if (!defaultValue.isEmpty()) {
						try {
							fieldValue = Long.parseLong(defaultValue);
						} catch (NumberFormatException e) {
							Log.w(TAG, field + " in " + baseObj.getClass() + " with invalid defaultValue. So we use null instead");
						}
					}
				}
				if (fieldValue == null) {
					// If Date field is still null, use Long.MAX_VALUE for save. Because it's a date that will never reach.
					fieldValue = Long.MAX_VALUE;
				}
			}
		}
		if (fieldValue != null) {
			// put content value only when value is not null. this allows to use defaultValue declared in annotation.
			Encrypt annotation = field.getAnnotation(Encrypt.class);
			if (annotation != null && "java.lang.String".equals(field.getType().getName())) {
				fieldValue = encryptValue(annotation.algorithm(), fieldValue);
			}
			Object[] parameters = new Object[] { changeCase(DBUtility.convertToValidColumnName(field.getName())), fieldValue };
			Class<?>[] parameterTypes = getParameterTypes(field, fieldValue, parameters);
			DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
		}
	}

    /**
     * putContentValuesForUpdate operation is almost same with putContentValuesForSave, except allowing put null fieldValue into database,
     * which is made for {@link LitePalSupport#setToDefault} function.
     *
     * @param baseObj
     *            The class of base object.
     * @param field
     *            Field to put into ContentValues.
     * @param values
     *            To store data of current model for persisting or updating.
     */
    protected void putContentValuesForUpdate(LitePalSupport baseObj, Field field, ContentValues values)
            throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object fieldValue = getFieldValue(baseObj, field);
        if ("java.util.Date".equals(field.getType().getName())) {
        	if (fieldValue != null) {
				Date date = (Date) fieldValue;
				fieldValue = date.getTime();
			} else {
				// If Date field is null, use Long.MAX_VALUE for save. Because it's a date that will never reach.
        		fieldValue = Long.MAX_VALUE;
			}
        }
        Encrypt annotation = field.getAnnotation(Encrypt.class);
        if (annotation != null && "java.lang.String".equals(field.getType().getName())) {
            fieldValue = encryptValue(annotation.algorithm(), fieldValue);
        }
        Object[] parameters = new Object[] { changeCase(DBUtility.convertToValidColumnName(field.getName())), fieldValue };
        Class<?>[] parameterTypes = getParameterTypes(field, fieldValue, parameters);
        DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
    }

    /**
     * Encrypt the field value with targeted algorithm.
     * @param algorithm
     *          The algorithm to encrypt value.
     * @param fieldValue
     *          Field value to encrypt.
     * @return Encrypted value by targeted algorithm.
     */
    protected Object encryptValue(String algorithm, Object fieldValue) {
        if (algorithm != null && fieldValue != null) {
            if (LitePalSupport.AES.equalsIgnoreCase(algorithm)) {
                fieldValue = CipherUtil.aesEncrypt((String) fieldValue);
            } else if (LitePalSupport.MD5.equalsIgnoreCase(algorithm)) {
                fieldValue = CipherUtil.md5Encrypt((String) fieldValue);
            }
        }
        return fieldValue;
    }

	/**
	 * Get the field value for model.
	 * 
	 * @param dataSupport
	 *            The model to get method from.
	 * @param field
	 *            Use to generate getter method name.
	 * @return The value returned by getter method.
	 */
	protected Object getFieldValue(LitePalSupport dataSupport, Field field)
			throws SecurityException, IllegalArgumentException,
			IllegalAccessException {
		if (shouldGetOrSet(dataSupport, field)) {
			return DynamicExecutor.getField(dataSupport, field.getName(), dataSupport.getClass());
		}
		return null;
	}

	/**
	 * Set the field value for model.
	 * 
	 * @param dataSupport
	 *            The model to set method to.
	 * @param field
	 *            Use to generate setter method name.
	 * @param parameter
	 *            The parameter to invoke setter method.
	 */
	protected void setFieldValue(LitePalSupport dataSupport, Field field, Object parameter)
			throws SecurityException, IllegalArgumentException, IllegalAccessException {
		if (shouldGetOrSet(dataSupport, field)) {
            DynamicExecutor.setField(dataSupport, field.getName(), parameter, dataSupport.getClass());
		}
	}

	/**
	 * Find all the associated models of currently model. Then add all the
	 * associated models into baseObj.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 */
	protected void analyzeAssociatedModels(LitePalSupport baseObj, Collection<AssociationsInfo> associationInfos) {
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
			throw new LitePalSupportException(e.getMessage(), e);
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
	 */
	protected LitePalSupport getAssociatedModel(LitePalSupport baseObj, AssociationsInfo associationInfo)
			throws SecurityException, IllegalArgumentException, IllegalAccessException {
		return (LitePalSupport) getFieldValue(baseObj,
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
	 */
	@SuppressWarnings("unchecked")
	protected Collection<LitePalSupport> getAssociatedModels(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, IllegalAccessException {
		return (Collection<LitePalSupport>) getFieldValue(baseObj,
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
	protected LitePalSupport getEmptyModel(LitePalSupport baseObj) {
		if (tempEmptyModel != null) {
			return tempEmptyModel;
		}
		String className = null;
		try {
			className = baseObj.getClassName();
			Class<?> modelClass = Class.forName(className);
			tempEmptyModel = (LitePalSupport) modelClass.newInstance();
			return tempEmptyModel;
		} catch (ClassNotFoundException e) {
			throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND
					+ className);
		} catch (InstantiationException e) {
			throw new LitePalSupportException(className + LitePalSupportException.INSTANTIATION_EXCEPTION, e);
		} catch (Exception e) {
			throw new LitePalSupportException(e.getMessage(), e);
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
		return conditions != null && conditions.length == 0;
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
	 * When executing {@link #getFieldValue(LitePalSupport, Field)} or
	 * {@link #setFieldValue(LitePalSupport, Field, Object)}, the
	 * dataSupport and field passed in should be protected from null value.
	 * 
	 * @param dataSupport
	 *            The object to execute set or get method.
	 * @param field
	 *            The field of generating set and get methods.
	 * @return True if dataSupport and field are not null, false otherwise.
	 */
	protected boolean shouldGetOrSet(LitePalSupport dataSupport, Field field) {
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
	protected String getIntermediateTableName(LitePalSupport baseObj, String associatedTableName) {
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
			throw new LitePalSupportException(e.getMessage(), e);
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
		if (constructors.length == 0) throw new LitePalSupportException( modelClass.getName() + " has no constructor. LitePal could not handle it");
		Constructor<?> bestSuitConstructor = null;
		int minConstructorParamLength = Integer.MAX_VALUE;
		for (Constructor<?> constructor : constructors) {
			Class<?>[] types = constructor.getParameterTypes();
			boolean canUseThisConstructor = true; // under some conditions, constructor can not use for create instance
			for (Class<?> parameterType : types) {
				if (parameterType == modelClass
					|| parameterType.getName().startsWith("com.android") && parameterType.getName().endsWith("InstantReloadException")) {
					// we can not use this constructor
					canUseThisConstructor = false;
					break;
				}
			}
			if (canUseThisConstructor) { // we can use this constructor
				if (types.length < minConstructorParamLength) { // find the constructor with least parameter
					bestSuitConstructor = constructor;
					minConstructorParamLength = types.length;
				}
			}
		}
		if (bestSuitConstructor != null) {
			bestSuitConstructor.setAccessible(true);
		} else {
			StringBuilder builder = new StringBuilder(modelClass.getName()).append(" has no suited constructor to new instance. Constructors defined in class:");
			for (Constructor<?> constructor : constructors) {
				builder.append("\n").append(constructor.toString());
			}
			throw new LitePalSupportException(builder.toString());
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
     * @param sparseArray
     *            Use SparseArray to cache the query information at first loop. Then the rest loop
     *            can get query information directly to speed up.
	 */
	protected void setValueToModel(Object modelInstance, List<Field> supportedFields,
			List<AssociationsInfo> foreignKeyAssociations, Cursor cursor, SparseArray<QueryInfoCache> sparseArray) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
        int cacheSize = sparseArray.size();
        if (cacheSize > 0) {
            for (int i = 0; i < cacheSize; i++) {
                int columnIndex = sparseArray.keyAt(i);
                QueryInfoCache cache = sparseArray.get(columnIndex);
                setToModelByReflection(modelInstance, cache.field, columnIndex, cache.getMethodName, cursor);
            }
        } else {
            for (Field field : supportedFields) {
                String getMethodName = genGetColumnMethod(field);
                String columnName = isIdColumn(field.getName()) ? "id" : DBUtility.convertToValidColumnName(field.getName());
                int columnIndex = cursor.getColumnIndex(BaseUtility.changeCase(columnName));
                if (columnIndex != -1) {
                    setToModelByReflection(modelInstance, field, columnIndex, getMethodName, cursor);
                    QueryInfoCache cache = new QueryInfoCache();
                    cache.getMethodName = getMethodName;
                    cache.field = field;
                    sparseArray.put(columnIndex, cache);
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
						LitePalSupport associatedObj = (LitePalSupport) Operator.find(
								Class.forName(associationInfo.getAssociatedClassName()),
								associatedClassId);
						if (associatedObj != null) {
							setFieldValue((LitePalSupport) modelInstance,
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
     * Get generic value from generic tables, then set the value into the baseObj.
     * @param baseObj
     *          The model to set into.
     * @param supportedGenericFields
     *          List of all supported generic fields.
     * @param genericModelMap
     *          Use HashMap to cache the query information at first loop. Then the rest loop can
     *          get query information directly to speed up.
     */
    protected void setGenericValueToModel(LitePalSupport baseObj, List<Field> supportedGenericFields,
                                          Map<Field, GenericModel> genericModelMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Field field : supportedGenericFields) {
            String tableName, genericValueColumnName, genericValueIdColumnName, getMethodName;
            Cursor cursor = null;
            GenericModel genericModel = genericModelMap.get(field);
            if (genericModel == null) {
                String genericTypeName = getGenericTypeName(field);
                if (baseObj.getClassName().equals(genericTypeName)) {
                    genericValueColumnName = DBUtility.getM2MSelfRefColumnName(field);
                    getMethodName = "getLong";
                } else {
                    genericValueColumnName = DBUtility.convertToValidColumnName(field.getName());
                    getMethodName = genGetColumnMethod(field);
                }
                tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                GenericModel model = new GenericModel();
                model.setTableName(tableName);
                model.setValueColumnName(genericValueColumnName);
                model.setValueIdColumnName(genericValueIdColumnName);
                model.setGetMethodName(getMethodName);
                genericModelMap.put(field, model);
            } else {
                tableName = genericModel.getTableName();
                genericValueColumnName = genericModel.getValueColumnName();
                genericValueIdColumnName = genericModel.getValueIdColumnName();
                getMethodName = genericModel.getGetMethodName();
            }
            try {
                cursor = mDatabase.query(tableName, null, genericValueIdColumnName + " = ?",
                        new String[]{ String.valueOf(baseObj.getBaseObjId()) }, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        int columnIndex = cursor.getColumnIndex(BaseUtility.changeCase(genericValueColumnName));
                        if (columnIndex != -1) {
                            setToModelByReflection(baseObj, field, columnIndex, getMethodName, cursor);
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
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
	protected Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
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
				switch (basicTypeName) {
					case "int":
						return Integer.class;
					case "short":
						return Short.class;
					case "long":
						return Long.class;
					case "float":
						return Float.class;
					case "double":
						return Double.class;
					case "boolean":
						return Boolean.class;
					case "char":
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
			return 0L;
		}
		if ("short".equals(paramTypeName) || "java.lang.Short".equals(paramTypeName)) {
			return 0;
		}
		if ("char".equals(paramTypeName) || "java.lang.Character".equals(paramTypeName)) {
			return ' ';
		}
        if ("[B".equals(paramTypeName) || "[Ljava.lang.Byte;".equals(paramTypeName)) {
            return new byte[0];
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
		return "boolean".equals(fieldType.getName());
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
	 */
	private void putFieldsValueDependsOnSaveOrUpdate(LitePalSupport baseObj, Field field, ContentValues values)
			throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (isUpdating()) {
			if (!isFieldWithDefaultValue(baseObj, field)) {
				putContentValuesForUpdate(baseObj, field, values);
			}
		} else if (isSaving()) {
            putContentValuesForSave(baseObj, field, values);
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
	 * Analyze the passed in field. Check if this field is with default value.
	 * The baseObj need a default constructor or {@link LitePalSupportException}
	 * will be thrown.
	 * 
	 * @param baseObj
	 *            Current model to update.
	 * @param field
	 *            To check if with default value.
	 * @return If the field is with default value, return true. Otherwise return
	 *         false.
	 */
	private boolean isFieldWithDefaultValue(LitePalSupport baseObj, Field field)
			throws IllegalAccessException, SecurityException, IllegalArgumentException {
		LitePalSupport emptyModel = getEmptyModel(baseObj);
		Object realReturn = getFieldValue(baseObj, field);
		Object defaultReturn = getFieldValue(emptyModel, field);
		if (realReturn != null && defaultReturn != null) {
			String realFieldValue = realReturn.toString();
			String defaultFieldValue = defaultReturn.toString();
			return realFieldValue.equals(defaultFieldValue);
		}
		return realReturn == defaultReturn;
	}

	/**
	 * Generate the getter method name by field, following the Android Studio rule.
	 * 
	 * @param field
	 *            The field to generate getter method from.
	 * @return The generated getter method name.
	 */
	protected String makeGetterMethodName(Field field) {
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
	 * Generate the setter method name by field, following the Android Studio rule.
	 * 
	 * @param field
	 *            The field to generate setter method from.
	 * @return The generated setter method name.
	 */
	protected String makeSetterMethodName(Field field) {
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
	 * Generates the getType method for cursor based on field. There're couple of
     * unusual conditions. If field type is boolean, generate getInt method. If
     * field type is char, generate getString method. If field type is Date, generate
     * getLong method. If filed type is Integer, generate getInt method. If field type
     * is bytes, generate getBlob method.
	 * 
	 * @param field
	 *            To generate getType method for cursor.
	 * @return The getType method for cursor.
	 */
	private String genGetColumnMethod(Field field) {
        Class<?> fieldType;
        if (isCollection(field.getType())) {
            fieldType = getGenericTypeClass(field);
        } else {
            fieldType = field.getType();
        }
		return genGetColumnMethod(fieldType);
	}

	/**
	 * Generates the getType method for cursor based on field. There're couple of
	 * unusual conditions. If field type is boolean, generate getInt method. If
	 * field type is char, generate getString method. If field type is Date, generate
     * getLong method. If filed type is Integer, generate getInt method. If field type
     * is bytes, generate getBlob method.
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
		switch (methodName) {
			case "getBoolean":
			case "getInteger":
				methodName = "getInt";
				break;
			case "getChar":
			case "getCharacter":
				methodName = "getString";
				break;
			case "getDate":
				methodName = "getLong";
				break;
		}
		return methodName;
	}

	/**
	 * Customize the passed in columns. If the columns contains an id column
	 * already, just return it. If contains an _id column, rename it to id. If
	 * not, an add id column then return. If it contains generic columns them
     * from query and use them in supported generic fields.
	 * 
	 * @param columns
	 *            The original columns that passed in.
	 * @param foreignKeyAssociations
	 *            Associated classes which have foreign keys in the current
	 *            model's table.
	 * @return Customized columns with id column always.
	 */
	private String[] getCustomizedColumns(String[] columns, List<Field> supportedGenericFields, List<AssociationsInfo> foreignKeyAssociations) {
		if (columns != null && columns.length > 0) {
            boolean columnsContainsId = false;
            List<String> convertList = Arrays.asList(columns);
            List<String> columnList = new ArrayList<>(convertList);
            List<String> supportedGenericFieldNames = new ArrayList<>();
            List<Integer> columnToRemove = new ArrayList<>();
            List<String> genericColumnsForQuery = new ArrayList<>();
            List<Field> tempSupportedGenericFields = new ArrayList<>();

            for (Field supportedGenericField : supportedGenericFields) {
                supportedGenericFieldNames.add(supportedGenericField.getName());
            }

            for (int i = 0; i < columnList.size(); i++) {
                String columnName = columnList.get(i);
                // find out all generic columns.
                if (BaseUtility.containsIgnoreCases(supportedGenericFieldNames, columnName)) {
                    columnToRemove.add(i);
                } else if (isIdColumn(columnName)) {
                    columnsContainsId = true;
                    if ("_id".equalsIgnoreCase(columnName)) {
                        columnList.set(i, BaseUtility.changeCase("id"));
                    }
                }
            }

            // remove generic columns cause they can't be used for query
            for (int i = columnToRemove.size() - 1; i >= 0 ; i--) {
                int index = columnToRemove.get(i);
                String genericColumn = columnList.remove(index);
                genericColumnsForQuery.add(genericColumn);
            }

            for (Field supportedGenericField : supportedGenericFields) {
                String fieldName = supportedGenericField.getName();
                if (BaseUtility.containsIgnoreCases(genericColumnsForQuery, fieldName)) {
                    tempSupportedGenericFields.add(supportedGenericField);
                }
            }

            supportedGenericFields.clear();
            supportedGenericFields.addAll(tempSupportedGenericFields);

            if (foreignKeyAssociations != null && foreignKeyAssociations.size() > 0) {
				for (int i = 0; i < foreignKeyAssociations.size(); i++) {
					String associatedTable = DBUtility
							.getTableNameByClassName(foreignKeyAssociations.get(i)
									.getAssociatedClassName());
                    columnList.add(getForeignKeyColumnName(associatedTable));
				}
			}
            if (!columnsContainsId) {
                columnList.add(BaseUtility.changeCase("id"));
            }
			return columnList.toArray(new String[0]);
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
			fkInCurrentModel = new ArrayList<>();
		} else {
			fkInCurrentModel.clear();
		}
		if (fkInOtherModel == null) {
			fkInOtherModel = new ArrayList<>();
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
	private void setAssociatedModel(LitePalSupport baseObj) {
		if (fkInOtherModel == null) {
			return;
		}
		for (AssociationsInfo info : fkInOtherModel) {
			Cursor cursor = null;
			String associatedClassName = info.getAssociatedClassName();
			boolean isM2M = info.getAssociationType() == Const.Model.MANY_TO_MANY;
			try {
				List<Field> supportedFields = getSupportedFields(associatedClassName);
                List<Field> supportedGenericFields = getSupportedGenericFields(associatedClassName);
				if (isM2M) {
					String tableName = baseObj.getTableName();
					String associatedTableName = DBUtility
							.getTableNameByClassName(associatedClassName);
					String intermediateTableName = DBUtility.getIntermediateTableName(tableName,
							associatedTableName);
					StringBuilder sql = new StringBuilder();
					sql.append("select * from ").append(associatedTableName)
							.append(" a inner join ").append(intermediateTableName)
							.append(" b on a.id = b.").append(associatedTableName).append("_id")
							.append(" where b.").append(tableName).append("_id = ?");
					cursor = Operator.findBySQL(BaseUtility.changeCase(sql.toString()),
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
				if (cursor != null && cursor.moveToFirst()) {
                    SparseArray<QueryInfoCache> queryInfoCacheSparseArray = new SparseArray<>();
                    Map<Field, GenericModel> genericModelMap = new HashMap<>();
					do {
						LitePalSupport modelInstance = (LitePalSupport) createInstanceFromClass(Class.forName(associatedClassName));
						giveBaseObjIdValue(modelInstance,
								cursor.getLong(cursor.getColumnIndexOrThrow("id")));
						setValueToModel(modelInstance, supportedFields, null, cursor, queryInfoCacheSparseArray);
                        setGenericValueToModel(modelInstance, supportedGenericFields, genericModelMap);
						if (info.getAssociationType() == Const.Model.MANY_TO_ONE || isM2M) {
                            Field field = info.getAssociateOtherModelFromSelf();
							Collection collection = (Collection) getFieldValue(baseObj, field);
                            if (collection == null) {
                                if (isList(field.getType())) {
                                    collection = new ArrayList();
                                } else {
                                    collection = new HashSet();
                                }
                                DynamicExecutor.setField(baseObj, field.getName(), collection, baseObj.getClass());
                            }
                            collection.add(modelInstance);
						} else if (info.getAssociationType() == Const.Model.ONE_TO_ONE) {
							setFieldValue(baseObj,
									info.getAssociateOtherModelFromSelf(), modelInstance);
						}
					} while (cursor.moveToNext());
                    queryInfoCacheSparseArray.clear();
                    genericModelMap.clear();
				}
			} catch (Exception e) {
				throw new LitePalSupportException(e.getMessage(), e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

    @SuppressWarnings("unchecked")
    private void setToModelByReflection(Object modelInstance, Field field, int columnIndex, String getMethodName, Cursor cursor)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> cursorClass = cursor.getClass();
        if (cursor.isNull(columnIndex)) return;
        Method method = cursorClass.getMethod(getMethodName, int.class);
        Object value = method.invoke(cursor, columnIndex);
        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            if ("0".equals(String.valueOf(value))) {
                value = false;
            } else if ("1".equals(String.valueOf(value))) {
                value = true;
            }
        } else if (field.getType() == char.class || field.getType() == Character.class) {
            value = ((String) value).charAt(0);
        } else if (field.getType() == Date.class) {
            long date = (long) value;
            if (date == Long.MAX_VALUE) { // Long.MAX_VALUE is a date that will never reach, which represents null in our case.
				value = null;
			} else {
				value = new Date(date);
			}
        }
        if (isCollection(field.getType())) {
            Collection<Object> collection = (Collection<Object>) DynamicExecutor.getField(modelInstance, field.getName(), modelInstance.getClass());
            if (collection == null) {
                if (isList(field.getType())) {
                    collection = new ArrayList<>();
                } else {
                    collection = new HashSet<>();
                }
                DynamicExecutor.setField(modelInstance, field.getName(), collection, modelInstance.getClass());
            }
            String genericTypeName = getGenericTypeName(field);
            if ("java.lang.String".equals(genericTypeName)) {
                Encrypt annotation = field.getAnnotation(Encrypt.class);
                if (annotation != null) {
                    value = decryptValue(annotation.algorithm(), value);
                }
            } else if (modelInstance.getClass().getName().equals(genericTypeName)) {
                if (value instanceof Long || value instanceof Integer) {
                    value = Operator.find(modelInstance.getClass(), (long) value);
                }
            }
            collection.add(value);
        } else {
            Encrypt annotation = field.getAnnotation(Encrypt.class);
            if (annotation != null && "java.lang.String".equals(field.getType().getName())) {
                value = decryptValue(annotation.algorithm(), value);
            }
            DynamicExecutor.setField(modelInstance, field.getName(), value,
                    modelInstance.getClass());
        }
    }

    /**
     * Decrypt the field value with targeted algorithm.
     * @param algorithm
     *          The algorithm to decrypt value.
     * @param fieldValue
     *          Field value to decrypt.
     * @return Decrypted value by targeted algorithm.
     */
    protected Object decryptValue(String algorithm, Object fieldValue) {
        if (algorithm != null && fieldValue != null) {
            if (LitePalSupport.AES.equalsIgnoreCase(algorithm)) {
                fieldValue = CipherUtil.aesDecrypt((String) fieldValue);
            }
        }
        return fieldValue;
    }

    /**
     * Cache core info for query operation to improve query performance.
     *
     * @since 1.3.1
     */
	static class QueryInfoCache {

        String getMethodName;

        Field field;

    }

}