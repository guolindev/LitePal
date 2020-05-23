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
 * ```
 * LitePal.select("name").where("age > ?", "14").order("age").limit(1).offset(2).find<Person>()
 * ```
 * You can also do the same job with SQLiteDatabase like this:
 * ```
 * getSQLiteDatabase().query("Person", "name", "age > ?", new String[] { "14" }, null, null, "age",
 * 		"2,1")
 * ```
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
@Deprecated("This method is deprecated and will be removed in the future releases.", ReplaceWith("Handle async db operation in your own logic instead."))
inline fun <reified T> FluentQuery.findAsync() = findAsync(T::class.java)

/**
 * It is mostly same as [FluentQuery.find(Class)] but an isEager
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
@Deprecated("This method is deprecated and will be removed in the future releases.", ReplaceWith("Handle async db operation in your own logic instead."))
inline fun <reified T> FluentQuery.findAsync(isEager: Boolean) = findAsync(T::class.java, isEager)

/**
 * Finds the first record by the cluster parameters. You can use the below
 * way to finish a complicated query:
 * ```
 * LitePal.select("name").where("age > ?", "14").order("age").limit(10).offset(2).findFirst<Person>()
 * ```
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * [FluentQuery.findFirst(Class, boolean)].
 *
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findFirst(): T? = findFirst(T::class.java)

/**
 * Basically same as {@link #findFirst(Class)} but pending to a new thread for executing.
 *
 * @return A FindExecutor instance.
 */
@Deprecated("This method is deprecated and will be removed in the future releases.", ReplaceWith("Handle async db operation in your own logic instead."))
inline fun <reified T> FluentQuery.findFirstAsync(): FindExecutor<T> = findFirstAsync(T::class.java)

/**
 * It is mostly same as [FluentQuery.findFirst(Class)] but an isEager
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
 * ```
 * LitePal.select("name").where("age > ?", "14").order("age").limit(10).offset(2).findLast<Person>()
 * ```
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * [FluentQuery.findLast(Class, boolean)].
 *
 * @return An object with founded data from database, or null.
 */
inline fun <reified T> FluentQuery.findLast(): T? = findLast(T::class.java)

/**
 * It is mostly same as [FluentQuery.findLast(Class)] but an isEager
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
 * ```
 * LitePal.count<Person>()
 * ```
 * This will count all rows in person table.
 *
 * You can also specify a where clause when counting.
 * ```
 * LitePal.where("age > ?", "15").count<Person>()
 * ```
 * @return Count of the specified table.
 */
inline fun <reified T> FluentQuery.count() = count(T::class.java)

/**
 * Calculates the average value on a given column.
 * ```
 * LitePal.average<Person>("age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").average<Person>("age")
 * ```
 * @param column
 * The based on column to calculate.
 * @return The average value on a given column.
 */
inline fun <reified T> FluentQuery.average(column: String) = average(T::class.java, column)

/**
 * Calculates the maximum value on a given column. The value is returned
 * with the same data type of the column.
 * ```
 * LitePal.max<Person, Int>("age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").max<Person, Int>("age")
 * ```
 * @param columnName
 * The based on column to calculate.
 *
 * @return The maximum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.max(columnName: String): R = max(T::class.java, columnName, R::class.java)

/**
 * Calculates the maximum value on a given column. The value is returned
 * with the same data type of the column.
 * ```
 * LitePal.max<Int>("person", "age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").max<Int>("person", "age")
 * ```
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
 * ```
 * LitePal.min<Person, Int>("age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").min<Person, Int>("age")
 * ```
 * @param columnName
 * The based on column to calculate.
 * @return The minimum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.min(columnName: String): R = min(T::class.java, columnName, R::class.java)

/**
 * Calculates the minimum value on a given column. The value is returned
 * with the same data type of the column.
 * ```
 * LitePal.min<Int>("person", "age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").min<Int>("person", "age")
 * ```
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
 * ```
 * LitePal.sum<Person, Int>("age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").sum<Person, Int>("age")
 * ```
 * @param columnName
 * The based on column to calculate.
 * @return The sum value on a given column.
 */
inline fun <reified T, reified R> FluentQuery.sum(columnName: String): R = sum(T::class.java, columnName, R::class.java)

/**
 * Calculates the sum of values on a given column. The value is returned
 * with the same data type of the column.
 * ```
 * LitePal.sum<Int>("person", "age")
 * ```
 * You can also specify a where clause when calculating.
 * ```
 * LitePal.where("age > ?", "15").sum<Int>("person", "age")
 * ```
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return The sum value on a given column.
 */
inline fun <reified R> FluentQuery.sum(tableName: String, columnName: String): R = sum(tableName, columnName, R::class.java)