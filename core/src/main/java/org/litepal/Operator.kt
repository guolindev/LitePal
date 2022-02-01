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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.litepal.crud.DeleteHandler;
import org.litepal.crud.LitePalSupport;
import org.litepal.crud.QueryHandler;
import org.litepal.crud.SaveHandler;
import org.litepal.crud.UpdateHandler;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalConfig;
import org.litepal.parser.LitePalParser;
import org.litepal.tablemanager.Connector;
import org.litepal.tablemanager.callback.DatabaseListener;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.SharedUtil;
import org.litepal.util.cipher.CipherUtil;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling {@link #initialize(Context)} method to make LitePal ready to
 * work. Also you can switch the using database by calling {@link #use(LitePalDB)} and {@link #useDefault()}
 * methods.
 *
 * @author Tony Green
 * @since 2.1
 */
public class Operator {

    private static Handler handler = new Handler(Looper.getMainLooper());

    private static DatabaseListener dbListener = null;

    /**
     * Get the main thread handler. You don't need this method. It's used by framework only.
     * @return Main thread handler.
     */
    public static Handler getHandler() {
        return handler;
    }

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * 		Application context.
     */
    public static void initialize(Context context) {
        LitePalApplication.sContext = context;
    }

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    public static SQLiteDatabase getDatabase() {
        return Connector.getDatabase();
    }

    /**
     * Begins a transaction in EXCLUSIVE mode.
     */
    public static void beginTransaction() {
        getDatabase().beginTransaction();
    }

    /**
     * End a transaction.
     */
    public static void endTransaction() {
        getDatabase().endTransaction();
    }

    /**
     * Marks the current transaction as successful. Do not do any more database work between calling this and calling endTransaction.
     * Do as little non-database work as possible in that situation too.
     * If any errors are encountered between this and endTransaction the transaction will still be committed.
     */
    public static void setTransactionSuccessful() {
        getDatabase().setTransactionSuccessful();
    }

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     *          The database to switch to.
     */
    public static void use(LitePalDB litePalDB) {
        synchronized (LitePalSupport.class) {
            LitePalAttr litePalAttr = LitePalAttr.getInstance();
            litePalAttr.setDbName(litePalDB.getDbName());
            litePalAttr.setVersion(litePalDB.getVersion());
            litePalAttr.setStorage(litePalDB.getStorage());
            litePalAttr.setClassNames(litePalDB.getClassNames());
            // set the extra key name only when use database other than default or litepal.xml not exists
            if (!isDefaultDatabase(litePalDB.getDbName())) {
                litePalAttr.setExtraKeyName(litePalDB.getDbName());
                litePalAttr.setCases("lower");
            }
            Connector.clearLitePalOpenHelperInstance();
        }
    }

    /**
     * Switch the using database to default with configuration by litepal.xml.
     */
    public static void useDefault() {
        synchronized (LitePalSupport.class) {
            LitePalAttr.clearInstance();
            Connector.clearLitePalOpenHelperInstance();
        }
    }

    /**
     * Delete the specified database.
     * @param dbName
     *          Name of database to delete.
     * @return True if delete success, false otherwise.
     */
    public static boolean deleteDatabase(String dbName) {
        synchronized (LitePalSupport.class) {
            if (!TextUtils.isEmpty(dbName)) {
                if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                    dbName = dbName + Const.Config.DB_NAME_SUFFIX;
                }
                File dbFile = LitePalApplication.getContext().getDatabasePath(dbName);
                if (dbFile.exists()) {
                    boolean result = dbFile.delete();
                    if (result) {
                        removeVersionInSharedPreferences(dbName);
                        Connector.clearLitePalOpenHelperInstance();
                    }
                    return result;
                }
                String path = LitePalApplication.getContext().getExternalFilesDir("") + "/databases/";
                dbFile = new File(path + dbName);
                boolean result = dbFile.delete();
                if (result) {
                    removeVersionInSharedPreferences(dbName);
                    Connector.clearLitePalOpenHelperInstance();
                }
                return result;
            }
            return false;
        }
    }

    public static void aesKey(String key) {
        CipherUtil.aesKey = key;
    }

    /**
     * Remove the database version in SharedPreferences file.
     * @param dbName
     *          Name of database to delete.
     */
    private static void removeVersionInSharedPreferences(String dbName) {
        if (isDefaultDatabase(dbName)) {
            SharedUtil.removeVersion(null);
        } else {
            SharedUtil.removeVersion(dbName);
        }
    }

    /**
     * Check the dbName is default database or not. If it's same as dbName in litepal.xml, then it is
     * default database.
     * @param dbName
     *          Name of database to check.
     * @return True if it's default database, false otherwise.
     */
    private static boolean isDefaultDatabase(String dbName) {
        if (BaseUtility.isLitePalXMLExists()) {
            if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                dbName = dbName + Const.Config.DB_NAME_SUFFIX;
            }
            LitePalConfig config = LitePalParser.parseLitePalConfiguration();
            String defaultDbName = config.getDbName();
            if (!defaultDbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                defaultDbName = defaultDbName + Const.Config.DB_NAME_SUFFIX;
            }
            return dbName.equalsIgnoreCase(defaultDbName);
        }
        return false;
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
     * @return A FluentQuery instance.
     */
    public static FluentQuery select(String... columns) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mColumns = columns;
        return cQuery;
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
     * @return A FluentQuery instance.
     */
    public static FluentQuery where(String... conditions) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mConditions = conditions;
        return cQuery;
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
     * @return A FluentQuery instance.
     */
    public static FluentQuery order(String column) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mOrderBy = column;
        return cQuery;
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
     * @return A FluentQuery instance.
     */
    public static FluentQuery limit(int value) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mLimit = String.valueOf(value);
        return cQuery;
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
     * @return A FluentQuery instance.
     */
    public static FluentQuery offset(int value) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mOffset = String.valueOf(value);
        return cQuery;
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
    public static int count(Class<?> modelClass) {
        return count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static CountExecutor countAsync(final Class<?> modelClass) {
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
    public static int count(String tableName) {
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            return cQuery.count(tableName);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static CountExecutor countAsync(final String tableName) {
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
    public static double average(Class<?> modelClass, String column) {
        return average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static AverageExecutor averageAsync(final Class<?> modelClass, final String column) {
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
    public static double average(String tableName, String column) {
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            return cQuery.average(tableName, column);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static AverageExecutor averageAsync(final String tableName, final String column) {
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
    public static <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        return max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> maxAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
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
    public static <T> T max(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            return cQuery.max(tableName, columnName, columnType);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
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
    public static <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        return min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> minAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
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
    public static <T> T min(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            return cQuery.min(tableName, columnName, columnType);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> minAsync(final String tableName, final String columnName, final Class<T> columnType) {
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
    public static <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        return sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> sumAsync(final Class<?> modelClass, final String columnName, final Class<T> columnType) {
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
    public static <T> T sum(String tableName, String columnName, Class<T> columnType) {
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            return cQuery.sum(tableName, columnName, columnType);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
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

    /**
     * Finds the record by a specific id.
     *
     * <pre>
     * Person p = LitePal.find(Person.class, 1);
     * </pre>
     *
     * The modelClass determines which table to query and the object type to
     * return. If no record can be found, then return null. <br>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link Operator#find(Class, long, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @return An object with found data from database, or null.
     */
    public static <T> T find(Class<T> modelClass, long id) {
        return find(modelClass, id, false);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findAsync(Class<T> modelClass, long id) {
        return findAsync(modelClass, id, false);
    }

    /**
     * It is mostly same as {@link Operator#find(Class, long)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param id
     *            Which record to query.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with found data from database, or null.
     */
    public static <T> T find(Class<T> modelClass, long id, boolean isEager) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onFind(modelClass, id, isEager);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findAsync(final Class<T> modelClass, final long id, final boolean isEager) {
        final FindExecutor<T> executor = new FindExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final T t = find(modelClass, id, isEager);
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
     * Finds the first record of a single table.
     *
     * <pre>
     * Person p = LitePal.findFirst(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link Operator#findFirst(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with data of first row, or null.
     */
    public static <T> T findFirst(Class<T> modelClass) {
        return findFirst(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@link Operator#findFirst(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with data of first row, or null.
     */
    public static <T> T findFirst(Class<T> modelClass, boolean isEager) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onFindFirst(modelClass, isEager);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findFirstAsync(final Class<T> modelClass, final boolean isEager) {
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
     * Finds the last record of a single table.
     *
     * <pre>
     * Person p = LitePal.findLast(Person.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link Operator#findLast(Class, boolean)}.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @return An object with data of last row, or null.
     */
    public static <T> T findLast(Class<T> modelClass) {
        return findLast(modelClass, false);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    /**
     * It is mostly same as {@link Operator#findLast(Class)} but an isEager
     * parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return.
     * @param isEager
     *            True to load the associated models, false not.
     * @return An object with data of last row, or null.
     */
    public static <T> T findLast(Class<T> modelClass, boolean isEager) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onFindLast(modelClass, isEager);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindExecutor<T> findLastAsync(final Class<T> modelClass, final boolean isEager) {
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
     * Finds multiple records by an id array.
     *
     * <pre>
     * List&lt;Person&gt; people = LitePal.findAll(Person.class, 1, 2, 3);
     *
     * long[] bookIds = { 10, 18 };
     * List&lt;Book&gt; books = LitePal.findAll(Book.class, bookIds);
     * </pre>
     *
     * Of course you can find all records by passing nothing to the ids
     * parameter.
     *
     * <pre>
     * List&lt;Book&gt; allBooks = LitePal.findAll(Book.class);
     * </pre>
     *
     * Note that the associated models won't be loaded by default considering
     * the efficiency, but you can do that by using
     * {@link Operator#findAll(Class, boolean, long...)}.
     *
     * The modelClass determines which table to query and the object type to
     * return.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param ids
     *            Which records to query. Or do not pass it to find all records.
     * @return An object list with found data from database, or an empty list.
     */
    public static <T> List<T> findAll(Class<T> modelClass, long... ids) {
        return findAll(modelClass, false, ids);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindMultiExecutor<T> findAllAsync(Class<T> modelClass, long... ids) {
        return findAllAsync(modelClass, false, ids);
    }

    /**
     * It is mostly same as {@link Operator#findAll(Class, long...)} but an
     * isEager parameter. If set true the associated models will be loaded as well.
     * <br>
     * Note that isEager will only work for one deep level relation, considering the query efficiency.
     * You have to implement on your own if you need to load multiple deepness of relation at once.
     *
     * @param modelClass
     *            Which table to query and the object type to return as a list.
     * @param isEager
     *            True to load the associated models, false not.
     * @param ids
     *            Which records to query. Or do not pass it to find all records.
     * @return An object list with found data from database, or an empty list.
     */
    public static <T> List<T> findAll(Class<T> modelClass, boolean isEager,
                                      long... ids) {
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            return queryHandler.onFindAll(modelClass, isEager, ids);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T> FindMultiExecutor<T> findAllAsync(final Class<T> modelClass, final boolean isEager, final long... ids) {
        final FindMultiExecutor<T> executor = new FindMultiExecutor<>();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final List<T> t = findAll(modelClass, isEager, ids);
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
     * Runs the provided SQL and returns a Cursor over the result set. You may
     * include ? in where clause in the query, which will be replaced by the
     * second to the last parameters, such as:
     *
     * <pre>
     * Cursor cursor = LitePal.findBySQL(&quot;select * from person where name=? and age=?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     * </pre>
     *
     * @param sql
     *            First parameter is the SQL clause to apply. Second to the last
     *            parameters will replace the place holders.
     * @return A Cursor object, which is positioned before the first entry. Note
     *         that Cursors are not synchronized, see the documentation for more
     *         details.
     */
    public static Cursor findBySQL(String... sql) {
        synchronized (LitePalSupport.class) {
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
    }

    /**
     * Deletes the record in the database by id.<br>
     * The data in other tables which is referenced with the record will be
     * removed too.
     *
     * <pre>
     * LitePal.delete(Person.class, 1);
     * </pre>
     *
     * This means that the record 1 in person table will be removed.
     *
     * @param modelClass
     *            Which table to delete from by class.
     * @param id
     *            Which record to delete.
     * @return The number of rows affected. Including cascade delete rows.
     */
    public static int delete(Class<?> modelClass, long id) {
        synchronized (LitePalSupport.class) {
            int rowsAffected;
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
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor deleteAsync(final Class<?> modelClass, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = delete(modelClass, id);
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
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * <pre>
     * LitePal.deleteAll(Person.class, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     * </pre>
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.<br>
     *
     * @param modelClass
     *            Which table to delete from by class.
     * @param conditions
     *            A string array representing the WHERE part of an SQL
     *            statement. First parameter is the WHERE clause to apply when
     *            deleting. The way of specifying place holders is to insert one
     *            or more question marks in the SQL. The first question mark is
     *            replaced by the second element of the array, the next question
     *            mark by the third, and so on. Passing empty string will update
     *            all rows.
     * @return The number of rows affected.
     */
    public static int deleteAll(Class<?> modelClass, String... conditions) {
        synchronized (LitePalSupport.class) {
            int rowsAffected;
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                DeleteHandler deleteHandler = new DeleteHandler(db);
                rowsAffected = deleteHandler.onDeleteAll(modelClass, conditions);
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
    public static UpdateOrDeleteExecutor deleteAllAsync(final Class<?> modelClass, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = deleteAll(modelClass, conditions);
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
     * Deletes all records with details given if they match a set of conditions
     * supplied. This method constructs a single SQL DELETE statement and sends
     * it to the database.
     *
     * <pre>
     * LitePal.deleteAll(&quot;person&quot;, &quot;name = ? and age = ?&quot;, &quot;Tom&quot;, &quot;14&quot;);
     * </pre>
     *
     * This means that all the records which name is Tom and age is 14 will be
     * removed.<br>
     *
     * Note that this method won't delete the referenced data in other tables.
     * You should remove those values by your own.
     *
     * @param tableName
     *            Which table to delete from.
     * @param conditions
     *            A string array representing the WHERE part of an SQL
     *            statement. First parameter is the WHERE clause to apply when
     *            deleting. The way of specifying place holders is to insert one
     *            or more question marks in the SQL. The first question mark is
     *            replaced by the second element of the array, the next question
     *            mark by the third, and so on. Passing empty string will update
     *            all rows.
     * @return The number of rows affected.
     */
    public static int deleteAll(String tableName, String... conditions) {
        synchronized (LitePalSupport.class) {
            DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
            return deleteHandler.onDeleteAll(tableName, conditions);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor deleteAllAsync(final String tableName, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = deleteAll(tableName, conditions);
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
     * Updates the corresponding record by id with ContentValues. Returns the
     * number of affected rows.
     *
     * <pre>
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, 1);
     * </pre>
     *
     * This means that the name of record 1 will be updated into Jim.<br>
     *
     * @param modelClass
     *            Which table to update by class.
     * @param values
     *            A map from column names to new column values. null is a valid
     *            value that will be translated to NULL.
     * @param id
     *            Which record to update.
     * @return The number of rows affected.
     */
    public static int update(Class<?> modelClass, ContentValues values, long id) {
        synchronized (LitePalSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            return updateHandler.onUpdate(modelClass, id, values);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAsync(final Class<?> modelClass, final ContentValues values, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = update(modelClass, values, id);
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
     * ContentValues cv = new ContentValues();
     * cv.put(&quot;name&quot;, &quot;Jim&quot;);
     * LitePal.update(Person.class, cv, &quot;name = ?&quot;, &quot;Tom&quot;);
     * </pre>
     *
     * This means that all the records which name is Tom will be updated into
     * Jim.
     *
     * @param modelClass
     *            Which table to update by class.
     * @param values
     *            A map from column names to new column values. null is a valid
     *            value that will be translated to NULL.
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
    public static int updateAll(Class<?> modelClass, ContentValues values,
                                String... conditions) {
        return updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.getName())), values, conditions);
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(Class<?> modelClass, ContentValues values, String... conditions) {
        return updateAllAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(
                modelClass.getName())), values, conditions);
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
     * </pre>
     *
     * This means that all the records which name is Tom will be updated into
     * Jim.
     *
     * @param tableName
     *            Which table to update.
     * @param values
     *            A map from column names to new column values. null is a valid
     *            value that will be translated to NULL.
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
    public static int updateAll(String tableName, ContentValues values,
                                String... conditions) {
        synchronized (LitePalSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            return updateHandler.onUpdateAll(tableName, values, conditions);
        }
    }

    /**
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(final String tableName, final ContentValues values, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = updateAll(tableName, values, conditions);
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
     * Saves the collection into database. <br>
     *
     * <pre>
     * LitePal.saveAll(people);
     * </pre>
     *
     * If the model in collection is a new record gets created in the database,
     * otherwise the existing record gets updated.<br>
     * If saving process failed by any accident, the whole action will be
     * cancelled and your database will be <b>rolled back</b>. <br>
     * This method acts the same result as the below way, but <b>much more
     * efficient</b>.
     *
     * <pre>
     * for (Person person : people) {
     * 	person.save();
     * }
     * </pre>
     *
     * So when your collection holds huge of models, saveAll(Collection) is the better choice.
     *
     * @param collection
     *            Holds all models to save.
     * @return True if all records in collection are saved. False none record in collection is saved. There won't be partial saved condition.
     */
    public static <T extends LitePalSupport> boolean saveAll(Collection<T> collection) {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                SaveHandler saveHandler = new SaveHandler(db);
                saveHandler.onSaveAll(collection);
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
     * This method is deprecated and will be removed in the future releases.
     * Handle async db operation in your own logic instead.
     */
    @Deprecated
    public static <T extends LitePalSupport> SaveExecutor saveAllAsync(final Collection<T> collection) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (LitePalSupport.class) {
                    boolean success;
                    try {
                        saveAll(collection);
                        success = true;
                    } catch (Exception e) {
                        success = false;
                    }
                    final boolean result = success;
                    if (executor.getListener() != null) {
                        Operator.getHandler().post(new Runnable() {
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
     * Provide a way to mark all models in collection as deleted. This means these models' save
     * state is no longer exist anymore. If save them again, they will be treated as inserting new
     * data instead of updating the exist one.
     * @param collection
     *          Collection of models which want to mark as deleted and clear their save state.
     */
    public static <T extends LitePalSupport> void markAsDeleted(Collection<T> collection) {
        for (T t : collection) {
            t.clearSavedState();
        }
    }

    /**
     * Check if the specified conditions data already exists in the table.
     * @param modelClass
     *          Which table to check by class.
     * @param conditions
     *          A filter declaring which data to check. Exactly same use as
     *          {@link Operator#where(String...)}, except null conditions will result in false.
     * @return Return true if the specified conditions data already exists in the table.
     *         False otherwise. Null conditions will result in false.
     */
    public static <T> boolean isExist(Class<T> modelClass, String... conditions) {
        return conditions != null && where(conditions).count(modelClass) > 0;
    }

    /**
     * Register a listener to listen database create and upgrade events.
     */
    public static void registerDatabaseListener(DatabaseListener listener) {
        dbListener = listener;
    }

    public static DatabaseListener getDBListener() {
        return dbListener;
    }

}