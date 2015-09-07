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

package org.litepal.util;

import java.util.ArrayList;
import java.util.List;

import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * A utility class to help LitePal with some database actions. These actions can
 * help classes just do the jobs they care, and help them out of the trivial
 * work.
 * 
 * @author Tony
 * @since 1.0
 */
public class DBUtility {

	/**
	 * Disable to create an instance of DBUtility.
	 */
	private DBUtility() {
	}

	/**
	 * Get the corresponding table name by the full class name with package. It
	 * will only get the short class name without package name as table name.
	 * 
	 * @param className
	 *            Full class name with package.
	 * @return Return the table name by getting the short class name. Return
	 *         null if the class name is null or invalid.
	 * 
	 */
	public static String getTableNameByClassName(String className) {
		if (!TextUtils.isEmpty(className)) {
			if ('.' == className.charAt(className.length() - 1)) {
				return null;
			} else {
                return className.substring(className.lastIndexOf(".") + 1);
			}
		}
		return null;
	}

	/**
	 * Get the corresponding table name list by the full class name list with
	 * package. Each table name will only get the short class name without
	 * package.
	 * 
	 * @param classNames
	 *            The list of full class name with package.
	 * @return Return the table name list.
	 */
	public static List<String> getTableNameListByClassNameList(List<String> classNames) {
		List<String> tableNames = new ArrayList<String>();
		if (classNames != null && !classNames.isEmpty()) {
			for (String className : classNames) {
				tableNames.add(getTableNameByClassName(className));
			}
		}
		return tableNames;
	}

	/**
	 * Get table name by the given foreign column name.
	 * 
	 * @param foreignColumnName
	 *            The foreign column name. Standard pattern is tablename_id.
	 * @return The table name. Return null if the given foreign column is null,
	 *         empty or invalid.
	 */
	public static String getTableNameByForeignColumn(String foreignColumnName) {
		if (!TextUtils.isEmpty(foreignColumnName)) {
			if (foreignColumnName.toLowerCase().endsWith("_id")) {
				return foreignColumnName.substring(0, foreignColumnName.length() - "_id".length());
			}
			return null;
		}
		return null;
	}

	/**
	 * Create intermediate join table name by the concatenation of the two
	 * target table names in alphabetical order with underline in the middle.
	 * 
	 * @param tableName
	 *            First table name.
	 * @param associatedTableName
	 *            The associated table name.
	 * @return The table name by the concatenation of the two target table names
	 *         in alphabetical order with underline in the middle. If the table
	 *         name or associated table name is null of empty, return null.
	 */
	public static String getIntermediateTableName(String tableName, String associatedTableName) {
		if (!(TextUtils.isEmpty(tableName) || TextUtils.isEmpty(associatedTableName))) {
			String intermediateTableName = null;
			if (tableName.toLowerCase().compareTo(associatedTableName.toLowerCase()) <= 0) {
				intermediateTableName = tableName + "_" + associatedTableName;
			} else {
				intermediateTableName = associatedTableName + "_" + tableName;
			}
			return intermediateTableName;
		}
		return null;
	}

	/**
	 * Judge the table name is an intermediate table or not.
	 * 
	 * @param tableName
	 *            Table name in database.
	 * @return Return true if the table name is an intermediate table. Otherwise
	 *         return false.
	 */
	public static boolean isIntermediateTable(String tableName, SQLiteDatabase db) {
		if (!TextUtils.isEmpty(tableName)) {
			if (tableName.matches("[0-9a-zA-Z]+_[0-9a-zA-Z]+")) {
				Cursor cursor = null;
				try {
					cursor = db.query(Const.TableSchema.TABLE_NAME, null, null, null, null, null,
							null);
					if (cursor.moveToFirst()) {
						do {
							String tableNameDB = cursor.getString(cursor
									.getColumnIndexOrThrow(Const.TableSchema.COLUMN_NAME));
							if (tableName.equalsIgnoreCase(tableNameDB)) {
								int tableType = cursor.getInt(cursor
										.getColumnIndexOrThrow(Const.TableSchema.COLUMN_TYPE));
								if (tableType == Const.TableSchema.INTERMEDIATE_JOIN_TABLE) {
									return true;
								}
								break;
							}
						} while (cursor.moveToNext());
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}
		return false;
	}

	/**
	 * Test if the table name passed in exists in the database. Cases are
	 * ignored.
	 * 
	 * @param tableName
	 *            The table name.
	 * @return Return true if there's already a same name table exist, otherwise
	 *         return false.
	 */
	public static boolean isTableExists(String tableName, SQLiteDatabase db) {
		boolean exist;
		try {
			exist = BaseUtility.containsIgnoreCases(findAllTableNames(db), tableName);
		} catch (Exception e) {
			e.printStackTrace();
			exist = false;
		}
		return exist;
	}

	/**
	 * Test if a column exists in a table. Cases are ignored.
	 * 
	 * @param columnName
	 *            The column name.
	 * @param tableName
	 *            The table name.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @return If there's a column named as the column name passed in, return
	 *         true. Or return false. If any of the passed in parameters is null
	 *         or empty, return false.
	 */
	public static boolean isColumnExists(String columnName, String tableName, SQLiteDatabase db) {
		if (TextUtils.isEmpty(columnName) || TextUtils.isEmpty(tableName)) {
			return false;
		}
		boolean exist = false;
        Cursor cursor = null;
		try {
            String checkingColumnSQL = "pragma table_info(" + tableName + ")";
            cursor = db.rawQuery(checkingColumnSQL, null);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (columnName.equalsIgnoreCase(name)) {
                        exist = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
		} catch (Exception e) {
            e.printStackTrace();
            exist = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
		return exist;
	}

	/**
	 * Find all table names in the database. If there's some wrong happens when
	 * finding tables, it will throw exceptions.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @return A list with all table names.
	 * @throws org.litepal.exceptions.DatabaseGenerateException
	 */
	public static List<String> findAllTableNames(SQLiteDatabase db) {
		List<String> tableNames = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from sqlite_master where type = ?", new String[] { "table" });
			if (cursor.moveToFirst()) {
				do {
					String tableName = cursor.getString(cursor.getColumnIndexOrThrow("tbl_name"));
					if (!tableNames.contains(tableName)) {
						tableNames.add(tableName);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseGenerateException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return tableNames;
	}

	/**
	 * Look from the database to find a table named same as the table name in
	 * table model. Then iterate the columns and types of this table to create a
	 * new instance of table model. If there's no such a table in the database,
	 * then throw DatabaseGenerateException.
	 * 
	 * @param tableName
	 *            Table name.
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @return A table model object with values from database table.
	 * @throws org.litepal.exceptions.DatabaseGenerateException
	 */
	public static TableModel findPragmaTableInfo(String tableName, SQLiteDatabase db) {
		if (isTableExists(tableName, db)) {
            List<String> uniqueColumns = findUniqueColumns(tableName, db);
			TableModel tableModelDB = new TableModel();
			tableModelDB.setTableName(tableName);
			String checkingColumnSQL = "pragma table_info(" + tableName + ")";
			Cursor cursor = null;
			try {
				cursor = db.rawQuery(checkingColumnSQL, null);
				if (cursor.moveToFirst()) {
					do {
                        ColumnModel columnModel = new ColumnModel();
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                        boolean nullable = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) != 1;
                        boolean unique = uniqueColumns.contains(name);
                        String defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"));
                        columnModel.setColumnName(name);
                        columnModel.setColumnType(type);
                        columnModel.setIsNullable(nullable);
                        columnModel.setIsUnique(unique);
                        if (defaultValue != null) {
                            defaultValue = defaultValue.replace("'", "");
                        } else {
                            defaultValue = "";
                        }
                        columnModel.setDefaultValue(defaultValue);
						tableModelDB.addColumnModel(columnModel);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new DatabaseGenerateException(e.getMessage());
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return tableModelDB;
		} else {
			throw new DatabaseGenerateException(
					DatabaseGenerateException.TABLE_DOES_NOT_EXIST_WHEN_EXECUTING + tableName);
		}
	}

    /**
     * Find all unique column names of specified table.
     * @param tableName
     *          The table to find unique columns.
     * @param db
     *          Instance of SQLiteDatabase.
     * @return A list with all unique column names of specified table.
     */
    public static List<String> findUniqueColumns(String tableName, SQLiteDatabase db) {
        List<String> columns = new ArrayList<String>();
        Cursor cursor = null;
        Cursor innerCursor = null;
        try {
            cursor = db.rawQuery("pragma index_list(" + tableName +")", null);
            if (cursor.moveToFirst()) {
                do {
                    int unique = cursor.getInt(cursor.getColumnIndexOrThrow("unique"));
                    if (unique == 1) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        innerCursor = db.rawQuery("pragma index_info(" + name + ")", null);
                        if (innerCursor.moveToFirst()) {
                            String columnName = innerCursor.getString(innerCursor.getColumnIndexOrThrow("name"));
                            columns.add(columnName);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseGenerateException(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (innerCursor != null) {
                innerCursor.close();
            }
        }
        return columns;
    }

}
