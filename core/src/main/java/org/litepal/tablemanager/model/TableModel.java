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

import org.litepal.util.BaseUtility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * A map contains all column models with column name, type and constraints.
     */
    private Map<String, ColumnModel> columnModelMap = new HashMap<>();

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
        columnModelMap.put(BaseUtility.changeCase(columnModel.getColumnName()), columnModel);
    }

    /**
     * Find all the column models of the current table model.
     * @return A list contains all column models.
     */
    public Collection<ColumnModel> getColumnModels() {
        return columnModelMap.values();
    }

    /**
     * Find the ColumnModel which can map the column name passed in.
     * @param columnName
     *          Name of column.
     * @return A ColumnModel which can map the column name passed in. Or null.
     */
    public ColumnModel getColumnModelByName(String columnName) {
        return columnModelMap.get(BaseUtility.changeCase(columnName));
    }

    /**
     * Remove a column model by the specified column name.
     * @param columnName
     *          Name of the column to remove.
     */
    public void removeColumnModelByName(String columnName) {
        columnModelMap.remove(BaseUtility.changeCase(columnName));
    }

    /**
     * Judge the table model has such a column or not.
     * @param columnName
     *          The name of column to check.
     * @return True if matches a column in the table model. False otherwise.
     */
    public boolean containsColumn(String columnName) {
        return columnModelMap.containsKey(BaseUtility.changeCase(columnName));
    }

}
