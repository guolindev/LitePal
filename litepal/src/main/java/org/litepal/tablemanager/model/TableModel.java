/*
 * Copyright (C)  Tony Green, Litepal Framework Open Source Project
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	 * Column name is key, and column type is value.
	 */
	private Map<String, String> columnsMap = new HashMap<String, String>();

	/**
	 * Class name for the table name. This value might be null. Don't rely on
	 * it.
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
	 * Find all the column names of a table.
	 * 
	 * @return Return a set of column names.
	 */
	public Set<String> getColumnNames() {
		return columnsMap.keySet();
	}

	/**
	 * Add a column into the table model.
	 * 
	 * @param columnName
	 *            The name of column.
	 * @param columnType
	 *            The data type of column.
	 */
	public void addColumn(String columnName, String columnType) {
		columnsMap.put(columnName, columnType);
	}

	/**
	 * Find all the columns with their names and data types.
	 * 
	 * @return A map contains all columns with column name as key and column
	 *         type as value.
	 */
	public Map<String, String> getColumns() {
		return columnsMap;
	}

	/**
	 * Remove a column from table model.
	 * 
	 * @param columnNameToRemove
	 *            The column name that need to remove.
	 */
	public void removeColumn(String columnNameToRemove) {
		columnsMap.remove(columnNameToRemove);
	}

	/**
	 * Remove a column from table model. The case of the passed in column name
	 * is ignored.
	 * 
	 * @param columnNameToRemove
	 *            The column name that need to remove.
	 */
	public void removeColumnIgnoreCases(String columnNameToRemove) {
		for (String columnName : getColumnNames()) {
			if (columnName.equalsIgnoreCase(columnNameToRemove)) {
				columnsMap.remove(columnName);
				return;
			}
		}
	}

}
