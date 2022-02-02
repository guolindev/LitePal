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

import android.text.TextUtils
import org.litepal.Operator.handler
import org.litepal.crud.QueryHandler
import org.litepal.crud.async.AverageExecutor
import org.litepal.crud.async.CountExecutor
import org.litepal.crud.async.FindExecutor
import org.litepal.crud.async.FindMultiExecutor
import org.litepal.exceptions.LitePalSupportException
import org.litepal.tablemanager.Connector
import org.litepal.util.BaseUtility
import org.litepal.util.DBUtility
import kotlin.concurrent.withLock

/**
 * Allows developers to query tables with fluent style.
 *
 * @author Tony Green
 * @since 2.0
 */
class FluentQuery
/**
 * Do not allow to create instance by developers.
 */
internal constructor() {
    /**
     * Representing the selected columns in SQL.
     */
    var mColumns: Array<out String> = arrayOf()

    /**
     * Representing the where clause in SQL.
     */
    var mConditions: Array<out String> = arrayOf()

    /**
     * Representing the order by clause in SQL.
     */
    var mOrderBy: String? = null

    /**
     * Representing the limit clause in SQL.
     */
    var mLimit: String? = null

    /**
     * Representing the offset in SQL.
     */
    var mOffset: String? = null

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
     * @return A ClusterQuery instance.
     */
    fun select(vararg columns: String): FluentQuery {
        mColumns = columns
        return this
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
     * @return A ClusterQuery instance.
     */
    fun where(vararg conditions: String): FluentQuery {
        mConditions = conditions
        return this
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
     * @return A ClusterQuery instance.
     */
    fun order(column: String?): FluentQuery {
        mOrderBy = column
        return this
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
     * @return A ClusterQuery instance.
     */
    fun limit(value: Int): FluentQuery {
        mLimit = value.toString()
        return this
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
     * @return A ClusterQuery instance.
     */
    fun offset(value: Int): FluentQuery {
        mOffset = value.toString()
        return this
    }

    /**
     * Finds multiple records by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2)
     * .find(Person.class);
    </pre> *
     *
     * You can also do the same job with SQLiteDatabase like this:
     *
     * <pre>
     * getSQLiteDatabase().query(&quot;Person&quot;, &quot;name&quot;, &quot;age &gt; ?&quot;, new String[] { &quot;14&quot; }, null, null, &quot;age&quot;,
     * &quot;2,1&quot;);
    </pre> *
     *
     * Obviously, the first way is much more semantic.<br></br>
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [FluentQuery.find].
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @return An object list with founded data from database, or an empty list.
     */
    fun <T> find(modelClass: Class<T>?): List<T> {
        return find(modelClass, false)
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAsync(modelClass: Class<T>?): FindMultiExecutor<T> {
        return findAsync(modelClass, false)
    }

    /**
     * It is mostly same as [FluentQuery.find] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return as a list.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object list with founded data from database, or an empty list.
     */
    fun <T> find(modelClass: Class<T>?, isEager: Boolean): List<T> {
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            val limit: String?
            if (mOffset == null) {
                limit = mLimit
            } else {
                if (mLimit == null) {
                    mLimit = "0"
                }
                limit = "$mOffset,$mLimit"
            }
                return@withLock queryHandler.onFind(
                    modelClass,
                    mColumns,
                    mConditions,
                    mOrderBy,
                    limit,
                    isEager
                )

        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun <T> findAsync(modelClass: Class<T>?, isEager: Boolean): FindMultiExecutor<T> {
        val executor = FindMultiExecutor<T>()
        val runnable = Runnable {
            reentrantLock.withLock {
                val t = find(modelClass, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds the first record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2)
     * .findFirst(Person.class);
    </pre> *
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [FluentQuery.findFirst].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    fun <T> findFirst(modelClass: Class<T>?): T? {
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
     * It is mostly same as [FluentQuery.findFirst] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    fun <T> findFirst(modelClass: Class<T>?, isEager: Boolean): T? {
        return reentrantLock.withLock {
            val limitTemp = mLimit
            if ("0" != mLimit) { // If mLimit not equals to 0, set mLimit to 1 to find the first record.
                mLimit = "1"
            }
            val list = find(modelClass, isEager)
            mLimit = limitTemp // Don't forget to change it back after finding operation.
            if (list.isNotEmpty()) {
                if (list.size != 1) throw LitePalSupportException("Found multiple records while only one record should be found at most.")
                return@withLock list[0]
            }
                return@withLock null
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
                val t: T? = findFirst(modelClass, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Finds the last record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2)
     * .findLast(Person.class);
    </pre> *
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * [FluentQuery.findLast].
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    fun <T> findLast(modelClass: Class<T>?): T? {
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
     * It is mostly same as [FluentQuery.findLast] but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br></br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     * Which table to query and the object type to return.
     * @param isEager
     * True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    fun <T> findLast(modelClass: Class<T>?, isEager: Boolean): T? {
        return reentrantLock.withLock {
            val orderByTemp = mOrderBy
            val limitTemp = mLimit
            if (TextUtils.isEmpty(mOffset) && TextUtils.isEmpty(mLimit)) { // If mOffset or mLimit is specified, we can't use the strategy in this block to speed up finding.
                if (TextUtils.isEmpty(mOrderBy)) {
                    // If mOrderBy is null, we can use id desc order, then the first record will be the record value where want to find.
                    mOrderBy = "id desc"
                } else {
                    // If mOrderBy is not null, check if it ends with desc.
                    if (mOrderBy!!.endsWith(" desc")) {
                        // If mOrderBy ends with desc, then the last record of desc order will be the first record of asc order, so we remove the desc.
                            mOrderBy = mOrderBy!!.replace(" desc", "")
                        } else {
                            // If mOrderBy not ends with desc, then the last record of asc order will be the first record of desc order, so we add the desc.
                            mOrderBy += " desc"
                        }
                    }
                    if ("0" != mLimit) {
                        mLimit = "1"
                    }
                }
                val list = find(modelClass, isEager)
                mOrderBy = orderByTemp
                mLimit = limitTemp
                val size = list.size
                return@withLock if (size > 0) {
                    list[size - 1]
                } else null

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
                val t: T? = findLast(modelClass, isEager)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(t) }
                }

            }
        }
        executor.submit(runnable)
        return executor
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
        return count(BaseUtility.changeCase(modelClass.simpleName))
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
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return@withLock queryHandler.onCount(tableName, mConditions)
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
        return average(BaseUtility.changeCase(modelClass.simpleName), column)
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
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            queryHandler.onAverage(tableName, column, mConditions)
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
        return max(BaseUtility.changeCase(modelClass.simpleName), columnName, columnType)
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
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return@withLock queryHandler.onMax(tableName, columnName, mConditions, columnType)
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
        return min(BaseUtility.changeCase(modelClass.simpleName), columnName, columnType)
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
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return@withLock queryHandler.onMin(tableName, columnName, mConditions, columnType)
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
        return sum(BaseUtility.changeCase(modelClass.simpleName), columnName, columnType)
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
        return reentrantLock.withLock {
            val queryHandler = QueryHandler(Connector.getDatabase())
            return@withLock queryHandler.onSum(tableName, columnName, mConditions, columnType)

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
}