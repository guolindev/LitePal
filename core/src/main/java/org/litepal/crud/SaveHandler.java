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
import android.util.Log;

import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.DBUtility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.litepal.util.BaseUtility.changeCase;

/**
 * This is a component under LitePalSupport. It deals with the saving stuff as
 * primary task. All the implementation based on the java reflection API and
 * Android SQLiteDatabase API. It will persist the model class into table. If
 * there're some associated models already persisted, it will build the
 * associations in database automatically between the current model and the
 * associated models.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class SaveHandler extends DataHandler {

    private ContentValues values;

    /**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of SaveHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
    public SaveHandler(SQLiteDatabase db) {
        values = new ContentValues();
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to save a model. It
	 * is called when a model class calls the save method. First of all, the
	 * passed in baseObj will be saved into database. Then LitePal will analyze
	 * the associations. If there're associated models detected, each associated
	 * model which is persisted will build association with current model in
	 * database.
	 * 
	 * @param baseObj
	 *            Current model to persist.
	 */
	void onSave(LitePalSupport baseObj) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String className = baseObj.getClassName();
		List<Field> supportedFields = getSupportedFields(className);
        List<Field> supportedGenericFields = getSupportedGenericFields(className);
		Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
		if (!baseObj.isSaved()) {
            analyzeAssociatedModels(baseObj, associationInfos);
			doSaveAction(baseObj, supportedFields, supportedGenericFields);
            analyzeAssociatedModels(baseObj, associationInfos);
		} else {
            analyzeAssociatedModels(baseObj, associationInfos);
			doUpdateAction(baseObj, supportedFields, supportedGenericFields);
		}
	}

	/**
	 * The open interface for other classes in CRUD package to save a model
	 * collection. It is called when developer calls
	 * {@link org.litepal.Operator#saveAll(java.util.Collection)}. Each model in the collection
	 * will be persisted. If there're associated models detected, each
	 * associated model which is persisted will build association with current
	 * model in database.
	 * 
	 * @param collection
	 *            Holds all models to persist.
	 */
	public <T extends LitePalSupport> void onSaveAll(Collection<T> collection) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if (collection != null && collection.size() > 0) {
			LitePalSupport[] array = collection.toArray(new LitePalSupport[0]);
			LitePalSupport firstObj = array[0];
			String className = firstObj.getClassName();
			List<Field> supportedFields = getSupportedFields(className);
            List<Field> supportedGenericFields = getSupportedGenericFields(className);
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
			for (LitePalSupport baseObj : array) {
				if (!baseObj.isSaved()) {
					analyzeAssociatedModels(baseObj, associationInfos);
					doSaveAction(baseObj, supportedFields, supportedGenericFields);
					analyzeAssociatedModels(baseObj, associationInfos);
				} else {
					analyzeAssociatedModels(baseObj, associationInfos);
					doUpdateAction(baseObj, supportedFields, supportedGenericFields);
				}
				baseObj.clearAssociatedData();
			}
		}
	}

	/**
	 * Persisting model class into database happens here. But first
	 * {@link #beforeSave(LitePalSupport, java.util.List, android.content.ContentValues)} will be called to
	 * put the values for ContentValues. When the model is saved,
	 * {@link #afterSave(LitePalSupport, java.util.List, java.util.List, long)} will be called to do stuffs
	 * after model is saved. Note that SaveSupport won't help with id. Any
	 * developer who wants to set value to id will be ignored here. The value of
	 * id will be generated by SQLite automatically.
	 * 
	 * @param baseObj
	 *            Current model to persist.
	 * @param supportedFields
	 *            List of all supported fields.
     * @param  supportedGenericFields
     *            List of all supported generic fields.
	 */
	private void doSaveAction(LitePalSupport baseObj, List<Field> supportedFields, List<Field> supportedGenericFields)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		values.clear();
		beforeSave(baseObj, supportedFields, values);
		long id = saving(baseObj, values);
		afterSave(baseObj, supportedFields, supportedGenericFields, id);
	}

	/**
	 * Before the self model is saved, it will be analyzed first. Put all the
	 * data contained by the model into ContentValues, including the fields
	 * value and foreign key value.
	 * 
	 * @param baseObj
	 *            Current model to persist.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param values
	 *            To store data of current model for persisting.
	 */
	private void beforeSave(LitePalSupport baseObj, List<Field> supportedFields, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		putFieldsValue(baseObj, supportedFields, values);
        putForeignKeyValue(values, baseObj);
	}

	/**
	 * Calling {@link android.database.sqlite.SQLiteDatabase#insert(String, String, android.content.ContentValues)} to
	 * persist the current model.
	 * 
	 * @param baseObj
	 *            Current model to persist.
	 * @param values
	 *            To store data of current model for persisting.
	 * @return The row ID of the newly inserted row, or -1 if an error occurred.
	 */
	private long saving(LitePalSupport baseObj, ContentValues values) {
        if (values.size() == 0) {
            values.putNull("id");
        }
		return mDatabase.insert(baseObj.getTableName(), null, values);
	}

	/**
	 * After the model is saved, do the extra work that need to do.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 * @param supportedFields
	 *            List of all supported fields.
     * @param  supportedGenericFields
     *            List of all supported generic fields.
	 * @param id
	 *            The current model's id.
	 */
	private void afterSave(LitePalSupport baseObj, List<Field> supportedFields,
                           List<Field> supportedGenericFields, long id) throws IllegalAccessException, InvocationTargetException {
		throwIfSaveFailed(id);
		assignIdValue(baseObj, getIdField(supportedFields), id);
        updateGenericTables(baseObj, supportedGenericFields, id);
        updateAssociatedTableWithFK(baseObj);
        insertIntermediateJoinTableValue(baseObj, false);
	}

	/**
	 * When a model is associated with two different models.
	 * 
	 * @param baseObj
	 *            The class of base object.
     * @param supportedFields
     *            List of all supported fields.
     * @param  supportedGenericFields
     *            List of all supported generic fields.
	 */
	private void doUpdateAction(LitePalSupport baseObj, List<Field> supportedFields, List<Field> supportedGenericFields)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		values.clear();
		beforeUpdate(baseObj, supportedFields, values);
		updating(baseObj, values);
		afterUpdate(baseObj, supportedGenericFields);
	}

	/**
	 * Before updating model, it will be analyzed first. Put all the data
	 * contained by the model into ContentValues, including the fields value and
	 * foreign key value. If the associations between models has been removed.
	 * The foreign key value in database should be cleared too.
	 * 
	 * @param baseObj
	 *            Current model to update.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param values
	 *            To store data of current model for updating.
	 */
	private void beforeUpdate(LitePalSupport baseObj, List<Field> supportedFields, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		putFieldsValue(baseObj, supportedFields, values);
        putForeignKeyValue(values, baseObj);
        for (String fkName : baseObj.getListToClearSelfFK()) {
            values.putNull(fkName);
        }
	}

	/**
	 * Calling
	 * {@link android.database.sqlite.SQLiteDatabase#update(String, android.content.ContentValues, String, String[])} to
	 * update the current model.
	 * 
	 * @param baseObj
	 *            Current model to update.
	 * @param values
	 *            To store data of current model for updating.
	 */
	private void updating(LitePalSupport baseObj, ContentValues values) {
	    if (values.size() > 0) {
            mDatabase.update(baseObj.getTableName(), values, "id = ?",
                    new String[] { String.valueOf(baseObj.getBaseObjId()) });
        }
	}

	/**
	 * After the model is updated, do the extra work that need to do.
	 * 
	 * @param baseObj
	 *            Current model that is updated.
     * @param  supportedGenericFields
     *            List of all supported generic fields.
	 */
	private void afterUpdate(LitePalSupport baseObj, List<Field> supportedGenericFields)
            throws InvocationTargetException, IllegalAccessException {
        updateGenericTables(baseObj, supportedGenericFields, baseObj.getBaseObjId());
        updateAssociatedTableWithFK(baseObj);
        insertIntermediateJoinTableValue(baseObj, true);
        clearFKValueInAssociatedTable(baseObj);
	}

	/**
	 * Get the id field by the passed in field list.
	 * 
	 * @param supportedFields
	 *            The field list to find from.
	 * @return The id field. If not found one return null.
	 */
	private Field getIdField(List<Field> supportedFields) {
		for (Field field : supportedFields) {
			if (isIdColumn(field.getName())) {
				return field;
			}
		}
		return null;
	}

	/**
	 * If the model saved failed, throw an exception.
	 * 
	 * @param id
	 *            The id returned by SQLite. -1 means saved failed.
	 */
	private void throwIfSaveFailed(long id) {
		if (id == -1) {
			throw new LitePalSupportException(LitePalSupportException.SAVE_FAILED);
		}
	}

	/**
	 * Assign the generated id value to the model. The
	 * {@link LitePalSupport#baseObjId} will be assigned anyway. If the model has a
	 * field named id or _id, LitePal will assign it too. The
	 * {@link LitePalSupport#baseObjId} will be used as identify of this model for
	 * system use. The id or _id field will help developers for their own
	 * purpose.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 * @param idField
	 *            The field of id.
	 * @param id
	 *            The value of id.
	 */
	private void assignIdValue(LitePalSupport baseObj, Field idField, long id) {
		try {
			giveBaseObjIdValue(baseObj, id);
			if (idField != null) {
				giveModelIdValue(baseObj, idField.getName(), idField.getType(), id);
			}
		} catch (Exception e) {
			throw new LitePalSupportException(e.getMessage(), e);
		}
	}

	/**
	 * After saving a model, the id for this model will be returned. Assign this
	 * id to the model's id or _id field if it exists.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @param idName
	 *            The name of id. Only id or _id is valid.
	 * @param idType
	 *            The type of id. Only int or long is valid.
	 * @param id
	 *            The value of id.
	 */
	private void giveModelIdValue(LitePalSupport baseObj, String idName, Class<?> idType, long id)
			throws SecurityException, IllegalArgumentException,
			IllegalAccessException {
		if (shouldGiveModelIdValue(idName, idType, id)) {
			Object value;
			if (idType == int.class || idType == Integer.class) {
				value = (int) id;
			} else if (idType == long.class || idType == Long.class) {
				value = id;
			} else {
				throw new LitePalSupportException(LitePalSupportException.ID_TYPE_INVALID_EXCEPTION);
			}
			DynamicExecutor.setField(baseObj, idName, value, baseObj.getClass());
		}
	}

	/**
	 * If the table for this model have a foreign key column, the value of
	 * foreign key id should be saved too.
	 * 
	 * @param values
	 *            The instance of ContentValues to put foreign key value.
	 */
	private void putForeignKeyValue(ContentValues values, LitePalSupport baseObj) {
		Map<String, Long> associatedModelMap = baseObj.getAssociatedModelsMapWithoutFK();
		for (String associatedTableName : associatedModelMap.keySet()) {
			values.put(getForeignKeyColumnName(associatedTableName),
					associatedModelMap.get(associatedTableName));
		}
	}

	/**
	 * Update the foreign keys in the associated model's table.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 */
	private void updateAssociatedTableWithFK(LitePalSupport baseObj) {
		Map<String, Set<Long>> associatedModelMap = baseObj.getAssociatedModelsMapWithFK();
		ContentValues values = new ContentValues();
		for (String associatedTableName : associatedModelMap.keySet()) {
			values.clear();
			String fkName = getForeignKeyColumnName(baseObj.getTableName());
			values.put(fkName, baseObj.getBaseObjId());
			Set<Long> ids = associatedModelMap.get(associatedTableName);
			if (ids != null && !ids.isEmpty()) {
				mDatabase.update(associatedTableName, values, getWhereOfIdsWithOr(ids), null);
			}
		}
	}

	/**
	 * When the associations breaks between current model and associated models,
	 * clear all the associated models' foreign key value if it exists.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 */
	private void clearFKValueInAssociatedTable(LitePalSupport baseObj) {
		List<String> associatedTableNames = baseObj.getListToClearAssociatedFK();
		for (String associatedTableName : associatedTableNames) {
			String fkColumnName = getForeignKeyColumnName(baseObj.getTableName());
			ContentValues values = new ContentValues();
			values.putNull(fkColumnName);
			String whereClause = fkColumnName + " = " + baseObj.getBaseObjId();
			mDatabase.update(associatedTableName, values, whereClause, null);
		}
	}

	/**
	 * Insert values into intermediate join tables for self model and associated
	 * models.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 * @param isUpdate
	 *            The current action is update or not.
	 */
	private void insertIntermediateJoinTableValue(LitePalSupport baseObj, boolean isUpdate) {
		Map<String, List<Long>> associatedIdsM2M = baseObj.getAssociatedModelsMapForJoinTable();
		ContentValues values = new ContentValues();
		for (String associatedTableName : associatedIdsM2M.keySet()) {
			String joinTableName = getIntermediateTableName(baseObj, associatedTableName);
			if (isUpdate) {
				mDatabase.delete(joinTableName, getWhereForJoinTableToDelete(baseObj),
						new String[] { String.valueOf(baseObj.getBaseObjId()) });
			}
			List<Long> associatedIdsM2MSet = associatedIdsM2M.get(associatedTableName);
			if (associatedIdsM2MSet != null) {
				for (long associatedId : associatedIdsM2MSet) {
					values.clear();
					values.put(getForeignKeyColumnName(baseObj.getTableName()), baseObj.getBaseObjId());
					values.put(getForeignKeyColumnName(associatedTableName), associatedId);
					mDatabase.insert(joinTableName, null, values);
				}
			}
		}
	}

	/**
	 * Get the where clause to delete intermediate join table's value for
	 * updating.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 * @return The where clause to execute.
	 */
	private String getWhereForJoinTableToDelete(LitePalSupport baseObj) {
		StringBuilder where = new StringBuilder();
		where.append(getForeignKeyColumnName(baseObj.getTableName()));
		where.append(" = ?");
		return where.toString();
	}

	/**
	 * Judge should assign id value to model's id field. The principle is that
	 * if id name is not null, id type is not null and id is greater than 0,
	 * then should assign id value to it.
	 * 
	 * @param idName
	 *            The name of id field.
	 * @param idType
	 *            The type of id field.
	 * @param id
	 *            The value of id.
	 * @return If id name is not null, id type is not null and id is greater
	 *         than 0, return true. Otherwise return false.
	 */
	private boolean shouldGiveModelIdValue(String idName, Class<?> idType, long id) {
		return idName != null && idType != null && id > 0;
	}

    /**
     * Update the generic data in generic tables. Need to delete the related generic data before
     * saving, because generic data has no id.
     * @param baseObj
     *          Current model that is persisted.
     *@param  supportedGenericFields
     *            List of all supported generic fields.
     * @param id
     *          The id of current model.
     */
    private void updateGenericTables(LitePalSupport baseObj, List<Field> supportedGenericFields,
                                     long id) throws IllegalAccessException, InvocationTargetException {
        for (Field field : supportedGenericFields) {
            Encrypt annotation = field.getAnnotation(Encrypt.class);
            String algorithm = null;
            String genericTypeName = getGenericTypeName(field);
            if (annotation != null && "java.lang.String".equals(genericTypeName)) {
                algorithm = annotation.algorithm();
            }
            field.setAccessible(true);
            Collection<?> collection = (Collection<?>) field.get(baseObj);
            if (collection != null) {
                Log.d(TAG, "updateGenericTables: class name is " + baseObj.getClassName() + " , field name is " + field.getName() );
                String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
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
                        Object[] parameters = new Object[] { changeCase(DBUtility.convertToValidColumnName(field.getName())), object };
                        Class<?>[] parameterTypes = new Class[] { String.class, getGenericTypeClass(field) };
                        DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
                    }
                    mDatabase.insert(tableName, null, values);
                }
            }
        }
    }

}
