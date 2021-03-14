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

import android.database.sqlite.SQLiteDatabase;

import org.litepal.Operator;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LitePalSupport connects classes to SQLite database tables to establish an almost
 * zero-configuration persistence layer for applications. In the context of an
 * application, these classes are commonly referred to as models. Models can
 * also be connected to other models.<br>
 * LitePalSupport relies heavily on naming in that it uses class and association
 * names to establish mappings between respective database tables and foreign
 * key columns.<br>
 * Automated mapping between classes and tables, attributes and columns.
 * 
 * <pre>
 * public class Person extends LitePalSupport {
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
 * @since 2.0
 */
public class LitePalSupport {

    /**
     * Constant for MD5 encryption.
     */
    protected static final String MD5 = "MD5";

    /**
     * Constant for AES encryption.
     */
    protected static final String AES = "AES";

	/**
	 * The identify of each model. LitePal will generate the value
	 * automatically. Do not try to assign or modify it.
	 */
	long baseObjId;

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
	Map<String, List<Long>> associatedModelsMapForJoinTable;

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
	public int delete() {
	    synchronized (LitePalSupport.class) {
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
	}

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public UpdateOrDeleteExecutor deleteAsync() {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = delete();
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
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
	public int update(long id) {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
                int rowsAffected = updateHandler.onUpdate(this, id);
                getFieldsToSetToDefault().clear();
                db.setTransactionSuccessful();
                return rowsAffected;
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            } finally {
                db.endTransaction();
            }
        }
	}

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public UpdateOrDeleteExecutor updateAsync(final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = update(id);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
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
	 * <b>Note: </b> 1. If you set a default value to a field, the corresponding
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
	public int updateAll(String... conditions) {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
                int rowsAffected = updateHandler.onUpdateAll(this, conditions);
                getFieldsToSetToDefault().clear();
                db.setTransactionSuccessful();
                return rowsAffected;
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            } finally {
                db.endTransaction();
            }
        }
	}

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public UpdateOrDeleteExecutor updateAllAsync(final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = updateAll(conditions);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

	/**
	 * Saves the model. <br>
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Tom&quot;);
	 * person.setAge(22);
	 * person.save();
	 * </pre>
	 * 
	 * If the model is a new record gets created in the database, otherwise the
	 * existing record gets updated.<br>
	 * If saving process failed by any accident, the whole action will be
	 * cancelled and your database will be <b>rolled back</b>. <br>
	 * If the model has a field named id or _id and field type is int or long,
	 * the id value generated by database will assign to it after the model is
	 * saved.<br>
	 * Note that if the associated models of this model is already saved. The
	 * associations between them will be built automatically in database after
	 * it saved.
	 * 
	 * @return If the model is saved successfully, return true. Any exception
	 *         happens, return false.
	 */
	public boolean save() {
        try {
            saveThrows();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public SaveExecutor saveAsync() {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final boolean success = save();
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(success);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    /**
	 * Saves the model. <br>
	 * 
	 * <pre>
	 * Person person = new Person();
	 * person.setName(&quot;Tom&quot;);
	 * person.setAge(22);
	 * person.saveThrows();
	 * </pre>
	 * 
	 * If the model is a new record gets created in the database, otherwise the
	 * existing record gets updated.<br>
	 * If saving process failed by any accident, the whole action will be
	 * cancelled and your database will be <b>rolled back</b> and throws
	 * {@link LitePalSupportException}<br>
	 * If the model has a field named id or _id and field type is int or long,
	 * the id value generated by database will assign to it after the model is
	 * saved.<br>
	 * Note that if the associated models of this model is already saved. The
	 * associations between them will be built automatically in database after
	 * it saved.
	 * 
	 * @throws LitePalSupportException
	 */
	public void saveThrows() {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                SaveHandler saveHandler = new SaveHandler(db);
                saveHandler.onSave(this);
                clearAssociatedData();
                db.setTransactionSuccessful();
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            } finally {
                db.endTransaction();
            }
        }
	}

    /**
     * Save the model if the conditions data not exist, or update the matching models if the conditions data exist. <br>
     *
     * <pre>
     * Person person = new Person();
     * person.setName(&quot;Tom&quot;);
     * person.setAge(22);
     * person.saveOrUpdate(&quot;name = ?&quot;, &quot;Tom&quot;);
     * </pre>
     *
     * If person table doesn't have a name with Tom, a new record gets created in the database,
     * otherwise all records which names are Tom will be updated.<br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be <b>rolled back</b>. <br>
     * If the model has a field named id or _id and field type is int or long,
     * the id value generated by database will assign to it after the model is
     * saved.<br>
     * Note that if the associated models of this model is already saved. The
     * associations between them will be built automatically in database after
     * it saved.
     *
     * @param conditions
     *            A string array representing the WHERE part of an SQL
     *            statement. First parameter is the WHERE clause to apply when
     *            updating. The way of specifying place holders is to insert one
     *            or more question marks in the SQL. The first question mark is
     *            replaced by the second element of the array, the next question
     *            mark by the third, and so on. Passing empty string will update
     *            all rows.
     * @return If the model saved or updated successfully, return true. Otherwise return false.
     */
    @SuppressWarnings("unchecked")
    public boolean saveOrUpdate(String... conditions) {
        synchronized (LitePalSupport.class) {
            if (conditions == null || conditions.length == 0) {
                return save();
            }
            List<LitePalSupport> list = (List<LitePalSupport>) Operator.where(conditions).find(getClass());
            if (list.isEmpty()) {
                return save();
            } else {
                SQLiteDatabase db = Connector.getDatabase();
                db.beginTransaction();
                try {
                    for (LitePalSupport support : list) {
                        baseObjId = support.getBaseObjId();
                        SaveHandler saveHandler = new SaveHandler(db);
                        saveHandler.onSave(this);
                        clearAssociatedData();
                    }
                    db.setTransactionSuccessful();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    db.endTransaction();
                }
            }
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public SaveExecutor saveOrUpdateAsync(final String... conditions) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final boolean success = saveOrUpdate(conditions);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(success);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
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
	 * When updating database with {@link LitePalSupport#update(long)}, you must
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
     *          Assigns value to baseObjId.
     */
    public void assignBaseObjId(long baseObjId) {
        this.baseObjId = baseObjId;
    }

	/**
	 * Disable developers to create instance of LitePalSupport directly. They
	 * should inherit this class with subclasses and operate on them.
	 */
	protected LitePalSupport() {
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
		List<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(
				associatedModelName);
		if (associatedIdsM2MSet == null) {
			associatedIdsM2MSet = new ArrayList<Long>();
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
		List<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(
				associatedModelName);
		if (associatedIdsM2MSet == null) {
			associatedIdsM2MSet = new ArrayList<Long>();
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
	Map<String, List<Long>> getAssociatedModelsMapForJoinTable() {
		if (associatedModelsMapForJoinTable == null) {
			associatedModelsMapForJoinTable = new HashMap<String, List<Long>>();
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