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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.LogUtil;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * When models have associations such as one2one, many2one or many2many, tables
 * should add foreign key column or create intermediate table to make the object
 * association mapping right. This process will be proceed automatically without
 * concerning by users. To make this happen, user just need to declare the
 * associations clearly in the models, and make sure all the mapping models are
 * added in the litepal.xml file.
 * 
 * @author Tony Green
 * @since 1.0
 */
public abstract class AssociationCreator extends Generator {

	protected abstract void createOrUpgradeTable(SQLiteDatabase db, boolean force);

	/**
	 * {@link org.litepal.tablemanager.AssociationCreator} analyzes two things. Add associations
	 * including add foreign key column to tables and create intermediate join
	 * tables.
	 */
	@Override
	protected void addOrUpdateAssociation(SQLiteDatabase db, boolean force) {
		addAssociations(getAllAssociations(), db, force);
	}

	/**
	 * Generate a create table SQL by the passed in parameters. Note that it
	 * will always generate a SQL with id/_id column in it as primary key and
	 * this id is auto increment as integer if the autoIncrementId is true, or
	 * no primary key will be added.
	 * 
	 * @param tableName
	 *            The table name.
	 * @param columnModels
	 *            A list contains all column models with column info.
	 * @param autoIncrementId
	 *            Generate an auto increment id or not. Only intermediate join table doesn't need
     *            an auto increment id.
	 * @return A generated create table SQL.
	 */
	protected String generateCreateTableSQL(String tableName, List<ColumnModel> columnModels,
			boolean autoIncrementId) {
		StringBuilder createTableSQL = new StringBuilder("create table ");
		createTableSQL.append(tableName).append(" (");
		if (autoIncrementId) {
			createTableSQL.append("id integer primary key autoincrement,");
		}
		if (columnModels.size() == 0) {
			createTableSQL.deleteCharAt(createTableSQL.length() - 1);
		}
		boolean needSeparator = false;
        for (ColumnModel columnModel : columnModels) {
            if (columnModel.isIdColumn()) {
                continue;
            }
            if (needSeparator) {
                createTableSQL.append(", ");
            }
            needSeparator = true;
            createTableSQL.append(columnModel.getColumnName()).append(" ").append(columnModel.getColumnType());
            if (!columnModel.isNullable()) {
                createTableSQL.append(" not null");
            }
            if (columnModel.isUnique()) {
                createTableSQL.append(" unique");
            }
            String defaultValue = columnModel.getDefaultValue();
            if (!TextUtils.isEmpty(defaultValue)) {
                createTableSQL.append(" default ").append(defaultValue);
            }
        }
		createTableSQL.append(")");
		LogUtil.d(TAG, "create table sql is >> " + createTableSQL);
		return createTableSQL.toString();
	}

	/**
	 * Generate a SQL for dropping table.
	 * 
	 * @param tableName
	 *            The table name.
	 * @return A SQL to drop table.
	 */
	protected String generateDropTableSQL(String tableName) {
		return "drop table if exists " + tableName;
	}

	/**
	 * Generate a SQL for add new column into the existing table.
	 * @param tableName
     *          The table which want to add a column
	 * @param columnModel
	 *          Which contains column info
	 * @return A SQL to add new column.
	 */
	protected String generateAddColumnSQL(String tableName, ColumnModel columnModel) {
		StringBuilder addColumnSQL = new StringBuilder();
		addColumnSQL.append("alter table ").append(tableName);
        addColumnSQL.append(" add column ").append(columnModel.getColumnName());
        addColumnSQL.append(" ").append(columnModel.getColumnType());
        if (!columnModel.isNullable()) {
            addColumnSQL.append(" not null");
        }
        if (columnModel.isUnique()) {
            addColumnSQL.append(" unique");
        }
        String defaultValue = columnModel.getDefaultValue();
        if (!TextUtils.isEmpty(defaultValue)) {
            addColumnSQL.append(" default ").append(defaultValue);
        } else {
            if (!columnModel.isNullable()) {
                if ("integer".equalsIgnoreCase(columnModel.getColumnType())) {
                    defaultValue = "0";
                } else if ("text".equalsIgnoreCase(columnModel.getColumnType())) {
                    defaultValue = "''";
                } else if ("real".equalsIgnoreCase(columnModel.getColumnType())) {
                    defaultValue = "0.0";
                }
                addColumnSQL.append(" default ").append(defaultValue);
            }
        }
		LogUtil.d(TAG, "add column sql is >> " + addColumnSQL);
		return addColumnSQL.toString();
	}

	/**
	 * Judge the passed in column is a foreign key column format or not. Each
	 * column name ends with _id will be considered as foreign key column
	 * format.
	 * 
	 * @param columnName
	 *            The name of column.
	 * @return Return true if it's foreign column format, otherwise return
	 *         false.
	 */
	protected boolean isForeignKeyColumnFormat(String columnName) {
		if (!TextUtils.isEmpty(columnName)) {
			return columnName.toLowerCase().endsWith("_id") && !columnName.equalsIgnoreCase("_id");
		}
		return false;
	}

	/**
	 * Once there's new table created. The table name will be saved into
	 * table_schema as a copy. Each table name will be saved only once.
	 * 
	 * @param tableName
	 *            The table name.
	 * @param tableType
	 *            0 means normal table, 1 means intermediate join table.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	protected void giveTableSchemaACopy(String tableName, int tableType, SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder("select * from ");
		sql.append(Const.TableSchema.TABLE_NAME);
		LogUtil.d(TAG, "giveTableSchemaACopy SQL is >> " + sql);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql.toString(), null);
			if (isNeedtoGiveACopy(cursor, tableName)) {
				ContentValues values = new ContentValues();
				values.put(Const.TableSchema.COLUMN_NAME, BaseUtility.changeCase(tableName));
				values.put(Const.TableSchema.COLUMN_TYPE, tableType);
				db.insert(Const.TableSchema.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Save the name of a created table into table_schema, but there're some
	 * extra rules. Each table name should be only saved once, and special
	 * tables will not be saved.
	 * 
	 * @param cursor
	 *            The cursor used to iterator values in the table.
	 * @param tableName
	 *            The table name.
	 * @return If all rules are passed return true, any of them failed return
	 *         false.
	 */
	private boolean isNeedtoGiveACopy(Cursor cursor, String tableName) {
		return !isValueExists(cursor, tableName) && !isSpecialTable(tableName);
	}

	/**
	 * Judge the table name has already exist in the table_schema or not.
	 * 
	 * @param cursor
	 *            The cursor used to iterator values in the table.
	 * @param tableName
	 *            The table name.
	 * @return If value exists return true, or return false.
	 */
	private boolean isValueExists(Cursor cursor, String tableName) {
		boolean exist = false;
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
				if (name.equalsIgnoreCase(tableName)) {
					exist = true;
					break;
				}
			} while (cursor.moveToNext());
		}
		return exist;
	}

	/**
	 * Judge a table is a special table or not. Currently table_schema is a
	 * special table.
	 * 
	 * @param tableName
	 *            The table name.
	 * @return Return true if it's special table.
	 */
	private boolean isSpecialTable(String tableName) {
		return Const.TableSchema.TABLE_NAME.equalsIgnoreCase(tableName);
	}

	/**
	 * Analyzing all the association models in the collection. Judge their
	 * association types. If it's one2one or many2one associations, add the
	 * foreign key column to the associated table. If it's many2many
	 * associations, create an intermediate join table.
	 * 
	 * @param associatedModels
	 *            A collection contains all the association models.Use the
	 *            association models to get association type and associated
	 *            table names.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	private void addAssociations(Collection<AssociationsModel> associatedModels, SQLiteDatabase db,
			boolean force) {
		for (AssociationsModel associationModel : associatedModels) {
			if (Const.Model.MANY_TO_ONE == associationModel.getAssociationType()
					|| Const.Model.ONE_TO_ONE == associationModel.getAssociationType()) {
				addForeignKeyColumn(associationModel.getTableName(),
						associationModel.getAssociatedTableName(),
						associationModel.getTableHoldsForeignKey(), db);
			} else if (Const.Model.MANY_TO_MANY == associationModel.getAssociationType()) {
				createIntermediateTable(associationModel.getTableName(),
						associationModel.getAssociatedTableName(), db, force);
			}
		}
	}

	/**
	 * When it comes to many2many associations. Database need to create an
	 * intermediate table for mapping this association. This method helps create
	 * such a table, and the table name follows the concatenation of the two
	 * target table names in alphabetical order with underline in the middle.
	 * 
	 * @param tableName
	 *            The table name.
	 * @param associatedTableName
	 *            The associated table name.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	private void createIntermediateTable(String tableName, String associatedTableName,
			SQLiteDatabase db, boolean force) {
        List<ColumnModel> columnModelList = new ArrayList<ColumnModel>();
        ColumnModel column1 = new ColumnModel();
        column1.setColumnName(tableName + "_id");
        column1.setColumnType("integer");
        ColumnModel column2 = new ColumnModel();
        column2.setColumnName(associatedTableName + "_id");
        column2.setColumnType("integer");
        columnModelList.add(column1);
        columnModelList.add(column2);
		String intermediateTableName = DBUtility.getIntermediateTableName(tableName,
				associatedTableName);
		List<String> sqls = new ArrayList<String>();
		if (DBUtility.isTableExists(intermediateTableName, db)) {
			if (force) {
				sqls.add(generateDropTableSQL(intermediateTableName));
				sqls.add(generateCreateTableSQL(intermediateTableName, columnModelList, false));
			}
		} else {
			sqls.add(generateCreateTableSQL(intermediateTableName, columnModelList, false));
		}
		execute(sqls.toArray(new String[0]), db);
		giveTableSchemaACopy(intermediateTableName, Const.TableSchema.INTERMEDIATE_JOIN_TABLE, db);
	}

	/**
	 * This method is used to add many to one association or one to one
	 * association on tables. It will automatically build a SQL to add foreign
	 * key to a table. If the passed in table name or associated table name
	 * doesn't exist, it will throw an exception.
	 * 
	 * @param tableName
	 *            The table name.
	 * @param associatedTableName
	 *            The associated table name.
	 * @param tableHoldsForeignKey
	 *            The table which holds the foreign key.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * 
	 * @throws org.litepal.exceptions.DatabaseGenerateException
	 */
	protected void addForeignKeyColumn(String tableName, String associatedTableName,
			String tableHoldsForeignKey, SQLiteDatabase db) {
		if (DBUtility.isTableExists(tableName, db)) {
			if (DBUtility.isTableExists(associatedTableName, db)) {
				String foreignKeyColumn = null;
				if (tableName.equals(tableHoldsForeignKey)) {
					foreignKeyColumn = getForeignKeyColumnName(associatedTableName);
				} else if (associatedTableName.equals(tableHoldsForeignKey)) {
					foreignKeyColumn = getForeignKeyColumnName(tableName);
				}
				if (!DBUtility.isColumnExists(foreignKeyColumn, tableHoldsForeignKey, db)) {
                    ColumnModel columnModel = new ColumnModel();
                    columnModel.setColumnName(foreignKeyColumn);
                    columnModel.setColumnType("integer");
					String[] sqls = { generateAddColumnSQL(tableHoldsForeignKey, columnModel) };
					execute(sqls, db);
				} else {
					LogUtil.d(TAG, "column " + foreignKeyColumn
							+ " is already exist, no need to add one");
				}
			} else {
				throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST
						+ associatedTableName);
			}
		} else {
			throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST
					+ tableName);
		}
	}

}
