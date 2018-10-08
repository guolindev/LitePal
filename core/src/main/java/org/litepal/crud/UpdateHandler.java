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
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import org.litepal.Operator;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.litepal.util.BaseUtility.changeCase;

/**
 * This is a component under LitePalSupport. It deals with the updating stuff as
 * primary task. Either updating specifying data with id or updating multiple
 * lines with conditions can be done here.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class UpdateHandler extends DataHandler {

	/**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of UpdateHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
    public UpdateHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to update. Using
	 * baseObj to decide which table to update, and id to decide a specific row.
	 * The value that need to update is stored in baseObj.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param id
	 *            Which record to update.
	 * @return The number of rows affected.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	int onUpdate(LitePalSupport baseObj, long id) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		List<Field> supportedGenericFields = getSupportedGenericFields(baseObj.getClassName());
        updateGenericTables(baseObj, supportedGenericFields, id);
		ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values, id);
		if (values.size() > 0) {
			return mDatabase.update(baseObj.getTableName(), values, "id = " + id, null);
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to update. Using
	 * modelClass to decide which table to update, and id to decide a specific
	 * row. The value that need to update is stored in ContentValues.
	 * 
	 * @param modelClass
	 *            Which table to update by class.
	 * @param id
	 *            Which record to update.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @return The number of rows affected.
	 */
    public int onUpdate(Class<?> modelClass, long id, ContentValues values) {
		if (values.size() > 0) {
            convertContentValues(values);
            return mDatabase.update(getTableName(modelClass), values, "id = " + id, null);
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to update multiple
	 * rows. Using baseObj to decide which table to update, and conditions
	 * representing the WHERE part of an SQL statement. The value that need to
	 * update is stored in baseObj.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The number of rows affected.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
    @SuppressWarnings("unchecked")
	int onUpdateAll(LitePalSupport baseObj, String... conditions) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
        List<Field> supportedGenericFields = getSupportedGenericFields(baseObj.getClassName());
        long[] ids = null;
        if (!supportedGenericFields.isEmpty()) {
            List<LitePalSupport> list = (List<LitePalSupport>) Operator.select("id").where(conditions).find(baseObj.getClass());
            if (list.size() > 0) {
                ids = new long[list.size()];
                for (int i = 0; i < ids.length; i++) {
                    LitePalSupport dataSupport = list.get(i);
                    ids[i] = dataSupport.getBaseObjId();
                }
                updateGenericTables(baseObj, supportedGenericFields, ids);
            }
        }
        ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values, ids);
		return doUpdateAllAction(baseObj.getTableName(), values, conditions);
	}

	/**
	 * The open interface for other classes in CRUD package to update multiple
	 * rows. Using tableName to decide which table to update, and conditions
	 * representing the WHERE part of an SQL statement. The value that need to
	 * update is stored in ContentValues.
	 * 
	 * @param tableName
	 *            Which table to update.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @return The number of rows affected.
	 */
    public int onUpdateAll(String tableName, ContentValues values, String... conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        convertContentValues(values);
		return doUpdateAllAction(tableName, values, conditions);
	}

	/**
	 * Do the action for updating multiple rows. It will check the validity of
	 * conditions, then update rows in database. If the format of conditions is
	 * invalid, throw LitePalSupportException.
	 * 
	 * @param tableName
	 *            Which table to delete from.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @return The number of rows affected.
	 */
	private int doUpdateAllAction(String tableName, ContentValues values, String... conditions) {
		BaseUtility.checkConditionsCorrect(conditions);
		if (values.size() > 0) {
			return mDatabase.update(tableName, values, getWhereClause(conditions),
					getWhereArgs(conditions));
		}
		return 0;
	}

	/**
	 * Iterate all the fields that need to set to default value. If the field is
	 * id, ignore it. Or put the default value of field into ContentValues.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param values
	 *            To store data of current model for persisting or updating.
     * @param ids
     *          The id array of query result.
	 */
	private void putFieldsToDefaultValue(LitePalSupport baseObj, ContentValues values, long... ids) {
		String fieldName = null;
		try {
			LitePalSupport emptyModel = getEmptyModel(baseObj);
			Class<?> emptyModelClass = emptyModel.getClass();
			for (String name : baseObj.getFieldsToSetToDefault()) {
				if (!isIdColumn(name)) {
					fieldName = name;
					Field field = emptyModelClass.getDeclaredField(fieldName);
                    if (isCollection(field.getType())) {
                        if (ids != null && ids.length > 0) {
                            String genericTypeName = getGenericTypeName(field);
                            if (BaseUtility.isGenericTypeSupported(genericTypeName)) {
                                String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                                String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                                StringBuilder whereClause = new StringBuilder();
                                boolean needOr = false;
                                for (long id : ids) {
                                    if (needOr) {
                                        whereClause.append(" or ");
                                    }
                                    whereClause.append(genericValueIdColumnName).append(" = ").append(id);
                                    needOr = true;
                                }
                                mDatabase.delete(tableName, whereClause.toString(), null);
                            }
                        }
                    } else {
					    putContentValuesForUpdate(emptyModel, field, values);
                    }
				}
			}
		} catch (NoSuchFieldException e) {
			throw new LitePalSupportException(LitePalSupportException.noSuchFieldExceptioin(
					baseObj.getClassName(), fieldName), e);
		} catch (Exception e) {
			throw new LitePalSupportException(e.getMessage(), e);
		}
	}

	/**
	 * Unused currently.
	 */
	@SuppressWarnings("unused")
	private int doUpdateAssociations(LitePalSupport baseObj, long id, ContentValues values) {
		int rowsAffected = 0;
		analyzeAssociations(baseObj);
		updateSelfTableForeignKey(baseObj, values);
		rowsAffected += updateAssociatedTableForeignKey(baseObj, id);
		return rowsAffected;
	}

	/**
	 * Analyze the associations of baseObj and store the result in it. The
	 * associations will be used when deleting referenced data of baseObj.
	 * Unused currently.
	 * 
	 * @param baseObj
	 *            The record to update.
	 */
	private void analyzeAssociations(LitePalSupport baseObj) {
		try {
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(baseObj
					.getClassName());
			analyzeAssociatedModels(baseObj, associationInfos);
		} catch (Exception e) {
			throw new LitePalSupportException(e.getMessage(), e);
		}
	}

	/**
	 * Unused currently.
	 */
	private void updateSelfTableForeignKey(LitePalSupport baseObj, ContentValues values) {
		Map<String, Long> associatedModelMap = baseObj.getAssociatedModelsMapWithoutFK();
		for (String associatedTable : associatedModelMap.keySet()) {
			String fkName = getForeignKeyColumnName(associatedTable);
			values.put(fkName, associatedModelMap.get(associatedTable));
		}
	}

	/**
	 * Unused currently.
	 */
	private int updateAssociatedTableForeignKey(LitePalSupport baseObj, long id) {
		Map<String, Set<Long>> associatedModelMap = baseObj.getAssociatedModelsMapWithFK();
		ContentValues values = new ContentValues();
		for (String associatedTable : associatedModelMap.keySet()) {
			values.clear();
			String fkName = getForeignKeyColumnName(baseObj.getTableName());
			values.put(fkName, id);
			Set<Long> ids = associatedModelMap.get(associatedTable);
			if (ids != null && !ids.isEmpty()) {
				return mDatabase.update(associatedTable, values, getWhereOfIdsWithOr(ids), null);
			}
		}
		return 0;
	}

    /**
     * Update the generic data in generic tables. Need to delete the related generic data before
     * saving, because generic data has no id. If generic collection is null or empty, the operation
     * will be abort. Clear generic collection data while updating should use {@link LitePalSupport#setToDefault(String)}
     * method.
     * @param baseObj
     *          Current model that is persisted.
     *@param  supportedGenericFields
     *            List of all supported generic fields.
     * @param ids
     *          The id array of models.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void updateGenericTables(LitePalSupport baseObj, List<Field> supportedGenericFields,
                                     long... ids) throws IllegalAccessException, InvocationTargetException {
        if (ids != null && ids.length > 0) {
            for (Field field : supportedGenericFields) {
                Encrypt annotation = field.getAnnotation(Encrypt.class);
                String algorithm = null;
                String genericTypeName = getGenericTypeName(field);
                if (annotation != null && "java.lang.String".equals(genericTypeName)) {
                    algorithm = annotation.algorithm();
                }
                field.setAccessible(true);
                Collection<?> collection = (Collection<?>) field.get(baseObj);
                if (collection != null && !collection.isEmpty()) {
                    String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                    String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                    for (long id : ids) {
                        mDatabase.delete(tableName, genericValueIdColumnName + " = ?", new String[] {String.valueOf(id)});
                        for (Object object : collection) {
                            ContentValues values = new ContentValues();
                            values.put(genericValueIdColumnName, id);
                            object = encryptValue(algorithm, object);
                            if (baseObj.getClassName().equals(genericTypeName)) {
                                LitePalSupport dataSupport = (LitePalSupport) object;
                                if (dataSupport == null) {
                                    continue;
                                }
                                long baseObjId = dataSupport.getBaseObjId();
                                if (baseObjId <= 0) {
                                    continue;
                                }
                                values.put(DBUtility.getM2MSelfRefColumnName(field), baseObjId);
                            } else {
                                Object[] parameters = new Object[] { DBUtility.convertToValidColumnName(changeCase(field.getName())), object };
                                Class<?>[] parameterTypes = new Class[] { String.class, getGenericTypeClass(field) };
                                DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
                            }
                            mDatabase.insert(tableName, null, values);
                        }
                    }
                }
            }
        }
    }

    /**
     * The keys in ContentValues may be put as valid in Java but invalid in database. So convert
     * them into valid keys.
     * @param values
     *          A map from column names to new column values. null is a valid
     *            value that will be translated to NULL.
     */
    private void convertContentValues(ContentValues values) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Map<String, Object> valuesToConvert = new HashMap<String, Object>();
            for (String key : values.keySet()) {
                if (DBUtility.isFieldNameConflictWithSQLiteKeywords(key)) {
                    Object object = values.get(key);
                    valuesToConvert.put(key, object);
                }
            }
            for (String key : valuesToConvert.keySet()) {
                String convertedKey = DBUtility.convertToValidColumnName(key);
                Object object = values.get(key);
                values.remove(key);
                if (object == null) {
                    values.putNull(convertedKey);
                } else {
                    String className = object.getClass().getName();
                    if ("java.lang.Byte".equals(className)) {
                        values.put(convertedKey, (Byte) object);
                    } else if ("[B".equals(className)) {
                        values.put(convertedKey, (byte[]) object);
                    } else if ("java.lang.Boolean".equals(className)) {
                        values.put(convertedKey, (Boolean) object);
                    } else if ("java.lang.String".equals(className)) {
                        values.put(convertedKey, (String) object);
                    } else if ("java.lang.Float".equals(className)) {
                        values.put(convertedKey, (Float) object);
                    } else if ("java.lang.Long".equals(className)) {
                        values.put(convertedKey, (Long) object);
                    } else if ("java.lang.Integer".equals(className)) {
                        values.put(convertedKey, (Integer) object);
                    } else if ("java.lang.Short".equals(className)) {
                        values.put(convertedKey, (Short) object);
                    } else if ("java.lang.Double".equals(className)) {
                        values.put(convertedKey, (Double) object);
                    }
                }
            }
        }
    }

}