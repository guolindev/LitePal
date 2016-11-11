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

package org.litepal.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for litepal.xml configuration file.
 * @author guolin
 * @since 2016/11/10
 */
public class LitePalConfig {

    /**
     * The version of database.
     */
    private int version;

    /**
     * The name of database.
     */
    private String dbName;

    /**
     * The case of table names and column names and SQL.
     */
    private String cases;

    /**
     * Define where the .db file should be. Option values: internal external.
     */
    private String storage;

    /**
     * All the model classes that want to map in the database. Each class should
     * be given the full name including package name.
     */
    private List<String> classNames;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
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

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public String getCases() {
        return cases;
    }

    public void setCases(String cases) {
        this.cases = cases;
    }

}
