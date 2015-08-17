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

import org.litepal.util.Const;

/**
 * This is a model class for table associations. It stores table name,
 * associated table name, table name which holds foreign key, and association
 * type. Relations have three types. One2One, Many2One and Many2Many. If the
 * association type is One2One or Many2One, the foreign key will be on the side
 * of tableHoldsForeignKey. If the association is Many2Many, a intermediate join
 * table will be built and named by the concatenation of the two target table
 * names in alphabetical order with underline in the middle.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class AssociationsModel {

	/**
	 * Table name.
	 */
	private String tableName;

	/**
	 * Associated table name.
	 */
	private String associatedTableName;

	/**
	 * The table which holds foreign key.
	 */
	private String tableHoldsForeignKey;

	/**
	 * The association type, including {@link Const.Model#MANY_TO_ONE},
	 * {@link Const.Model#MANY_TO_MANY}, {@link Const.Model#ONE_TO_ONE}.
	 */
	private int associationType;

	/**
	 * Get table name.
	 * 
	 * @return Return the table name.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Set table name.
	 * 
	 * @param tableName
	 *            The table name to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Get associated table name.
	 * 
	 * @return Return the associated table name.
	 */
	public String getAssociatedTableName() {
		return associatedTableName;
	}

	/**
	 * Set associated table name.
	 * 
	 * @param associatedTableName
	 *            The associated table name.
	 */
	public void setAssociatedTableName(String associatedTableName) {
		this.associatedTableName = associatedTableName;
	}

	/**
	 * Get the table which holds foreign key.
	 * 
	 * @return The table which holds foreign key.
	 */
	public String getTableHoldsForeignKey() {
		return tableHoldsForeignKey;
	}

	/**
	 * Set the table which holds foreign key.
	 * 
	 * @param tableHoldsForeignKey
	 *            The table which holds foreign key to set.
	 */
	public void setTableHoldsForeignKey(String tableHoldsForeignKey) {
		this.tableHoldsForeignKey = tableHoldsForeignKey;
	}

	/**
	 * Get the association type.
	 * 
	 * @return The association type.
	 */
	public int getAssociationType() {
		return associationType;
	}

	/**
	 * Set the association type.
	 * 
	 * @param associationType
	 *            Within {@link Const.Model#MANY_TO_ONE},
	 *            {@link Const.Model#MANY_TO_MANY},
	 *            {@link Const.Model#ONE_TO_ONE}.
	 */
	public void setAssociationType(int associationType) {
		this.associationType = associationType;
	}

	/**
	 * Override equals method to make sure that if two associated tables in the
	 * association model are same ignoring sides, they are same association
	 * model.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof AssociationsModel) {
			AssociationsModel association = (AssociationsModel) o;
			if (association.getTableName() != null && association.getAssociatedTableName() != null) {
				if (association.getAssociationType() == associationType
						&& association.getTableHoldsForeignKey().equals(tableHoldsForeignKey)) {
					if (association.getTableName().equals(tableName)
							&& association.getAssociatedTableName().equals(associatedTableName)
							&& association.getTableHoldsForeignKey().equals(tableHoldsForeignKey)) {
						return true;
					} else if (association.getTableName().equals(associatedTableName)
							&& association.getAssociatedTableName().equals(tableName)
							&& association.getTableHoldsForeignKey().equals(tableHoldsForeignKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
