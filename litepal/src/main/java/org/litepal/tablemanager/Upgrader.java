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

package org.litepal.tablemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.LogUtil;

import android.database.sqlite.SQLiteDatabase;

/**
 * Upgrade the database. The first step is to remove the columns that can not
 * find the corresponding field in the model class. Then add the new added field
 * as new column into the table. At last it will check all the types of columns
 * to see which are changed.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class Upgrader extends AssociationUpdater {
	/**
	 * Model class for table.
	 */
	protected TableModel mTableModel;

	/**
	 * Analyzing the table model, them remove the dump columns and add new
	 * columns of a table.
	 */
	@Override
	protected void createOrUpgradeTable(SQLiteDatabase db, boolean force) {
		mDb = db;
		for (TableModel tableModel : getAllTableModels()) {
			mTableModel = tableModel;
			upgradeTable();
		}
	}

	/**
	 * Upgrade table actions. Include remove dump columns, add new columns and
	 * change column types. All the actions above will be done by the
	 * description order.
	 */
	private void upgradeTable() {
		removeColumns(findColumnsToRemove(), mTableModel.getTableName());
		addColumn(findColumnsToAdd());
		changeColumnsType(findColumnTypesToChange());
	}

	/**
	 * It will find the difference between class model and table model. If
	 * there's a field in the class without a corresponding column in the table,
	 * this field is a new added column. This method find all new added columns.
	 * 
	 * @return A map contains all new columns with column name as key and column
	 *         type as value.
	 */
	private Map<String, String> findColumnsToAdd() {
		Map<String, String> newColumnsMap = new HashMap<String, String>();
		for (String columnName : mTableModel.getColumnNames()) {
			boolean isNewColumn = true;
			for (String dbColumnName : getTableModelFromDB(mTableModel.getTableName())
					.getColumnNames()) {
				if (columnName.equalsIgnoreCase(dbColumnName)) {
					isNewColumn = false;
					break;
				}
			}
			if (isNewColumn) {
				// add column action
				if (!isIdColumn(columnName)) {
					newColumnsMap.put(columnName, mTableModel.getColumns().get(columnName));
				}
			}
		}
		return newColumnsMap;
	}

	/**
	 * This method helps find the difference between table model from class and
	 * table model from database. Database should always be synchronized with
	 * model class. If there're some fields are removed from class, the table
	 * model from database will be compared to find out which fields are
	 * removed. But there're still some exceptions. The columns named id or _id
	 * won't ever be removed. The foreign key column will be checked some where
	 * else, not from here.
	 * 
	 * @return A list with column names need to remove.
	 */
	private List<String> findColumnsToRemove() {
		TableModel tableModelDB = getTableModelFromDB(mTableModel.getTableName());
		List<String> removeColumns = new ArrayList<String>();
		Map<String, String> dbColumnsMap = tableModelDB.getColumns();
		Set<String> dbColumnNames = dbColumnsMap.keySet();
		for (String dbColumnName : dbColumnNames) {
			if (isNeedToRemove(dbColumnName)) {
				removeColumns.add(dbColumnName);
			}
		}
		for (String removeColumn : removeColumns) {
			LogUtil.d(TAG, "remove column is >> " + removeColumn);
		}
		return removeColumns;
	}

	/**
	 * It will check each class in the mapping list. Find their types for each
	 * field is changed or not by comparing with the types in table columns. If
	 * there's a column have same name as a field in class but with different
	 * type, then it's a type changed column.
	 * 
	 * @return A map contains all type changed columns with column name as key
	 *         and column type as value.
	 */
	private Map<String, String> findColumnTypesToChange() {
		Map<String, String> changeTypeColumns = new HashMap<String, String>();
		TableModel tableModelDB = getTableModelFromDB(mTableModel.getTableName());
		for (String columnNameDB : tableModelDB.getColumnNames()) {
			for (String columnName : mTableModel.getColumnNames()) {
				if (columnNameDB.equalsIgnoreCase(columnName)) {
					String columnTypeDB = tableModelDB.getColumns().get(columnNameDB);
					String columnType = mTableModel.getColumns().get(columnName);
					if (!columnTypeDB.equalsIgnoreCase(columnType)) {
						// column type is changed
						changeTypeColumns.put(columnName, columnType);
					}
				}
			}
		}
		return changeTypeColumns;
	}

	/**
	 * Tell LitePal the column is need to remove or not. The column can be
	 * remove only on the condition that the following three rules are all
	 * passed. First the corresponding field for this column is removed in the
	 * class. Second this column is not an id column. Third this column is not a
	 * foreign key column.
	 * 
	 * @param columnName
	 *            The column name to judge
	 * @return Need to remove return true, otherwise return false.
	 */
	private boolean isNeedToRemove(String columnName) {
		return isRemovedFromClass(columnName) && !isIdColumn(columnName)
				&& !isForeignKeyColumn(mTableModel, columnName);
	}

	/**
	 * Read a column name from database, and judge the corresponding field in
	 * class is removed or not.
	 * 
	 * @param columnName
	 *            The column name to judge.
	 * @return If it's removed return true, or return false.
	 */
	private boolean isRemovedFromClass(String columnName) {
		return !BaseUtility.containsIgnoreCases(mTableModel.getColumnNames(), columnName);
	}

	/**
	 * Generate a SQL for add new column into the existing table.
	 * 
	 * @param columnName
	 *            The new column name.
	 * @param columnType
	 *            The new column type.
	 * @return A SQL to add new column.
	 */
	private String generateAddColumnSQL(String columnName, String columnType) {
		return generateAddColumnSQL(mTableModel.getTableName(), columnName, columnType);
	}

	/**
	 * This method create a SQL array for the all new columns to add them into
	 * table.
	 * 
	 * @param newColumnsMap
	 *            A column map with column name as key and column type as value.
	 * @return A SQL array contains add all new columns job.
	 */
	private String[] getAddColumnSQLs(Map<String, String> newColumnsMap) {
		List<String> sqls = new ArrayList<String>();
		for (String columnName : newColumnsMap.keySet()) {
			sqls.add(generateAddColumnSQL(columnName, newColumnsMap.get(columnName)));
		}
		return sqls.toArray(new String[0]);
	}

	/**
	 * When some fields are added into the class after last upgrade, the table
	 * should synchronize the changes by adding the corresponding columns.
	 * 
	 * @param columnsMap
	 *            A map contains all the new columns need to add with column
	 *            names as key and column types as value.
	 */
	private void addColumn(Map<String, String> columnsMap) {
		execute(getAddColumnSQLs(columnsMap), mDb);
	}

	/**
	 * When some fields type are changed in class, the table should drop the
	 * before columns and create new columns with same name but new types.
	 * 
	 * @param changeTypeColumns
	 *            A map contains all the columns need to change type with column
	 *            names as key and column types as value.
	 */
	private void changeColumnsType(Map<String, String> changeTypeColumns) {
		removeColumns(changeTypeColumns.keySet(), mTableModel.getTableName());
		addColumn(changeTypeColumns);
	}

}
