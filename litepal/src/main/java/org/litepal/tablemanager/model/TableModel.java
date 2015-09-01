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

import java.util.ArrayList;
import java.util.List;

/**
 * This is a model class for tables. It stores a table name and a HashMap for
 * columns in the table.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class TableModel {

	/**
	 * Table name.
	 */
	private String tableName;

    /**
     * A list contains all column models with column name, type and constraints.
     */
    private List<ColumnModel> columnModels = new ArrayList<ColumnModel>();

	/**
	 * Class name for the table name. This value might be null. Don't rely on it.
	 */
	private String className;

	/**
	 * Get table name.
	 * 
	 * @return Name of table.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Set table name.
	 * 
	 * @param tableName
	 *            Name of table.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Get class name.
	 * 
	 * @return Return the class name or null.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Set class name.
	 * 
	 * @param className
	 *            The class name.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

    /**
     * Add a column model into the table model.
     *
     * @param columnModel
     *            A column model contains name, type and constraints.
     */
    public void addColumnModel(ColumnModel columnModel) {
        columnModels.add(columnModel);
    }

    /**
     * Find all the column models of the current table model.
     * @return A list contains all column models.
     */
    public List<ColumnModel> getColumnModels() {
        return columnModels;
    }

    /**
     * Find the ColumnModel which can map the column name passed in.
     * @param columnName
     *          Name of column.
     * @return A ColumnModel which can map the column name passed in. Or null.
     */
    public ColumnModel getColumnModelByName(String columnName) {
        for (ColumnModel columnModel : columnModels) {
            if (columnModel.getColumnName().equalsIgnoreCase(columnName)) {
                return columnModel;
            }
        }
        return null;
    }

    /**
     * Remove a column model by the specified column name.
     * @param columnName
     *          Name of the column to remove.
     */
    public void removeColumnModelByName(String columnName) {
        if (TextUtils.isEmpty(columnName)) {
            return;
        }
        int indexToRemove = -1;
        for (int i = 0; i < columnModels.size(); i++) {
            ColumnModel columnModel = columnModels.get(i);
            if (columnName.equalsIgnoreCase(columnModel.getColumnName())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            columnModels.remove(indexToRemove);
        }
    }

    /**
     * Judge the table model has such a column or not.
     * @param columnName
     *          The name of column to check.
     * @return True if matches a column in the table model. False otherwise.
     */
    public boolean containsColumn(String columnName) {
        for (int i = 0; i < columnModels.size(); i++) {
            ColumnModel columnModel = columnModels.get(i);
            if (columnName.equalsIgnoreCase(columnModel.getColumnName())) {
                return true;
            }
        }
        return false;
    }

}
