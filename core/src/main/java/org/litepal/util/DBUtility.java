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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Pair;

import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to help LitePal with some database actions. These actions can
 * help classes just do the jobs they care, and help them out of the trivial
 * work.
 * 
 * @author Tony
 * @since 1.0
 */
public class DBUtility {

    private static final String TAG = "DBUtility";

    private static final String SQLITE_KEYWORDS = ",abort,add,after,all,alter,and,as,asc,autoincrement,before,begin,between,by,cascade,check,collate,column,commit,conflict,constraint,create,cross,database,deferrable,deferred,delete,desc,distinct,drop,each,end,escape,except,exclusive,exists,foreign,from,glob,group,having,in,index,inner,insert,intersect,into,is,isnull,join,like,limit,match,natural,not,notnull,null,of,offset,on,or,order,outer,plan,pragma,primary,query,raise,references,regexp,reindex,release,rename,replace,restrict,right,rollback,row,savepoint,select,set,table,temp,temporary,then,to,transaction,trigger,union,unique,update,using,vacuum,values,view,virtual,when,where,";

    private static final String KEYWORDS_COLUMN_SUFFIX = "_lpcolumn";

    private static final String REG_OPERATOR = "\\s*(=|!=|<>|<|>)";

    private static final String REG_FUZZY = "\\s+(not\\s+)?(like|between)\\s+";

    private static final String REG_COLLECTION = "\\s+(not\\s+)?(in)\\s*\\(";

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
	 * Get the index name by column name.
	 * In LitePal index name always named like columnName_index.
	 * @param tableName
	 * 			Table name.
	 * @param columnName
	 * 			Column name.
	 * @return Index name or null if column name is null or empty.
	 */
	public static String getIndexName(String tableName, String columnName) {
		if (!TextUtils.isEmpty(tableName) && !TextUtils.isEmpty(columnName)) {
			return tableName + "_" + columnName + "_index";
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
			if (foreignColumnName.toLowerCase(Locale.US).endsWith("_id")) {
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
            String intermediateTableName;
            if (tableName.toLowerCase(Locale.US).compareTo(associatedTableName.toLowerCase(Locale.US)) <= 0) {
                intermediateTableName = tableName + "_" + associatedTableName;
            } else {
                intermediateTableName = associatedTableName + "_" + tableName;
            }
            return intermediateTableName;
        }
        return null;
	}

    /**
     * Create generic table name by the concatenation of the class model's table name and simple
     * generic type name with underline in the middle.
     * @param className
     *          Name of the class model.
     * @param fieldName
     *          Name of the generic type field.
     * @return Table name by the concatenation of the class model's table name and simple
     *         generic type name with underline in the middle.
     */
    public static String getGenericTableName(String className, String fieldName) {
        String tableName = getTableNameByClassName(className);
        return BaseUtility.changeCase(tableName + "_" + fieldName);
    }

    /**
     * The column name for referenced id in generic table.
     * @param className
     *          Name of the class model.
     * @return The column name for referenced id in generic table.
     */
    public static String getGenericValueIdColumnName(String className) {
        return BaseUtility.changeCase(getTableNameByClassName(className) + "_id");
    }

    public static String getM2MSelfRefColumnName(Field field) {
        return BaseUtility.changeCase(field.getName() + "_id");
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
     * Judge the table name is an generic table or not.
     *
     * @param tableName
     *            Table name in database.
     * @return Return true if the table name is an generic table. Otherwise
     *         return false.
     */
    public static boolean isGenericTable(String tableName, SQLiteDatabase db) {
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
                                if (tableType == Const.TableSchema.GENERIC_TABLE) {
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
			Pair<Set<String>, Set<String>> indexPair = findIndexedColumns(tableName, db);
			Set<String> indexColumns = indexPair.first;
			Set<String> uniqueColumns = indexPair.second;
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
                        boolean hasIndex = indexColumns.contains(name);
                        String defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"));
                        columnModel.setColumnName(name);
                        columnModel.setColumnType(type);
                        columnModel.setNullable(nullable);
                        columnModel.setUnique(unique);
                        columnModel.setHasIndex(hasIndex);
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
     * Find all columns with index, including normal index and unique index of specified table.
     * @param tableName
     *          The table to find unique columns.
     * @param db
     *          Instance of SQLiteDatabase.
     * @return A pair contains two types of index columns. First is normal index columns. Second is unique index columns.
     */
    public static Pair<Set<String>, Set<String>> findIndexedColumns(String tableName, SQLiteDatabase db) {
		Set<String> indexColumns = new HashSet<>();
		Set<String> uniqueColumns = new HashSet<>();
        Cursor cursor = null;
        Cursor innerCursor = null;
        try {
            cursor = db.rawQuery("pragma index_list(" + tableName +")", null);
            if (cursor.moveToFirst()) {
                do {
                    boolean unique = cursor.getInt(cursor.getColumnIndexOrThrow("unique")) == 1;
					String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
					innerCursor = db.rawQuery("pragma index_info(" + name + ")", null);
					if (innerCursor.moveToFirst()) {
						String columnName = innerCursor.getString(innerCursor.getColumnIndexOrThrow("name"));
						if (unique) {
							uniqueColumns.add(columnName);
						} else {
							indexColumns.add(columnName);
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
        return new Pair<>(indexColumns, uniqueColumns);
    }

    /**
     * If the field name is conflicted with SQLite keywords. Return true if conflicted, return false
     * otherwise.
     * @param fieldName
     *          Name of the field.
     * @return True if conflicted, false otherwise.
     */
    public static boolean isFieldNameConflictWithSQLiteKeywords(String fieldName) {
        if (!TextUtils.isEmpty(fieldName)) {
            String fieldNameWithComma = "," + fieldName.toLowerCase(Locale.US) + ",";
			return SQLITE_KEYWORDS.contains(fieldNameWithComma);
        }
        return false;
    }

    /**
     * Convert the passed in name to valid column name if the name is conflicted with SQLite keywords.
     * The convert rule is to append {@link #KEYWORDS_COLUMN_SUFFIX} to the name as new column name.
     * @param columnName
     *          Original column name.
     * @return Converted name as new column name if conflicted with SQLite keywords.
     */
    public static String convertToValidColumnName(String columnName) {
        if (isFieldNameConflictWithSQLiteKeywords(columnName)) {
            return columnName + KEYWORDS_COLUMN_SUFFIX;
        }
        return columnName;
    }

    /**
     * Convert the where clause if it contains invalid column names which conflict with SQLite keywords.
     * @param whereClause
     *          where clause for query, update or delete.
     * @return Converted where clause with valid column names.
     */
    public static String convertWhereClauseToColumnName(String whereClause) {
        if (!TextUtils.isEmpty(whereClause)) {
            try {
                StringBuffer convertedWhereClause = new StringBuffer();
                Pattern p = Pattern.compile("(\\w+" + REG_OPERATOR + "|\\w+" + REG_FUZZY + "|\\w+" + REG_COLLECTION + ")");
                Matcher m = p.matcher(whereClause);
                while (m.find()) {
                    String matches = m.group();
                    String column = matches.replaceAll("(" + REG_OPERATOR + "|" + REG_FUZZY + "|" + REG_COLLECTION + ")", "");
                    String rest = matches.replace(column, "");
                    column = convertToValidColumnName(column);
                    m.appendReplacement(convertedWhereClause, column + rest);
                }
                m.appendTail(convertedWhereClause);
                return convertedWhereClause.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return whereClause;
    }

    /**
     * Convert the select clause if it contains invalid column names which conflict with SQLite keywords.
     * @param columns
     *          A String array of which columns to return. Passing null will
     *          return all columns.
     * @return Converted select clause with valid column names.
     */
    public static String[] convertSelectClauseToValidNames(String[] columns) {
        if (columns != null && columns.length > 0) {
            String[] convertedColumns = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                convertedColumns[i] = convertToValidColumnName(columns[i]);
            }
            return convertedColumns;
        }
        return null;
    }

    /**
     * Convert the order by clause if it contains invalid column names which conflict with SQLite keywords.
     * @param orderBy
     *          How to order the rows, formatted as an SQL ORDER BY clause. Passing null will use
     *          the default sort order, which may be unordered.
     * @return Converted order by clause with valid column names.
     */
    public static String convertOrderByClauseToValidName(String orderBy) {
        if (!TextUtils.isEmpty(orderBy)) {
            orderBy = orderBy.trim().toLowerCase(Locale.US);
            if (orderBy.contains(",")) {
                String[] orderByItems = orderBy.split(",");
                StringBuilder builder = new StringBuilder();
                boolean needComma = false;
                for (String orderByItem : orderByItems) {
                    if (needComma) {
                        builder.append(",");
                    }
                    builder.append(convertOrderByItem(orderByItem));
                    needComma = true;
                }
                orderBy = builder.toString();
            } else {
                orderBy = convertOrderByItem(orderBy);
            }
            return orderBy;
        }
        return null;
    }

    /**
     * Convert the order by item if it is invalid column name which conflict with SQLite keywords.
     * @param orderByItem
     *          The single order by condition.
     * @return Converted order by item with valid column name.
     */
    private static String convertOrderByItem(String orderByItem) {
        String column;
        String append;
        if (orderByItem.endsWith("asc")) {
            column = orderByItem.replace("asc", "").trim();
            append = " asc";
        } else if (orderByItem.endsWith("desc")) {
            column = orderByItem.replace("desc", "").trim();
            append = " desc";
        } else {
            column = orderByItem;
            append = "";
        }
        return convertToValidColumnName(column) + append;
    }

}
