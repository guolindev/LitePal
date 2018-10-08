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

package org.litepal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import org.litepal.crud.*
import org.litepal.crud.async.*
import org.litepal.exceptions.LitePalSupportException
import org.litepal.parser.LitePalAttr
import org.litepal.parser.LitePalParser
import org.litepal.tablemanager.Connector
import org.litepal.tablemanager.callback.DatabaseListener
import org.litepal.util.BaseUtility
import org.litepal.util.Const
import org.litepal.util.DBUtility
import org.litepal.util.SharedUtil
import org.litepal.util.cipher.CipherUtil
import java.io.File
import kotlin.math.tan

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling {@link #initialize(Context)} method to make LitePal ready to
 * work. Also you can switch the using database by calling {@link #use(LitePalDB)} and {@link #useDefault()}
 * methods.
 *
 * @author Tony Green
 * @since 2.0
 */
object LitePal {

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * Application context.
     */
    @JvmStatic
    fun initialize(context: Context) {
        Operator.initialize(context)
    }

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    @JvmStatic
    fun getDatabase(): SQLiteDatabase = Operator.getDatabase()

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     * The database to switch to.
     */
    @JvmStatic
    fun use(litePalDB: LitePalDB) {
        Operator.use(litePalDB)
    }


    /**
     * Switch the using database to default with configuration by litepal.xml.
     */
    @JvmStatic
    fun useDefault() {
        Operator.useDefault()
    }

    /**
     * Delete the specified database.
     * @param dbName
     * Name of database to delete.
     * @return True if delete success, false otherwise.
     */
    @JvmStatic
    fun deleteDatabase(dbName: String) = Operator.deleteDatabase(dbName)

    @JvmStatic
    fun aesKey(key: String) {
        Operator.aesKey(key)
    }

    /**
     * Declaring to query which columns in table.
     *
     * LitePal.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
     *
     * This will find all rows with name and age columns in Person table.
     *
     * @param columns
     * A String array of which columns to return. Passing null will
     * return all columns.
     *
     * @return A FluentQuery instance.
     */
    @JvmStatic
    fun select(vararg columns: String?) = Operator.select(*columns)

    /**
     * Declaring to query which rows in table.
     *
     * LitePal.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
     *
     * This will find rows which name is Tom or age greater than 14 in Person
     * table.
     *
     * @param conditions
     * A filter declaring which rows to return, formatted as an SQL
     * WHERE clause. Passing null will return all rows.
     * @return A FluentQuery instance.
     */
    @JvmStatic
    fun where(vararg conditions: String?) = Operator.where(*conditions)

    /**
     * Declaring how to order the rows queried from table.
     *
     * LitePal.order(&quot;name desc&quot;).find(Person.class);
     *
     * This will find all rows in Person table sorted by name with inverted
     * order.
     *
     * @param column
     * How to order the rows, formatted as an SQL ORDER BY clause.
     * Passing null will use the default sort order, which may be
     * unordered.
     * @return A FluentQuery instance.
     */
    @JvmStatic
    fun order(column: String?) = Operator.order(column)

    /**
     * Limits the number of rows returned by the query.
     *
     * LitePal.limit(2).find(Person.class);
     *
     * This will find the top 2 rows in Person table.
     *
     * @param value
     * Limits the number of rows returned by the query, formatted as
     * LIMIT clause.
     * @return A FluentQuery instance.
     */
    @JvmStatic
    fun limit(value: Int) = Operator.limit(value)

    /**
     * Declaring the offset of rows returned by the query. This method must be
     * used with [LitePal.limit], or nothing will return.
     *
     * LitePal.limit(1).offset(2).find(Person.class);
     *
     * This will find the third row in Person table.
     *
     * @param value
     * The offset amount of rows returned by the query.
     * @return A FluentQuery instance.
     */
    @JvmStatic
    fun offset(value: Int) = Operator.offset(value)

    /**
     * Count the records.
     *
     * LitePal.count(Person.class);
     *
     * This will count all rows in person table.
     *
     * You can also specify a where clause when counting.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
     *
     * @param modelClass
     * Which table to query from by class.
     * @return Count of the specified table.
     */
    @JvmStatic
    fun count(modelClass: Class<*>) = Operator.count(modelClass)

    /**
     * Basically same as [LitePal.count] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @return A CountExecutor instance.
     */
    @JvmStatic
    fun countAsync(modelClass: Class<*>) = Operator.countAsync(modelClass)

    /**
     * Count the records.
     *
     * LitePal.count(&quot;person&quot;);
     *
     * This will count all rows in person table.
     *
     * You can also specify a where clause when counting.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
     *
     * @param tableName
     * Which table to query from.
     * @return Count of the specified table.
     */
    @JvmStatic
    fun count(tableName: String) = Operator.count(tableName)

    /**
     * Basically same as [LitePal.count] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @return A CountExecutor instance.
     */
    @JvmStatic
    fun countAsync(tableName: String) = Operator.countAsync(tableName)

    /**
     * Calculates the average value on a given column.
     *
     * LitePal.average(Person.class, &quot;age&quot;);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
     *
     * @param modelClass
     * Which table to query from by class.
     * @param column
     * The based on column to calculate.
     * @return The average value on a given column.
     */
    @JvmStatic
    fun average(modelClass: Class<*>, column: String) = Operator.average(modelClass, column)

    /**
     * Basically same as [LitePal.average] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @param column
     * The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    @JvmStatic
    fun averageAsync(modelClass: Class<*>, column: String) = Operator.averageAsync(modelClass, column)

    /**
     * Calculates the average value on a given column.
     *
     * LitePal.average(&quot;person&quot;, &quot;age&quot;);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
     *
     * @param tableName
     * Which table to query from.
     * @param column
     * The based on column to calculate.
     * @return The average value on a given column.
     */
    @JvmStatic
    fun average(tableName: String, column: String) = Operator.average(tableName, column)

    /**
     * Basically same as [LitePal.average] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @param column
     * The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    @JvmStatic
    fun averageAsync(tableName: String, column: String) = Operator.averageAsync(tableName, column)

    /**
     * Calculates the maximum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.max(Person.class, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The maximum value on a given column.
     */
    @JvmStatic
    fun <T> max(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.max(modelClass, columnName, columnType)

    /**
     * Basically same as [LitePal.max] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> maxAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.maxAsync(modelClass, columnName, columnType)

    /**
     * Calculates the maximum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.max(&quot;person&quot;, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The maximum value on a given column.
     */
    @JvmStatic
    fun <T> max(tableName: String, columnName: String, columnType: Class<T>) = Operator.max(tableName, columnName, columnType)

    /**
     * Basically same as [LitePal.max] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> maxAsync(tableName: String, columnName: String, columnType: Class<T>) = Operator.maxAsync(tableName, columnName, columnType)

    /**
     * Calculates the minimum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.min(Person.class, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The minimum value on a given column.
     */
    @JvmStatic
    fun <T> min(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.min(modelClass, columnName, columnType)

    /**
     * Basically same as [LitePal.min] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> minAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.minAsync(modelClass, columnName, columnType)

    /**
     * Calculates the minimum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.min(&quot;person&quot;, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The minimum value on a given column.
     */
    @JvmStatic
    fun <T> min(tableName: String, columnName: String, columnType: Class<T>) = Operator.min(tableName, columnName, columnType)

    /**
     * Basically same as [LitePal.min] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> minAsync(tableName: String, columnName: String, columnType: Class<T>) = Operator.minAsync(tableName, columnName, columnType)

    /**
     * Calculates the sum of values on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.sum(Person.class, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The sum value on a given column.
     */
    @JvmStatic
    fun <T> sum(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.sum(modelClass, columnName, columnType)

    /**
     * Basically same as [LitePal.sum] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> sumAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = Operator.sumAsync(modelClass, columnName, columnType)

    /**
     * Calculates the sum of values on a given column. The value is returned
     * with the same data type of the column.
     *
     * LitePal.sum(&quot;person&quot;, &quot;age&quot;, int.class);
     *
     * You can also specify a where clause when calculating.
     *
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The sum value on a given column.
     */
    @JvmStatic
    fun <T> sum(tableName: String, columnName: String, columnType: Class<T>) = Operator.sum(tableName, columnName, columnType)

    /**
     * Basically same as [LitePal.sum] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> sumAsync(tableName: String, columnName: String, columnType: Class<T>) = Operator.sumAsync(tableName, columnName, columnType)

    /**
     * Finds the record by a specific id.
     *
     * Person p = LitePal.find(Person.class, 1);
     *
     * The modelClass determines which table to query and the object type to
     * return. If no record can be found, then return null.
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [LitePal.find].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @return An object with found data from database, or null.
     */
    @JvmStatic
    fun <T> find(modelClass: Class<T>, id: Long) = Operator.find(modelClass, id)

    /**
     * Basically same as [LitePal.find] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findAsync(modelClass: Class<T>, id: Long) = Operator.findAsync(modelClass, id)

    /**
     * It is mostly same as [LitePal.find] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     *
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with found data from database, or null.
     */
    @JvmStatic
    fun <T> find(modelClass: Class<T>, id: Long, isEager: Boolean) = Operator.find(modelClass, id, isEager)

    /**
     * Basically same as [LitePal.find] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @param isEager
     * True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findAsync(modelClass: Class<T>, id: Long, isEager: Boolean) = Operator.findAsync(modelClass, id, isEager)

    /**
     * Finds the first record of a single table.
     *
     * Person p = LitePal.findFirst(Person.class);
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [LitePal.findFirst].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with data of first row, or null.
     */
    @JvmStatic
    fun <T> findFirst(modelClass: Class<T>) = Operator.findFirst(modelClass)

    /**
     * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findFirstAsync(modelClass: Class<T>) = Operator.findFirstAsync(modelClass)

    /**
     * It is mostly same as [LitePal.findFirst] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     *
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with data of first row, or null.
     */
    @JvmStatic
    fun <T> findFirst(modelClass: Class<T>, isEager: Boolean) = Operator.findFirst(modelClass, isEager)

    /**
     * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findFirstAsync(modelClass: Class<T>, isEager: Boolean) = Operator.findFirstAsync(modelClass, isEager)

    /**
     * Finds the last record of a single table.
     *
     * Person p = LitePal.findLast(Person.class);
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [LitePal.findLast].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with data of last row, or null.
     */
    @JvmStatic
    fun <T> findLast(modelClass: Class<T>) = Operator.findLast(modelClass)

    /**
     * Basically same as [LitePal.findLast] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findLastAsync(modelClass: Class<T>) = Operator.findLastAsync(modelClass)

    /**
     * It is mostly same as [LitePal.findLast] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     *
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with data of last row, or null.
     */
    @JvmStatic
    fun <T> findLast(modelClass: Class<T>, isEager: Boolean) = Operator.findLast(modelClass, isEager)

    /**
     * Basically same as [LitePal.findLast] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    @JvmStatic
    fun <T> findLastAsync(modelClass: Class<T>, isEager: Boolean) = Operator.findLastAsync(modelClass, isEager)

    /**
     * Finds multiple records by an id array.
     *
     * List&lt;Person&gt; people = LitePal.findAll(Person.class, 1, 2, 3);
     *
     * long[] bookIds = { 10, 18 };
     * List&lt;Book&gt; books = LitePal.findAll(Book.class, bookIds);
     *
     * Of course you can find all records by passing nothing to the ids
     * parameter.
     *
     * List&lt;Book&gt; allBooks = LitePal.findAll(Book.class);
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [LitePal.findAll].
     *
     * The modelClass determines which table to query and the object type to
     * return.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param ids
     * Which records to query. Or do not pass it to find all records.
     * @return An object list with found data from database, or an empty list.
     */
    @JvmStatic
    fun <T> findAll(modelClass: Class<T>, vararg ids: Long) = Operator.findAll(modelClass, *ids)

    /**
     * Basically same as [LitePal.findAll] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param ids
     * Which records to query. Or do not pass it to find all records.
     * @return A FindMultiExecutor instance.
     */
    @JvmStatic
    fun <T> findAllAsync(modelClass: Class<T>, vararg ids: Long) = Operator.findAllAsync(modelClass, *ids)

    /**
     * It is mostly same as [LitePal.findAll] but an
     * isEager parameter. If set true the associated models will be loaded as well.
     *
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param isEager
     * True to load the associated models, false not.
     * @param ids
     * Which records to query. Or do not pass it to find all records.
     * @return An object list with found data from database, or an empty list.
     */
    @JvmStatic
    fun <T> findAll(modelClass: Class<T>, isEager: Boolean, vararg ids: Long) = Operator.findAll(modelClass, isEager, *ids)

    /**
     * Basically same as [LitePal.findAll] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param isEager
     * True to load the associated models, false not.
     * @param ids
     * Which records to query. Or do not pass it to find all records.
     * @return A FindMultiExecutor instance.
     */
    @JvmStatic
    fun <T> findAllAsync(modelClass: Class<T>, isEager: Boolean, vararg ids: Long) = Operator.findAllAsync(modelClass, isEager, *ids)

    /**
     * Runs the provided SQL and returns a Cursor over the result set. You may
     * include ? in where clause in the query, which will be replaced by the
     * second to the last parameters, such as:
     *
     * Cursor cursor = LitePal.findBySQL(&quot;select * from person where name=? and age=?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     *
     * @param sql
     * First parameter is the SQL clause to apply. Second to the last
     * parameters will replace the place holders.
     * @return A Cursor object, which is positioned before the first entry. Note
     * that Cursors are not synchronized, see the documentation for more
     * details.
     */
    @JvmStatic
    fun findBySQL(vararg sql: String) = Operator.findBySQL(*sql)

    /**
     * Deletes the record in the database by id.
     *
     * The data in other tables which is referenced with the record will be
     * removed too.
     *
     * LitePal.delete(Person.class, 1);
     *
     * This means that the record 1 in person table will be removed.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param id
     * Which record to delete.
     * @return The number of rows affected. Including cascade delete rows.
     */
    @JvmStatic
    fun delete(modelClass: Class<*>, id: Long) = Operator.delete(modelClass, id)

    /**
     * Basically same as [LitePal.delete] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param id
     * Which record to delete.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun deleteAsync(modelClass: Class<*>, id: Long) = Operator.deleteAsync(modelClass, id)

    /**
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * LitePal.deleteAll(Person.class, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * deleting. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will delete
     * all rows.
     * @return The number of rows affected.
     */
    @JvmStatic
    fun deleteAll(modelClass: Class<*>, vararg conditions: String?) = Operator.deleteAll(modelClass, *conditions)

    /**
     * Basically same as [LitePal.deleteAll] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * deleting. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will delete
     * all rows.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun deleteAllAsync(modelClass: Class<*>, vararg conditions: String?) = Operator.deleteAllAsync(modelClass, *conditions)

    /**
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * LitePal.deleteAll(&quot;person&quot;, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.
     *
     * Note that this method won't delete the referenced data in other tables.
     * You should remove those values by your own.
     *
     * @param tableName
     * Which table to delete from.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * deleting. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will delete
     * all rows.
     * @return The number of rows affected.
     */
    @JvmStatic
    fun deleteAll(tableName: String, vararg conditions: String?) = Operator.deleteAll(tableName, *conditions)

    /**
     * Basically same as [LitePal.deleteAll] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to delete from.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * deleting. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will delete
     * all rows.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun deleteAllAsync(tableName: String, vararg conditions: String?) = Operator.deleteAllAsync(tableName, *conditions)

    /**
     * Updates the corresponding record by id with ContentValues. Returns the
     * number of affected rows.
     *
     * ContentValues cv = new ContentValues();
     *
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     *
     * LitePal.update(Person.class, cv, 1);
     *
     * This means that the name of record 1 will be updated into Jim.
     *
     * @param modelClass
     * Which table to update by class.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param id
     * Which record to update.
     * @return The number of rows affected.
     */
    @JvmStatic
    fun update(modelClass: Class<*>, values: ContentValues, id: Long) = Operator.update(modelClass, values, id)

    /**
     * Basically same as [LitePal.update] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to update by class.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param id
     * Which record to update.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun updateAsync(modelClass: Class<*>, values: ContentValues, id: Long) = Operator.updateAsync(modelClass, values, id)

    /**
     * Updates all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL UPDATE statement and sends
     * it to the database.
     *
     * ContentValues cv = new ContentValues();
     *
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     *
     * LitePal.update(Person.class, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
     *
     * This means that all the records which name is Tom will be updated into
     * Jim.
     *
     * @param modelClass
     * Which table to update by class.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * updating. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return The number of rows affected.
     */
    @JvmStatic
    fun updateAll(modelClass: Class<*>, values: ContentValues, vararg conditions: String?) = Operator.updateAll(modelClass, values, *conditions)

    /**
     * Basically same as [LitePal.updateAll] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to update by class.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * updating. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun updateAllAsync(modelClass: Class<*>, values: ContentValues, vararg conditions: String?) = Operator.updateAllAsync(modelClass, values, *conditions)

    /**
     * Updates all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL UPDATE statement and sends
     * it to the database.
     *
     * ContentValues cv = new ContentValues();
     *
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     *
     * LitePal.update(&quot;person&quot;, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
     *
     * This means that all the records which name is Tom will be updated into
     * Jim.
     *
     * @param tableName
     * Which table to update.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * updating. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return The number of rows affected.
     */
    @JvmStatic
    fun updateAll(tableName: String, values: ContentValues, vararg conditions: String?) = Operator.updateAll(tableName, values, *conditions)

    /**
     * Basically same as [LitePal.updateAll] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to update.
     * @param values
     * A map from column names to new column values. null is a valid
     * value that will be translated to NULL.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * updating. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic
    fun updateAllAsync(tableName: String, values: ContentValues, vararg conditions: String?) = Operator.updateAllAsync(tableName, values, *conditions)

    /**
     * Saves the collection into database.
     *
     * LitePal.saveAll(people);
     *
     * If the model in collection is a new record gets created in the database,
     * otherwise the existing record gets updated.
     *
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be **rolled back**.
     *
     * This method acts the same result as the below way, but **much more
     * efficient**.
     *
     * for (Person person : people) {
     *      person.save();
     * }
     *
     * So when your collection holds huge of models, saveAll(Collection) is the better choice.
     *
     * @param collection
     * Holds all models to save.
     */
    @JvmStatic
    fun <T : LitePalSupport> saveAll(collection: Collection<T>) = Operator.saveAll(collection)

    /**
     * Basically same as [LitePal.saveAll] but pending to a new thread for executing.
     *
     * @param collection
     * Holds all models to save.
     * @return A SaveExecutor instance.
     */
    @JvmStatic
    fun <T : LitePalSupport> saveAllAsync(collection: Collection<T>) = Operator.saveAllAsync(collection)

    /**
     * Provide a way to mark all models in collection as deleted. This means these models' save
     * state is no longer exist anymore. If save them again, they will be treated as inserting new
     * data instead of updating the exist one.
     * @param collection
     * Collection of models which want to mark as deleted and clear their save state.
     */
    @JvmStatic
    fun <T : LitePalSupport> markAsDeleted(collection: Collection<T>) {
        Operator.markAsDeleted(collection)
    }

    /**
     * Check if the specified conditions data already exists in the table.
     * @param modelClass
     * Which table to check by class.
     * @param conditions
     * A filter declaring which data to check. Exactly same use as
     * [LitePal.where], except null conditions will result in false.
     * @return Return true if the specified conditions data already exists in the table.
     * False otherwise. Null conditions will result in false.
     */
    @JvmStatic
    fun <T> isExist(modelClass: Class<T>, vararg conditions: String?) = Operator.isExist(modelClass, *conditions)

    /**
     * Register a listener to listen database create and upgrade events.
     */
    @JvmStatic
    fun registerDatabaseListener(listener: DatabaseListener) {
        Operator.registerDatabaseListener(listener)
    }

}