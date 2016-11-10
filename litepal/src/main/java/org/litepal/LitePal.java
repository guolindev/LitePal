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

import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalParser;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;

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
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     *          The database to switch to.
     */
    public static void use(LitePalDB litePalDB) {
        String dbNameInXML = null;
        if (BaseUtility.isLitePalXMLExists()) {
            LitePalParser.parseLitePalConfiguration();
            dbNameInXML = LitePalAttr.getInstance().getDbName();
        }
        LitePalAttr litePalAttr = LitePalAttr.getInstance();
        litePalAttr.setDbName(litePalDB.getDbName());
        litePalAttr.setVersion(litePalDB.getVersion());
        litePalAttr.setStorage(litePalDB.isExternalStorage() ? "external" : "internal");
        litePalAttr.setClassNames(litePalDB.getClassNames());
        // set the extra key name only when use database other than default or litepal.xml not exists
        if (dbNameInXML == null || !dbNameInXML.equalsIgnoreCase(litePalDB.getDbName())) {
            litePalAttr.setExtraKeyName(litePalDB.getDbName());
            litePalAttr.setCases("lower");
        }
        Connector.clearLitePalOpenHelperInstance();
    }

    /**
     * Switch the using database to the one specified by litepal.xml.
     */
    public static void useDefault() {
        LitePalAttr.clearInstance();
        Connector.clearLitePalOpenHelperInstance();
    }

}
