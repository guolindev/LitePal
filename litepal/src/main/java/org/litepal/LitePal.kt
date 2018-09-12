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

    private val handler = Handler(Looper.getMainLooper())

    private var dbListener: DatabaseListener? = null

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * Application context.
     */
    @JvmStatic fun initialize(context: Context) {
        LitePalApplication.sContext = context
    }

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    @JvmStatic fun getDatabase(): SQLiteDatabase {
        synchronized(LitePalSupport::class.java) {
            return Connector.getDatabase()
        }
    }

    /**
     * Get the main thread handler. You don't need this method. It's used by framework only.
     * @return Main thread handler.
     */
    @JvmStatic fun getHandler() = handler

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     * The database to switch to.
     */
    @JvmStatic fun use(litePalDB: LitePalDB) {
        synchronized(LitePalSupport::class.java) {
            val litePalAttr = LitePalAttr.getInstance()
            litePalAttr.dbName = litePalDB.dbName
            litePalAttr.version = litePalDB.version
            litePalAttr.storage = litePalDB.storage
            litePalAttr.classNames = litePalDB.classNames
            // set the extra key name only when use database other than default or litepal.xml not exists
            if (!isDefaultDatabase(litePalDB.dbName)) {
                litePalAttr.extraKeyName = litePalDB.dbName
                litePalAttr.cases = "lower"
            }
            Connector.clearLitePalOpenHelperInstance()
        }
    }


    /**
     * Switch the using database to default with configuration by litepal.xml.
     */
    @JvmStatic fun useDefault() {
        synchronized(LitePalSupport::class.java) {
            LitePalAttr.clearInstance()
            Connector.clearLitePalOpenHelperInstance()
        }
    }

    /**
     * Delete the specified database.
     * @param dbName
     * Name of database to delete.
     * @return True if delete success, false otherwise.
     */
    @JvmStatic fun deleteDatabase(dbName: String): Boolean {
        var realDBName: String
        synchronized(LitePalSupport::class.java) {
            if (!TextUtils.isEmpty(dbName)) {
                realDBName = if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                    dbName + Const.Config.DB_NAME_SUFFIX
                } else {
                    dbName
                }
                var dbFile = LitePalApplication.getContext().getDatabasePath(realDBName)
                if (dbFile.exists()) {
                    val result = dbFile.delete()
                    if (result) {
                        removeVersionInSharedPreferences(realDBName)
                        Connector.clearLitePalOpenHelperInstance()
                    }
                    return result
                }
                val path = LitePalApplication.getContext().getExternalFilesDir("")!!.toString() + "/databases/"
                dbFile = File(path + realDBName)
                val result = dbFile.delete()
                if (result) {
                    removeVersionInSharedPreferences(realDBName)
                    Connector.clearLitePalOpenHelperInstance()
                }
                return result
            }
            return false
        }
    }

    @JvmStatic fun aesKey(key: String) {
        CipherUtil.aesKey = key
    }

    /**
     * Remove the database version in SharedPreferences file.
     * @param dbName
     * Name of database to delete.
     */
    @JvmStatic private fun removeVersionInSharedPreferences(dbName: String) {
        if (isDefaultDatabase(dbName)) {
            SharedUtil.removeVersion(null)
        } else {
            SharedUtil.removeVersion(dbName)
        }
    }

    /**
     * Check the dbName is default database or not. If it's same as dbName in litepal.xml, then it is
     * default database.
     * @param dbName
     * Name of database to check.
     * @return True if it's default database, false otherwise.
     */
    @JvmStatic private fun isDefaultDatabase(dbName: String): Boolean {
        var realDBName = dbName
        if (BaseUtility.isLitePalXMLExists()) {
            if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                realDBName = dbName + Const.Config.DB_NAME_SUFFIX
            }
            val config = LitePalParser.parseLitePalConfiguration()
            var defaultDbName = config.dbName
            if (!defaultDbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                defaultDbName += Const.Config.DB_NAME_SUFFIX
            }
            return realDBName.equals(defaultDbName, ignoreCase = true)
        }
        return false
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
    @JvmStatic fun select(vararg columns: String?): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mColumns = columns
        return cQuery
    }

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
    @JvmStatic fun where(vararg conditions: String?): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mConditions = conditions
        return cQuery
    }

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
    @JvmStatic fun order(column: String?): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mOrderBy = column
        return cQuery
    }

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
    @JvmStatic fun limit(value: Int): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mLimit = value.toString()
        return cQuery
    }

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
    @JvmStatic fun offset(value: Int): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mOffset = value.toString()
        return cQuery
    }

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
    @JvmStatic fun count(modelClass: Class<*>) = count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)))

    /**
     * Basically same as [LitePal.count] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @return A CountExecutor instance.
     */
    @JvmStatic fun countAsync(modelClass: Class<*>) = countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)))

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
    @JvmStatic fun count(tableName: String): Int {
        synchronized(LitePalSupport::class.java) {
            val cQuery = FluentQuery()
            return cQuery.count(tableName)
        }
    }

    /**
     * Basically same as [LitePal.count] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @return A CountExecutor instance.
     */
    @JvmStatic fun countAsync(tableName: String): CountExecutor {
        val executor = CountExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val count = count(tableName)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(count) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun average(modelClass: Class<*>, column: String) = average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), column)

    /**
     * Basically same as [LitePal.average] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query from by class.
     * @param column
     * The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    @JvmStatic fun averageAsync(modelClass: Class<*>, column: String) = averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), column)

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
    @JvmStatic fun average(tableName: String, column: String): Double {
        synchronized(LitePalSupport::class.java) {
            val cQuery = FluentQuery()
            return cQuery.average(tableName, column)
        }
    }

    /**
     * Basically same as [LitePal.average] but pending to a new thread for executing.
     *
     * @param tableName
     * Which table to query from.
     * @param column
     * The based on column to calculate.
     * @return A AverageExecutor instance.
     */
    @JvmStatic fun averageAsync(tableName: String, column: String): AverageExecutor {
        val executor = AverageExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val average = average(tableName, column)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(average) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> max(modelClass: Class<*>, columnName: String, columnType: Class<T>) = max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> maxAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> max(tableName: String, columnName: String, columnType: Class<T>): T {
        synchronized(LitePalSupport::class.java) {
            val cQuery = FluentQuery()
            return cQuery.max(tableName, columnName, columnType)
        }
    }

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
    @JvmStatic fun <T> maxAsync(tableName: String, columnName: String, columnType: Class<T>): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = max(tableName, columnName, columnType)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> min(modelClass: Class<*>, columnName: String, columnType: Class<T>) = min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> minAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> min(tableName: String, columnName: String, columnType: Class<T>): T {
        synchronized(LitePalSupport::class.java) {
            val cQuery = FluentQuery()
            return cQuery.min(tableName, columnName, columnType)
        }
    }

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
    @JvmStatic fun <T> minAsync(tableName: String, columnName: String, columnType: Class<T>): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = min(tableName, columnName, columnType)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> sum(modelClass: Class<*>, columnName: String, columnType: Class<T>) = sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> sumAsync(modelClass: Class<*>, columnName: String, columnType: Class<T>) = sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)), columnName, columnType)

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
    @JvmStatic fun <T> sum(tableName: String, columnName: String, columnType: Class<T>): T {
        synchronized(LitePalSupport::class.java) {
            val cQuery = FluentQuery()
            return cQuery.sum(tableName, columnName, columnType)
        }
    }

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
    @JvmStatic fun <T> sumAsync(tableName: String, columnName: String, columnType: Class<T>): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = sum(tableName, columnName, columnType)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> find(modelClass: Class<T>, id: Long) = find(modelClass, id, false)

    /**
     * Basically same as [LitePal.find] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @return A FindExecutor instance.
     */
    @JvmStatic fun <T> findAsync(modelClass: Class<T>, id: Long) = findAsync(modelClass, id, false)

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
    @JvmStatic fun <T> find(modelClass: Class<T>, id: Long, isEager: Boolean): T? {
        synchronized(LitePalSupport::class.java) {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFind(modelClass, id, isEager)
        }
    }

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
    @JvmStatic fun <T> findAsync(modelClass: Class<T>, id: Long, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = find(modelClass, id, isEager)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> findFirst(modelClass: Class<T>) = findFirst(modelClass, false)

    /**
     * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    @JvmStatic fun <T> findFirstAsync(modelClass: Class<T>) = findFirstAsync(modelClass, false)

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
    @JvmStatic fun <T> findFirst(modelClass: Class<T>, isEager: Boolean): T? {
        synchronized(LitePalSupport::class.java) {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindFirst(modelClass, isEager)
        }
    }

    /**
     * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    @JvmStatic fun <T> findFirstAsync(modelClass: Class<T>, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = findFirst(modelClass, isEager)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> findLast(modelClass: Class<T>) = findLast(modelClass, false)

    /**
     * Basically same as [LitePal.findLast] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return A FindExecutor instance.
     */
    @JvmStatic fun <T> findLastAsync(modelClass: Class<T>) = findLastAsync(modelClass, false)

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
    @JvmStatic fun <T> findLast(modelClass: Class<T>, isEager: Boolean): T? {
        synchronized(LitePalSupport::class.java) {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindLast(modelClass, isEager)
        }
    }

    /**
     * Basically same as [LitePal.findLast] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return A FindExecutor instance.
     */
    @JvmStatic fun <T> findLastAsync(modelClass: Class<T>, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = findLast(modelClass, isEager)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T> findAll(modelClass: Class<T>, vararg ids: Long) = findAll(modelClass, false, *ids)

    /**
     * Basically same as [LitePal.findAll] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param ids
     * Which records to query. Or do not pass it to find all records.
     * @return A FindMultiExecutor instance.
     */
    @JvmStatic fun <T> findAllAsync(modelClass: Class<T>, vararg ids: Long) = findAllAsync(modelClass, false, *ids)

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
    @JvmStatic fun <T> findAll(modelClass: Class<T>, isEager: Boolean, vararg ids: Long): List<T> {
        synchronized(LitePalSupport::class.java) {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindAll(modelClass, isEager, *ids)
        }
    }

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
    @JvmStatic fun <T> findAllAsync(modelClass: Class<T>, isEager: Boolean, vararg ids: Long): FindMultiExecutor<T> {
        val executor = FindMultiExecutor<T>()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val t = findAll(modelClass, isEager, *ids)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(t) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun findBySQL(vararg sql: String): Cursor? {
        synchronized(LitePalSupport::class.java) {
            BaseUtility.checkConditionsCorrect(*sql)
            if (sql == null) {
                return null
            }
            if (sql.isEmpty()) {
                return null
            }
            val selectionArgs: Array<String?>?
            if (sql.size == 1) {
                selectionArgs = null
            } else {
                selectionArgs = arrayOfNulls(sql.size - 1)
                System.arraycopy(sql, 1, selectionArgs, 0, sql.size - 1)
            }
            return Connector.getDatabase().rawQuery(sql[0], selectionArgs)
        }
    }

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
    @JvmStatic fun delete(modelClass: Class<*>, id: Long): Int {
        synchronized(LitePalSupport::class.java) {
            val rowsAffected: Int
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val deleteHandler = DeleteHandler(db)
                rowsAffected = deleteHandler.onDelete(modelClass, id)
                db.setTransactionSuccessful()
                return rowsAffected
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Basically same as [LitePal.delete] but pending to a new thread for executing.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param id
     * Which record to delete.
     * @return A UpdateOrDeleteExecutor instance.
     */
    @JvmStatic fun deleteAsync(modelClass: Class<*>, id: Long): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val rowsAffected = delete(modelClass, id)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(rowsAffected) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun deleteAll(modelClass: Class<*>, vararg conditions: String?): Int {
        synchronized(LitePalSupport::class.java) {
            val deleteHandler = DeleteHandler(Connector.getDatabase())
            return deleteHandler.onDeleteAll(modelClass, *conditions)
        }
    }

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
    @JvmStatic fun deleteAllAsync(modelClass: Class<*>, vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val rowsAffected = deleteAll(modelClass, *conditions)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(rowsAffected) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun deleteAll(tableName: String, vararg conditions: String?): Int {
        synchronized(LitePalSupport::class.java) {
            val deleteHandler = DeleteHandler(Connector.getDatabase())
            return deleteHandler.onDeleteAll(tableName, *conditions)
        }
    }

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
    @JvmStatic fun deleteAllAsync(tableName: String, vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val rowsAffected = deleteAll(tableName, *conditions)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(rowsAffected) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun update(modelClass: Class<*>, values: ContentValues, id: Long): Int {
        synchronized(LitePalSupport::class.java) {
            val updateHandler = UpdateHandler(Connector.getDatabase())
            return updateHandler.onUpdate(modelClass, id, values)
        }
    }

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
    @JvmStatic fun updateAsync(modelClass: Class<*>, values: ContentValues, id: Long): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val rowsAffected = update(modelClass, values, id)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(rowsAffected) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun updateAll(modelClass: Class<*>, values: ContentValues,
                  vararg conditions: String?): Int {
        return updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.name)), values, *conditions)
    }

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
    @JvmStatic fun updateAllAsync(modelClass: Class<*>, values: ContentValues, vararg conditions: String?): UpdateOrDeleteExecutor {
        return updateAllAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.name)), values, *conditions)
    }

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
    @JvmStatic fun updateAll(tableName: String, values: ContentValues,
                  vararg conditions: String?): Int {
        synchronized(LitePalSupport::class.java) {
            val updateHandler = UpdateHandler(Connector.getDatabase())
            return updateHandler.onUpdateAll(tableName, values, *conditions)
        }
    }

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
    @JvmStatic fun updateAllAsync(tableName: String, values: ContentValues, vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val rowsAffected = updateAll(tableName, values, *conditions)
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(rowsAffected) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

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
    @JvmStatic fun <T : LitePalSupport> saveAll(collection: Collection<T>) {
        synchronized(LitePalSupport::class.java) {
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val saveHandler = SaveHandler(db)
                saveHandler.onSaveAll(collection)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                throw LitePalSupportException(e.message, e)
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * Basically same as [LitePal.saveAll] but pending to a new thread for executing.
     *
     * @param collection
     * Holds all models to save.
     * @return A SaveExecutor instance.
     */
    @JvmStatic fun <T : LitePalSupport> saveAllAsync(collection: Collection<T>): SaveExecutor {
        val executor = SaveExecutor()
        val runnable = Runnable {
            synchronized(LitePalSupport::class.java) {
                val success = try {
                    saveAll(collection)
                    true
                } catch (e: Exception) {
                    false
                }
                if (executor.listener != null) {
                    LitePal.getHandler().post { executor.listener.onFinish(success) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Provide a way to mark all models in collection as deleted. This means these models' save
     * state is no longer exist anymore. If save them again, they will be treated as inserting new
     * data instead of updating the exist one.
     * @param collection
     * Collection of models which want to mark as deleted and clear their save state.
     */
    @JvmStatic fun <T : LitePalSupport> markAsDeleted(collection: Collection<T>) {
        for (t in collection) {
            t.clearSavedState()
        }
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
    @JvmStatic fun <T> isExist(modelClass: Class<T>, vararg conditions: String?): Boolean {
        return conditions != null && where(*conditions).count(modelClass) > 0
    }

    /**
     * Register a listener to listen database create and upgrade events.
     */
    @JvmStatic fun registerDatabaseListener(listener: DatabaseListener) {
        dbListener = listener
    }

    @JvmStatic internal fun getDBListener() = dbListener

}