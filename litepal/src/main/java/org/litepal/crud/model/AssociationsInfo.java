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

package org.litepal.crud.model;

import java.lang.reflect.Field;

/**
 * This model holds necessary information when comes to analyze and handle
 * associated models of self model.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class AssociationsInfo {

	/**
	 * The class name of self class.
	 */
	private String selfClassName;

	/**
	 * The class name of the class which associated with self class.
	 */
	private String associatedClassName;

	/**
	 * The class which holds foreign key.
	 */
	private String classHoldsForeignKey;

	/**
	 * The field of self class to declare has association with other class.
	 */
	private Field associateOtherModelFromSelf;

	/**
	 * The field of the associated class to declare has association with self
	 * class.
	 */
	private Field associateSelfFromOtherModel;

	/**
	 * The association type, including Many2One One2One Many2Many.
	 */
	private int associationType;

	/**
	 * Get the class name of self class.
	 * 
	 * @return The self class name.
	 */
	public String getSelfClassName() {
		return selfClassName;
	}

	/**
	 * Set the class name of self class.
	 * 
	 * @param selfClassName
	 *            The self class name to set.
	 */
	public void setSelfClassName(String selfClassName) {
		this.selfClassName = selfClassName;
	}

	/**
	 * Get the class name of the class which associated with self class.
	 * 
	 * @return The associated class name.
	 */
	public String getAssociatedClassName() {
		return associatedClassName;
	}

	/**
	 * Set the class name of the class which associated with self class.
	 * 
	 * @param associatedClassName
	 *            The associated class name to set.
	 */
	public void setAssociatedClassName(String associatedClassName) {
		this.associatedClassName = associatedClassName;
	}

	/**
	 * Get the class which holds foreign key.
	 * 
	 * @return The class which holds foreign key.
	 */
	public String getClassHoldsForeignKey() {
		return classHoldsForeignKey;
	}

	/**
	 * Set the class which holds foreign key.
	 * 
	 * @param classHoldsForeignKey
	 *            The class which holds foreign key to set.
	 */
	public void setClassHoldsForeignKey(String classHoldsForeignKey) {
		this.classHoldsForeignKey = classHoldsForeignKey;
	}

	/**
	 * Get the field of self class which declares has association with other
	 * class.
	 * 
	 * @return The field which declares has association with other class.
	 */
	public Field getAssociateOtherModelFromSelf() {
		return associateOtherModelFromSelf;
	}

	/**
	 * Set the field of self class which declares has association with other
	 * class.
	 * 
	 * @param associateOtherModelFromSelf
	 *            The field which declares has association with other class to
	 *            set.
	 */
	public void setAssociateOtherModelFromSelf(Field associateOtherModelFromSelf) {
		this.associateOtherModelFromSelf = associateOtherModelFromSelf;
	}

	/**
	 * Get the field of the associated class which declares has association with
	 * self class.
	 * 
	 * @return The field of the associated class which declares has association
	 *         with self class.
	 */
	public Field getAssociateSelfFromOtherModel() {
		return associateSelfFromOtherModel;
	}

	/**
	 * Set the field of the associated class which declares has association with
	 * self class.
	 * 
	 * @param associateSelfFromOtherModel
	 *            The field of the associated class which declares has
	 *            association with self class to set.
	 */
	public void setAssociateSelfFromOtherModel(Field associateSelfFromOtherModel) {
		this.associateSelfFromOtherModel = associateSelfFromOtherModel;
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
	 *            Within ONE_TO_ONE, MANY_TO_ONE and MANY_TO_MANY constants.
	 */
	public void setAssociationType(int associationType) {
		this.associationType = associationType;
	}

	/**
	 * Override equals method to make sure that if two associated classes in the
	 * association info model are same ignoring sides, they are same association
	 * info model.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof AssociationsInfo) {
			AssociationsInfo other = (AssociationsInfo) o;
			if (o != null && other != null) {
				if (other.getAssociationType() == associationType
						&& other.getClassHoldsForeignKey().equals(classHoldsForeignKey)) {
					if (other.getSelfClassName().equals(selfClassName)
							&& other.getAssociatedClassName().equals(associatedClassName)) {
						return true;
					}
					if (other.getSelfClassName().equals(associatedClassName)
							&& other.getAssociatedClassName().equals(selfClassName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
