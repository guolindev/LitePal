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
import java.util.Collection;
import java.util.List;

import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.BaseUtility;
import org.litepal.util.LitePalLog;

import android.database.sqlite.SQLiteDatabase;

/**
 * Upgrade the associations between model classes into tables. Creating new
 * tables and adding new foreign key columns are done in
 * {@link org.litepal.tablemanager.AssociationUpdater}. So this class just deal with the simple job of
 * removing foreign key columns and dropping dump intermediate join tables.
 * 
 * @author Tony Green
 * @since 1.0
 */
public abstract class AssociationUpdater extends Creator {

	public static final String TAG = "AssociationUpdater";

	/**
	 * A collection contains all the association models.
	 */
	private Collection<AssociationsModel> mAssociationModels;

	/**
	 * Instance of SQLiteDatabase.
	 */
	protected SQLiteDatabase mDb;

	/**
	 * Analysis the {@link org.litepal.tablemanager.model.TableModel} by the purpose of subclasses, and
	 * generate a SQL to do the intention job. The implementation of this method
	 * is totally delegated to the subclasses.
	 */
	@Override
	protected abstract void createOrUpgradeTable(SQLiteDatabase db, boolean force);

	/**
	 * {@link org.litepal.tablemanager.AssociationUpdater} does two jobs. Removing foreign key columns
	 * when two models are not associated anymore, and remove the intermediate
	 * join tables when two models are not associated anymore.
	 */
	@Override
	protected void addOrUpdateAssociation(SQLiteDatabase db, boolean force) {
		mAssociationModels = getAllAssociations();
		mDb = db;
		removeAssociations();
	}

	/**
	 * This method looks around all the columns in the table, and judge which of
	 * them are foreign key columns.
	 * 
	 * @param tableModel
	 *            Use the TableModel to get table name and columns name to
	 *            generate SQL.
	 * @return All the foreign key columns in a list.
	 */
	protected List<String> getForeignKeyColumns(TableModel tableModel) {
		List<String> foreignKeyColumns = new ArrayList<>();
        Collection<ColumnModel> columnModels = getTableModelFromDB(tableModel.getTableName()).getColumnModels();
		for (ColumnModel columnModel : columnModels) {
            String columnName = columnModel.getColumnName();
			if (isForeignKeyColumnFormat(columnModel.getColumnName())) {
                if (!tableModel.containsColumn(columnName)) {
                    // Now this is a foreign key column.
                    LitePalLog.d(TAG, "getForeignKeyColumnNames >> foreign key column is " + columnName);
                    foreignKeyColumns.add(columnName);
                }
			}
		}
		return foreignKeyColumns;
	}

	/**
	 * Judge the passed in column is a foreign key column or not. Each column
	 * name ends with _id will be considered as foreign key column.
	 * 
	 * @param tableModel
	 *            Use the TableModel to get table name and columns name to
	 *            generate SQL.
	 * @param columnName
	 *            The column to judge.
	 * @return Return true if it's foreign column, otherwise return false.
	 */
	protected boolean isForeignKeyColumn(TableModel tableModel, String columnName) {
		return BaseUtility.containsIgnoreCases(getForeignKeyColumns(tableModel), columnName);
	}

	/**
	 * Look from the database to find a table named same as the table name in
	 * table model. Then iterate the columns and types of this table to create a
	 * new instance of table model. If there's no such a table in the database,
	 * then throw DatabaseGenerateException.
	 * 
	 * @param tableName
	 *            The table name use to get table model from database.
	 * @return A table model object with values from database table.
	 */
	protected TableModel getTableModelFromDB(String tableName) {
		return DBUtility.findPragmaTableInfo(tableName, mDb);
	}

	/**
	 * Drop the tables by the passing table name.
	 * 
	 * @param dropTableNames
	 *            The names of the tables that need to drop.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	protected void dropTables(List<String> dropTableNames, SQLiteDatabase db) {
		if (dropTableNames != null && !dropTableNames.isEmpty()) {
            List<String> dropTableSQLS = new ArrayList<>();
			for (int i = 0; i < dropTableNames.size(); i++) {
                dropTableSQLS.add(generateDropTableSQL(dropTableNames.get(i)));
			}
			execute(dropTableSQLS, db);
		}
	}

	/**
	 * When some fields are removed from class, the table should synchronize the
	 * changes by removing the corresponding columns.
	 * 
	 * @param removeColumnNames
	 *            The column names that need to remove.
	 * @param tableName
	 *            The table name to remove columns from.
	 */
	protected void removeColumns(Collection<String> removeColumnNames, String tableName) {
		if (removeColumnNames != null && !removeColumnNames.isEmpty()) {
			execute(getRemoveColumnSQLs(removeColumnNames, tableName), mDb);
		}
	}

	/**
	 * The values in table_schame should be synchronized with the model tables
	 * in the database. If a model table is dropped, the corresponding data
	 * should be removed from table_schema too.
	 * 
	 * @param tableNames
	 *            The table names need to remove from table_schema.
	 */
	protected void clearCopyInTableSchema(List<String> tableNames) {
		if (tableNames != null && !tableNames.isEmpty()) {
			StringBuilder deleteData = new StringBuilder("delete from ");
			deleteData.append(Const.TableSchema.TABLE_NAME).append(" where");
			boolean needOr = false;
			for (String tableName : tableNames) {
				if (needOr) {
					deleteData.append(" or ");
				}
				needOr = true;
				deleteData.append(" lower(").append(Const.TableSchema.COLUMN_NAME).append(") ");
				deleteData.append("=").append(" lower('").append(tableName).append("')");
			}
			LitePalLog.d(TAG, "clear table schema value sql is " + deleteData);
            List<String> sqls = new ArrayList<>();
            sqls.add(deleteData.toString());
			execute(sqls, mDb);
		}
	}

	/**
	 * When the association between two tables are no longer associated in the
	 * classes, database should remove the foreign key column or intermediate
	 * join table that keeps these two tables associated.
	 */
	private void removeAssociations() {
		removeForeignKeyColumns();
		removeIntermediateTables();
        removeGenericTables();
	}

	/**
	 * Analyzing the table models, then remove all the foreign key columns if
	 * their association in model classes are no longer exist any more.
	 */
	private void removeForeignKeyColumns() {
		for (String className : LitePalAttr.getInstance().getClassNames()) {
			TableModel tableModel = getTableModel(className);
			removeColumns(findForeignKeyToRemove(tableModel), tableModel.getTableName());
		}
	}

	/**
	 * If there're intermediate join tables for two tables, when the two classes
	 * are not associated, the join table should be dropped.
	 */
	private void removeIntermediateTables() {
		List<String> tableNamesToDrop = findIntermediateTablesToDrop();
		dropTables(tableNamesToDrop, mDb);
		clearCopyInTableSchema(tableNamesToDrop);
	}

    /**
     * If there're generic tables for generic fields, when the fields are removed
     * from class, the generic tables should be dropped.
     */
    private void removeGenericTables() {
        List<String> tableNamesToDrop = findGenericTablesToDrop();
        dropTables(tableNamesToDrop, mDb);
        clearCopyInTableSchema(tableNamesToDrop);
    }

	/**
	 * This method gives back the names of the foreign key columns that need to
	 * remove, cause their associations in the classes are no longer exist.
	 * 
	 * @param tableModel
	 *            Use the TableModel to get table name and columns name to
	 *            generate SQL.
	 * @return The foreign key columns need to remove in a list.
	 */
	private List<String> findForeignKeyToRemove(TableModel tableModel) {
		List<String> removeRelations = new ArrayList<>();
		List<String> foreignKeyColumns = getForeignKeyColumns(tableModel);
		String selfTableName = tableModel.getTableName();
		for (String foreignKeyColumn : foreignKeyColumns) {
			String associatedTableName = DBUtility.getTableNameByForeignColumn(foreignKeyColumn);
			if (shouldDropForeignKey(selfTableName, associatedTableName)) {
				removeRelations.add(foreignKeyColumn);
			}
		}
		LitePalLog.d(TAG, "findForeignKeyToRemove >> " + tableModel.getTableName() + " "
				+ removeRelations);
		return removeRelations;
	}

	/**
	 * When many2many associations are no longer exist between two models, the
	 * intermediate join table should be dropped from database. This method
	 * helps find out those intermediate join tables which should be dropped
	 * cause their associations in classes are done.
	 * 
	 * @return A list with all intermediate join tables to drop.
	 */
	private List<String> findIntermediateTablesToDrop() {
		List<String> intermediateTables = new ArrayList<>();
		for (String tableName : DBUtility.findAllTableNames(mDb)) {
			if (DBUtility.isIntermediateTable(tableName, mDb)) {
				boolean dropIntermediateTable = true;
				for (AssociationsModel associationModel : mAssociationModels) {
					if (associationModel.getAssociationType() == Const.Model.MANY_TO_MANY) {
						String intermediateTableName = DBUtility.getIntermediateTableName(
								associationModel.getTableName(),
								associationModel.getAssociatedTableName());
						if (tableName.equalsIgnoreCase(intermediateTableName)) {
							dropIntermediateTable = false;
						}
					}
				}
				if (dropIntermediateTable) {
					// drop the intermediate join table
					intermediateTables.add(tableName);
				}
			}
		}
		LitePalLog.d(TAG, "findIntermediateTablesToDrop >> " + intermediateTables);
		return intermediateTables;
	}

    /**
     * When generic fields are no longer exist in the class models, the generic tables should be
     * dropped from database. This method helps find out those generic tables which should be dropped
     * cause their generic fields in classes are removed.
     *
     * @return A list with all generic tables to drop.
     */
    private List<String> findGenericTablesToDrop() {
        List<String> genericTablesToDrop = new ArrayList<>();
        for (String tableName : DBUtility.findAllTableNames(mDb)) {
            if (DBUtility.isGenericTable(tableName, mDb)) {
                boolean dropGenericTable = true;
                for (GenericModel genericModel : getGenericModels()) {
                    String genericTableName = genericModel.getTableName();
					if (tableName.equalsIgnoreCase(genericTableName)) {
						dropGenericTable = false;
						break;
					}
                }
                if (dropGenericTable) {
                    // drop the generic table
                    genericTablesToDrop.add(tableName);
                }
            }
        }
        return genericTablesToDrop;
    }

	/**
	 * Generate a SQL for renaming the table into a temporary table.
	 * 
	 * @param tableName
	 *            The table name use to alter to temporary table.
	 * @return SQL to rename table.
	 */
	protected String generateAlterToTempTableSQL(String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append("alter table ").append(tableName).append(" rename to ")
				.append(getTempTableName(tableName));
		return sql.toString();
	}

	/**
	 * Generate a SQL to create new table by the table model from database. Also
	 * it will remove the columns that need to remove before generating the SQL.
	 * 
	 * @param removeColumnNames
	 *            The column names need to remove.
	 * @param tableModel
	 *            Which contains table name use to create new table.
	 * @return SQL to create new table.
	 */
	private String generateCreateNewTableSQL(Collection<String> removeColumnNames, TableModel tableModel) {
		for (String removeColumnName : removeColumnNames) {
            tableModel.removeColumnModelByName(removeColumnName);
		}
		return generateCreateTableSQL(tableModel);
	}

	/**
	 * Generate a SQL to do the data migration job to avoid losing data.
	 *
	 * @param tableModel
	 *            Which contains table name use to migrate data.
	 * @return SQL to migrate data.
	 */
	protected String generateDataMigrationSQL(TableModel tableModel) {
        String tableName = tableModel.getTableName();
		Collection<ColumnModel> columnModels = tableModel.getColumnModels();
		if (!columnModels.isEmpty()) {
			StringBuilder sql = new StringBuilder();
			sql.append("insert into ").append(tableName).append("(");
			boolean needComma = false;
			for (ColumnModel columnModel : columnModels) {
				if (needComma) {
					sql.append(", ");
				}
				needComma = true;
				sql.append(columnModel.getColumnName());
			}
			sql.append(") ");
			sql.append("select ");
			needComma = false;
			for (ColumnModel columnModel : columnModels) {
				if (needComma) {
					sql.append(", ");
				}
				needComma = true;
				sql.append(columnModel.getColumnName());
			}
			sql.append(" from ").append(getTempTableName(tableName));
			return sql.toString();
		} else {
			return null;
		}
	}

	/**
	 * Generate a SQL to drop the temporary table.
	 * 
	 * @param tableName
	 *            The table name use to drop temporary table.
	 * @return SQL to drop the temporary table.
	 */
	protected String generateDropTempTableSQL(String tableName) {
		return generateDropTableSQL(getTempTableName(tableName));
	}

	/**
	 * Removing or resizing columns from tables must need a temporary table to
	 * store data, and here's the table name.
	 * 
	 * @param tableName
	 *            The table name use to generate temporary table name.
	 * @return Temporary table name
	 */
	protected String getTempTableName(String tableName) {
		return tableName + "_temp";
	}

	/**
	 * This method create a SQL array for the whole remove dump columns job.
	 * 
	 * @param removeColumnNames
	 *            The column names need to remove.
	 * @param tableName
	 *            The table name to remove from.
	 * @return A SQL list contains create temporary table, create new table,
	 *         migrate data and drop temporary table.
	 */
	private List<String> getRemoveColumnSQLs(Collection<String> removeColumnNames, String tableName) {
        TableModel tableModelFromDB = getTableModelFromDB(tableName);
		String alterToTempTableSQL = generateAlterToTempTableSQL(tableName);
		LitePalLog.d(TAG, "generateRemoveColumnSQL >> " + alterToTempTableSQL);
		String createNewTableSQL = generateCreateNewTableSQL(removeColumnNames, tableModelFromDB);
		LitePalLog.d(TAG, "generateRemoveColumnSQL >> " + createNewTableSQL);
		String dataMigrationSQL = generateDataMigrationSQL(tableModelFromDB);
		LitePalLog.d(TAG, "generateRemoveColumnSQL >> " + dataMigrationSQL);
		String dropTempTableSQL = generateDropTempTableSQL(tableName);
		LitePalLog.d(TAG, "generateRemoveColumnSQL >> " + dropTempTableSQL);
		List<String> createIndexSQLs = generateCreateIndexSQLs(tableModelFromDB);
        List<String> sqls = new ArrayList<>();
        sqls.add(alterToTempTableSQL);
        sqls.add(createNewTableSQL);
        sqls.add(dataMigrationSQL);
        sqls.add(dropTempTableSQL);
        sqls.addAll(createIndexSQLs);
		return sqls;
	}

	/**
	 * Judge if the current iterated foreign key column should be dropped. It is
	 * only used in {@link #findForeignKeyToRemove(org.litepal.tablemanager.model.TableModel)} when iterating
	 * the foreign key column list. When this foreign key can not be found in
	 * the association model collection, this foreign key should be dropped.
	 * 
	 * @param selfTableName
	 *            The table name of currently table model.
	 * @param associatedTableName
	 *            The associated table name of current table model.
	 * @return If the foreign key currently iterated should be dropped, return
	 *         true. Otherwise return false.
	 */
	private boolean shouldDropForeignKey(String selfTableName, String associatedTableName) {
		for (AssociationsModel associationModel : mAssociationModels) {
			if (associationModel.getAssociationType() == Const.Model.ONE_TO_ONE) {
				if (selfTableName.equalsIgnoreCase(associationModel.getTableHoldsForeignKey())) {
					if (associationModel.getTableName().equalsIgnoreCase(selfTableName)) {
						if (isRelationCorrect(associationModel, selfTableName, associatedTableName)) {
							return false;
						}
					} else if (associationModel.getAssociatedTableName().equalsIgnoreCase(
							selfTableName)) {
						if (isRelationCorrect(associationModel, associatedTableName, selfTableName)) {
							return false;
						}
					}
				}
			} else if (associationModel.getAssociationType() == Const.Model.MANY_TO_ONE) {
				if (isRelationCorrect(associationModel, associatedTableName, selfTableName)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Judge if the tableName1 equals {@link org.litepal.tablemanager.model.AssociationsModel#getTableName()}
	 * and tableName2 equals {@link org.litepal.tablemanager.model.AssociationsModel#getAssociatedTableName()}.
	 * 
	 * @param associationModel
	 *            The association model to get table name and associated table
	 *            name.
	 * @param tableName1
	 *            The table name to match table name from association model.
	 * @param tableName2
	 *            The table name to match associated table name from association
	 *            model.
	 * @return Return true if the description is true, otherwise return false.
	 */
	private boolean isRelationCorrect(AssociationsModel associationModel, String tableName1,
			String tableName2) {
		return associationModel.getTableName().equalsIgnoreCase(tableName1)
				&& associationModel.getAssociatedTableName().equalsIgnoreCase(tableName2);
	}

}
