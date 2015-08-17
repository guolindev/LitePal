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

package org.litepal.model;

/**
 * This class is a constant model class. It stores each table name of the
 * corresponding model classes added by developers. When synchronizing the
 * tables with the model classes, the table names are necessary to decide which
 * tables are created by the developers and which are created by system. The
 * values in table_schema are totally generated automatically, do not try to
 * change any value in it or the synchronization might be failed.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class Table_Schema {

	/**
	 * The table name of model class.
	 */
	private String name;

	/**
	 * Type of the table. 0 normal table, 1 intermediate join table.
	 */
	private int type;

	/**
	 * Get the table name.
	 * 
	 * @return The table name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the table name.
	 * 
	 * @param name
	 *            The table name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the table type.
	 * 
	 * @return The table type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Set the table type.
	 * 
	 * @param type
	 *            The table type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

}
