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
import kotlin.concurrent.withLock

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling [.initialize] method to make LitePal ready to
 * work. Also you can switch the using database by calling [.use] and [.useDefault]
 * methods.
 *
 * @author Tony Green
 * @since 2.1
 */
object Operator {
    /**
     * Get the main thread handler. You don't need this method. It's used by framework only.
     * @return Main thread handler.
     */
    @JvmStatic
    val handler = Handler(Looper.getMainLooper())

    @JvmField
    var dBListener: DatabaseListener? = null

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * Application context.
     */
    fun initialize(context: Context?) {
        LitePalApplication.sContext = context
    }

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    val database: SQLiteDatabase
        get() = Connector.getDatabase()

    /**
     * Begins a transaction in EXCLUSIVE mode.
     */
    fun beginTransaction() {
        database.beginTransaction()
    }

    /**
     * End a transaction.
     */
    fun endTransaction() {
        database.endTransaction()
    }

    /**
     * Marks the current transaction as successful. Do not do any more database work between calling this and calling endTransaction.
     * Do as little non-database work as possible in that situation too.
     * If any errors are encountered between this and endTransaction the transaction will still be committed.
     */
    fun setTransactionSuccessful() {
        database.setTransactionSuccessful()
    }

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     * The database to switch to.
     */
    fun use(litePalDB: LitePalDB) {
        reentrantLock.withLock {
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
    fun useDefault() {
        reentrantLock.withLock {
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
    fun deleteDatabase(dbName: String): Boolean {
        var dbName = dbName
        reentrantLock.withLock {
            if (!TextUtils.isEmpty(dbName)) {
                if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                    dbName = dbName + Const.Config.DB_NAME_SUFFIX
                }
                var dbFile = LitePalApplication.getContext().getDatabasePath(dbName)
                if (dbFile.exists()) {
                    val result = dbFile.delete()
                    if (result) {
                        removeVersionInSharedPreferences(dbName)
                        Connector.clearLitePalOpenHelperInstance()
                    }
                    return result
                }
                val path = LitePalApplication.getContext().getExternalFilesDir("")
                    .toString() + "/databases/"
                dbFile = File(path + dbName)
                val result = dbFile.delete()
                if (result) {
                    removeVersionInSharedPreferences(dbName)
                    Connector.clearLitePalOpenHelperInstance()
                }
                return result
            }
            return false
        }
    }

    fun aesKey(key: String?) {
        CipherUtil.aesKey = key
    }

    /**
     * Remove the database version in SharedPreferences file.
     * @param dbName
     * Name of database to delete.
     */
    private fun removeVersionInSharedPreferences(dbName: String) {
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
    private fun isDefaultDatabase(dbName: String): Boolean {
        var dbName = dbName
        if (BaseUtility.isLitePalXMLExists()) {
            if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                dbName = dbName + Const.Config.DB_NAME_SUFFIX
            }
            val config = LitePalParser.parseLitePalConfiguration()
            var defaultDbName = config.dbName
            if (!defaultDbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                defaultDbName = defaultDbName + Const.Config.DB_NAME_SUFFIX
            }
            return dbName.equals(defaultDbName, ignoreCase = true)
        }
        return false
    }

    /**
     * Declaring to query which columns in table.
     *
     * <pre>
     * LitePal.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
    </pre> *
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
    fun select(vararg columns: String): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mColumns = columns
        return cQuery
    }

    /**
     * Declaring to query which rows in table.
     *
     * <pre>
     * LitePal.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
    </pre> *
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
    fun where(vararg conditions: String): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mConditions = conditions
        return cQuery
    }

    /**
     * Declaring how to order the rows queried from table.
     *
     * <pre>
     * LitePal.order(&quot;name desc&quot;).find(Person.class);
    </pre> *
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
    fun order(column: String?): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mOrderBy = column
        return cQuery
    }

    /**
     * Limits the number of rows returned by the query.
     *
     * <pre>
     * LitePal.limit(2).find(Person.class);
    </pre> *
     *
     * This will find the top 2 rows in Person table.
     *
     * @param value
     * Limits the number of rows returned by the query, formatted as
     * LIMIT clause.
     * @return A FluentQuery instance.
     */
    fun limit(value: Int): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mLimit = value.toString()
        return cQuery
    }

    /**
     * Declaring the offset of rows returned by the query. This method must be
     * used with [.limit], or nothing will return.
     *
     * <pre>
     * LitePal.limit(1).offset(2).find(Person.class);
    </pre> *
     *
     * This will find the third row in Person table.
     *
     * @param value
     * The offset amount of rows returned by the query.
     * @return A FluentQuery instance.
     */
    fun offset(value: Int): FluentQuery {
        val cQuery = FluentQuery()
        cQuery.mOffset = value.toString()
        return cQuery
    }

    /**
     * Count the records.
     *
     * <pre>
     * LitePal.count(Person.class);
    </pre> *
     *
     * This will count all rows in person table.<br></br>
     * You can also specify a where clause when counting.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
    </pre> *
     *
     * @param modelClass
     * Which table to query from by class.
     * @return Count of the specified table.
     */
    fun count(modelClass: Class<*>): Int {
        return count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)))
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun countAsync(modelClass: Class<*>): CountExecutor {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)))
    }

    /**
     * Count the records.
     *
     * <pre>
     * LitePal.count(&quot;person&quot;);
    </pre> *
     *
     * This will count all rows in person table.<br></br>
     * You can also specify a where clause when counting.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
    </pre> *
     *
     * @param tableName
     * Which table to query from.
     * @return Count of the specified table.
     */
    fun count(tableName: String?): Int {
        reentrantLock.withLock {
            val cQuery = FluentQuery()
            return cQuery.count(tableName)
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun countAsync(tableName: String?): CountExecutor {
        val executor = CountExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val count = count(tableName)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(count) }
                }
            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Calculates the average value on a given column.
     *
     * <pre>
     * LitePal.average(Person.class, &quot;age&quot;);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
    </pre> *
     *
     * @param modelClass
     * Which table to query from by class.
     * @param column
     * The based on column to calculate.
     * @return The average value on a given column.
     */
    fun average(modelClass: Class<*>, column: String?): Double {
        return average(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            column
        )
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun averageAsync(modelClass: Class<*>, column: String?): AverageExecutor {
        return averageAsync(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            column
        )
    }

    /**
     * Calculates the average value on a given column.
     *
     * <pre>
     * LitePal.average(&quot;person&quot;, &quot;age&quot;);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
    </pre> *
     *
     * @param tableName
     * Which table to query from.
     * @param column
     * The based on column to calculate.
     * @return The average value on a given column.
     */
    fun average(tableName: String?, column: String?): Double {
        reentrantLock.withLock {
            val cQuery = FluentQuery()
            return cQuery.average(tableName, column)
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun averageAsync(tableName: String?, column: String?): AverageExecutor {
        val executor = AverageExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val average = average(tableName, column)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(average) }
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
     * <pre>
     * LitePal.max(Person.class, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The maximum value on a given column.
     */
    fun <T> max(modelClass: Class<*>, columnName: String?, columnType: Class<T>?): T {
        return max(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> maxAsync(
        modelClass: Class<*>,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        return maxAsync(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * Calculates the maximum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.max(&quot;person&quot;, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The maximum value on a given column.
     */
    fun <T> max(tableName: String?, columnName: String?, columnType: Class<T>?): T {
        reentrantLock.withLock {
            val cQuery = FluentQuery()
            return cQuery.max(tableName, columnName, columnType)
        }

    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> maxAsync(
        tableName: String?,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = max(tableName, columnName, columnType)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
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
     * <pre>
     * LitePal.min(Person.class, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The minimum value on a given column.
     */
    fun <T> min(modelClass: Class<*>, columnName: String?, columnType: Class<T>?): T {
        return min(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> minAsync(
        modelClass: Class<*>,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        return minAsync(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * Calculates the minimum value on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.min(&quot;person&quot;, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The minimum value on a given column.
     */
    fun <T> min(tableName: String?, columnName: String?, columnType: Class<T>?): T {
        reentrantLock.withLock {
            val cQuery = FluentQuery()
            return cQuery.min(tableName, columnName, columnType)
        }

    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> minAsync(
        tableName: String?,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = min(tableName, columnName, columnType)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
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
     * <pre>
     * LitePal.sum(Person.class, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param modelClass
     * Which table to query from by class.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The sum value on a given column.
     */
    fun <T> sum(modelClass: Class<*>, columnName: String?, columnType: Class<T>?): T {
        return sum(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> sumAsync(
        modelClass: Class<*>,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        return sumAsync(
            BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.name)),
            columnName,
            columnType
        )
    }

    /**
     * Calculates the sum of values on a given column. The value is returned
     * with the same data type of the column.
     *
     * <pre>
     * LitePal.sum(&quot;person&quot;, &quot;age&quot;, int.class);
    </pre> *
     *
     * You can also specify a where clause when calculating.
     *
     * <pre>
     * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
    </pre> *
     *
     * @param tableName
     * Which table to query from.
     * @param columnName
     * The based on column to calculate.
     * @param columnType
     * The type of the based on column.
     * @return The sum value on a given column.
     */
    fun <T> sum(tableName: String?, columnName: String?, columnType: Class<T>?): T {
        reentrantLock.withLock {
            val cQuery = FluentQuery()
            return cQuery.sum(tableName, columnName, columnType)
        }

    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> sumAsync(
        tableName: String?,
        columnName: String?,
        columnType: Class<T>?
    ): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = sum(tableName, columnName, columnType)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds the record by a specific id.
     *
     * <pre>
     * Person p = LitePal.find(Person.class, 1);
    </pre> *
     *
     * The modelClass determines which table to query and the object type to
     * return. If no record can be found, then return null. <br></br>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [Operator.find].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param id
     * Which record to query.
     * @return An object with found data from database, or null.
     */
    @JvmStatic
    fun <T> find(modelClass: Class<T>?, id: Long): T? {
        return find(modelClass, id, false)
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAsync(modelClass: Class<T>?, id: Long): FindExecutor<T> {
        return findAsync(modelClass, id, false)
    }

    /**
     * It is mostly same as [Operator.find] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
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
    fun <T> find(modelClass: Class<T>?, id: Long, isEager: Boolean): T {
        reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFind(modelClass, id, isEager)
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAsync(modelClass: Class<T>?, id: Long, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = find(modelClass, id, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }
            }

        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds the first record of a single table.
     *
     * <pre>
     * Person p = LitePal.findFirst(Person.class);
    </pre> *
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [Operator.findFirst].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with data of first row, or null.
     */
    fun <T> findFirst(modelClass: Class<T>?): T {
        return findFirst(modelClass, false)
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findFirstAsync(modelClass: Class<T>?): FindExecutor<T> {
        return findFirstAsync(modelClass, false)
    }

    /**
     * It is mostly same as [Operator.findFirst] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with data of first row, or null.
     */
    fun <T> findFirst(modelClass: Class<T>?, isEager: Boolean): T {
        reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindFirst(modelClass, isEager)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findFirstAsync(modelClass: Class<T>?, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = findFirst(modelClass, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds the last record of a single table.
     *
     * <pre>
     * Person p = LitePal.findLast(Person.class);
    </pre> *
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [Operator.findLast].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with data of last row, or null.
     */
    fun <T> findLast(modelClass: Class<T>?): T {
        return findLast(modelClass, false)
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findLastAsync(modelClass: Class<T>?): FindExecutor<T> {
        return findLastAsync(modelClass, false)
    }

    /**
     * It is mostly same as [Operator.findLast] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with data of last row, or null.
     */
    fun <T> findLast(modelClass: Class<T>?, isEager: Boolean): T {
        reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindLast(modelClass, isEager)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findLastAsync(modelClass: Class<T>?, isEager: Boolean): FindExecutor<T> {
        val executor = FindExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = findLast(modelClass, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }

                }
            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds multiple records by an id array.
     *
     * <pre>
     * List&lt;Person&gt; people = LitePal.findAll(Person.class, 1, 2, 3);
     *
     * long[] bookIds = { 10, 18 };
     * List&lt;Book&gt; books = LitePal.findAll(Book.class, bookIds);
    </pre> *
     *
     * Of course you can find all records by passing nothing to the ids
     * parameter.
     *
     * <pre>
     * List&lt;Book&gt; allBooks = LitePal.findAll(Book.class);
    </pre> *
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [Operator.findAll].
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
    fun <T> findAll(modelClass: Class<T>?, vararg ids: Long): List<T> {
        return findAll(modelClass, false, *ids)
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAllAsync(modelClass: Class<T>?, vararg ids: Long): FindMultiExecutor<T> {
        return findAllAsync(modelClass, false, *ids)
    }

    /**
     * It is mostly same as [Operator.findAll] but an
     * isEager parameter. If set true the associated models will be loaded as well.
     * <br></br>
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
    fun <T> findAll(
        modelClass: Class<T>?, isEager: Boolean,
        vararg ids: Long
    ): List<T> {
        reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return queryHandler.onFindAll(modelClass, isEager, *ids)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAllAsync(
        modelClass: Class<T>?,
        isEager: Boolean,
        vararg ids: Long
    ): FindMultiExecutor<T> {
        val executor = FindMultiExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = findAll(modelClass, isEager, *ids)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
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
     * <pre>
     * Cursor cursor = LitePal.findBySQL(&quot;select * from person where name=? and age=?&quot;, &quot;Tom&quot;, &quot;14&quot;);
    </pre> *
     *
     * @param sql
     * First parameter is the SQL clause to apply. Second to the last
     * parameters will replace the place holders.
     * @return A Cursor object, which is positioned before the first entry. Note
     * that Cursors are not synchronized, see the documentation for more
     * details.
     */
    @JvmStatic
    fun findBySQL(vararg sql: String?): Cursor? {
        return reentrantLock.withLock {
            BaseUtility.checkConditionsCorrect(*sql)
            if (sql == null) {
                return@withLock null
            }
            if (sql.isEmpty()) {
                return@withLock null
            }
            val selectionArgs: Array<String?>?
            if (sql.size == 1) {
                selectionArgs = null
            } else {
                selectionArgs = arrayOfNulls(sql.size - 1)
                System.arraycopy(sql, 1, selectionArgs, 0, sql.size - 1)
            }
            return@withLock Connector.getDatabase().rawQuery(sql[0], selectionArgs)
        }
    }

    /**
     * Deletes the record in the database by id.<br></br>
     * The data in other tables which is referenced with the record will be
     * removed too.
     *
     * <pre>
     * LitePal.delete(Person.class, 1);
    </pre> *
     *
     * This means that the record 1 in person table will be removed.
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param id
     * Which record to delete.
     * @return The number of rows affected. Including cascade delete rows.
     */
    fun delete(modelClass: Class<*>?, id: Long): Int {
        return reentrantLock.withLock {
            val rowsAffected: Int
            val db = Connector.getDatabase()
            db.beginTransaction()
            return@withLock try {
                val deleteHandler = DeleteHandler(db)
                rowsAffected = deleteHandler.onDelete(modelClass, id)
                db.setTransactionSuccessful()
                rowsAffected
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun deleteAsync(modelClass: Class<*>?, id: Long): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = delete(modelClass, id)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }
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
     * <pre>
     * LitePal.deleteAll(Person.class, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
    </pre> *
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.<br></br>
     *
     * @param modelClass
     * Which table to delete from by class.
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * deleting. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return The number of rows affected.
     */
    fun deleteAll(modelClass: Class<*>?, vararg conditions: String?): Int {
        return reentrantLock.withLock {
            val rowsAffected: Int
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val deleteHandler = DeleteHandler(db)
                rowsAffected = deleteHandler.onDeleteAll(modelClass, *conditions)
                db.setTransactionSuccessful()
                rowsAffected
            } finally {
                db.endTransaction()
            }
        }

    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun deleteAllAsync(modelClass: Class<*>?, vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = deleteAll(modelClass, *conditions)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }
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
     * <pre>
     * LitePal.deleteAll(&quot;person&quot;, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
    </pre> *
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.<br></br>
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
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return The number of rows affected.
     */
    fun deleteAll(tableName: String?, vararg conditions: String?): Int {
        return reentrantLock.withLock {
            val deleteHandler = DeleteHandler(Connector.getDatabase())
            deleteHandler.onDeleteAll(tableName, *conditions)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun deleteAllAsync(tableName: String?, vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = deleteAll(tableName, *conditions)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }
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
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, 1);
    </pre> *
     *
     * This means that the name of record 1 will be updated into Jim.<br></br>
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
    fun update(modelClass: Class<*>?, values: ContentValues?, id: Long): Int {
        return reentrantLock.withLock {
            val updateHandler = UpdateHandler(Connector.getDatabase())
            updateHandler.onUpdate(modelClass, id, values)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun updateAsync(
        modelClass: Class<*>?,
        values: ContentValues?,
        id: Long
    ): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = update(modelClass, values, id)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }

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
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
    </pre> *
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
    fun updateAll(
        modelClass: Class<*>, values: ContentValues?,
        vararg conditions: String?
    ): Int {
        return updateAll(
            BaseUtility.changeCase(
                DBUtility.getTableNameByClassName(
                    modelClass.name
                )
            ), values, *conditions
        )
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun updateAllAsync(
        modelClass: Class<*>,
        values: ContentValues?,
        vararg conditions: String?
    ): UpdateOrDeleteExecutor {
        return updateAllAsync(
            BaseUtility.changeCase(
                DBUtility.getTableNameByClassName(
                    modelClass.name
                )
            ), values, *conditions
        )
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
    </pre> *
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
    fun updateAll(
        tableName: String?, values: ContentValues?,
        vararg conditions: String?
    ): Int {
        return reentrantLock.withLock {
            val updateHandler = UpdateHandler(Connector.getDatabase())
            updateHandler.onUpdateAll(tableName, values, *conditions)

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun updateAllAsync(
        tableName: String?,
        values: ContentValues?,
        vararg conditions: String?
    ): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = updateAll(tableName, values, *conditions)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }

                }
            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Saves the collection into database. <br></br>
     *
     * <pre>
     * LitePal.saveAll(people);
    </pre> *
     *
     * If the model in collection is a new record gets created in the database,
     * otherwise the existing record gets updated.<br></br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be **rolled back**. <br></br>
     * This method acts the same result as the below way, but **much more
     * efficient**.
     *
     * <pre>
     * for (Person person : people) {
     * person.save();
     * }
    </pre> *
     *
     * So when your collection holds huge of models, saveAll(Collection) is the better choice.
     *
     * @param collection
     * Holds all models to save.
     * @return True if all records in collection are saved. False none record in collection is saved. There won't be partial saved condition.
     */
    @JvmStatic
    fun <T : LitePalSupport?> saveAll(collection: Collection<T>?): Boolean {
        return reentrantLock.withLock {
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val saveHandler = SaveHandler(db)
                saveHandler.onSaveAll(collection)
                db.setTransactionSuccessful()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                db.endTransaction()
            }

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T : LitePalSupport?> saveAllAsync(collection: Collection<T>?): SaveExecutor {
        val executor = SaveExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val success: Boolean = try {
                    saveAll(collection)
                    true
                } catch (e: Exception) {
                    false
                }
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(success) }
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
    fun <T : LitePalSupport?> markAsDeleted(collection: Collection<T>) {
        for (t in collection) {
            t?.clearSavedState()
        }
    }

    /**
     * Check if the specified conditions data already exists in the table.
     * @param modelClass
     * Which table to check by class.
     * @param conditions
     * A filter declaring which data to check. Exactly same use as
     * [Operator.where], except null conditions will result in false.
     * @return Return true if the specified conditions data already exists in the table.
     * False otherwise. Null conditions will result in false.
     */
    fun <T> isExist(modelClass: Class<T>, vararg conditions: String): Boolean {
        return conditions != null && where(*conditions).count(modelClass) > 0
    }

    /**
     * Register a listener to listen database create and upgrade events.
     */
    fun registerDatabaseListener(listener: DatabaseListener?) {
        dBListener = listener
    }
}