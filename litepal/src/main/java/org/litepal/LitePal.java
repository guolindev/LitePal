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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalConfig;
import org.litepal.parser.LitePalParser;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.SharedUtil;

import java.io.File;

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling {@link #initialize(Context)} method to make LitePal ready to
 * work. Also you can switch the using database by calling {@link #use(LitePalDB)} and {@link #useDefault()}
 * methods.
 *
 * @author Tony Green
 * @since 1.4
 */
public class LitePal {

    private static Handler handler = new Handler(Looper.getMainLooper());

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
     * Get the main thread handler. You don't need this method. It's used by framework only.
     * @return Main thread handler.
     */
    public static Handler getHandler() {
        return handler;
    }

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     *          The database to switch to.
     */
    public static void use(LitePalDB litePalDB) {
        LitePalAttr litePalAttr = LitePalAttr.getInstance();
        litePalAttr.setDbName(litePalDB.getDbName());
        litePalAttr.setVersion(litePalDB.getVersion());
        litePalAttr.setStorage(litePalDB.isExternalStorage() ? "external" : "internal");
        litePalAttr.setClassNames(litePalDB.getClassNames());
        // set the extra key name only when use database other than default or litepal.xml not exists
        if (!isDefaultDatabase(litePalDB.getDbName())) {
            litePalAttr.setExtraKeyName(litePalDB.getDbName());
            litePalAttr.setCases("lower");
        }
        Connector.clearLitePalOpenHelperInstance();
    }

    /**
     * Switch the using database to default with configuration by litepal.xml.
     */
    public static void useDefault() {
        LitePalAttr.clearInstance();
        Connector.clearLitePalOpenHelperInstance();
    }

    /**
     * Delete the specified database.
     * @param dbName
     *          Name of database to delete.
     * @return True if delete success, false otherwise.
     */
    public static boolean deleteDatabase(String dbName) {
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

    /**
     * Remove the database version in SharedPreferences file.
     * @param dbName
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

}
