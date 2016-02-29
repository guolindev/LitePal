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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DataSupportException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is a component under DataSupport. It deals with the saving stuff as
 * primary task. All the implementation based on the java reflection API and
 * Android SQLiteDatabase API. It will persist the model class into table. If
 * there're some associated models already persisted, it will build the
 * associations in database automatically between the current model and the
 * associated models.
 * 
 * @author Tony Green
 * @since 1.1
 */
class SaveHandler extends DataHandler {

    /**
     * indicates that associations can be ignored while saving.
     */
    boolean ignoreAssociations = false;

	/**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of SaveHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
	SaveHandler(SQLiteDatabase db) {
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
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	void onSave(DataSupport baseObj) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String className = baseObj.getClassName();
		List<Field> supportedFields = getSupportedFields(className);
		Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
		if (!baseObj.isSaved()) {
            if (!ignoreAssociations) {
                analyzeAssociatedModels(baseObj, associationInfos);
            }
			doSaveAction(baseObj, supportedFields);
            if (!ignoreAssociations) {
                analyzeAssociatedModels(baseObj, associationInfos);
            }
		} else {
            if (!ignoreAssociations) {
                analyzeAssociatedModels(baseObj, associationInfos);
            }
			doUpdateAction(baseObj, supportedFields);
		}
	}

    /**
     * The open interface for other classes in CRUD package to save a model. It
     * is called when a model class calls the save method. This method will only
     * save the baseObj into database without analyzing any association, so that
     * the saving process will be faster.
     *
     * @param baseObj
     *            Current model to persist.
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    void onSaveFast(DataSupport baseObj) throws SecurityException, IllegalArgumentException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ignoreAssociations = true;
        onSave(baseObj);
    }

	/**
	 * The open interface for other classes in CRUD package to save a model
	 * collection. It is called when developer calls
	 * {@link org.litepal.crud.DataSupport#saveAll(java.util.Collection)}. Each model in the collection
	 * will be persisted. If there're associated models detected, each
	 * associated model which is persisted will build association with current
	 * model in database.
	 * 
	 * @param collection
	 *            Holds all models to persist.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	<T extends DataSupport> void onSaveAll(Collection<T> collection) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if (collection != null && collection.size() > 0) {
			DataSupport[] array = collection.toArray(new DataSupport[0]);
			DataSupport firstObj = array[0];
			String className = firstObj.getClassName();
			List<Field> supportedFields = getSupportedFields(className);
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
			for (DataSupport baseObj : array) {
				if (!baseObj.isSaved()) {
					analyzeAssociatedModels(baseObj, associationInfos);
					doSaveAction(baseObj, supportedFields);
					analyzeAssociatedModels(baseObj, associationInfos);
				} else {
					analyzeAssociatedModels(baseObj, associationInfos);
					doUpdateAction(baseObj, supportedFields);
				}
				baseObj.clearAssociatedData();
			}
		}
	}

	/**
	 * Persisting model class into database happens here. But first
	 * {@link #beforeSave(org.litepal.crud.DataSupport, java.util.List, android.content.ContentValues)} will be called to
	 * put the values for ContentValues. When the model is saved,
	 * {@link #afterSave(org.litepal.crud.DataSupport, java.util.List, long)} will be called to do stuffs
	 * after model is saved. Note that SaveSupport won't help with id. Any
	 * developer who wants to set value to id will be ignored here. The value of
	 * id will be generated by SQLite automatically.
	 * 
	 * @param baseObj
	 *            Current model to persist.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	private void doSaveAction(DataSupport baseObj, List<Field> supportedFields)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		ContentValues values = new ContentValues();
		beforeSave(baseObj, supportedFields, values);
		long id = saving(baseObj, values);
		afterSave(baseObj, supportedFields, id);
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
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	private void beforeSave(DataSupport baseObj, List<Field> supportedFields, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		putFieldsValue(baseObj, supportedFields, values);
        if (!ignoreAssociations) {
            putForeignKeyValue(values, baseObj);
        }
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
	private long saving(DataSupport baseObj, ContentValues values) {
		return mDatabase.insert(baseObj.getTableName(), null, values);
	}

	/**
	 * After the model is saved, do the extra work that need to do.
	 * 
	 * @param baseObj
	 *            Current model that is persisted.
	 * @param supportedFields
	 *            List of all supported fields.
	 * @param id
	 *            The current model's id.
	 */
	private void afterSave(DataSupport baseObj, List<Field> supportedFields, long id) {
		throwIfSaveFailed(id);
		assignIdValue(baseObj, getIdField(supportedFields), id);
        if (!ignoreAssociations) {
            updateAssociatedTableWithFK(baseObj);
            insertIntermediateJoinTableValue(baseObj, false);
        }
	}

	/**
	 * When a model is associated with two different models.
	 * 
	 * @param baseObj
	 *            The class of base object.
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	private void doUpdateAction(DataSupport baseObj, List<Field> supportedFields)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		ContentValues values = new ContentValues();
		beforeUpdate(baseObj, supportedFields, values);
		updating(baseObj, values);
		afterUpdate(baseObj);
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
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	private void beforeUpdate(DataSupport baseObj, List<Field> supportedFields, ContentValues values)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		putFieldsValue(baseObj, supportedFields, values);
        if (!ignoreAssociations) {
            putForeignKeyValue(values, baseObj);
            for (String fkName : baseObj.getListToClearSelfFK()) {
                values.putNull(fkName);
            }
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
	private void updating(DataSupport baseObj, ContentValues values) {
		mDatabase.update(baseObj.getTableName(), values, "id = ?",
				new String[] { String.valueOf(baseObj.getBaseObjId()) });
	}

	/**
	 * After the model is updated, do the extra work that need to do.
	 * 
	 * @param baseObj
	 *            Current model that is updated.
	 */
	private void afterUpdate(DataSupport baseObj) {
        if (!ignoreAssociations) {
            updateAssociatedTableWithFK(baseObj);
            insertIntermediateJoinTableValue(baseObj, true);
            clearFKValueInAssociatedTable(baseObj);
        }
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
			throw new DataSupportException(DataSupportException.SAVE_FAILED);
		}
	}

	/**
	 * Assign the generated id value to the model. The
	 * {@link org.litepal.crud.DataSupport#baseObjId} will be assigned anyway. If the model has a
	 * field named id or _id, LitePal will assign it too. The
	 * {@link org.litepal.crud.DataSupport#baseObjId} will be used as identify of this model for
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
	private void assignIdValue(DataSupport baseObj, Field idField, long id) {
		try {
			giveBaseObjIdValue(baseObj, id);
			if (idField != null) {
				giveModelIdValue(baseObj, idField.getName(), idField.getType(), id);
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
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
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void giveModelIdValue(DataSupport baseObj, String idName, Class<?> idType, long id)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		if (shouldGiveModelIdValue(idName, idType, id)) {
			Object value;
			if (idType == int.class || idType == Integer.class) {
				value = (int) id;
			} else if (idType == long.class || idType == Long.class) {
				value = id;
			} else {
				throw new DataSupportException(DataSupportException.ID_TYPE_INVALID_EXCEPTION);
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
	private void putForeignKeyValue(ContentValues values, DataSupport baseObj) {
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
	private void updateAssociatedTableWithFK(DataSupport baseObj) {
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
	private void clearFKValueInAssociatedTable(DataSupport baseObj) {
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
	private void insertIntermediateJoinTableValue(DataSupport baseObj, boolean isUpdate) {
		Map<String, Set<Long>> associatedIdsM2M = baseObj.getAssociatedModelsMapForJoinTable();
		ContentValues values = new ContentValues();
		for (String associatedTableName : associatedIdsM2M.keySet()) {
			String joinTableName = getIntermediateTableName(baseObj, associatedTableName);
			if (isUpdate) {
				mDatabase.delete(joinTableName, getWhereForJoinTableToDelete(baseObj),
						new String[] { String.valueOf(baseObj.getBaseObjId()) });
			}
			Set<Long> associatedIdsM2MSet = associatedIdsM2M.get(associatedTableName);
			for (long associatedId : associatedIdsM2MSet) {
				values.clear();
				values.put(getForeignKeyColumnName(baseObj.getTableName()), baseObj.getBaseObjId());
				values.put(getForeignKeyColumnName(associatedTableName), associatedId);
				mDatabase.insert(joinTableName, null, values);
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
	private String getWhereForJoinTableToDelete(DataSupport baseObj) {
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
}
