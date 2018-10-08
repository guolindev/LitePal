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

import java.util.List;

import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import android.database.sqlite.SQLiteDatabase;

/**
 * This is a component under LitePalSupport. It deals with query stuff as primary
 * task.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class QueryHandler extends DataHandler {

	/**
	 * Initialize {@link org.litepal.crud.DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of QueryHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
    public QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to query a record
	 * based on id. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param id
	 *            Which record to query.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with found data from database, or null.
	 */
    public <T> T onFind(Class<T> modelClass, long id, boolean isEager) {
		List<T> dataList = query(modelClass, null, "id = ?", new String[] { String.valueOf(id) },
				null, null, null, null, getForeignKeyAssociations(modelClass.getName(), isEager));
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query the first
	 * record in a table. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with data of first row, or null.
	 */
    public <T> T onFindFirst(Class<T> modelClass, boolean isEager) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id", "1",
				getForeignKeyAssociations(modelClass.getName(), isEager));
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query the last
	 * record in a table. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with data of last row, or null.
	 */
    public <T> T onFindLast(Class<T> modelClass, boolean isEager) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id desc", "1",
				getForeignKeyAssociations(modelClass.getName(), isEager));
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query multiple
	 * records by an id array. Pass no ids means query all rows.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @param ids
	 *            Which records to query. Or do not pass it to find all records.
	 * @return An object list with found data from database, or an empty list.
	 */
    public <T> List<T> onFindAll(Class<T> modelClass, boolean isEager, long... ids) {
		List<T> dataList;
		if (isAffectAllLines(ids)) {
			dataList = query(modelClass, null, null, null, null, null, "id", null,
					getForeignKeyAssociations(modelClass.getName(), isEager));
		} else {
			dataList = query(modelClass, null, getWhereOfIdsWithOr(ids), null, null, null, "id",
					null, getForeignKeyAssociations(modelClass.getName(), isEager));
		}
		return dataList;
	}

	/**
	 * The open interface for other classes in CRUD package to query multiple
	 * records by parameters.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object list with found data from database, or an empty list.
	 */
	public <T> List<T> onFind(Class<T> modelClass, String[] columns, String[] conditions, String orderBy,
			String limit, boolean isEager) {
		BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        orderBy = DBUtility.convertOrderByClauseToValidName(orderBy);
		return query(modelClass, columns, getWhereClause(conditions),
                getWhereArgs(conditions), null, null, orderBy, limit,
                getForeignKeyAssociations(modelClass.getName(), isEager));
	}

	/**
	 * The open interface for other classes in CRUD package to Count the
	 * records.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return Count of the specified table.
	 */
    public int onCount(String tableName, String[] conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		return mathQuery(tableName, new String[] { "count(1)" }, conditions, int.class);
	}

	/**
	 * The open interface for other classes in CRUD package to calculate the
	 * average value on a given column.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return The average value on a given column.
	 */
    public double onAverage(String tableName, String column, String[] conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		return mathQuery(tableName, new String[] { "avg(" + column + ")" }, conditions,
				double.class);
	}

	/**
	 * The open interface for other classes in CRUD package to calculate the
	 * maximum value on a given column.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param type
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
    public <T> T onMax(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		return mathQuery(tableName, new String[] { "max(" + column + ")" }, conditions, type);
	}

	/**
	 * The open interface for other classes in CRUD package to calculate the
	 * minimum value on a given column.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param type
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
    public <T> T onMin(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		return mathQuery(tableName, new String[] { "min(" + column + ")" }, conditions, type);
	}

	/**
	 * The open interface for other classes in CRUD package to calculate the sum
	 * of values on a given column.
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param type
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
    public <T> T onSum(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
		return mathQuery(tableName, new String[] { "sum(" + column + ")" }, conditions, type);
	}

}