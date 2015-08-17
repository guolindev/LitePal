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
import org.litepal.util.BaseUtility;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is a component under DataSupport. It deals with the updating stuff as
 * primary task. Either updating specifying data with id or updating multiple
 * lines with conditions can be done here.
 * 
 * @author Tony Green
 * @since 1.1
 */
class UpdateHandler extends DataHandler {

	/**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of UpdateHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
	UpdateHandler(SQLiteDatabase db) {
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
	int onUpdate(DataSupport baseObj, long id) throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values);
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
	int onUpdate(Class<?> modelClass, long id, ContentValues values) {
		if (values.size() > 0) {
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
	int onUpdateAll(DataSupport baseObj, String... conditions) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values);
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
	int onUpdateAll(String tableName, ContentValues values, String... conditions) {
		return doUpdateAllAction(tableName, values, conditions);
	}

	/**
	 * Do the action for updating multiple rows. It will check the validity of
	 * conditions, then update rows in database. If the format of conditions is
	 * invalid, throw DataSupportException.
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
	 */
	private void putFieldsToDefaultValue(DataSupport baseObj, ContentValues values) {
		String fieldName = null;
		try {
			DataSupport emptyModel = getEmptyModel(baseObj);
			Class<?> emptyModelClass = emptyModel.getClass();
			for (String name : baseObj.getFieldsToSetToDefault()) {
				if (!isIdColumn(name)) {
					fieldName = name;
					Field field = emptyModelClass.getDeclaredField(fieldName);
					putContentValues(emptyModel, field, values);
				}
			}
		} catch (NoSuchFieldException e) {
			throw new DataSupportException(DataSupportException.noSuchFieldExceptioin(
					baseObj.getClassName(), fieldName));
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Unused currently.
	 */
	@SuppressWarnings("unused")
	private int doUpdateAssociations(DataSupport baseObj, long id, ContentValues values) {
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
	private void analyzeAssociations(DataSupport baseObj) {
		try {
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(baseObj
					.getClassName());
			analyzeAssociatedModels(baseObj, associationInfos);
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Unused currently.
	 */
	private void updateSelfTableForeignKey(DataSupport baseObj, ContentValues values) {
		Map<String, Long> associatedModelMap = baseObj.getAssociatedModelsMapWithoutFK();
		for (String associatedTable : associatedModelMap.keySet()) {
			String fkName = getForeignKeyColumnName(associatedTable);
			values.put(fkName, associatedModelMap.get(associatedTable));
		}
	}

	/**
	 * Unused currently.
	 */
	private int updateAssociatedTableForeignKey(DataSupport baseObj, long id) {
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

}