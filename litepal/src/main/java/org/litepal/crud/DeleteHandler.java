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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DataSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;

import android.database.sqlite.SQLiteDatabase;

/**
 * This is a component under DataSupport. It deals with the deleting stuff as
 * primary task. If deletes a saved model or delete a record with id, the
 * cascade delete function would work. But considering efficiency, if deletes
 * with deleteAll method, the referenced data in other tables will not be
 * affected. Developers should remove those referenced data by their own.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class DeleteHandler extends DataHandler {

	/**
	 * To store associated tables of current model's table. Only used while
	 * deleting by id and deleting all by model class.
	 */
	private List<String> foreignKeyTableToDelete;

	/**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of DeleteHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
	DeleteHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to delete. Using
	 * baseObj to decide which record to delete. The baseObj must be saved
	 * already(using {@link org.litepal.crud.DataSupport#isSaved()} to test), or nothing will be
	 * deleted. This method can action cascade delete. When the record is
	 * deleted from database, all the referenced data such as foreign key value
	 * will be removed too.
	 * 
	 * @param baseObj
	 *            The record to delete.
	 * @return The number of rows affected. Including cascade delete rows.
	 */
	int onDelete(DataSupport baseObj) {
		if (baseObj.isSaved()) {
			Collection<AssociationsInfo> associationInfos = analyzeAssociations(baseObj);
			int rowsAffected = deleteCascade(baseObj);
			rowsAffected += mDatabase.delete(baseObj.getTableName(), "id = "
					+ baseObj.getBaseObjId(), null);
			clearAssociatedModelSaveState(baseObj, associationInfos);
			return rowsAffected;
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to delete. Using
	 * modelClass to decide which table to delete from, and id to decide a
	 * specific row. This method can action cascade delete. When the record is
	 * deleted from database, all the referenced data such as foreign key value
	 * will be removed too.
	 * 
	 * @param modelClass
	 *            Which table to delete from.
	 * @param id
	 *            Which record to delete.
	 * @return The number of rows affected. Including cascade delete rows.
	 */
	int onDelete(Class<?> modelClass, long id) {
		analyzeAssociations(modelClass);
		int rowsAffected = deleteCascade(modelClass, id);
		rowsAffected += mDatabase.delete(getTableName(modelClass),
				"id = " + id, null);
		getForeignKeyTableToDelete().clear();
		return rowsAffected;
	}

	/**
	 * The open interface for other classes in CRUD package to delete multiple
	 * rows. Using tableName to decide which table to delete from, and
	 * conditions representing the WHERE part of an SQL statement.
	 * 
	 * @param tableName
	 *            Which table to delete from.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The number of rows affected.
	 */
	int onDeleteAll(String tableName, String... conditions) {
		BaseUtility.checkConditionsCorrect(conditions);
		return mDatabase.delete(tableName, getWhereClause(conditions),
				getWhereArgs(conditions));
	}
	
	int onDeleteAll(Class<?> modelClass, String... conditions) {
		BaseUtility.checkConditionsCorrect(conditions);
		analyzeAssociations(modelClass);
		int rowsAffected = deleteAllCascade(modelClass, conditions);
		rowsAffected += mDatabase.delete(getTableName(modelClass), getWhereClause(conditions),
				getWhereArgs(conditions));
		getForeignKeyTableToDelete().clear();
		return rowsAffected;
	}

	/**
	 * Analyze the associations of modelClass and store the associated tables.
	 * The associated tables might be used when deleting referenced data of a
	 * specified row.
	 * 
	 * @param modelClass
	 *            To get associations of this class.
	 */
	private void analyzeAssociations(Class<?> modelClass) {
		Collection<AssociationsInfo> associationInfos = getAssociationInfo(modelClass
				.getName());
		for (AssociationsInfo associationInfo : associationInfos) {
			String associatedTableName = DBUtility
					.getTableNameByClassName(associationInfo
							.getAssociatedClassName());
			if (associationInfo.getAssociationType() == Const.Model.MANY_TO_ONE
					|| associationInfo.getAssociationType() == Const.Model.ONE_TO_ONE) {
				String classHoldsForeignKey = associationInfo
						.getClassHoldsForeignKey();
				if (!modelClass.getName().equals(classHoldsForeignKey)) {
					getForeignKeyTableToDelete().add(associatedTableName);
				}
			} else if (associationInfo.getAssociationType() == Const.Model.MANY_TO_MANY) {
				String joinTableName = DBUtility.getIntermediateTableName(
						getTableName(modelClass), associatedTableName);
				joinTableName = BaseUtility.changeCase(joinTableName);
				getForeignKeyTableToDelete().add(joinTableName);
			}
		}
	}

	/**
	 * Use the analyzed result of associations to delete referenced data. So
	 * this method must be called after {@link #analyzeAssociations(Class)}.
	 * There're two parts of referenced data to delete. The foreign key rows in
	 * associated table and the foreign key rows in intermediate join table.
	 * 
	 * @param modelClass
	 *            To get the table name and combine with id as a foreign key
	 *            column.
	 * @param id
	 *            Delete all the rows which referenced with this id.
	 * @return The number of rows affected in associated tables and intermediate
	 *         join tables.
	 */
	private int deleteCascade(Class<?> modelClass, long id) {
		int rowsAffected = 0;
		for (String associatedTableName : getForeignKeyTableToDelete()) {
			String fkName = getForeignKeyColumnName(getTableName(modelClass));
			rowsAffected += mDatabase.delete(associatedTableName, fkName
					+ " = " + id, null);
		}
		return rowsAffected;
	}

	private int deleteAllCascade(Class<?> modelClass, String... conditions) {
		int rowsAffected = 0;
		for (String associatedTableName : getForeignKeyTableToDelete()) {
			String tableName = getTableName(modelClass);
			String fkName = getForeignKeyColumnName(tableName);
			StringBuilder whereClause = new StringBuilder();
			whereClause.append(fkName).append(" in (select id from ");
			whereClause.append(tableName);
			if (conditions != null && conditions.length > 0) {
				whereClause.append(" where ").append(buildConditionString(conditions));
			}
			whereClause.append(")");
			rowsAffected += mDatabase.delete(associatedTableName,
					BaseUtility.changeCase(whereClause.toString()), null);
		}
		return rowsAffected;
	}
	
	private String buildConditionString(String... conditions) {
		  int argCount = conditions.length - 1;
          String whereClause = conditions[0];
          for (int i = 0; i < argCount; i++) {
                  whereClause = whereClause.replaceFirst("\\?", "'" + conditions[i+1] + "'");
          }
          return whereClause;
	}

	/**
	 * Analyze the associations of baseObj and store the result in it. The
	 * associations will be used when deleting referenced data of baseObj.
	 * 
	 * @param baseObj
	 *            The record to delete.
	 */
	private Collection<AssociationsInfo> analyzeAssociations(DataSupport baseObj) {
		try {
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(baseObj
					.getClassName());
			analyzeAssociatedModels(baseObj, associationInfos);
			return associationInfos;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Clear associated models' save state. After this method, the associated
	 * models of baseObj which data is removed from database will become
	 * unsaved.
	 * 
	 * @param baseObj
	 *            The record to delete.
	 * @param associationInfos
	 *            The associated info.
	 */
	private void clearAssociatedModelSaveState(DataSupport baseObj,
			Collection<AssociationsInfo> associationInfos) {
		try {
			for (AssociationsInfo associationInfo : associationInfos) {
				if (associationInfo.getAssociationType() == Const.Model.MANY_TO_ONE
						&& !baseObj.getClassName().equals(
								associationInfo.getClassHoldsForeignKey())) {
					Collection<DataSupport> associatedModels = getAssociatedModels(
							baseObj, associationInfo);
					if (associatedModels != null && !associatedModels.isEmpty()) {
						for (DataSupport model : associatedModels) {
							if (model != null) {
								model.resetBaseObjId();
							}
						}
					}
				} else if (associationInfo.getAssociationType() == Const.Model.ONE_TO_ONE) {
					DataSupport model = getAssociatedModel(baseObj,
							associationInfo);
					if (model != null) {
						model.resetBaseObjId();
					}
				}
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Use the analyzed result of associations to delete referenced data. So
	 * this method must be called after
	 * {@link #analyzeAssociations(org.litepal.crud.DataSupport)}. There're two parts of
	 * referenced data to delete. The foreign key rows in associated tables and
	 * the foreign key rows in intermediate join tables.
	 * 
	 * @param baseObj
	 *            The record to delete. Now contains associations info.
	 * @return The number of rows affected in associated table and intermediate
	 *         join table.
	 */
	private int deleteCascade(DataSupport baseObj) {
		int rowsAffected;
		rowsAffected = deleteAssociatedForeignKeyRows(baseObj);
		rowsAffected += deleteAssociatedJoinTableRows(baseObj);
		return rowsAffected;
	}

	/**
	 * Delete the referenced rows of baseObj in associated tables(Many2One and
	 * One2One conditions).
	 * 
	 * @param baseObj
	 *            The record to delete. Now contains associations info.
	 * @return The number of rows affected in all associated tables.
	 */
	private int deleteAssociatedForeignKeyRows(DataSupport baseObj) {
		int rowsAffected = 0;
		Map<String, Set<Long>> associatedModelMap = baseObj
				.getAssociatedModelsMapWithFK();
		for (String associatedTableName : associatedModelMap.keySet()) {
			String fkName = getForeignKeyColumnName(baseObj.getTableName());
			rowsAffected += mDatabase.delete(associatedTableName, fkName
					+ " = " + baseObj.getBaseObjId(), null);
		}
		return rowsAffected;
	}

	/**
	 * Delete the referenced rows of baseObj in intermediate join
	 * tables(Many2Many condition).
	 * 
	 * @param baseObj
	 *            The record to delete. Now contains associations info.
	 * @return The number of rows affected in all intermediate join tables.
	 */
	private int deleteAssociatedJoinTableRows(DataSupport baseObj) {
		int rowsAffected = 0;
		Set<String> associatedTableNames = baseObj
				.getAssociatedModelsMapForJoinTable().keySet();
		for (String associatedTableName : associatedTableNames) {
			String joinTableName = DBUtility.getIntermediateTableName(
					baseObj.getTableName(), associatedTableName);
			String fkName = getForeignKeyColumnName(baseObj.getTableName());
			rowsAffected += mDatabase.delete(joinTableName, fkName + " = "
					+ baseObj.getBaseObjId(), null);
		}
		return rowsAffected;
	}

	/**
	 * Get all the associated tables of current model's table. Only used while
	 * deleting by id.
	 * 
	 * @return All the associated tables of current model's table.
	 */
	private List<String> getForeignKeyTableToDelete() {
		if (foreignKeyTableToDelete == null) {
			foreignKeyTableToDelete = new ArrayList<String>();
		}
		return foreignKeyTableToDelete;
	}

}
