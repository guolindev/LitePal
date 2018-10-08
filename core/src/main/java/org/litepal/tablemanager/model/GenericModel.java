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

package org.litepal.tablemanager.model;

/**
 * This is a model class for generic table. It stores table name, value column name, value column
 * type and value id column name. This class is used to create generic tables when generic collection
 * fields are declared in the model class.
 *
 * @author Tony Green
 * @since 1.4
 */
public class GenericModel {

    /**
     * Table name.
     */
    private String tableName;

    /**
     * Column name for storing value.
     */
    private String valueColumnName;

    /**
     * Column type for storing value.
     */
    private String valueColumnType;

    /**
     * Column name for reference with main table.
     */
    private String valueIdColumnName;

    /**
     * Only used when query generic data. This is cache fields for improving performance.
     */
    private String getMethodName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getValueColumnType() {
        return valueColumnType;
    }

    public void setValueColumnType(String valueColumnType) {
        this.valueColumnType = valueColumnType;
    }

    public String getValueIdColumnName() {
        return valueIdColumnName;
    }

    public void setValueIdColumnName(String valueIdColumnName) {
        this.valueIdColumnName = valueIdColumnName;
    }

    public String getGetMethodName() {
        return getMethodName;
    }

    public void setGetMethodName(String getMethodName) {
        this.getMethodName = getMethodName;
    }
}
