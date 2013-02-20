package org.litepal.crud;

import java.lang.reflect.InvocationTargetException;

import org.litepal.LitePalBase;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.util.DBUtility;

/**
 * Deals analysis work when comes to two models are associated with One2One
 * associations.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class One2OneAnalyzer extends AssociationsAnalyzer {

	/**
	 * Analyzing the AssociationInfo. It will help baseObj assign the necessary
	 * values automatically. If the two associated models have bidirectional
	 * associations in class files but developer has only build unidirectional
	 * associations in models, it will force to build the bidirectional
	 * associations. Besides
	 * {@link DataSupport#addAssociatedModelWithFK(String, long)} and
	 * {@link DataSupport#addAssociatedModelWithoutFK(String, long)} will be
	 * called here to put right values into tables.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist or update.
	 * @param associatedInfo
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getRelatedInfo}.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	void analyze(DataSupport baseObj, AssociationsInfo associationInfo) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		DataSupport associatedModel = getAssociatedModel(baseObj, associationInfo);
		if (associatedModel != null) {
			buildBidirectionalAssociations(baseObj, associatedModel, associationInfo);
			dealAssociatedModel(baseObj, associatedModel, associationInfo);
		} else {
			String tableName = DBUtility.getTableNameByClassName(associationInfo
					.getAssociatedClassName());
			baseObj.addAssociatedTableNameToClearFK(tableName);
		}
	}

	/**
	 * Check the association type. If it's bidirectional association, calls
	 * {@link #bidirectionalCondition(DataSupport, DataSupport)}. If it's
	 * unidirectional association, calls
	 * {@link #unidirectionalCondition(DataSupport, DataSupport)}.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associatedModel
	 *            The associated model of baseObj.
	 * @param associationInfo
	 *            The associated info analyzed by
	 *            {@link LitePalBase#getRelatedInfo}.
	 */
	private void dealAssociatedModel(DataSupport baseObj, DataSupport associatedModel,
			AssociationsInfo associationInfo) {
		if (associationInfo.getAssociateSelfFromOtherModel() != null) {
			bidirectionalCondition(baseObj, associatedModel);
		} else {
			unidirectionalCondition(baseObj, associatedModel);
		}
	}

	/**
	 * Deals bidirectional association condition. If associated model is saved,
	 * add its' name and id to baseObj by calling
	 * {@link DataSupport#addAssociatedModelWithFK(String, long)}. Add its' name
	 * and id to baseObj by calling
	 * {@link DataSupport#addAssociatedModelWithoutFK(String, long)}.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associatedModel
	 *            The associated model of baseObj.
	 */
	private void bidirectionalCondition(DataSupport baseObj, DataSupport associatedModel) {
		if (associatedModel.isSaved()) {
			baseObj.addAssociatedModelWithFK(associatedModel.getTableName(),
					associatedModel.getBaseObjId());
			baseObj.addAssociatedModelWithoutFK(associatedModel.getTableName(),
					associatedModel.getBaseObjId());
		}
	}

	/**
	 * Deals unidirectional associations condition.
	 * 
	 * @param baseObj
	 *            The baseObj currently want to persist.
	 * @param associatedModel
	 *            The associated model of baseObj.
	 */
	private void unidirectionalCondition(DataSupport baseObj, DataSupport associatedModel) {
		dealsAssociationsOnTheSideWithoutFK(baseObj, associatedModel);
	}

}
