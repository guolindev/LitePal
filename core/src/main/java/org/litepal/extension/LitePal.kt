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

import android.content.ContentValues
import org.litepal.LitePal

/**
 * Extension of LitePal class for Kotlin api.
 * @author Tony Green
 * @since 2.1
 */

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
inline fun <reified T> LitePal.count() = count(T::class.java)

/**
 * Basically same as [LitePal.count] but pending to a new thread for executing.
 *
 * @return A CountExecutor instance.
 */
inline fun <reified T> LitePal.countAsync() = countAsync(T::class.java)

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
inline fun <reified T> LitePal.average(column: String) = average(T::class.java, column)

/**
 * Basically same as [LitePal.average] but pending to a new thread for executing.
 *
 * @param column
 * The based on column to calculate.
 * @return A AverageExecutor instance.
 */
inline fun <reified T> LitePal.averageAsync(column: String) = averageAsync(T::class.java, column)

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
inline fun <reified T, reified R> LitePal.max(columnName: String) = max(T::class.java, columnName, R::class.java)

/**
 * Basically same as [LitePal.max] but pending to a new thread for executing.
 *
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified T, reified R> LitePal.maxAsync(columnName: String) = maxAsync(T::class.java, columnName, R::class.java)

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
inline fun <reified R> LitePal.max(tableName: String, columnName: String) = max(tableName, columnName, R::class.java)

/**
 * Basically same as [LitePal.max] but pending to a new thread for executing.
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified R> LitePal.maxAsync(tableName: String, columnName: String) = maxAsync(tableName, columnName, R::class.java)

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
inline fun <reified T, reified R> LitePal.min(columnName: String) = min(T::class.java, columnName, R::class.java)

/**
 * Basically same as [LitePal.min] but pending to a new thread for executing.
 *
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified T, reified R> LitePal.minAsync(columnName: String) = minAsync(T::class.java, columnName, R::class.java)

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
inline fun <reified R> LitePal.min(tableName: String, columnName: String) = min(tableName, columnName, R::class.java)

/**
 * Basically same as [LitePal.min] but pending to a new thread for executing.
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified R> LitePal.minAsync(tableName: String, columnName: String) = minAsync(tableName, columnName, R::class.java)

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
inline fun <reified T, reified R> LitePal.sum(columnName: String) = sum(T::class.java, columnName, R::class.java)

/**
 * Basically same as [LitePal.sum] but pending to a new thread for executing.
 *
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified T, reified R> LitePal.sumAsync(columnName: String) = sumAsync(T::class.java, columnName, R::class.java)

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
inline fun <reified R> LitePal.sum(tableName: String, columnName: String) = sum(tableName, columnName, R::class.java)

/**
 * Basically same as [LitePal.sum] but pending to a new thread for executing.
 *
 * @param tableName
 * Which table to query from.
 * @param columnName
 * The based on column to calculate.
 * @return A FindExecutor instance.
 */
inline fun <reified R> LitePal.sumAsync(tableName: String, columnName: String) = sumAsync(tableName, columnName, R::class.java)

/**
 * Finds the record by a specific id.
 *
 * val person = LitePal.find&lt;Person&gt;(1)
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using[LitePal.find] with isEager parameter.
 *
 * @param id
 * Which record to query.
 * @return An object with found data from database, or null.
 */
inline fun <reified T> LitePal.find(id: Long): T? = find(T::class.java, id)

/**
 * Basically same as [LitePal.find] but pending to a new thread for executing.
 *
 * @param id
 * Which record to query.
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findAsync(id: Long) = findAsync(T::class.java, id)

/**
 * It is mostly same as [LitePal.find] but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param id
 * Which record to query.
 * @param isEager
 * True to load the associated models, false not.
 * @return An object with found data from database, or null.
 */
inline fun <reified T> LitePal.find(id: Long, isEager: Boolean) = find(T::class.java, id, isEager)

/**
 * Basically same as [LitePal.find] but pending to a new thread for executing.
 *
 * @param id
 * Which record to query.
 * @param isEager
 * True to load the associated models, false not.
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findAsync(id: Long, isEager: Boolean) = find(T::class.java, id, isEager)

/**
 * Finds the first record of a single table.
 *
 * val person = LitePal.findFirst&lt;Person&gt;()
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * [LitePal.findFirst] with isEager parameter.
 *
 * @return An object with data of first row, or null.
 */
inline fun <reified T> LitePal.findFirst() = findFirst(T::class.java)

/**
 * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
 *
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findFirstAsync() = findFirstAsync(T::class.java)

/**
 * It is mostly same as [LitePal.findFirst] but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @return An object with data of first row, or null.
 */
inline fun <reified T> LitePal.findFirst(isEager: Boolean) = findFirst(T::class.java, isEager)

/**
 * Basically same as [LitePal.findFirst] but pending to a new thread for executing.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findFirstAsync(isEager: Boolean) = findFirstAsync(T::class.java, isEager)

/**
 * Finds the last record of a single table.
 *
 * val p = LitePal.findLast&lt;Person&gt;()
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * [LitePal.findLast] with isEager parameter.
 *
 * @return An object with data of last row, or null.
 */
inline fun <reified T> LitePal.findLast() = findLast(T::class.java)

/**
 * Basically same as [LitePal.findLast] but pending to a new thread for executing.
 *
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findLastAsync() = findLastAsync(T::class.java)

/**
 * It is mostly same as [LitePal.findLast] but an isEager
 * parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @return An object with data of last row, or null.
 */
inline fun <reified T> LitePal.findLast(isEager: Boolean) = findLast(T::class.java, isEager)

/**
 * Basically same as [LitePal.findLast] but pending to a new thread for executing.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @return A FindExecutor instance.
 */
inline fun <reified T> LitePal.findLastAsync(isEager: Boolean) = findLastAsync(T::class.java, isEager)

/**
 * Finds multiple records by an id array.
 *
 * val people = LitePal.findAll&lt;Person&gt;(1, 2, 3)
 *
 * val bookIds = longArrayOf(10, 18)
 *
 * LitePal.findAll&lt;Book&gt;(*bookIds)
 *
 * Of course you can find all records by passing nothing to the ids
 * parameter.
 *
 * val allBooks = LitePal.findAll&lt;Book&gt;()
 *
 * Note that the associated models won't be loaded by default considering
 * the efficiency, but you can do that by using
 * [LitePal.findAll] with isEager parameter.
 *
 * @param ids
 * Which records to query. Or do not pass it to find all records.
 * @return An object list with found data from database, or an empty list.
 */
inline fun <reified T> LitePal.findAll(vararg ids: Long) = findAll(T::class.java, *ids)

/**
 * Basically same as [LitePal.findAll] but pending to a new thread for executing.
 *
 * @param ids
 * Which records to query. Or do not pass it to find all records.
 * @return A FindMultiExecutor instance.
 */
inline fun <reified T> LitePal.findAllAsync(vararg ids: Long) = findAllAsync(T::class.java, *ids)

/**
 * It is mostly same as [LitePal.findAll] but an
 * isEager parameter. If set true the associated models will be loaded as well.
 *
 * Note that isEager will only work for one deep level relation, considering the query efficiency.
 * You have to implement on your own if you need to load multiple deepness of relation at once.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @param ids
 * Which records to query. Or do not pass it to find all records.
 * @return An object list with found data from database, or an empty list.
 */
inline fun <reified T> LitePal.findAll(isEager: Boolean, vararg ids: Long) = findAll(T::class.java, isEager, *ids)

/**
 * Basically same as [LitePal.findAll] but pending to a new thread for executing.
 *
 * @param isEager
 * True to load the associated models, false not.
 * @param ids
 * Which records to query. Or do not pass it to find all records.
 * @return A FindMultiExecutor instance.
 */
inline fun <reified T> LitePal.findAllAsync(isEager: Boolean, vararg ids: Long) = findAllAsync(T::class.java, isEager, *ids)

/**
 * Deletes the record in the database by id.
 *
 * The data in other tables which is referenced with the record will be
 * removed too.
 *
 * LitePal.delete&lt;Person&gt;(1)
 *
 * This means that the record 1 in person table will be removed.
 *
 * @param id
 * Which record to delete.
 * @return The number of rows affected. Including cascade delete rows.
 */
inline fun <reified T> LitePal.delete(id: Long) = delete(T::class.java, id)

/**
 * Basically same as [LitePal.delete] but pending to a new thread for executing.
 *
 * @param id
 * Which record to delete.
 * @return A UpdateOrDeleteExecutor instance.
 */
inline fun <reified T> LitePal.deleteAsync(id: Long) = deleteAsync(T::class.java, id)

/**
 * Deletes all records with details given if they match a set of conditions
 * supplied. This method constructs a single SQL DELETE statement and sends
 * it to the database.
 *
 * LitePal.deleteAll&lt;Person&gt;(&quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;)
 *
 * This means that all the records which name is Tom and age is 14 will be
 * removed.
 *
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
inline fun <reified T> LitePal.deleteAll(vararg conditions: String?) = deleteAll(T::class.java, *conditions)

/**
 * Basically same as [LitePal.deleteAll] but pending to a new thread for executing.
 *
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
inline fun <reified T> LitePal.deleteAllAsync(vararg conditions: String?) = deleteAllAsync(T::class.java, *conditions)

/**
 * Updates the corresponding record by id with ContentValues. Returns the
 * number of affected rows.
 *
 * val cv = ContentValues()
 *
 * cv.put(&quot;name&quot;, &quot;Jim&quot;)
 *
 * LitePal.update&lt;Person&gt;(cv, 1)
 *
 * This means that the name of record 1 will be updated into Jim.
 *
 * @param values
 * A map from column names to new column values. null is a valid
 * value that will be translated to NULL.
 * @param id
 * Which record to update.
 * @return The number of rows affected.
 */
inline fun <reified T> LitePal.update(values: ContentValues, id: Long) = update(T::class.java, values, id)

/**
 * Basically same as [LitePal.update] but pending to a new thread for executing.
 *
 * @param values
 * A map from column names to new column values. null is a valid
 * value that will be translated to NULL.
 * @param id
 * Which record to update.
 * @return A UpdateOrDeleteExecutor instance.
 */
inline fun <reified T> LitePal.updateAsync(values: ContentValues, id: Long) = updateAsync(T::class.java, values, id)

/**
 * Updates all records with details given if they match a set of conditions
 * supplied. This method constructs a single SQL UPDATE statement and sends
 * it to the database.
 *
 * val cv = ContentValues()
 *
 * cv.put(&quot;name&quot;, &quot;Jim&quot;)
 *
 * LitePal.update&lt;Person&gt;(cv, &quot;name = ?&quot;, &quot;Tom&quot;)
 *
 * This means that all the records which name is Tom will be updated into
 * Jim.
 *
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
inline fun <reified T> LitePal.updateAll(values: ContentValues, vararg conditions: String?) = updateAll(T::class.java, values, *conditions)

/**
 * Basically same as [LitePal.updateAll] but pending to a new thread for executing.
 *
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
inline fun <reified T> LitePal.updateAllAsync(values: ContentValues, vararg conditions: String?) = updateAllAsync(T::class.java, values, *conditions)

/**
 * Check if the specified conditions data already exists in the table.
 * @param conditions
 * A filter declaring which data to check. Exactly same use as
 * [LitePal.where], except null conditions will result in false.
 * @return Return true if the specified conditions data already exists in the table.
 * False otherwise. Null conditions will result in false.
 */
inline fun <reified T> LitePal.isExist(vararg conditions: String?) = isExist(T::class.java, *conditions)