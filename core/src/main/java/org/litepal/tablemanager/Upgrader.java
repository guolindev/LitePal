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

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.litepal.crud.model.AssociationsInfo;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.LitePalLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * Model class for table from database.
     */
    protected TableModel mTableModelDB;

    /**
     * Indicates that column constraints has changed or not.
     */
    private boolean hasConstraintChanged;

	/**
	 * Analyzing the table model, them remove the dump columns and add new
	 * columns of a table.
	 */
	@Override
	protected void createOrUpgradeTable(SQLiteDatabase db, boolean force) {
		mDb = db;
		for (TableModel tableModel : getAllTableModels()) {
			mTableModel = tableModel;
            mTableModelDB = getTableModelFromDB(tableModel.getTableName());
            LitePalLog.d(TAG, "createOrUpgradeTable: model is " + mTableModel.getTableName());
            upgradeTable();
		}
	}

	/**
	 * Upgrade table actions. Include remove dump columns, add new columns and
	 * change column types. All the actions above will be done by the description
     * order.
	 */
	private void upgradeTable() {
        if (hasNewUniqueOrNotNullColumn()) {
            // Need to drop the table and create new one. Cause unique column can not be added, and null data can not be migrated.
            createOrUpgradeTable(mTableModel, mDb, true);
            // add foreign keys of the table.
            Collection<AssociationsInfo> associationsInfo = getAssociationInfo(mTableModel.getClassName());
            for (AssociationsInfo info : associationsInfo) {
                if (info.getAssociationType() == Const.Model.MANY_TO_ONE
                        || info.getAssociationType() == Const.Model.ONE_TO_ONE) {
                    if (info.getClassHoldsForeignKey().equalsIgnoreCase(mTableModel.getClassName())) {
                        String associatedTableName = DBUtility.getTableNameByClassName(info.getAssociatedClassName());
                        addForeignKeyColumn(mTableModel.getTableName(), associatedTableName, mTableModel.getTableName(), mDb);
                    }
                }
            }
        } else {
            hasConstraintChanged = false;
            removeColumns(findColumnsToRemove());
            addColumns(findColumnsToAdd());
            changeColumnsType(findColumnTypesToChange());
            changeColumnsConstraints();
        }
	}

    /**
     * Check if the current model add or upgrade an unique or not null column.
     * @return True if has new unique or not null column. False otherwise.
     */
    private boolean hasNewUniqueOrNotNullColumn() {
        Collection<ColumnModel> columnModels = mTableModel.getColumnModels();
        for (ColumnModel columnModel : columnModels) {
            if (columnModel.isIdColumn()) continue; // id don't check unique or nullable, we never upgrade it.
            ColumnModel columnModelDB = mTableModelDB.getColumnModelByName(columnModel.getColumnName());
            if (columnModel.isUnique()) {
                if (columnModelDB == null || !columnModelDB.isUnique()) {
                    return true;
                }
            }
            if (columnModelDB != null && !columnModel.isNullable() && columnModelDB.isNullable()) {
                return true;
            }
        }
        return false;
    }

	/**
	 * It will find the difference between class model and table model. If
	 * there's a field in the class without a corresponding column in the table,
	 * this field is a new added column. This method find all new added columns.
	 * 
	 * @return List with ColumnModel contains information of new columns.
	 */
	private List<ColumnModel> findColumnsToAdd() {
        List<ColumnModel> columnsToAdd = new ArrayList<>();
        for (ColumnModel columnModel : mTableModel.getColumnModels()) {
            String columnName = columnModel.getColumnName();
            if (!mTableModelDB.containsColumn(columnName)) {
                // add column action
                columnsToAdd.add(columnModel);
            }
        }
		return columnsToAdd;
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
        String tableName = mTableModel.getTableName();
		List<String> removeColumns = new ArrayList<>();
        Collection<ColumnModel> columnModels = mTableModelDB.getColumnModels();
        for (ColumnModel columnModel : columnModels) {
            String dbColumnName = columnModel.getColumnName();
            if (isNeedToRemove(dbColumnName)) {
                removeColumns.add(dbColumnName);
            }
        }
        LitePalLog.d(TAG, "remove columns from " + tableName + " >> " + removeColumns);
		return removeColumns;
	}

	/**
	 * It will check each class in the mapping list. Find their types for each
	 * field is changed or not by comparing with the types in table columns. If
	 * there's a column have same name as a field in class but with different
	 * type, then it's a type changed column.
	 *
	 * @return A list contains all ColumnModel which type are changed from database.
	 */
	private List<ColumnModel> findColumnTypesToChange() {
        List<ColumnModel> columnsToChangeType = new ArrayList<>();
        for (ColumnModel columnModelDB : mTableModelDB.getColumnModels()) {
            for (ColumnModel columnModel : mTableModel.getColumnModels()) {
                if (columnModelDB.getColumnName().equalsIgnoreCase(columnModel.getColumnName())) {
                    if (!columnModelDB.getColumnType().equalsIgnoreCase(columnModel.getColumnType())) {
                        if (columnModel.getColumnType().equalsIgnoreCase("blob") && TextUtils.isEmpty(columnModelDB.getColumnType())) {
                            // Case for binary array type upgrade. Do nothing under this condition.
                        } else {
                            // column type is changed
                            columnsToChangeType.add(columnModel);
                        }
                    }
                    if (!hasConstraintChanged) {
                        // for reducing loops, check column constraints change here.
                        LitePalLog.d(TAG, "default value db is:" + columnModelDB.getDefaultValue() + ", default value is:" + columnModel.getDefaultValue());
                        if (columnModelDB.isNullable() != columnModel.isNullable() ||
                                !columnModelDB.getDefaultValue().equalsIgnoreCase(columnModel.getDefaultValue()) ||
                                columnModelDB.hasIndex() != columnModel.hasIndex() ||
                                (columnModelDB.isUnique() && !columnModel.isUnique())) { // unique constraint can not be added
                            hasConstraintChanged = true;
                        }
                    }
                }
            }
        }
		return columnsToChangeType;
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
        return !mTableModel.containsColumn(columnName);
	}

	/**
	 * Generate a SQL for add new column into the existing table.
	 * 
	 * @param columnModel
	 *            Which contains column info.
	 * @return A SQL to add new column.
	 */
	private List<String> generateAddColumnSQLs(ColumnModel columnModel) {
	    List<String> sqls = new ArrayList<>();
	    sqls.add(generateAddColumnSQL(mTableModel.getTableName(), columnModel));
	    if (columnModel.hasIndex()) {
	        sqls.add(generateCreateIndexSQL(mTableModel.getTableName(), columnModel));
        }
		return sqls;
	}

	/**
	 * This method create a SQL array for the all new columns to add them into
	 * table.
	 * 
	 * @param columnModelList
	 *            List with ColumnModel to add new column.
	 * @return A SQL list contains add all new columns job.
	 */
	private List<String> getAddColumnSQLs(List<ColumnModel> columnModelList) {
		List<String> sqls = new ArrayList<>();
		for (ColumnModel columnModel : columnModelList) {
			sqls.addAll(generateAddColumnSQLs(columnModel));
		}
		return sqls;
	}

    /**
     * When some fields are removed from class, the table should synchronize the
     * changes by removing the corresponding columns.
     *
     * @param removeColumnNames
     *            The column names that need to remove.
     */
    private void removeColumns(List<String> removeColumnNames) {
        LitePalLog.d(TAG, "do removeColumns " + removeColumnNames);
        removeColumns(removeColumnNames, mTableModel.getTableName());
        for (String columnName : removeColumnNames) {
            mTableModelDB.removeColumnModelByName(columnName);
        }
    }

	/**
	 * When some fields are added into the class after last upgrade, the table
	 * should synchronize the changes by adding the corresponding columns.
	 * 
	 * @param columnModelList
	 *            List with ColumnModel to add new column.
	 */
	private void addColumns(List<ColumnModel> columnModelList) {
        LitePalLog.d(TAG, "do addColumn");
		execute(getAddColumnSQLs(columnModelList), mDb);
        for (ColumnModel columnModel : columnModelList) {
            mTableModelDB.addColumnModel(columnModel);
        }
	}

	/**
	 * When some fields type are changed in class, the table should drop the
	 * before columns and create new columns with same name but new types.
	 * 
	 * @param columnModelList
	 *            List with ColumnModel to change column type.
	 */
	private void changeColumnsType(List<ColumnModel> columnModelList) {
        LitePalLog.d(TAG, "do changeColumnsType");
        List<String> columnNames = new ArrayList<>();
        if (columnModelList != null && !columnModelList.isEmpty()) {
            for (ColumnModel columnModel : columnModelList) {
                columnNames.add(columnModel.getColumnName());
            }
        }
		removeColumns(columnNames);
		addColumns(columnModelList);
	}

    /**
     * When fields annotation changed in class, table should change the corresponding constraints
     * make them sync to the fields annotation.
     */
    private void changeColumnsConstraints() {
        if (hasConstraintChanged) {
            LitePalLog.d(TAG, "do changeColumnsConstraints");
            execute(getChangeColumnsConstraintsSQL(), mDb);
        }
    }

    /**
     * This method create a SQL array for the whole changing column constraints job.
     * @return A SQL list contains create temporary table, create new table, add foreign keys,
     *         migrate data and drop temporary table.
     */
    private List<String> getChangeColumnsConstraintsSQL() {
        String alterToTempTableSQL = generateAlterToTempTableSQL(mTableModel.getTableName());
        String createNewTableSQL = generateCreateTableSQL(mTableModel);
        List<String> addForeignKeySQLs = generateAddForeignKeySQL();
        String dataMigrationSQL = generateDataMigrationSQL(mTableModelDB);
        String dropTempTableSQL = generateDropTempTableSQL(mTableModel.getTableName());
        List<String> createIndexSQLs = generateCreateIndexSQLs(mTableModel);
        List<String> sqls = new ArrayList<>();
        sqls.add(alterToTempTableSQL);
        sqls.add(createNewTableSQL);
        sqls.addAll(addForeignKeySQLs);
        sqls.add(dataMigrationSQL);
        sqls.add(dropTempTableSQL);
        sqls.addAll(createIndexSQLs);
        LitePalLog.d(TAG, "generateChangeConstraintSQL >> ");
        for (String sql : sqls) {
            LitePalLog.d(TAG, sql);
        }
        LitePalLog.d(TAG, "<< generateChangeConstraintSQL");
        return sqls;
    }

    /**
     * Generate a SQL List for adding foreign keys. Changing constraints job should remain all the
     * existing columns including foreign keys. This method add origin foreign keys after creating
     * table.
     * @return A SQL List for adding foreign keys.
     */
    private List<String> generateAddForeignKeySQL() {
        List<String> addForeignKeySQLs = new ArrayList<>();
        List<String> foreignKeyColumns = getForeignKeyColumns(mTableModel);
        for (String foreignKeyColumn : foreignKeyColumns) {
            if (!mTableModel.containsColumn(foreignKeyColumn)) {
                ColumnModel columnModel = new ColumnModel();
                columnModel.setColumnName(foreignKeyColumn);
                columnModel.setColumnType("integer");
                addForeignKeySQLs.add(generateAddColumnSQL(mTableModel.getTableName(), columnModel));
            }
        }
        return addForeignKeySQLs;
    }

}
