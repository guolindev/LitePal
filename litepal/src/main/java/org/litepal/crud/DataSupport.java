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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.LitePal;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import java.util.Collection;
import java.util.List;

/**
 * DataSupport is deprecated and will be removed in the future release.
 * For model inheritance, use {@link LitePalSupport} instead.
 * For static CRUD method, use {@link LitePal} instead.
 *
 * @author Tony Green
 * @since 1.1
 */
@Deprecated
public class DataSupport extends LitePalSupport {

	/**
	 * This method is deprecated and will be removed in the future release. Use {@link LitePal#select(String...)} instead.
	 */
    @Deprecated
	public static synchronized ClusterQuery select(String... columns) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mColumns = columns;
		return cQuery;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#where(String...)} instead.
     */
    @Deprecated
	public static synchronized ClusterQuery where(String... conditions) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mConditions = conditions;
		return cQuery;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#order(String)} instead.
     */
    @Deprecated
	public static synchronized ClusterQuery order(String column) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mOrderBy = column;
		return cQuery;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#limit(int)} instead.
     */
    @Deprecated
	public static synchronized ClusterQuery limit(int value) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mLimit = String.valueOf(value);
		return cQuery;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#offset(int)} instead.
     */
    @Deprecated
	public static synchronized ClusterQuery offset(int value) {
		ClusterQuery cQuery = new ClusterQuery();
		cQuery.mOffset = String.valueOf(value);
		return cQuery;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#count(String)} instead.
     */
    @Deprecated
	public static synchronized int count(Class<?> modelClass) {
		return count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#countAsync(Class)} instead.
     */
    @Deprecated
    public static CountExecutor countAsync(final Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#count(String)} instead.
     */
    @Deprecated
	public static synchronized int count(String tableName) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.count(tableName);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#countAsync(String)} instead.
     */
    @Deprecated
    public static CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int count = count(tableName);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(count);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#average(Class, String)} instead.
     */
    @Deprecated
	public static synchronized double average(Class<?> modelClass, String column) {
		return average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#averageAsync(Class, String)} instead.
     */
    @Deprecated
    public static AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#average(String, String)} instead.
     */
    @Deprecated
	public static synchronized double average(String tableName, String column) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.average(tableName, column);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#averageAsync(String, String)} instead.
     */
    @Deprecated
    public static AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final double average = average(tableName, column);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(average);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#max(Class, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
		return max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#maxAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#max(String, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.max(tableName, columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#maxAsync(String, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#min(Class, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
		return min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#minAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#min(String, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.min(tableName, columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#minAsync(String, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#sum(Class, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
		return sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#sumAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#sum(String, String, Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
		ClusterQuery cQuery = new ClusterQuery();
		return cQuery.sum(tableName, columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#sumAsync(String, String, Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#find(Class, long)} instead.
     */
    @Deprecated
	public static synchronized <T> T find(Class<T> modelClass, long id) {
		return find(modelClass, id, false);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAsync(Class, long)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findAsync(Class<T> modelClass, long id) {
        return findAsync(modelClass, id, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#find(Class, long, boolean)} instead.
     */
    @Deprecated
	public static synchronized <T> T find(Class<T> modelClass, long id, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFind(modelClass, id, isEager);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAsync(Class, long, boolean)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findAsync(final Class<T> modelClass, final long id, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = find(modelClass, id, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findFirst(Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T findFirst(Class<T> modelClass) {
		return findFirst(modelClass, false);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findFirstAsync(Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findFirst(Class, boolean)} instead.
     */
    @Deprecated
	public static synchronized <T> T findFirst(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindFirst(modelClass, isEager);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findFirstAsync(Class, boolean)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findLast(Class)} instead.
     */
    @Deprecated
	public static synchronized <T> T findLast(Class<T> modelClass) {
		return findLast(modelClass, false);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findLastAsync(Class)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findLast(Class, boolean)} instead.
     */
    @Deprecated
	public static synchronized <T> T findLast(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindLast(modelClass, isEager);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findLastAsync(Class, boolean)} instead.
     */
    @Deprecated
    public static <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final T t = findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAll(Class, long...)} instead.
     */
    @Deprecated
	public static synchronized <T> List<T> findAll(Class<T> modelClass, long... ids) {
		return findAll(modelClass, false, ids);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAllAsync(Class, long...)} instead.
     */
    @Deprecated
    public static <T> FindMultiExecutor findAllAsync(Class<T> modelClass, long... ids) {
        return findAllAsync(modelClass, false, ids);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAll(Class, boolean, long...)} instead.
     */
    @Deprecated
	public static synchronized <T> List<T> findAll(Class<T> modelClass, boolean isEager,
			long... ids) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFindAll(modelClass, isEager, ids);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findAllAsync(Class, boolean, long...)} instead.
     */
    @Deprecated
    public static <T> FindMultiExecutor findAllAsync(final Class<T> modelClass, final boolean isEager, final long... ids) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final List<T> t = findAll(modelClass, isEager, ids);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(t);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#findBySQL(String...)} instead.
     */
    @Deprecated
	public static synchronized Cursor findBySQL(String... sql) {
		BaseUtility.checkConditionsCorrect(sql);
		if (sql == null) {
			return null;
		}
		if (sql.length <= 0) {
			return null;
		}
		String[] selectionArgs;
		if (sql.length == 1) {
			selectionArgs = null;
		} else {
			selectionArgs = new String[sql.length - 1];
			System.arraycopy(sql, 1, selectionArgs, 0, sql.length - 1);
		}
		return Connector.getDatabase().rawQuery(sql[0], selectionArgs);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#delete(Class, long)} instead.
     */
    @Deprecated
	public static synchronized int delete(Class<?> modelClass, long id) {
		int rowsAffected = 0;
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			DeleteHandler deleteHandler = new DeleteHandler(db);
			rowsAffected = deleteHandler.onDelete(modelClass, id);
			db.setTransactionSuccessful();
			return rowsAffected;
		} finally {
			db.endTransaction();
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#deleteAsync(Class, long)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor deleteAsync(final Class<?> modelClass, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = delete(modelClass, id);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#deleteAll(Class, String...)} instead.
     */
    @Deprecated
	public static synchronized int deleteAll(Class<?> modelClass, String... conditions) {
		DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
		return deleteHandler.onDeleteAll(modelClass, conditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#deleteAllAsync(Class, String...)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor deleteAllAsync(final Class<?> modelClass, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = deleteAll(modelClass, conditions);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#deleteAll(String, String...)} instead.
     */
    @Deprecated
	public static synchronized int deleteAll(String tableName, String... conditions) {
		DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
		return deleteHandler.onDeleteAll(tableName, conditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#deleteAllAsync(String, String...)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor deleteAllAsync(final String tableName, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = deleteAll(tableName, conditions);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#update(Class, ContentValues, long)} instead.
     */
    @Deprecated
	public static synchronized int update(Class<?> modelClass, ContentValues values, long id) {
		UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
		return updateHandler.onUpdate(modelClass, id, values);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#updateAsync(Class, ContentValues, long)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAsync(final Class<?> modelClass, final ContentValues values, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = update(modelClass, values, id);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#updateAll(Class, ContentValues, String...)} instead.
     */
    @Deprecated
	public static synchronized int updateAll(Class<?> modelClass, ContentValues values,
			String... conditions) {
		return updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.getName())), values, conditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#updateAllAsync(Class, ContentValues, String...)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(Class<?> modelClass, ContentValues values, String... conditions) {
        return updateAllAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.getName())), values, conditions);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#updateAll(String, ContentValues, String...)} instead.
     */
    @Deprecated
	public static synchronized int updateAll(String tableName, ContentValues values,
			String... conditions) {
		UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
		return updateHandler.onUpdateAll(tableName, values, conditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#updateAllAsync(String, ContentValues, String...)} instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(final String tableName, final ContentValues values, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = updateAll(tableName, values, conditions);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#saveAll(Collection)} instead.
     */
    @Deprecated
	public static synchronized <T extends DataSupport> void saveAll(Collection<T> collection) {
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			SaveHandler saveHandler = new SaveHandler(db);
			saveHandler.onSaveAll(collection);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage(), e);
		} finally {
			db.endTransaction();
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#saveAllAsync(Collection)} (Class)} instead.
     */
    @Deprecated
    public static <T extends DataSupport> SaveExecutor saveAllAsync(final Collection<T> collection) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    boolean success;
                    try {
                        saveAll(collection);
                        success = true;
                    } catch (Exception e) {
                        success = false;
                    }
                    final boolean result = success;
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                executor.getListener().onFinish(result);
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
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#markAsDeleted(Collection)} instead.
     */
    @Deprecated
    public static <T extends DataSupport> void markAsDeleted(Collection<T> collection) {
        for (T t : collection) {
            t.clearSavedState();
        }
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#isExist(Class, String...)} instead.
     */
    @Deprecated
    public static <T> boolean isExist(Class<T> modelClass, String... conditions) {
        if (conditions == null) {
            return false;
        }
        return where(conditions).count(modelClass) > 0;
    }

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public synchronized int delete() {
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

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public UpdateOrDeleteExecutor deleteAsync() {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = delete();
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public synchronized int update(long id) {
		try {
			UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
			int rowsAffected = updateHandler.onUpdate(this, id);
			getFieldsToSetToDefault().clear();
			return rowsAffected;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage(), e);
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public UpdateOrDeleteExecutor updateAsync(final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = update(id);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public synchronized int updateAll(String... conditions) {
		try {
			UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
			int rowsAffected = updateHandler.onUpdateAll(this, conditions);
			getFieldsToSetToDefault().clear();
			return rowsAffected;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage(), e);
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public UpdateOrDeleteExecutor updateAllAsync(final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = updateAll(conditions);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public synchronized boolean save() {
		try {
			saveThrows();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public SaveExecutor saveAsync() {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final boolean success = save();
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public synchronized void saveThrows() {
		SQLiteDatabase db = Connector.getDatabase();
		db.beginTransaction();
		try {
			SaveHandler saveHandler = new SaveHandler(db);
			saveHandler.onSave(this);
			clearAssociatedData();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage(), e);
		} finally {
			db.endTransaction();
		}
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public synchronized boolean saveOrUpdate(String... conditions) {
        if (conditions == null) {
            return save();
        }
        List<DataSupport> list = (List<DataSupport>) where(conditions).find(getClass());
        if (list.isEmpty()) {
            return save();
        } else {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                for (DataSupport dataSupport : list) {
                    baseObjId = dataSupport.getBaseObjId();
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

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public SaveExecutor saveOrUpdateAsync(final String... conditions) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (DataSupport.class) {
                    final boolean success = saveOrUpdate(conditions);
                    if (executor.getListener() != null) {
                        LitePal.getHandler().post(new Runnable() {
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
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public boolean isSaved() {
		return baseObjId > 0;
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public void clearSavedState() {
        baseObjId = 0;
    }

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	public void setToDefault(String fieldName) {
		getFieldsToSetToDefault().add(fieldName);
	}

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
    public void assignBaseObjId(int baseObjId) {
        this.baseObjId = baseObjId;
    }

    /**
     * This method is deprecated and will be removed in the future release. Please inherits {@link LitePalSupport} instead of {@link DataSupport} for your models .
     */
    @Deprecated
	protected DataSupport() {
	}

}