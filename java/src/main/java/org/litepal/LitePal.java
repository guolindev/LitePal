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

package org.litepal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.crud.LitePalSupport;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.tablemanager.callback.DatabaseListener;

import java.util.Collection;
import java.util.List;

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling {@link #initialize(Context)} method to make LitePal ready to
 * work. Also you can switch the using database by calling {@link #use(LitePalDB)} and {@link #useDefault()}
 * methods.
 *
 * @author Tony Green
 * @since 1.4
 */
public class LitePal {

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * 		Application context.
     */
    public static void initialize(Context context) {
        Operator.initialize(context);
    }

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    public static SQLiteDatabase getDatabase() {
        return Operator.getDatabase();
    }

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     *          The database to switch to.
     */
    public static void use(LitePalDB litePalDB) {
        Operator.use(litePalDB);
    }

    /**
     * Switch the using database to default with configuration by litepal.xml.
     */
    public static void useDefault() {
        Operator.useDefault();
    }

    /**
     * Delete the specified database.
     * @param dbName
     *          Name of database to delete.
     * @return True if delete success, false otherwise.
     */
    public static boolean deleteDatabase(String dbName) {
        return Operator.deleteDatabase(dbName);
    }

    public static void aesKey(String key) {
        Operator.aesKey(key);
    }

    /**
     * Declaring to query which columns in table.
     *
     * <pre>
     * LitePal.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
     * </pre>
     *
     * This will find all rows with name and age columns in Person table.
     *
     * @param columns
     *            A String array of which columns to return. Passing null will
     *            return all columns.
     *
     * @return A FluentQuery instance.
     */
    public static FluentQuery select(String... columns) {
        return Operator.select(columns);
    }

    /**
     * Declaring to query which rows in table.
     *
     * <pre>
     * LitePal.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
     * </pre>
     *
     * This will find rows which name is Tom or age greater than 14 in Person
     * table.
     *
     * @param conditions
     *            A filter declaring which rows to return, formatted as an SQL
     *            WHERE clause. Passing null will return all rows.
     * @return A FluentQuery instance.
     */
    public static FluentQuery where(String... conditions) {
        return Operator.where(conditions);
    }

    /**
     * Declaring how to order the rows queried from table.
     *
     * <pre>
     * LitePal.order(&quot;name desc&quot;).find(Person.class);
     * </pre>
     *
     * This will find all rows in Person table sorted by name with inverted
     * order.
     *
     * @param column
     *            How to order the rows, formatted as an SQL ORDER BY clause.
     *            Passing null will use the default sort order, which may be
     *            unordered.
     * @return A FluentQuery instance.
     */
    public static FluentQuery order(String column) {
        return Operator.order(column);
    }

    /**
     * Limits the number of rows returned by the query.
     *
     * <pre>
     * LitePal.limit(2).find(Person.class);
     * </pre>
     *
     * This will find the top 2 rows in Person table.
     *
     * @param value
     *            Limits the number of rows returned by the query, formatted as
     *            LIMIT clause.
     * @return A FluentQuery instance.
     */
    public static FluentQuery limit(int value) {
        return Operator.limit(value);
    }

    /**
     * Declaring the offset of rows returned by the query. This method must be
     * used with {@link #limit(int)}, or nothing will return.
     *
     * <pre>
     * LitePal.limit(1).offset(2).find(Person.class);
     * </pre>
     *
     * This will find the third row in Person table.
     *
     * @param value
     *            The offset amount of rows returned by the query.
     * @return A FluentQuery instance.
     */
    public static FluentQuery offset(int value) {
        return Operator.offset(value);
    }

    /**
     * Count the records.
     *
     * <pre>
     * LitePal.count(Person.class);
     * </pre>
     *
     * This will count all rows in person table.<br>
     * You can also specify a where clause when counting.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
     * </pre>
     *
     * @param modelClass
     *            Which table to query from by class.
     * @return Count of the specified table.
     */
    public static int count(Class<?> modelClass) {
        return Operator.count(modelClass);
    }

    /**
     * Basically same as {@link #count(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *          Which table to query from by class.
     * @return A CountExecutor instance.
     */
    public static CountExecutor countAsync(final Class<?> modelClass) {
        return Operator.countAsync(modelClass);
    }

    /**
     * Count the records.
     *
     * <pre>
     * LitePal.count(&quot;person&quot;);
     * </pre>
     *
     * This will count all rows in person table.<br>
     * You can also specify a where clause when counting.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
     * </pre>
     *
     * @param tableName
     *            Which table to query from.
     * @return Count of the specified table.
     */
    public static int count(String tableName) {
        return Operator.count(tableName);
    }

    /**
     * Basically same as {@link #count(String)} but pending to a new thread for executing.
     *
     * @param tableName
     *          Which table to query from.
     * @return A CountExecutor instance.
     */
    public static CountExecutor countAsync(final String tableName) {
        return Operator.countAsync(tableName);
    }

    /**
     * Calculates the average value on a given column.
     *
     * <pre>
     * LitePal.average(Person.class, &quot;age&quot;);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
     * </pre>
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param column
     *            The based on column to calculate.
     * @return The average value on a given column.
     */
    public static double average(Class<?> modelClass, String column) {
        return Operator.average(modelClass, column);
    }

    /**
     * Basically same as {@link #average(Class, String)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param column
     *            The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    public static AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
        return Operator.averageAsync(modelClass, column);
    }

    /**
     * Calculates the average value on a given column.
     *
     * <pre>
     * LitePal.average(&quot;person&quot;, &quot;age&quot;);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
     * </pre>
     *
     * @param tableName
     *            Which table to query from.
     * @param column
     *            The based on column to calculate.
     * @return The average value on a given column.
     */
    public static double average(String tableName, String column) {
        return Operator.average(tableName, column);
    }

    /**
     * Basically same as {@link #average(String, String)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param column
     *            The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    public static AverageExecutor averageAsync(final String tableName, final String column) {
        return Operator.averageAsync(tableName, column);
    }

    /**
     * Calculates the maximum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.max(Person.class, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        return Operator.max(modelClass, columnName, columnType);
    }

    /**
     * Basically same as {@link #max(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return Operator.maxAsync(modelClass, columnName, columnType);
    }

    /**
     * Calculates the maximum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.max(&quot;person&quot;, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T max(String tableName, String columnName, Class<T> columnType) {
        return Operator.max(tableName, columnName, columnType);
    }

    /**
     * Basically same as {@link #max(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        return Operator.maxAsync(tableName, columnName, columnType);
    }

    /**
     * Calculates the minimum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.min(Person.class, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        return Operator.min(modelClass, columnName, columnType);
    }

    /**
     * Basically same as {@link #min(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return Operator.minAsync(modelClass, columnName, columnType);
    }

    /**
     * Calculates the minimum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.min(&quot;person&quot;, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T min(String tableName, String columnName, Class<T> columnType) {
        return Operator.min(tableName, columnName, columnType);
    }

    /**
     * Basically same as {@link #min(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        return Operator.minAsync(tableName, columnName, columnType);
    }

    /**
     * Calculates the sum of values on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.sum(Person.class, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        return Operator.sum(modelClass, columnName, columnType);
    }

    /**
     * Basically same as {@link #sum(Class, String, Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query from by class.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return Operator.sumAsync(modelClass, columnName, columnType);
    }

    /**
     * Calculates the sum of values on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.sum(&quot;person&quot;, &quot;age&quot;, int.class);
     * </pre>
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
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
    public static <T> T sum(String tableName, String columnName, Class<T> columnType) {
        return Operator.sum(tableName, columnName, columnType);
    }

    /**
     * Basically same as {@link #sum(String, String, Class)} but pending to a new thread for executing.
     *
     * @param tableName
     *            Which table to query from.
     * @param columnName
     *            The based on column to calculate.
     * @param columnType
     *            The type of the based on column.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        return Operator.sumAsync(tableName, columnName, columnType);
    }

    /**
     * Finds the record by a specific id.
     *
     * <pre>
     * Person p = LitePal.find(Person.class, 1);
     * </pre>
     *
     * The modelClass determines which table to query and the object type to
     * return. If no record can be found, then return null. <br>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link LitePal#find(Class, long, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @return An object with found data from database, or null.
     */
    public static <T> T find(Class<T> modelClass, long id) {
        return Operator.find(modelClass, id);
    }

    /**
     * Basically same as {@link #find(Class, long)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findAsync(Class<T> modelClass, long id) {
        return Operator.findAsync(modelClass, id);
    }

    /**
     * It is mostly same as {@link LitePal#find(Class, long)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with found data from database, or null.
     */
    public static <T> T find(Class<T> modelClass, long id, boolean isEager) {
        return Operator.find(modelClass, id, isEager);
    }

    /**
     * Basically same as {@link #find(Class, long, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findAsync(final Class<T> modelClass, final long id, final boolean isEager) {
        return Operator.findAsync(modelClass, id, isEager);
    }

    /**
     * Finds the first record of a single table.
     *
     * <pre>
     * Person p = LitePal.findFirst(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link LitePal#findFirst(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with data of first row, or null.
     */
    public static <T> T findFirst(Class<T> modelClass) {
        return Operator.findFirst(modelClass);
    }

    /**
     * Basically same as {@link #findFirst(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findFirstAsync(Class<T> modelClass) {
        return Operator.findFirstAsync(modelClass);
    }

    /**
     * It is mostly same as {@link LitePal#findFirst(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with data of first row, or null.
     */
    public static <T> T findFirst(Class<T> modelClass, boolean isEager) {
        return Operator.findFirst(modelClass, isEager);
    }

    /**
     * Basically same as {@link #findFirst(Class, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        return Operator.findFirstAsync(modelClass, isEager);
    }

    /**
     * Finds the last record of a single table.
     *
     * <pre>
     * Person p = LitePal.findLast(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link LitePal#findLast(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with data of last row, or null.
     */
    public static <T> T findLast(Class<T> modelClass) {
        return Operator.findLast(modelClass);
    }

    /**
     * Basically same as {@link #findLast(Class)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findLastAsync(Class<T> modelClass) {
        return Operator.findLastAsync(modelClass);
    }

    /**
     * It is mostly same as {@link LitePal#findLast(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with data of last row, or null.
     */
    public static <T> T findLast(Class<T> modelClass, boolean isEager) {
        return Operator.findLast(modelClass, isEager);
    }

    /**
     * Basically same as {@link #findLast(Class, boolean)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    public static <T> FindExecutor<T> findLastAsync(final Class<T> modelClass, final boolean isEager) {
        return Operator.findLastAsync(modelClass, isEager);
    }

    /**
     * Finds multiple records by an id array.
     *
     * <pre>
     * List&lt;Person&gt; people = LitePal.findAll(Person.class, 1, 2, 3);
     *
     * long[] bookIds = { 10, 18 };
     * List&lt;Book&gt; books = LitePal.findAll(Book.class, bookIds);
     * </pre>
     *
     * Of course you can find all records by passing nothing to the ids
     * parameter.
     *
     * <pre>
     * List&lt;Book&gt; allBooks = LitePal.findAll(Book.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link LitePal#findAll(Class, boolean, long...)}.
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
    public static <T> List<T> findAll(Class<T> modelClass, long... ids) {
        return Operator.findAll(modelClass, ids);
    }

    /**
     * Basically same as {@link #findAll(Class, long...)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param ids
     *            Which records to query. Or do not pass it to find all records.
     * @return A FindMultiExecutor instance.
     */
    public static <T> FindMultiExecutor<T> findAllAsync(Class<T> modelClass, long... ids) {
        return Operator.findAllAsync(modelClass, ids);
    }

    /**
     * It is mostly same as {@link LitePal#findAll(Class, long...)} but an
     * isEager parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param isEager
     *            True to load the associated models, false not.
     * @param ids
     *            Which records to query. Or do not pass it to find all records.
     * @return An object list with found data from database, or an empty list.
     */
    public static <T> List<T> findAll(Class<T> modelClass, boolean isEager, long... ids) {
        return Operator.findAll(modelClass, isEager, ids);
    }

    /**
     * Basically same as {@link #findAll(Class, boolean, long...)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param isEager
     *            True to load the associated models, false not.
     * @param ids
     *            Which records to query. Or do not pass it to find all records.
     * @return A FindMultiExecutor instance.
     */
    public static <T> FindMultiExecutor<T> findAllAsync(final Class<T> modelClass, final boolean isEager, final long... ids) {
        return Operator.findAllAsync(modelClass, isEager, ids);
    }

    /**
     * Runs the provided SQL and returns a Cursor over the result set. You may
     * include ? in where clause in the query, which will be replaced by the
     * second to the last parameters, such as:
     *
     * <pre>
     * Cursor cursor = LitePal.findBySQL(&quot;select * from person where name=? and age=?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     * </pre>
     *
     * @param sql
     *            First parameter is the SQL clause to apply. Second to the last
     *            parameters will replace the place holders.
     * @return A Cursor object, which is positioned before the first entry. Note
     *         that Cursors are not synchronized, see the documentation for more
     *         details.
     */
    public static Cursor findBySQL(String... sql) {
        return Operator.findBySQL(sql);
    }

    /**
     * Deletes the record in the database by id.<br>
     * The data in other tables which is referenced with the record will be
     * removed too.
     *
     * <pre>
     * LitePal.delete(Person.class, 1);
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
    public static int delete(Class<?> modelClass, long id) {
        return Operator.delete(modelClass, id);
    }

    /**
     * Basically same as {@link #delete(Class, long)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to delete from by class.
     * @param id
     *            Which record to delete.
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor deleteAsync(final Class<?> modelClass, final long id) {
        return Operator.deleteAsync(modelClass, id);
    }

    /**
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * <pre>
     * LitePal.deleteAll(Person.class, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
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
    public static int deleteAll(Class<?> modelClass, String... conditions) {
        return Operator.deleteAll(modelClass, conditions);
    }

    /**
     * Basically same as {@link #deleteAll(Class, String...)} but pending to a new thread for executing.
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
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor deleteAllAsync(final Class<?> modelClass, final String... conditions) {
        return Operator.deleteAllAsync(modelClass, conditions);
    }

    /**
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * <pre>
     * LitePal.deleteAll(&quot;person&quot;, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
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
    public static int deleteAll(String tableName, String... conditions) {
        return Operator.deleteAll(tableName, conditions);
    }

    /**
     * Basically same as {@link #deleteAll(String, String...)} but pending to a new thread for executing.
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
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor deleteAllAsync(final String tableName, final String... conditions) {
        return Operator.deleteAllAsync(tableName, conditions);
    }

    /**
     * Updates the corresponding record by id with ContentValues. Returns the
     * number of affected rows.
     *
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, 1);
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
    public static int update(Class<?> modelClass, ContentValues values, long id) {
        return Operator.update(modelClass, values, id);
    }

    /**
     * Basically same as {@link #update(Class, ContentValues, long)} but pending to a new thread for executing.
     *
     * @param modelClass
     *            Which table to update by class.
     * @param values
     *            A map from column names to new column values. null is a valid
     *            value that will be translated to NULL.
     * @param id
     *            Which record to update.
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor updateAsync(final Class<?> modelClass, final ContentValues values, final long id) {
        return Operator.updateAsync(modelClass, values, id);
    }

    /**
     * Updates all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL UPDATE statement and sends
     * it to the database.
     *
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
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
    public static int updateAll(Class<?> modelClass, ContentValues values, String... conditions) {
        return Operator.updateAll(modelClass, values, conditions);
    }

    /**
     * Basically same as {@link #updateAll(Class, ContentValues, String...)} but pending to a new thread for executing.
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
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor updateAllAsync(Class<?> modelClass, ContentValues values, String... conditions) {
        return Operator.updateAllAsync(modelClass, values, conditions);
    }

    /**
     * Updates all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL UPDATE statement and sends
     * it to the database.
     *
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(&quot;person&quot;, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
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
    public static int updateAll(String tableName, ContentValues values, String... conditions) {
        return Operator.updateAll(tableName, values, conditions);
    }

    /**
     * Basically same as {@link #updateAll(String, ContentValues, String...)} but pending to a new thread for executing.
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
     * @return A UpdateOrDeleteExecutor instance.
     */
    public static UpdateOrDeleteExecutor updateAllAsync(final String tableName, final ContentValues values, final String... conditions) {
        return Operator.updateAllAsync(tableName, values, conditions);
    }

    /**
     * Saves the collection into database. <br>
     *
     * <pre>
     * LitePal.saveAll(people);
     * </pre>
     *
     * If the model in collection is a new record gets created in the database,
     * otherwise the existing record gets updated.<br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be <b>rolled back</b>. <br>
     * This method acts the same result as the below way, but <b>much more
     * efficient</b>.
     *
     * <pre>
     * for (Person person : people) {
     * 	person.save();
     * }
     * </pre>
     *
     * So when your collection holds huge of models, saveAll(Collection) is the better choice.
     *
     * @param collection
     *            Holds all models to save.
     */
    public static <T extends LitePalSupport> void saveAll(Collection<T> collection) {
        Operator.saveAll(collection);
    }

    /**
     * Basically same as {@link #saveAll(Collection)} but pending to a new thread for executing.
     *
     * @param collection
     *            Holds all models to save.
     * @return A SaveExecutor instance.
     */
    public static <T extends LitePalSupport> SaveExecutor saveAllAsync(final Collection<T> collection) {
        return Operator.saveAllAsync(collection);
    }

    /**
     * Provide a way to mark all models in collection as deleted. This means these models' save
     * state is no longer exist anymore. If save them again, they will be treated as inserting new
     * data instead of updating the exist one.
     * @param collection
     *          Collection of models which want to mark as deleted and clear their save state.
     */
    public static <T extends LitePalSupport> void markAsDeleted(Collection<T> collection) {
        Operator.markAsDeleted(collection);
    }

    /**
     * Check if the specified conditions data already exists in the table.
     * @param modelClass
     *          Which table to check by class.
     * @param conditions
     *          A filter declaring which data to check. Exactly same use as
     *          {@link LitePal#where(String...)}, except null conditions will result in false.
     * @return Return true if the specified conditions data already exists in the table.
     *         False otherwise. Null conditions will result in false.
     */
    public static <T> boolean isExist(Class<T> modelClass, String... conditions) {
        return Operator.isExist(modelClass, conditions);
    }

    /**
     * Register a listener to listen database create and upgrade events.
     */
    public static void registerDatabaseListener(DatabaseListener listener) {
        Operator.registerDatabaseListener(listener);
    }

}