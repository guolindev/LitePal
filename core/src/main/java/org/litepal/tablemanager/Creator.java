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

import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a subclass of Generator. Use to create tables. It will automatically
 * build a create table SQL based on the passing TableModel object. In case of
 * there's already a table with the same name in the database, LitePal will
 * always drop the table first before create a new one. If there's syntax error
 * in the executing SQL by accident, Creator will throw a
 * DatabaseGenerateException.
 * 
 * @author Tony Green
 * @since 1.0
 */
class Creator extends AssociationCreator {
	public static final String TAG = "Creator";

	/**
	 * Analyzing the table model, create a table in the database based on the
	 * table model's value.
	 */
	@Override
	protected void createOrUpgradeTable(SQLiteDatabase db, boolean force) {
		for (TableModel tableModel : getAllTableModels()) {
			createOrUpgradeTable(tableModel, db, force);
		}
	}

    protected void createOrUpgradeTable(TableModel tableModel, SQLiteDatabase db, boolean force) {
        execute(getCreateTableSQLs(tableModel, db, force), db);
        giveTableSchemaACopy(tableModel.getTableName(), Const.TableSchema.NORMAL_TABLE, db);
    }

	/**
	 * When creating a new table, it should always try to drop the same name
	 * table if exists. This method create a SQL array for the whole create
	 * table job.
	 * 
	 * @param tableModel
	 *            The table model.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 * @return A SQL array contains drop table if it exists and create new
	 *         table.
	 */
	protected List<String> getCreateTableSQLs(TableModel tableModel, SQLiteDatabase db, boolean force) {
        List<String> sqls = new ArrayList<>();
		if (force) {
            sqls.add(generateDropTableSQL(tableModel));
            sqls.add(generateCreateTableSQL(tableModel));
		} else {
			if (DBUtility.isTableExists(tableModel.getTableName(), db)) {
				return null;
			} else {
                sqls.add(generateCreateTableSQL(tableModel));
			}
		}
		sqls.addAll(generateCreateIndexSQLs(tableModel)); // create index after create table
        return sqls;
	}

	/**
	 * Generate a SQL for dropping table.
	 * 
	 * @param tableModel
	 *            The table model.
	 * @return A SQL to drop table.
	 */
    private String generateDropTableSQL(TableModel tableModel) {
		return generateDropTableSQL(tableModel.getTableName());
	}

	/**
	 * Generate a create table SQL by analyzing the TableModel. Note that it
	 * will always generate a SQL with id/_id column in it as primary key, and
	 * this id is auto increment as integer. Do not try to assign or modify it.
	 * 
	 * @param tableModel
	 *            Use the TableModel to get table name and columns name to
	 *            generate SQL.
	 * @return A generated create table SQL.
	 */
    String generateCreateTableSQL(TableModel tableModel) {
		return generateCreateTableSQL(tableModel.getTableName(), tableModel.getColumnModels(), true);
	}

	/**
	 * Generate create index SQLs by analyzing the TableModel.
	 *
	 * @param tableModel
	 *            Use the TableModel to get table name and columns name to
	 *            generate SQLs.
	 * @return A generated create index SQLs.
	 */
	List<String> generateCreateIndexSQLs(TableModel tableModel) {
		return generateCreateIndexSQLs(tableModel.getTableName(), tableModel.getColumnModels());
	}

}
