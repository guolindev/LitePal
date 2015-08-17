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

package org.litepal.crud;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.litepal.LitePalBase;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.util.DBUtility;

/**
 * Deals analysis work when comes to two models are associated with Many2One
 * associations.
 * 
 * @author Tony Green
 * @since 1.1
 */
class Many2OneAnalyzer extends AssociationsAnalyzer {

	/**
	 * Analyzing the AssociationInfo. It will help baseObj assign the necessary
	 * values automatically. If the two associated models have bidirectional
	 * associations in class files but developer has only build unidirectional
	 * associations in models, it will force to build the bidirectional
	 * associations. Besides
	 * {@link org.litepal.crud.DataSupport#addAssociatedModelWithFK(String, long)} and
	 * {@link org.litepal.crud.DataSupport#addAssociatedModelWithoutFK(String, long)} will be
	 * called here to put right values into tables.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associationInfo
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getAssociationInfo(String)}.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	void analyze(DataSupport baseObj, AssociationsInfo associationInfo) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if (baseObj.getClassName().equals(associationInfo.getClassHoldsForeignKey())) {
			analyzeManySide(baseObj, associationInfo);
		} else {
			analyzeOneSide(baseObj, associationInfo);
		}
	}

	/**
	 * When it's on the M side. Get the associated model first, then use it to
	 * get the associated model collection on the O side. Initialize the
	 * collection by calling
	 * {@link #checkAssociatedModelCollection(java.util.Collection, java.lang.reflect.Field)}
	 * and calling
	 * {@link #dealAssociatedModelOnManySide(java.util.Collection, org.litepal.crud.DataSupport, org.litepal.crud.DataSupport)}
	 * to set foreign key.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist or update.
	 * @param associationInfo
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getAssociationInfo(String)}.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private void analyzeManySide(DataSupport baseObj, AssociationsInfo associationInfo)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		DataSupport associatedModel = getAssociatedModel(baseObj, associationInfo);
		if (associatedModel != null) {
			// now it's m2o bidirectional association.
			Collection<DataSupport> tempCollection = getReverseAssociatedModels(associatedModel,
					associationInfo);
			Collection<DataSupport> reverseAssociatedModels = checkAssociatedModelCollection(
					tempCollection, associationInfo.getAssociateSelfFromOtherModel());
			setReverseAssociatedModels(associatedModel, associationInfo, reverseAssociatedModels);
			dealAssociatedModelOnManySide(reverseAssociatedModels, baseObj, associatedModel);
		} else {
			mightClearFKValue(baseObj, associationInfo);
		}
	}

	/**
	 * When it's on the O side. Get the associated model collection first, then
	 * iterate all the associated models. Each associated model calls
	 * {@link #buildBidirectionalAssociations(org.litepal.crud.DataSupport, org.litepal.crud.DataSupport, org.litepal.crud.model.AssociationsInfo)}
	 * to build bidirectional association if they haven't built yet. Then calls
	 * {@link #dealAssociatedModelOnOneSide(org.litepal.crud.DataSupport, org.litepal.crud.DataSupport)} to set
	 * foreign key.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associationInfo
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getAssociationInfo(String)}.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private void analyzeOneSide(DataSupport baseObj, AssociationsInfo associationInfo)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Collection<DataSupport> associatedModels = getAssociatedModels(baseObj, associationInfo);
		if (associatedModels == null || associatedModels.isEmpty()) {
			String tableName = DBUtility.getTableNameByClassName(associationInfo
					.getAssociatedClassName());
			baseObj.addAssociatedTableNameToClearFK(tableName);
			return;
		}
		for (DataSupport associatedModel : associatedModels) {
			buildBidirectionalAssociations(baseObj, associatedModel, associationInfo);
			dealAssociatedModelOnOneSide(baseObj, associatedModel);
		}
	}

	/**
	 * Check if the baseObj is already existed in the associatedModels
	 * collection. If not add baseObj into the collection. Then if the
	 * associated model is saved, add its' name and id to baseObj by calling
	 * {@link org.litepal.crud.DataSupport#addAssociatedModelWithFK(String, long)}.
	 * 
	 * @param associatedModels
	 *            The associated model collection.
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associatedModel
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getAssociationInfo(String)}.
	 */
	private void dealAssociatedModelOnManySide(Collection<DataSupport> associatedModels,
			DataSupport baseObj, DataSupport associatedModel) {
		if (!associatedModels.contains(baseObj)) {
			associatedModels.add(baseObj);
		}
		if (associatedModel.isSaved()) {
			baseObj.addAssociatedModelWithoutFK(associatedModel.getTableName(),
					associatedModel.getBaseObjId());
		}
	}

	/**
	 * Deals with associated model on one side.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associatedModel
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getAssociationInfo(String)}.
	 */
	private void dealAssociatedModelOnOneSide(DataSupport baseObj, DataSupport associatedModel) {
		dealsAssociationsOnTheSideWithoutFK(baseObj, associatedModel);
	}
}
