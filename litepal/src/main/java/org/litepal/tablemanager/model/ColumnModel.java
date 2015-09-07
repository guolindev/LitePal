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

import android.text.TextUtils;

/**
 * This is a model class for columns. It stores column name, column type, and column constraints
 * information.
 * @author Tony Green
 * @since 1.3
 */
public class ColumnModel {

    /**
     * Name of column.
     */
    private String columnName;

    /**
     * Type for column.
     */
    private String columnType;

    /**
     * Nullable constraint.
     */
    private boolean isNullable = true;

    /**
     * Unique constraint.
     */
    private boolean isUnique = false;

    /**
     * Default constraint.
     */
    private String defaultValue = "";

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setIsUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        if ("text".equalsIgnoreCase(columnType)) {
            if (!TextUtils.isEmpty(defaultValue)) {
                this.defaultValue = "'" + defaultValue + "'";
            }
        } else {
            this.defaultValue = defaultValue;
        }
    }

    /**
     * Judge current ColumnModel is id column or not.
     * @return True if it's id column. False otherwise.
     */
    public boolean isIdColumn() {
        return "_id".equalsIgnoreCase(columnName) || "id".equalsIgnoreCase(columnName);
    }

}
