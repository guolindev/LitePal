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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DataSupport connects classes to SQLite database tables to establish an almost
 * zero-configuration persistence layer for applications. In the context of an
 * application, these classes are commonly referred to as models. Models can
 * also be connected to other models.<br>
 * DataSupport relies heavily on naming in that it uses class and association
 * names to establish mappings between respective database tables and foreign
 * key columns.<br>
 * Automated mapping between classes and tables, attributes and columns.
 * 
 * <pre>
 * public class Person extends DataSupport {
 * 	private int id;
 * 	private String name;
 * 	private int age;
 * }
 * 
 * The Person class is automatically mapped to the table named "person",
 * which might look like this:
 * 
 * CREATE TABLE person (
 * 	id integer primary key autoincrement,
 * 	age integer, 
 * 	name text
 * );
 * </pre>
 * 
 * @author Tony Green
 * @since 1.1
 */
public class DataSupport {

	/**
	 * The identify of each model. LitePal will generate the value
	 * automatically. Do not try to assign or modify it.
	 */
	private long baseObjId;

	/**
	 * A map contains all the associated models' id with M2O or O2O
	 * associations. Each corresponding table of these models contains a foreign
	 * key column.
	 */
	private Map<String, Set<Long>> associatedModelsMapWithFK;

	/**
	 * A map contains all the associated models' id with M2O or O2O association.
	 * Each corresponding table of these models doesn't contain foreign key
	 * column. Instead self model has a foreign key column in the corresponding
	 * table.
	 */
	private Map<String, Long> associatedModelsMapWithoutFK;

	/**
	 * A map contains all the associated models' id with M2M association.
	 */
	private Map<String, Set<Long>> associatedModelsMapForJoinTable;

	/**
	 * When updating a model and the associations breaks between current model
	 * and others, if current model holds a foreign key, it need to be cleared.
	 * This list holds all the foreign key names that need to clear.
	 */
	private List<String> listToClearSelfFK;

	/**
	 * When updating a model and the associations breaks between current model
	 * and others, clear all the associated models' foreign key value if it
	 * exists. This list holds all the associated table names that need to
	 * clear.
	 */
	private List<String> listToClearAssociatedFK;

	/**
	 * A list holds all the field names which need to be updated into default
	 * value of model.
	 */
	private List<String> fieldsToSetToDefault;

	/**
	 * Declaring to query which columns in table.
	 * 
	 * <pre>
	 * DataSupport.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows with name and age columns in Person table.
	 * 
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 * 
	 * @return A ClusterQuery instance.
	 */
	public static synchronized ClusterQuery select(String... columns) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mColumns = columns;
		return cQuery;
	}

	/**
	 * Declaring to query which rows in table.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find rows which name is Tom or age greater than 14 in Person
	 * table.
	 * 
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return A ClusterQuery instance.
	 */
	public static synchronized ClusterQuery where(String... conditions) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mConditions = conditions;
		return cQuery;
	}

	/**
	 * Declaring how to order the rows queried from table.
	 * 
	 * <pre>
	 * DataSupport.order(&quot;name desc&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows in Person table sorted by name with inverted
	 * order.
	 * 
	 * @param column
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @return A ClusterQuery instance.
	 */
	public static synchronized ClusterQuery order(String column) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mOrderBy = column;
		return cQuery;
	}

	/**
	 * Limits the number of rows returned by the query.
	 * 
	 * <pre>
	 * DataSupport.limit(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the top 2 rows in Person table.
	 * 
	 * @param value
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @return A ClusterQuery instance.
	 */
	public static synchronized ClusterQuery limit(int value) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mLimit = String.valueOf(value);
		return cQuery;
	}

	/**
	 * Declaring the offset of rows returned by the query. This method must be
	 * used with {@link #limit(int)}, or nothing will return.
	 * 
	 * <pre>
	 * DataSupport.limit(1).offset(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the third row in Person table.
	 * 
	 * @param value
	 *            The offset amount of rows returned by the query.
	 * @return A ClusterQuery instance.
	 */
	public static synchronized ClusterQuery offset(int value) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mOffset = String.valueOf(value);
		return cQuery;
	}

	/**
	 * Count the records.
	 * 
	 * <pre>
	 * DataSupport.count(Person.class);
	 * </pre>
	 * 
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @return Count of the specified table.
	 */
	public static synchronized int count(Class<?> modelClass) {
		return count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
	}

	/**
	 * Count the records.
	 * 
	 * <pre>
	 * DataSupport.count(&quot;person&quot;);
	 * </pre>
	 * 
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @return Count of the specified table.
	 */
	public static synchronized int count(String tableName) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.count(tableName);
	}

	/**
	 * Calculates the average value on a given column.
	 * 
	 * <pre>
	 * DataSupport.average(Person.class, &quot;age&quot;);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public static synchronized double average(Class<?> modelClass, String column) {
		return average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
	}

	/**
	 * Calculates the average value on a given column.
	 * 
	 * <pre>
	 * DataSupport.average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public static synchronized double average(String tableName, String column) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.average(tableName, column);
	}

	/**
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.max(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public static synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
		return max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

	/**
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.max(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public static synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.max(tableName, columnName, columnType);
	}

	/**
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.min(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public static synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
		return min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

	/**
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.min(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public static synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.min(tableName, columnName, columnType);
	}

	/**
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.sum(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public static synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
		return sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

	/**
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 * 
	 * <pre>
	 * DataSupport.sum(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 * 
	 * You can also specify a where clause when calculating.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 * 
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public static synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.sum(tableName, columnName, columnType);
	}

	/**
	 * Finds the record by a specific id.
	 * 
	 * <pre>
	 * Person p = DataSupport.find(Person.class, 1);
	 * </pre>
	 * 
	 * The modelClass determines which table to query and the object type to
	 * return. If no record can be found, then return null. <br>
	 * 
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
	 * {@link org.litepal.crud.DataSupport#find(Class, long, boolean)}.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param id
	 *            Which record to query.
	 * @return An object with found data from database, or null.
	 */
	public static synchronized <T> T find(Class<T> modelClass, long id) {
		return find(modelClass, id, false);
	}

	/**
	 * It is mostly same as {@link org.litepal.crud.DataSupport#find(Class, long)} but an isEager
	 * parameter. If set true the associated models will be loaded as well.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param id
	 *            Which record to query.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with found data from database, or null.
	 */
	public static synchronized <T> T find(Class<T> modelClass, long id, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFind(modelClass, id, isEager);
	}

	/**
	 * Finds the first record of a single table.
	 * 
	 * <pre>
	 * Person p = DataSupport.findFirst(Person.class);
	 * </pre>
	 * 
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
	 * {@link org.litepal.crud.DataSupport#findFirst(Class, boolean)}.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @return An object with data of first row, or null.
	 */
	public static synchronized <T> T findFirst(Class<T> modelClass) {
		return findFirst(modelClass, false);
	}

	/**
	 * It is mostly same as {@link org.litepal.crud.DataSupport#findFirst(Class)} but an isEager
	 * parameter. If set true the associated models will be loaded as well.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with data of first row, or null.
	 */
	public static synchronized <T> T findFirst(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindFirst(modelClass, isEager);
	}

	/**
	 * Finds the last record of a single table.
	 * 
	 * <pre>
	 * Person p = DataSupport.findLast(Person.class);
	 * </pre>
	 * 
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
	 * {@link org.litepal.crud.DataSupport#findLast(Class, boolean)}.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @return An object with data of last row, or null.
	 */
	public static synchronized <T> T findLast(Class<T> modelClass) {
		return findLast(modelClass, false);
	}

	/**
	 * It is mostly same as {@link org.litepal.crud.DataSupport#findLast(Class)} but an isEager
	 * parameter. If set true the associated models will be loaded as well.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object with data of last row, or null.
	 */
	public static synchronized <T> T findLast(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindLast(modelClass, isEager);
	}

	/**
	 * Finds multiple records by an id array.
	 * 
	 * <pre>
	 * List&lt;Person&gt; people = DataSupport.findAll(Person.class, 1, 2, 3);
	 * 
	 * long[] bookIds = { 10, 18 };
	 * List&lt;Book&gt; books = DataSupport.findAll(Book.class, bookIds);
	 * </pre>
	 * 
	 * Of course you can find all records by passing nothing to the ids
	 * parameter.
	 * 
	 * <pre>
	 * List&lt;Book&gt; allBooks = DataSupport.findAll(Book.class);
	 * </pre>
	 * 
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
	 * {@link org.litepal.crud.DataSupport#findAll(Class, boolean, long...)}.
	 * 
	 * The modelClass determines which table to query and the object type to
	 * return.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param ids
	 *            Which records to query. Or do not pass it to find all records.
	 * @return An object list with found data from database, or an empty list.
	 */
	public static synchronized <T> List<T> findAll(Class<T> modelClass, long... ids) {
		return findAll(modelClass, false, ids);
	}

	/**
	 * It is mostly same as {@link org.litepal.crud.DataSupport#findAll(Class, long...)} but an
	 * isEager parameter. If set true the associated models will be loaded as
	 * well.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @param ids
	 *            Which records to query. Or do not pass it to find all records.
	 * @return An object list with found data from database, or an empty list.
	 */
	public static synchronized <T> List<T> findAll(Class<T> modelClass, boolean isEager,
			long... ids) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindAll(modelClass, isEager, ids);
	}

	/**
	 * Runs the provided SQL and returns a Cursor over the result set. You may
	 * include ?s in where clause in the query, which will be replaced by the
	 * second to the last parameters, such as:
	 * 
	 * <pre>
	 * Cursor cursor = DataSupport.findBySQL(&quot;select * from person where name=? and age=?&quot;, &quot;Tom&quot;, &quot;14&quot;);
	 * </pre>
	 * 
	 * @param sql
	 *            First parameter is the SQL clause to apply. Second to the last
	 *            parameters will replace the place holders.
	 * @return A Cursor object, which is positioned before the first entry. Note
	 *         that Cursors are not synchronized, see the documentation for more
	 *         details.
	 */
	public static synchronized Cursor findBySQL(String... sql) {
		BaseUtility.checkConditionsCorrect(sql);
		if (sql == null) {
			return null;
		}
		if (sql.length <= 0) {
			return null;
		}
		String[] selectionArgs;
		if (sql.length == 1) {
			selectionArgs = null;
		} else {
			selectionArgs = new String[sql.length - 1];
			System.arraycopy(sql, 1, selectionArgs, 0, sql.length - 1);
		}
		return Connector.getDatabase().rawQuery(sql[0], selectionArgs);
	}

	/**
	 * Deletes the record in the database by id.<br>
	 * The data in other tables which is referenced with the record will be
	 * removed too.
	 * 
	 * <pre>
	 * DataSupport.delete(Person.class, 1);
	 * </pre>
	 * 
	 * This means that the record 1 in person table will be removed.
	 * 
	 * @param modelClass
	 *            Which table to delete from by class.
	 * @param id
	 *            Which record to delete.
	 * @return The number of rows affected. Including cascade delete rows.
	 */
	public static synchronized int delete(Class<?> modelClass, long id) {
		int rowsAffected = 0;
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			DeleteHandler deleteHandler = new DeleteHandler(db);
			rowsAffected = deleteHandler.onDelete(modelClass, id);
			db.setTransactionSuccessful();
			return rowsAffected;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Deletes all records with details given if they match a set of conditions
	 * supplied. This method constructs a single SQL DELETE statement and sends
	 * it to the database.
	 * 
	 * <pre>
	 * DataSupport.deleteAll(Person.class, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
	 * </pre>
	 * 
	 * This means that all the records which name is Tom and age is 14 will be
	 * removed.<br>
	 * 
	 * @param modelClass
	 *            Which table to delete from by class.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement. First parameter is the WHERE clause to apply when
	 *            deleting. The way of specifying place holders is to insert one
	 *            or more question marks in the SQL. The first question mark is
	 *            replaced by the second element of the array, the next question
	 *            mark by the third, and so on. Passing empty string will update
	 *            all rows.
	 * @return The number of rows affected.
	 */
	public static synchronized int deleteAll(Class<?> modelClass, String... conditions) {
		DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
		return deleteHandler.onDeleteAll(modelClass, conditions);
	}

	/**
	 * Deletes all records with details given if they match a set of conditions
	 * supplied. This method constructs a single SQL DELETE statement and sends
	 * it to the database.
	 * 
	 * <pre>
	 * DataSupport.deleteAll(&quot;person&quot;, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
	 * </pre>
	 * 
	 * This means that all the records which name is Tom and age is 14 will be
	 * removed.<br>
	 * 
	 * Note that this method won't delete the referenced data in other tables.
	 * You should remove those values by your own.
	 * 
	 * @param tableName
	 *            Which table to delete from.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement. First parameter is the WHERE clause to apply when
	 *            deleting. The way of specifying place holders is to insert one
	 *            or more question marks in the SQL. The first question mark is
	 *            replaced by the second element of the array, the next question
	 *            mark by the third, and so on. Passing empty string will update
	 *            all rows.
	 * @return The number of rows affected.
	 */
	public static synchronized int deleteAll(String tableName, String... conditions) {
		DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
		return deleteHandler.onDeleteAll(tableName, conditions);
	}

	/**
	 * Updates the corresponding record by id with ContentValues. Returns the
	 * number of affected rows.
	 * 
	 * <pre>
	 * ContentValues cv = new ContentValues();
	 * cv.put(&quot;name&quot;, &quot;Jim&quot;);
	 * DataSupport.update(Person.class, cv, 1);
	 * </pre>
	 * 
	 * This means that the name of record 1 will be updated into Jim.<br>
	 * 
	 * @param modelClass
	 *            Which table to update by class.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @param id
	 *            Which record to update.
	 * @return The number of rows affected.
	 */
	public static synchronized int update(Class<?> modelClass, ContentValues values, long id) {
		UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
		return updateHandler.onUpdate(modelClass, id, values);
	}

	/**
	 * Updates all records with details given if they match a set of conditions
	 * supplied. This method constructs a single SQL UPDATE statement and sends
	 * it to the database.
	 * 
	 * <pre>
	 * ContentValues cv = new ContentValues();
	 * cv.put(&quot;name&quot;, &quot;Jim&quot;);
	 * DataSupport.update(Person.class, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
	 * </pre>
	 * 
	 * This means that all the records which name is Tom will be updated into
	 * Jim.
	 * 
	 * @param modelClass
	 *            Which table to update by class.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement. First parameter is the WHERE clause to apply when
	 *            updating. The way of specifying place holders is to insert one
	 *            or more question marks in the SQL. The first question mark is
	 *            replaced by the second element of the array, the next question
	 *            mark by the third, and so on. Passing empty string will update
	 *            all rows.
	 * @return The number of rows affected.
	 */
	public static synchronized int updateAll(Class<?> modelClass, ContentValues values,
			String... conditions) {
		return updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.getName())), values, conditions);
	}

	/**
	 * Updates all records with details given if they match a set of conditions
	 * supplied. This method constructs a single SQL UPDATE statement and sends
	 * it to the database.
	 * 
	 * <pre>
	 * ContentValues cv = new ContentValues();
	 * cv.put(&quot;name&quot;, &quot;Jim&quot;);
	 * DataSupport.update(&quot;person&quot;, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
	 * </pre>
	 * 
	 * This means that all the records which name is Tom will be updated into
	 * Jim.
	 * 
	 * @param tableName
	 *            Which table to update.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement. First parameter is the WHERE clause to apply when
	 *            updating. The way of specifying place holders is to insert one
	 *            or more question marks in the SQL. The first question mark is
	 *            replaced by the second element of the array, the next question
	 *            mark by the third, and so on. Passing empty string will update
	 *            all rows.
	 * @return The number of rows affected.
	 */
	public static synchronized int updateAll(String tableName, ContentValues values,
			String... conditions) {
		UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
		return updateHandler.onUpdateAll(tableName, values, conditions);
	}

	/**
	 * Saves the collection into database. <br />
	 * 
	 * <pre>
	 * DataSupport.saveAll(people);
	 * </pre>
	 * 
	 * If the model in collection is a new record gets created in the database,
	 * otherwise the existing record gets updated.<br />
	 * If saving process failed by any accident, the whole action will be
	 * cancelled and your database will be <b>rolled back</b>. <br />
	 * This method acts the same result as the below way, but <b>much more
	 * efficient</b>.
	 * 
	 * <pre>
	 * for (Person person : people) {
	 * 	person.save();
	 * }
	 * </pre>
	 * 
	 * So when your collection holds huge of models,
	 * {@link #saveAll(java.util.Collection)} is the better choice.
	 * 
	 * @param collection
	 *            Holds all models to save.
	 */
	public static synchronized <T extends DataSupport> void saveAll(Collection<T> collection) {
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			SaveHandler saveHandler = new SaveHandler(db);
			saveHandler.onSaveAll(collection);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		} finally {
			db.endTransaction();
		}
	}

    /**
     * Provide a way to mark all models in collection as deleted. This means these models' save
     * state is no longer exist anymore. If save them again, they will be treated as inserting new
     * data instead of updating the exist one.
     * @param collection
     *          Collection of models which want to mark as deleted and clear their save state.
     */
    public static <T extends DataSupport> void markAsDeleted(Collection<T> collection) {
        for (T t : collection) {
            t.clearSavedState();
        }
    }

	/**
	 * Deletes the record in the database. The record must be saved already.<br>
	 * The data in other tables which is referenced with the record will be
	 * removed too.
	 * 
	 * <pre>
	 * Person person;
	 * ....
	 * if (person.isSaved()) {
	 * 		person.delete();
	 * }
	 * </pre>
	 * 
	 * @return The number of rows affected. Including cascade delete rows.
	 */
	public synchronized int delete() {
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			DeleteHandler deleteHandler = new DeleteHandler(db);
			int rowsAffected = deleteHandler.onDelete(this);
			baseObjId = 0;
			db.setTransactionSuccessful();
			return rowsAffected;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Updates the corresponding record by id. Use setXxx to decide which
	 * columns to update.
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Jim&quot;);
	 * person.update(1);
	 * </pre>
	 * 
	 * This means that the name of record 1 will be updated into Jim.<br>
	 * 
	 * <b>Note: </b> 1. If you set a default value to a field, the corresponding
	 * column won't be updated. Use {@link #setToDefault(String)} to update
	 * columns into default value. 2. This method couldn't update foreign key in
	 * database. So do not use setXxx to set associations between models.
	 * 
	 * @param id
	 *            Which record to update.
	 * @return The number of rows affected.
	 */
	public synchronized int update(long id) {
		try {
			UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
			int rowsAffected = updateHandler.onUpdate(this, id);
			getFieldsToSetToDefault().clear();
			return rowsAffected;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Updates all records with details given if they match a set of conditions
	 * supplied. This method constructs a single SQL UPDATE statement and sends
	 * it to the database.
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Jim&quot;);
	 * person.updateAll(&quot;name = ?&quot;, &quot;Tom&quot;);
	 * </pre>
	 * 
	 * This means that all the records which name is Tom will be updated into
	 * Jim.<br>
	 * 
	 * <b>Note: <b> 1. If you set a default value to a field, the corresponding
	 * column won't be updated. Use {@link #setToDefault(String)} to update
	 * columns into default value. 2. This method couldn't update foreign key in
	 * database. So do not use setXxx to set associations between models.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement. First parameter is the WHERE clause to apply when
	 *            updating. The way of specifying place holders is to insert one
	 *            or more question marks in the SQL. The first question mark is
	 *            replaced by the second element of the array, the next question
	 *            mark by the third, and so on. Passing empty string will update
	 *            all rows.
	 * @return The number of rows affected.
	 */
	public synchronized int updateAll(String... conditions) {
		try {
			UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
			int rowsAffected = updateHandler.onUpdateAll(this, conditions);
			getFieldsToSetToDefault().clear();
			return rowsAffected;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

	/**
	 * Saves the model. <br />
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Tom&quot;);
	 * person.setAge(22);
	 * person.save();
	 * </pre>
	 * 
	 * If the model is a new record gets created in the database, otherwise the
	 * existing record gets updated.<br />
	 * If saving process failed by any accident, the whole action will be
	 * cancelled and your database will be <b>rolled back</b>. <br />
	 * If the model has a field named id or _id and field type is int or long,
	 * the id value generated by database will assign to it after the model is
	 * saved.<br />
	 * Note that if the associated models of this model is already saved. The
	 * associations between them will be built automatically in database after
	 * it saved.
	 * 
	 * @return If the model is saved successfully, return true. Any exception
	 *         happens, return false.
	 */
	public synchronized boolean save() {
		try {
			saveThrows();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    /**
	 * Saves the model. <br />
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Tom&quot;);
	 * person.setAge(22);
	 * person.saveThrows();
	 * </pre>
	 * 
	 * If the model is a new record gets created in the database, otherwise the
	 * existing record gets updated.<br />
	 * If saving process failed by any accident, the whole action will be
	 * cancelled and your database will be <b>rolled back</b> and throws
	 * {@link DataSupportException}<br />
	 * If the model has a field named id or _id and field type is int or long,
	 * the id value generated by database will assign to it after the model is
	 * saved.<br />
	 * Note that if the associated models of this model is already saved. The
	 * associations between them will be built automatically in database after
	 * it saved.
	 * 
	 * @throws DataSupportException
	 */
	public synchronized void saveThrows() {
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			SaveHandler saveHandler = new SaveHandler(db);
			saveHandler.onSave(this);
			clearAssociatedData();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		} finally {
			db.endTransaction();
		}
	}

    /**
     * Saves the model ignore associations, so that the saving process will be faster. <br />
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Tom&quot;);
     * person.setAge(22);
     * person.save();
     * </pre>
     *
     * If the model is a new record gets created in the database, otherwise the
     * existing record gets updated.<br />
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be <b>rolled back</b>. <br />
     * If the model has a field named id or _id and field type is int or long,
     * the id value generated by database will assign to it after the model is
     * saved.<br />
     * If your model doesn't has any association, you can use this method to save faster.
     *
     * @return If the model is saved successfully, return true. Any exception
     *         happens, return false.
     */
    public synchronized boolean saveFast() {
        SQLiteDatabase db = Connector.getDatabase();
        db.beginTransaction();
        try {
            SaveHandler saveHandler = new SaveHandler(db);
            saveHandler.onSaveFast(this);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

	/**
	 * Current model is saved or not.
	 * 
	 * @return If saved return true, or return false.
	 */
	public boolean isSaved() {
		return baseObjId > 0;
	}

    /**
     * It model is saved, clear the saved state and model becomes unsaved. Otherwise nothing will happen.
     */
    public void clearSavedState() {
        baseObjId = 0;
    }

	/**
	 * When updating database with {@link org.litepal.crud.DataSupport#update(long)}, you must
	 * use this method to update a field into default value. Use setXxx with
	 * default value of the model won't update anything. <br>
	 * 
	 * @param fieldName
	 *            The name of field to update into default value.
	 */
	public void setToDefault(String fieldName) {
		getFieldsToSetToDefault().add(fieldName);
	}

    /**
     * Assigns value to baseObjId. This will override the original value. <b>Never call this method
     * unless you know exactly what you are doing.</b>
     * @param baseObjId
     */
    public void assignBaseObjId(int baseObjId) {
        this.baseObjId = baseObjId;
    }

	/**
	 * Disable developers to create instance of DataSupport directly. They
	 * should inherit this class with subclasses and operate on them.
	 */
	protected DataSupport() {
	}

	/**
	 * Get the baseObjId of this model if it's useful for developers. It's for
	 * system use usually. Do not try to assign or modify it.
	 * 
	 * @return The base object id.
	 */
	protected long getBaseObjId() {
		return baseObjId;
	}
	
	/**
	 * Get the full class name of self.
	 * 
	 * @return The full class name of self.
	 */
	protected String getClassName() {
		return getClass().getName();
	}

	/**
	 * Get the corresponding table name of current model.
	 * 
	 * @return The corresponding table name of current model.
	 */
	protected String getTableName() {
		return BaseUtility.changeCase(DBUtility.getTableNameByClassName(getClassName()));
	}

	/**
	 * Get the list which holds all field names to update them into default
	 * value of model in database.
	 * 
	 * @return List holds all the field names which need to be updated into
	 *         default value of model.
	 */
	List<String> getFieldsToSetToDefault() {
		if (fieldsToSetToDefault == null) {
			fieldsToSetToDefault = new ArrayList<String>();
		}
		return fieldsToSetToDefault;
	}

	/**
	 * Add the id of an associated model into self model's associatedIdsWithFK
	 * map. The associated model has a foreign key column in the corresponding
	 * table.
	 * 
	 * @param associatedTableName
	 *            The table name of associated model.
	 * @param associatedId
	 *            The {@link #baseObjId} of associated model after it is saved.
	 */
	void addAssociatedModelWithFK(String associatedTableName, long associatedId) {
		Set<Long> associatedIdsWithFKSet = getAssociatedModelsMapWithFK().get(associatedTableName);
		if (associatedIdsWithFKSet == null) {
			associatedIdsWithFKSet = new HashSet<Long>();
			associatedIdsWithFKSet.add(associatedId);
			associatedModelsMapWithFK.put(associatedTableName, associatedIdsWithFKSet);
		} else {
			associatedIdsWithFKSet.add(associatedId);
		}
	}

	/**
	 * Get the associated model's map of self model. It can be used for
	 * associations actions of CRUD. The key is the name of associated model.
	 * The value is a List of id of associated models.
	 * 
	 * @return An associated model's map to update all the foreign key columns
	 *         of associated models' table with self model's id.
	 */
	Map<String, Set<Long>> getAssociatedModelsMapWithFK() {
		if (associatedModelsMapWithFK == null) {
			associatedModelsMapWithFK = new HashMap<String, Set<Long>>();
		}
		return associatedModelsMapWithFK;
	}

	/**
	 * Add the id of an associated model into self model's associatedIdsM2M map.
	 * 
	 * @param associatedModelName
	 *            The name of associated model.
	 * @param associatedId
	 *            The id of associated model.
	 */
	void addAssociatedModelForJoinTable(String associatedModelName, long associatedId) {
		Set<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(
				associatedModelName);
		if (associatedIdsM2MSet == null) {
			associatedIdsM2MSet = new HashSet<Long>();
			associatedIdsM2MSet.add(associatedId);
			associatedModelsMapForJoinTable.put(associatedModelName, associatedIdsM2MSet);
		} else {
			associatedIdsM2MSet.add(associatedId);
		}
	}

	/**
	 * Add an empty Set into {@link #associatedModelsMapForJoinTable} with
	 * associated model name as key. Might be useful when comes to update
	 * intermediate join table.
	 * 
	 * @param associatedModelName
	 *            The name of associated model.
	 */
	void addEmptyModelForJoinTable(String associatedModelName) {
		Set<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(
				associatedModelName);
		if (associatedIdsM2MSet == null) {
			associatedIdsM2MSet = new HashSet<Long>();
			associatedModelsMapForJoinTable.put(associatedModelName, associatedIdsM2MSet);
		}
	}

	/**
	 * Get the associated model's map for intermediate join table. It is used to
	 * save values into intermediate join table. The key is the name of
	 * associated model. The value is the id of associated model.
	 * 
	 * @return An associated model's map to save values into intermediate join
	 *         table
	 */
	Map<String, Set<Long>> getAssociatedModelsMapForJoinTable() {
		if (associatedModelsMapForJoinTable == null) {
			associatedModelsMapForJoinTable = new HashMap<String, Set<Long>>();
		}
		return associatedModelsMapForJoinTable;
	}

	/**
	 * Add the id of an associated model into self model's association
	 * collection. The associated model doesn't have a foreign key column in the
	 * corresponding table. Instead self model has a foreign key column in the
	 * corresponding table.
	 * 
	 * @param associatedTableName
	 *            The simple class name of associated model.
	 * @param associatedId
	 *            The {@link #baseObjId} of associated model after it is saved.
	 */
	void addAssociatedModelWithoutFK(String associatedTableName, long associatedId) {
		getAssociatedModelsMapWithoutFK().put(associatedTableName, associatedId);
	}

	/**
	 * Get the associated model's map of self model. It can be used for
	 * associations actions of CRUD. The key is the name of associated model's
	 * table. The value is the id of associated model.
	 * 
	 * @return An associated model's map to save self model with foreign key.
	 */
	Map<String, Long> getAssociatedModelsMapWithoutFK() {
		if (associatedModelsMapWithoutFK == null) {
			associatedModelsMapWithoutFK = new HashMap<String, Long>();
		}
		return associatedModelsMapWithoutFK;
	}

	/**
	 * Add a foreign key name into the clear list.
	 * 
	 * @param foreignKeyName
	 *            The name of foreign key.
	 */
	void addFKNameToClearSelf(String foreignKeyName) {
		List<String> list = getListToClearSelfFK();
		if (!list.contains(foreignKeyName)) {
			list.add(foreignKeyName);
		}
	}

	/**
	 * Get the foreign key name list to clear foreign key value in current
	 * model's table.
	 * 
	 * @return The list of foreign key names to clear in current model's table.
	 */
	List<String> getListToClearSelfFK() {
		if (listToClearSelfFK == null) {
			listToClearSelfFK = new ArrayList<String>();
		}
		return listToClearSelfFK;
	}

	/**
	 * Add an associated table name into the list to clear.
	 * 
	 * @param associatedTableName
	 *            The name of associated table.
	 */
	void addAssociatedTableNameToClearFK(String associatedTableName) {
		List<String> list = getListToClearAssociatedFK();
		if (!list.contains(associatedTableName)) {
			list.add(associatedTableName);
		}
	}

	/**
	 * Get the associated table names list which need to clear their foreign key
	 * values.
	 * 
	 * @return The list with associated table names to clear foreign key values.
	 */
	List<String> getListToClearAssociatedFK() {
		if (listToClearAssociatedFK == null) {
			listToClearAssociatedFK = new ArrayList<String>();
		}
		return listToClearAssociatedFK;
	}

	/**
	 * Clear all the data for storing associated models' data.
	 */
	void clearAssociatedData() {
		clearIdOfModelWithFK();
		clearIdOfModelWithoutFK();
		clearIdOfModelForJoinTable();
		clearFKNameList();
	}

	/**
	 * Clear all the data in {@link #associatedModelsMapWithFK}.
	 */
	private void clearIdOfModelWithFK() {
		for (String associatedModelName : getAssociatedModelsMapWithFK().keySet()) {
			associatedModelsMapWithFK.get(associatedModelName).clear();
		}
		associatedModelsMapWithFK.clear();
	}

	/**
	 * Clear all the data in {@link #associatedModelsMapWithoutFK}.
	 */
	private void clearIdOfModelWithoutFK() {
		getAssociatedModelsMapWithoutFK().clear();
	}

	/**
	 * Clear all the data in {@link #associatedModelsMapForJoinTable}.
	 */
	private void clearIdOfModelForJoinTable() {
		for (String associatedModelName : getAssociatedModelsMapForJoinTable().keySet()) {
			associatedModelsMapForJoinTable.get(associatedModelName).clear();
		}
		associatedModelsMapForJoinTable.clear();
	}

	/**
	 * Clear all the data in {@link #listToClearSelfFK}.
	 */
	private void clearFKNameList() {
		getListToClearSelfFK().clear();
		getListToClearAssociatedFK().clear();
	}

}