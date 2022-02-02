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
package org.litepal.crud

import org.litepal.Operator.handler
import org.litepal.Operator.where
import org.litepal.crud.async.SaveExecutor
import org.litepal.crud.async.UpdateOrDeleteExecutor
import org.litepal.exceptions.LitePalSupportException
import org.litepal.reentrantLock
import org.litepal.tablemanager.Connector
import org.litepal.util.BaseUtility
import org.litepal.util.DBUtility
import kotlin.concurrent.withLock

/**
 * LitePalSupport connects classes to SQLite database tables to establish an almost
 * zero-configuration persistence layer for applications. In the context of an
 * application, these classes are commonly referred to as models. Models can
 * also be connected to other models.<br></br>
 * LitePalSupport relies heavily on naming in that it uses class and association
 * names to establish mappings between respective database tables and foreign
 * key columns.<br></br>
 * Automated mapping between classes and tables, attributes and columns.
 *
 * <pre>
 * public class Person extends LitePalSupport {
 * private int id;
 * private String name;
 * private int age;
 * }
 *
 * The Person class is automatically mapped to the table named "person",
 * which might look like this:
 *
 * CREATE TABLE person (
 * id integer primary key autoincrement,
 * age integer,
 * name text
 * );
</pre> *
 *
 * @author Tony Green
 * @since 2.0
 */
open class LitePalSupport
/**
 * Disable developers to create instance of LitePalSupport directly. They
 * should inherit this class with subclasses and operate on them.
 */
protected constructor() {
    /**
     * Get the baseObjId of this model if it's useful for developers. It's for
     * system use usually. Do not try to assign or modify it.
     *
     * @return The base object id.
     */
    /**
     * The identify of each model. LitePal will generate the value
     * automatically. Do not try to assign or modify it.
     */
    @JvmField
    var baseObjId: Long = 0

    /**
     * A map contains all the associated models' id with M2O or O2O
     * associations. Each corresponding table of these models contains a foreign
     * key column.
     */
    val associatedModelsMapWithFK: MutableMap<String, MutableSet<Long>> by lazy {
        HashMap<String, MutableSet<Long>>()
    }
    /**
     * Get the associated model's map of self model. It can be used for
     * associations actions of CRUD. The key is the name of associated model's
     * table. The value is the id of associated model.
     *
     * @return An associated model's map to save self model with foreign key.
     */
    /**
     * A map contains all the associated models' id with M2O or O2O association.
     * Each corresponding table of these models doesn't contain foreign key
     * column. Instead self model has a foreign key column in the corresponding
     * table.
     */
    val associatedModelsMapWithoutFK: MutableMap<String, Long>? by lazy {
        HashMap()
    }

    /**
     * A map contains all the associated models' id with M2M association.
     */
    val associatedModelsMapForJoinTable: MutableMap<String, MutableList<Long>> by lazy {
        HashMap<String, MutableList<Long>>()
    }
    /**
     * Get the foreign key name list to clear foreign key value in current
     * model's table.
     *
     * @return The list of foreign key names to clear in current model's table.
     */
    /**
     * When updating a model and the associations breaks between current model
     * and others, if current model holds a foreign key, it need to be cleared.
     * This list holds all the foreign key names that need to clear.
     */
    var listToClearSelfFK: MutableList<String>? = null
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }
        private set
    /**
     * Get the associated table names list which need to clear their foreign key
     * values.
     *
     * @return The list with associated table names to clear foreign key values.
     */
    /**
     * When updating a model and the associations breaks between current model
     * and others, clear all the associated models' foreign key value if it
     * exists. This list holds all the associated table names that need to
     * clear.
     */
    var listToClearAssociatedFK: MutableList<String>? = null
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }
        private set
    /**
     * Get the list which holds all field names to update them into default
     * value of model in database.
     *
     * @return List holds all the field names which need to be updated into
     * default value of model.
     */
    /**
     * A list holds all the field names which need to be updated into default
     * value of model.
     */
    var fieldsToSetToDefault: MutableList<String>? = null
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }
        private set

    /**
     * Deletes the record in the database. The record must be saved already.<br></br>
     * The data in other tables which is referenced with the record will be
     * removed too.
     *
     * <pre>
     * Person person;
     * ....
     * if (person.isSaved()) {
     * person.delete();
     * }
    </pre> *
     *
     * @return The number of rows affected. Including cascade delete rows.
     */
    fun delete(): Int {
        return reentrantLock.withLock {
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val deleteHandler = DeleteHandler(db)
                val rowsAffected = deleteHandler.onDelete(this@LitePalSupport)
                baseObjId = 0
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
    fun deleteAsync(): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = delete()
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Updates the corresponding record by id. Use setXxx to decide which
     * columns to update.
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Jim&quot;);
     * person.update(1);
    </pre> *
     *
     * This means that the name of record 1 will be updated into Jim.<br></br>
     *
     * **Note: ** 1. If you set a default value to a field, the corresponding
     * column won't be updated. Use [.setToDefault] to update
     * columns into default value. 2. This method couldn't update foreign key in
     * database. So do not use setXxx to set associations between models.
     *
     * @param id
     * Which record to update.
     * @return The number of rows affected.
     */
    fun update(id: Long): Int {
        return reentrantLock.withLock {
            val db = Connector.getDatabase()
            db.beginTransaction()
            return@withLock try {
                val updateHandler = UpdateHandler(Connector.getDatabase())
                val rowsAffected = updateHandler.onUpdate(this@LitePalSupport, id)
                fieldsToSetToDefault!!.clear()
                db.setTransactionSuccessful()
                rowsAffected
            } catch (e: Exception) {
                throw LitePalSupportException(e.message, e)
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
    fun updateAsync(id: Long): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = update(id)
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
     * Person person = new Person();
     * person.setName(&quot;Jim&quot;);
     * person.updateAll(&quot;name = ?&quot;, &quot;Tom&quot;);
    </pre> *
     *
     * This means that all the records which name is Tom will be updated into
     * Jim.<br></br>
     *
     * **Note: ** 1. If you set a default value to a field, the corresponding
     * column won't be updated. Use [.setToDefault] to update
     * columns into default value. 2. This method couldn't update foreign key in
     * database. So do not use setXxx to set associations between models.
     *
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
    fun updateAll(vararg conditions: String?): Int {
        return reentrantLock.withLock {
            val db = Connector.getDatabase()
            db.beginTransaction()
            return@withLock try {
                val updateHandler = UpdateHandler(Connector.getDatabase())
                val rowsAffected = updateHandler.onUpdateAll(this@LitePalSupport, *conditions)
                fieldsToSetToDefault!!.clear()
                db.setTransactionSuccessful()
                rowsAffected
            } catch (e: Exception) {
                throw LitePalSupportException(e.message, e)
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
    fun updateAllAsync(vararg conditions: String?): UpdateOrDeleteExecutor {
        val executor = UpdateOrDeleteExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val rowsAffected = updateAll(*conditions)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(rowsAffected) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Saves the model. <br></br>
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Tom&quot;);
     * person.setAge(22);
     * person.save();
    </pre> *
     *
     * If the model is a new record gets created in the database, otherwise the
     * existing record gets updated.<br></br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be **rolled back**. <br></br>
     * If the model has a field named id or _id and field type is int or long,
     * the id value generated by database will assign to it after the model is
     * saved.<br></br>
     * Note that if the associated models of this model is already saved. The
     * associations between them will be built automatically in database after
     * it saved.
     *
     * @return If the model is saved successfully, return true. Any exception
     * happens, return false.
     */
    fun save(): Boolean {
        return try {
            saveThrows()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun saveAsync(): SaveExecutor {
        val executor = SaveExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val success = save()
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(success) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Saves the model. <br></br>
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Tom&quot;);
     * person.setAge(22);
     * person.saveThrows();
    </pre> *
     *
     * If the model is a new record gets created in the database, otherwise the
     * existing record gets updated.<br></br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be **rolled back** and throws
     * [LitePalSupportException]<br></br>
     * If the model has a field named id or _id and field type is int or long,
     * the id value generated by database will assign to it after the model is
     * saved.<br></br>
     * Note that if the associated models of this model is already saved. The
     * associations between them will be built automatically in database after
     * it saved.
     *
     * @throws LitePalSupportException
     */
    fun saveThrows() {
        return reentrantLock.withLock {
            val db = Connector.getDatabase()
            db.beginTransaction()
            try {
                val saveHandler = SaveHandler(db)
                saveHandler.onSave(this@LitePalSupport)
                clearAssociatedData()
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                throw LitePalSupportException(e.message, e)
            } finally {
                db.endTransaction()
            }

        }
    }

    /**
     * Save the model if the conditions data not exist, or update the matching models if the conditions data exist. <br></br>
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Tom&quot;);
     * person.setAge(22);
     * person.saveOrUpdate(&quot;name = ?&quot;, &quot;Tom&quot;);
    </pre> *
     *
     * If person table doesn't have a name with Tom, a new record gets created in the database,
     * otherwise all records which names are Tom will be updated.<br></br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be **rolled back**. <br></br>
     * If the model has a field named id or _id and field type is int or long,
     * the id value generated by database will assign to it after the model is
     * saved.<br></br>
     * Note that if the associated models of this model is already saved. The
     * associations between them will be built automatically in database after
     * it saved.
     *
     * @param conditions
     * A string array representing the WHERE part of an SQL
     * statement. First parameter is the WHERE clause to apply when
     * updating. The way of specifying place holders is to insert one
     * or more question marks in the SQL. The first question mark is
     * replaced by the second element of the array, the next question
     * mark by the third, and so on. Passing empty string will update
     * all rows.
     * @return If the model saved or updated successfully, return true. Otherwise return false.
     */
    fun saveOrUpdate(vararg conditions: String): Boolean {
        return reentrantLock.withLock {
            if (conditions == null || conditions.size == 0) {
                return save()
            }
            val list = where(*conditions).find(javaClass) as List<LitePalSupport>
            return@withLock if (list.isEmpty()) {
                save()
            } else {
                val db = Connector.getDatabase()
                db.beginTransaction()
                try {
                    for (support in list) {
                        baseObjId = support.baseObjId
                        val saveHandler = SaveHandler(db)
                        saveHandler.onSave(this@LitePalSupport)
                        clearAssociatedData()
                    }
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
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated("")
    fun saveOrUpdateAsync(vararg conditions: String): SaveExecutor {
        val executor = SaveExecutor()
        val runnable = Runnable {
            reentrantLock.withLock {
                val success = saveOrUpdate(*conditions)
                if (executor.listener != null) {
                    handler.post { executor.listener.onFinish(success) }
                }

            }
        }
        executor.submit(runnable)
        return executor
    }

    /**
     * Current model is saved or not.
     *
     * @return If saved return true, or return false.
     */
    val isSaved: Boolean
        get() = baseObjId > 0

    /**
     * It model is saved, clear the saved state and model becomes unsaved. Otherwise nothing will happen.
     */
    fun clearSavedState() {
        baseObjId = 0
    }

    /**
     * When updating database with [LitePalSupport.update], you must
     * use this method to update a field into default value. Use setXxx with
     * default value of the model won't update anything. <br></br>
     *
     * @param fieldName
     * The name of field to update into default value.
     */
    fun setToDefault(fieldName: String) {
        fieldsToSetToDefault!!.add(fieldName)
    }

    /**
     * Assigns value to baseObjId. This will override the original value. **Never call this method
     * unless you know exactly what you are doing.**
     * @param baseObjId
     * Assigns value to baseObjId.
     */
    fun assignBaseObjId(baseObjId: Long) {
        this.baseObjId = baseObjId
    }

    /**
     * Get the full class name of self.
     *
     * @return The full class name of self.
     */
    val className: String
        get() = javaClass.name

    /**
     * Get the corresponding table name of current model.
     *
     * @return The corresponding table name of current model.
     */
    val tableName: String
        get() = BaseUtility.changeCase(DBUtility.getTableNameByClassName(className))

    /**
     * Add the id of an associated model into self model's associatedIdsWithFK
     * map. The associated model has a foreign key column in the corresponding
     * table.
     *
     * @param associatedTableName
     * The table name of associated model.
     * @param associatedId
     * The [.baseObjId] of associated model after it is saved.
     */
    fun addAssociatedModelWithFK(associatedTableName: String, associatedId: Long) {
        var associatedIdsWithFKSet = associatedModelsMapWithFK[associatedTableName]
        if (associatedIdsWithFKSet == null) {
            associatedIdsWithFKSet = HashSet()
            associatedIdsWithFKSet.add(associatedId)
            associatedModelsMapWithFK[associatedTableName] = associatedIdsWithFKSet
        } else {
            associatedIdsWithFKSet.add(associatedId)
        }
    }


    /**
     * Add the id of an associated model into self model's associatedIdsM2M map.
     *
     * @param associatedModelName
     * The name of associated model.
     * @param associatedId
     * The id of associated model.
     */
    fun addAssociatedModelForJoinTable(associatedModelName: String, associatedId: Long) {
        var associatedIdsM2MSet = associatedModelsMapForJoinTable[associatedModelName]
        if (associatedIdsM2MSet == null) {
            associatedIdsM2MSet = ArrayList()
            associatedIdsM2MSet.add(associatedId)
            associatedModelsMapForJoinTable[associatedModelName] = associatedIdsM2MSet
        } else {
            associatedIdsM2MSet.add(associatedId)
        }
    }

    /**
     * Add an empty Set into [.associatedModelsMapForJoinTable] with
     * associated model name as key. Might be useful when comes to update
     * intermediate join table.
     *
     * @param associatedModelName
     * The name of associated model.
     */
    fun addEmptyModelForJoinTable(associatedModelName: String) {
        var associatedIdsM2MSet = associatedModelsMapForJoinTable[associatedModelName]
        if (associatedIdsM2MSet == null) {
            associatedIdsM2MSet = ArrayList()
            associatedModelsMapForJoinTable[associatedModelName] = associatedIdsM2MSet
        }
    }

    /**
     * Add the id of an associated model into self model's association
     * collection. The associated model doesn't have a foreign key column in the
     * corresponding table. Instead self model has a foreign key column in the
     * corresponding table.
     *
     * @param associatedTableName
     * The simple class name of associated model.
     * @param associatedId
     * The [.baseObjId] of associated model after it is saved.
     */
    fun addAssociatedModelWithoutFK(associatedTableName: String, associatedId: Long) {
        associatedModelsMapWithoutFK!![associatedTableName] = associatedId
    }

    /**
     * Add a foreign key name into the clear list.
     *
     * @param foreignKeyName
     * The name of foreign key.
     */
    fun addFKNameToClearSelf(foreignKeyName: String) {
        val list = listToClearSelfFK!!
        if (!list.contains(foreignKeyName)) {
            list.add(foreignKeyName)
        }
    }

    /**
     * Add an associated table name into the list to clear.
     *
     * @param associatedTableName
     * The name of associated table.
     */
    fun addAssociatedTableNameToClearFK(associatedTableName: String) {
        val list = listToClearAssociatedFK!!
        if (!list.contains(associatedTableName)) {
            list.add(associatedTableName)
        }
    }

    /**
     * Clear all the data for storing associated models' data.
     */
    fun clearAssociatedData() {
        clearIdOfModelWithFK()
        clearIdOfModelWithoutFK()
        clearIdOfModelForJoinTable()
        clearFKNameList()
    }

    /**
     * Clear all the data in [.associatedModelsMapWithFK].
     */
    private fun clearIdOfModelWithFK() {
        for (associatedModelName in associatedModelsMapWithFK.keys) {
            associatedModelsMapWithFK[associatedModelName]!!.clear()
        }
        associatedModelsMapWithFK.clear()
    }

    /**
     * Clear all the data in [.associatedModelsMapWithoutFK].
     */
    private fun clearIdOfModelWithoutFK() {
        associatedModelsMapWithoutFK!!.clear()
    }

    /**
     * Clear all the data in [.associatedModelsMapForJoinTable].
     */
    private fun clearIdOfModelForJoinTable() {
        for (associatedModelName in associatedModelsMapForJoinTable.keys) {
            associatedModelsMapForJoinTable[associatedModelName]!!.clear()
        }
        associatedModelsMapForJoinTable.clear()
    }

    /**
     * Clear all the data in [.listToClearSelfFK].
     */
    private fun clearFKNameList() {
        listToClearSelfFK!!.clear()
        listToClearAssociatedFK!!.clear()
    }

    companion object {
        /**
         * Constant for MD5 encryption.
         */
        const val MD5 = "MD5"

        /**
         * Constant for AES encryption.
         */
        const val AES = "AES"
    }
}