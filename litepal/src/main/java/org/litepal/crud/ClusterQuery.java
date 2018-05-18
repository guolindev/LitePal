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

import java.util.List;

import org.litepal.LitePal;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

/**
 * Allows developers to query tables with cluster style.
 * This class is deprecated. Use {@link org.litepal.FluentQuery} instead.
 * 
 * @author Tony Green
 * @since 1.1
 */
@Deprecated
public class ClusterQuery {

	/**
	 * Representing the selected columns in SQL.
	 */
	String[] mColumns;

	/**
	 * Representing the where clause in SQL.
	 */
	String[] mConditions;

	/**
	 * Representing the order by clause in SQL.
	 */
	String mOrderBy;

	/**
	 * Representing the limit clause in SQL.
	 */
	String mLimit;

	/**
	 * Representing the offset in SQL.
	 */
	String mOffset;

	/**
	 * Do not allow to create instance by developers.
	 */
	ClusterQuery() {
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#select(String...)} instead.
     */
    @Deprecated
	public ClusterQuery select(String... columns) {
		mColumns = columns;
		return this;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#where(String...)} instead.
     */
    @Deprecated
	public ClusterQuery where(String... conditions) {
		mConditions = conditions;
		return this;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#order(String)} instead.
     */
    @Deprecated
	public ClusterQuery order(String column) {
		mOrderBy = column;
		return this;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#limit(int)} instead.
     */
    @Deprecated
	public ClusterQuery limit(int value) {
		mLimit = String.valueOf(value);
		return this;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link LitePal#offset(int)} instead.
     */
    @Deprecated
	public ClusterQuery offset(int value) {
		mOffset = String.valueOf(value);
		return this;
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#find(Class)} instead.
     */
    @Deprecated
	public <T> List<T> find(Class<T> modelClass) {
		return find(modelClass, false);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findAsync(Class)} instead.
     */
    @Deprecated
    public <T> FindMultiExecutor findAsync(final Class<T> modelClass) {
        return findAsync(modelClass, false);
    }
    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#find(Class, boolean)} instead.
     */
    @Deprecated
	public synchronized <T> List<T> find(Class<T> modelClass, boolean isEager) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		String limit;
		if (mOffset == null) {
			limit = mLimit;
		} else {
			if (mLimit == null) {
				mLimit = "0";
			}
			limit = mOffset + "," + mLimit;
		}
		return queryHandler.onFind(modelClass, mColumns, mConditions, mOrderBy, limit, isEager);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findAsync(Class, boolean)} instead.
     */
    @Deprecated
    public <T> FindMultiExecutor findAsync(final Class<T> modelClass, final boolean isEager) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final List<T> t = find(modelClass, isEager);
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findFirst(Class)} instead.
     */
    @Deprecated
    public <T> T findFirst(Class<T> modelClass) {
        return findFirst(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findFirstAsync(Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findFirst(Class, boolean)} instead.
     */
    @Deprecated
    public <T> T findFirst(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findFirstAsync(Class, boolean)} instead.
     */
    @Deprecated
    public <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findLast(Class)} instead.
     */
    @Deprecated
    public <T> T findLast(Class<T> modelClass) {
        return findLast(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findLastAsync(Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findLast(Class, boolean)} instead.
     */
    @Deprecated
    public <T> T findLast(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        int size = list.size();
        if (size > 0) {
            return list.get(size - 1);
        }
        return null;
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.FluentQuery#findLastAsync(Class, boolean)} instead.
     */
    @Deprecated
    public <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#count(Class)} instead.
     */
    @Deprecated
	public synchronized int count(Class<?> modelClass) {
		return count(BaseUtility.changeCase(modelClass.getSimpleName()));
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#countAsync(Class)} instead.
     */
    @Deprecated
    public CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#count(String)} instead.
     */
    @Deprecated
	public synchronized int count(String tableName) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onCount(tableName, mConditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#countAsync(String)} instead.
     */
    @Deprecated
    public CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#average(Class, String)} instead.
     */
    @Deprecated
	public synchronized double average(Class<?> modelClass, String column) {
		return average(BaseUtility.changeCase(modelClass.getSimpleName()), column);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#averageAsync(Class, String)} instead.
     */
    @Deprecated
    public AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#average(String, String)} instead.
     */
    @Deprecated
	public synchronized double average(String tableName, String column) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onAverage(tableName, column, mConditions);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#averageAsync(String, String)} instead.
     */
    @Deprecated
    public AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#max(Class, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
		return max(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#maxAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#max(String, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onMax(tableName, columnName, mConditions, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#maxAsync(String, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#min(Class, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
		return min(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#minAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#min(String, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onMin(tableName, columnName, mConditions, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#minAsync(String, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#sum(Class, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
		return sum(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#sumAsync(Class, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#sum(String, String, Class)} instead.
     */
    @Deprecated
	public synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onSum(tableName, columnName, mConditions, columnType);
	}

    /**
     * This method is deprecated and will be removed in the future release. Use {@link org.litepal.LitePal#sumAsync(String, String, Class)} instead.
     */
    @Deprecated
    public <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
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

}