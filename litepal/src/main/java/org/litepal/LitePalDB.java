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

import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalConfig;
import org.litepal.parser.LitePalParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of LitePal database. It's similar to litepal.xml configuration, but allows to
 * configure database details at runtime. This is very important when comes to support multiple
 * databases functionality.
 *
 * @author Tony Green
 * @since 1.4
 */
public class LitePalDB {

    /**
     * The version of database.
     */
    private int version;

    /**
     * The name of database.
     */
    private String dbName;

    /**
     * Define where the .db file should be. Option values: internal, external, or path in sdcard.
     */
    private String storage;

    /**
     * Indicates that the database file stores in external storage or not.
     */
    private boolean isExternalStorage = false;

    /**
     * All the model classes that want to map in the database. Each class should
     * be given the full name including package name.
     */
    private List<String> classNames;

    /**
     * Construct a LitePalDB instance from the default configuration by litepal.xml. But database
     * name must be different than the default.
     * @param dbName
     *          Name of database.
     * @return A LitePalDB instance which used the default configuration in litepal.xml but with a specified database name.
     */
    public static LitePalDB fromDefault(String dbName) {
        LitePalConfig config = LitePalParser.parseLitePalConfiguration();
        LitePalDB litePalDB = new LitePalDB(dbName, config.getVersion());
        litePalDB.setStorage(config.getStorage());
        litePalDB.setClassNames(config.getClassNames());
        return litePalDB;
    }

    /**
     * Construct a LitePalDB instance. Database name and version are necessary fields.
     * @param dbName
     *          Name of database.
     * @param version
     *          Version of database.
     */
    public LitePalDB(String dbName, int version) {
        this.dbName = dbName;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public String getDbName() {
        return dbName;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public boolean isExternalStorage() {
        return isExternalStorage;
    }

    public void setExternalStorage(boolean isExternalStorage) {
        this.isExternalStorage = isExternalStorage;
    }

    /**
     * Get the class name list. Always add table_schema as a value.
     *
     * @return The class name list.
     */
    public List<String> getClassNames() {
        if (classNames == null) {
            classNames = new ArrayList<String>();
            classNames.add("org.litepal.model.Table_Schema");
        } else if (classNames.isEmpty()) {
            classNames.add("org.litepal.model.Table_Schema");
        }
        return classNames;
    }

    /**
     * Add a class name into the current mapping model list.
     *
     * @param className
     *            Full package class name.
     */
    public void addClassName(String className) {
        getClassNames().add(className);
    }

    void setClassNames(List<String> className) {
        this.classNames = className;
    }

}