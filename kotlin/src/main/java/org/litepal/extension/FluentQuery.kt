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

package org.litepal.extension

import org.litepal.FluentQuery
import org.litepal.crud.async.FindExecutor

/**
 * Extension of FluentQuery class for Kotlin api.
 * @author Tony Green
 * @since 2.1
 */

/**
 * Finds multiple records by the cluster parameters. You can use the below
 * way to finish a complicated query:
 *
 * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2).find&lt;Person&gt;()
 *
 * You can also do the same job with SQLiteDatabase like this:
 *
 * getSQLiteDatabase().query(&quot;Person&quot;, &quot;name&quot;, &quot;age &gt; ?&quot;, new String[] { &quot;14&quot; }, null, null, &quot;age&quot;,
 * 		&quot;2,1&quot;)
 *
 * Obviously, the first way is much more semantic.<br>
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * {@link FluentQuery#find(Class, boolean)}.
 *
 * @return An object list with founded data from database, or an empty list.
 */
inline fun <reified T> FluentQuery.find(): List<T> = find(T::class.java)

/**
 * Basically same as {@link #find(Class)} but pending to a new thread for executing.
 *
 * @return A FindMultiExecutor instance.
 */
inline fun <reified T> FluentQuery.findAsync() = findAsync(T::class.java)

/**
 * It is mostly same as {@link FluentQuery#find(Class)} but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 *            True to load the associated models, false not.
 * @return An object list with founded data from database, or an empty list.
 */
inline fun <reified T> FluentQuery.find(isEager: Boolean): List<T> = find(T::class.java, isEager)

/**
 * Basically same as {@link #find(Class, boolean)} but pending to a new thread for executing.
 *
 * @param isEager
 *            True to load the associated models, false not.
 * @return A FindMultiExecutor instance.
 */
inline fun <reified T> FluentQuery.findAsync(isEager: Boolean) = findAsync(T::class.java, isEager)

/**
 * Finds the first record by the cluster parameters. You can use the below
 * way to finish a complicated query:
 *
 * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2).findFirst&lt;Person&gt;()
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * {@link FluentQuery#findFirst(Class, boolean)}.
 *
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findFirst(): T? = findFirst(T::class.java)

/**
 * Basically same as {@link #findFirst(Class)} but pending to a new thread for executing.
 *
 * @return A FindExecutor instance.
 */
inline fun <reified T> FluentQuery.findFirstAsync(): FindExecutor<T> = findFirstAsync(T::class.java)

/**
 * It is mostly same as {@link FluentQuery#findFirst(Class)} but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 *            True to load the associated models, false not.
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findFirst(isEager: Boolean): T? = findFirst(T::class.java, isEager)

/**
 * Finds the last record by the cluster parameters. You can use the below
 * way to finish a complicated query:
 *
 * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2).findLast&lt;Person&gt;()
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * {@link FluentQuery#findLast(Class, boolean)}.
 *
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findLast(): T? = findLast(T::class.java)

/**
 * It is mostly same as {@link FluentQuery#findLast(Class)} but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 *            True to load the associated models, false not.
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findLast(isEager: Boolean): T? = findLast(T::class.java, isEager)

/**
 * Count the records.
 *
 * LitePal.count&lt;Person&gt;()
 *
 * This will count all rows in person table.
 *
 * You can also specify a where clause when counting.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count&lt;Person&gt;()
 *
 * @return Count of the specified table.
 */
inline fun <reified T> FluentQuery.count() = count(T::class.java)

/**
 * Calculates the average value on a given column.
 *
 * LitePal.average&lt;Person&gt;(&quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average&lt;Person&gt;(&quot;age&quot;)
 *
 * @param column
 * The based on column to calculate.
 * @return The average value on a given column.
 */
inline fun <reified T> FluentQuery.average(column: String) = average(T::class.java, column)

/**
 * Calculates the maximum value on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.max&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * @param columnName
 * The based on column to calculate.
 *
 * @return The maximum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.max(columnName: String): R = max(T::class.java, columnName, R::class.java)

/**
 * Calculates the maximum value on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.max&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return The maximum value on a given column.
 */
inline fun <reified R> FluentQuery.max(tableName: String, columnName: String): R = max(tableName, columnName, R::class.java)

/**
 * Calculates the minimum value on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.min&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * @param columnName
 * The based on column to calculate.
 * @return The minimum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.min(columnName: String): R = min(T::class.java, columnName, R::class.java)

/**
 * Calculates the minimum value on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.min&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return The minimum value on a given column.
 */
inline fun <reified R> FluentQuery.min(tableName: String, columnName: String): R = min(tableName, columnName, R::class.java)

/**
 * Calculates the sum of values on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.sum&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum&lt;Person, Int&gt;(&quot;age&quot;)
 *
 * @param columnName
 * The based on column to calculate.
 * @return The sum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.sum(columnName: String): R = sum(T::class.java, columnName, R::class.java)

/**
 * Calculates the sum of values on a given column. The value is returned
 * with the same data type of the column.
 *
 * LitePal.sum&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * You can also specify a where clause when calculating.
 *
 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum&lt;Int&gt;(&quot;person&quot;, &quot;age&quot;)
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return The sum value on a given column.
 */
inline fun <reified R> FluentQuery.sum(tableName: String, columnName: String): R = sum(tableName, columnName, R::class.java)