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

package org.litepal;

import android.text.TextUtils;

import org.litepal.crud.LitePalSupport;
import org.litepal.crud.QueryHandler;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import java.util.List;

/**
 * Allows developers to query tables with fluent style.
 *
 * @author Tony Green
 * @since 2.0
 */
public class FluentQuery {

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
	FluentQuery() {
	}

	/**
	 * Declaring to query which columns in table.
	 *
	 * <pre>
	 * LitePal.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
	 * </pre>
	 *
	 * This will find all rows with name and age columns in Person table.
	 *
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 *
	 * @return A ClusterQuery instance.
	 */
	public FluentQuery select(String... columns) {
        mColumns = columns;
        return this;
	}

	/**
	 * Declaring to query which rows in table.
	 *
	 * <pre>
	 * LitePal.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
	 * </pre>
	 *
	 * This will find rows which name is Tom or age greater than 14 in Person
	 * table.
	 *
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return A ClusterQuery instance.
	 */
	public FluentQuery where(String... conditions) {
        mConditions = conditions;
        return this;
	}

	/**
	 * Declaring how to order the rows queried from table.
	 *
	 * <pre>
	 * LitePal.order(&quot;name desc&quot;).find(Person.class);
	 * </pre>
	 *
	 * This will find all rows in Person table sorted by name with inverted
	 * order.
	 *
	 * @param column
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @return A ClusterQuery instance.
	 */
	public FluentQuery order(String column) {
        mOrderBy = column;
        return this;
	}

	/**
	 * Limits the number of rows returned by the query.
	 *
	 * <pre>
	 * LitePal.limit(2).find(Person.class);
	 * </pre>
	 *
	 * This will find the top 2 rows in Person table.
	 *
	 * @param value
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @return A ClusterQuery instance.
	 */
	public FluentQuery limit(int value) {
        mLimit = String.valueOf(value);
        return this;
	}

	/**
	 * Declaring the offset of rows returned by the query. This method must be
	 * used with {@link #limit(int)}, or nothing will return.
	 *
	 * <pre>
	 * LitePal.limit(1).offset(2).find(Person.class);
	 * </pre>
	 *
	 * This will find the third row in Person table.
	 *
	 * @param value
	 *            The offset amount of rows returned by the query.
	 * @return A ClusterQuery instance.
	 */
	public FluentQuery offset(int value) {
        mOffset = String.valueOf(value);
        return this;
	}

	/**
	 * Finds multiple records by the cluster parameters. You can use the below
	 * way to finish a complicated query:
	 *
	 * <pre>
	 * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(1).offset(2)
	 * 		.find(Person.class);
	 * </pre>
	 *
	 * You can also do the same job with SQLiteDatabase like this:
	 *
	 * <pre>
	 * getSQLiteDatabase().query(&quot;Person&quot;, &quot;name&quot;, &quot;age &gt; ?&quot;, new String[] { &quot;14&quot; }, null, null, &quot;age&quot;,
	 * 		&quot;2,1&quot;);
	 * </pre>
	 *
	 * Obviously, the first way is much more semantic.<br>
	 * Note that the associated models won't be loaded by default considering
	 * the efficiency, but you can do that by using
	 * {@link FluentQuery#find(Class, boolean)}.
	 *
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @return An object list with founded data from database, or an empty list.
	 */
	public <T> List<T> find(Class<T> modelClass) {
        return find(modelClass, false);
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindMultiExecutor<T> findAsync(final Class<T> modelClass) {
        return findAsync(modelClass, false);
    }

	/**
	 * It is mostly same as {@link FluentQuery#find(Class)} but an isEager
	 * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
	 *
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param isEager
	 *            True to load the associated models, false not.
	 * @return An object list with founded data from database, or an empty list.
	 */
	public <T> List<T> find(Class<T> modelClass, boolean isEager) {
        synchronized (LitePalSupport.class) {
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
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindMultiExecutor<T> findAsync(final Class<T> modelClass, final boolean isEager) {
        final FindMultiExecutor<T> executor = new FindMultiExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final List<T> t = find(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
     * Finds the first record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2)
     * 		.findFirst(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link FluentQuery#findFirst(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    public <T> T findFirst(Class<T> modelClass) {
        return findFirst(modelClass, false);
    }

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@link FluentQuery#findFirst(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    public <T> T findFirst(Class<T> modelClass, boolean isEager) {
        synchronized (LitePalSupport.class) {
        	String limitTemp = mLimit;
        	if (!"0".equals(mLimit)) { // If mLimit not equals to 0, set mLimit to 1 to find the first record.
        		mLimit = "1";
			}
            List<T> list = find(modelClass, isEager);
        	mLimit = limitTemp; // Don't forget to change it back after finding operation.
            if (list.size() > 0) {
				if (list.size() != 1) throw new LitePalSupportException("Found multiple records while only one record should be found at most.");
                return list.get(0);
            }
            return null;
        }
    }

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
     * Finds the last record by the cluster parameters. You can use the below
     * way to finish a complicated query:
     *
     * <pre>
     * LitePal.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, &quot;14&quot;).order(&quot;age&quot;).limit(10).offset(2)
     * 		.findLast(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link FluentQuery#findLast(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with founded data from database, or null.
     */
    public <T> T findLast(Class<T> modelClass) {
        return findLast(modelClass, false);
    }

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@link FluentQuery#findLast(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with founded data from database, or null.
     */
    public <T> T findLast(Class<T> modelClass, boolean isEager) {
        synchronized (LitePalSupport.class) {
			String orderByTemp = mOrderBy;
			String limitTemp = mLimit;
        	if (TextUtils.isEmpty(mOffset) && TextUtils.isEmpty(mLimit)) { // If mOffset or mLimit is specified, we can't use the strategy in this block to speed up finding.
				if (TextUtils.isEmpty(mOrderBy)) {
					// If mOrderBy is null, we can use id desc order, then the first record will be the record value where want to find.
					mOrderBy = "id desc";
				} else {
					// If mOrderBy is not null, check if it ends with desc.
					if (mOrderBy.endsWith(" desc")) {
						// If mOrderBy ends with desc, then the last record of desc order will be the first record of asc order, so we remove the desc.
						mOrderBy = mOrderBy.replace(" desc", "");
					} else {
						// If mOrderBy not ends with desc, then the last record of asc order will be the first record of desc order, so we add the desc.
						mOrderBy += " desc";
					}
				}
				if (!"0".equals(mLimit)) {
					mLimit = "1";
				}
			}
            List<T> list = find(modelClass, isEager);
        	mOrderBy = orderByTemp;
        	mLimit = limitTemp;
            int size = list.size();
            if (size > 0) {
                return list.get(size - 1);
            }
            return null;
        }
    }

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
	 * Count the records.
	 *
	 * <pre>
	 * LitePal.count(Person.class);
	 * </pre>
	 *
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(Person.class);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @return Count of the specified table.
	 */
	public int count(Class<?> modelClass) {
        return count(BaseUtility.changeCase(modelClass.getSimpleName()));
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

	/**
	 * Count the records.
	 *
	 * <pre>
	 * LitePal.count(&quot;person&quot;);
	 * </pre>
	 *
	 * This will count all rows in person table.<br>
	 * You can also specify a where clause when counting.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).count(&quot;person&quot;);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @return Count of the specified table.
	 */
	public int count(String tableName) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onCount(tableName, mConditions);
        }
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
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
                        Operator.getHandler().post(new Runnable() {
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
	 * Calculates the average value on a given column.
	 *
	 * <pre>
	 * LitePal.average(Person.class, &quot;age&quot;);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(Person.class, &quot;age&quot;);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public double average(Class<?> modelClass, String column) {
        return average(BaseUtility.changeCase(modelClass.getSimpleName()), column);
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

	/**
	 * Calculates the average value on a given column.
	 *
	 * <pre>
	 * LitePal.average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).average(&quot;person&quot;, &quot;age&quot;);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param column
	 *            The based on column to calculate.
	 * @return The average value on a given column.
	 */
	public double average(String tableName, String column) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onAverage(tableName, column, mConditions);
        }
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
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
                        Operator.getHandler().post(new Runnable() {
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
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.max(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        return max(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

	/**
	 * Calculates the maximum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.max(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).max(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The maximum value on a given column.
	 */
	public <T> T max(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onMax(tableName, columnName, mConditions, columnType);
        }
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.min(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        return min(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

	/**
	 * Calculates the minimum value on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.min(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).min(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The minimum value on a given column.
	 */
	public <T> T min(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onMin(tableName, columnName, mConditions, columnType);
        }
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.sum(Person.class, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(Person.class, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param modelClass
	 *            Which table to query from by class.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        return sum(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
	 * Calculates the sum of values on a given column. The value is returned
	 * with the same data type of the column.
	 *
	 * <pre>
	 * LitePal.sum(&quot;person&quot;, &quot;age&quot;, int.class);
	 * </pre>
	 *
	 * You can also specify a where clause when calculating.
	 *
	 * <pre>
	 * LitePal.where(&quot;age &gt; ?&quot;, &quot;15&quot;).sum(&quot;person&quot;, &quot;age&quot;, Integer.TYPE);
	 * </pre>
	 *
	 * @param tableName
	 *            Which table to query from.
	 * @param columnName
	 *            The based on column to calculate.
	 * @param columnType
	 *            The type of the based on column.
	 * @return The sum value on a given column.
	 */
	public <T> T sum(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onSum(tableName, columnName, mConditions, columnType);
        }
	}

	/**
	 * This method is deprecated and will be removed in the future releases.
	 * Handle async db operation in your own logic instead.
	 */
	@Deprecated
    public <T> FindExecutor<T> sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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