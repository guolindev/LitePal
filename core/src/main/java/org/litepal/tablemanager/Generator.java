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

package org.litepal.tablemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.litepal.LitePalBase;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.BaseUtility;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * This class is the basic class for managing database dynamically. It is used
 * to create or update tables by the mapping classes from litepal.xml file.
 * Generator is a superclass, it just read the fields from classes and format
 * the fields types into database types. The analysis job is delegated to the
 * subclasses to do. Then subclasses can invoke the execute method to finish the
 * job or they can override to do their own logic.
 * 
 * @author Tony Green
 * @since 1.0
 */
public abstract class Generator extends LitePalBase {
	public static final String TAG = "Generator";

	/**
	 * The collection contains all table models. Use a global variable store
	 * table model to improve performance. Avoiding look up for table model each
	 * time.
	 */
	private Collection<TableModel> mTableModels;

	/**
	 * The collection contains all association models.
	 */
	private Collection<AssociationsModel> mAllRelationModels;

	/**
	 * This is a shortcut way to get all the table models for each model class
	 * defined in the mapping list. No need to iterate all the model classes and
	 * get table model for each one.
	 * 
	 * @return A collection contains all table models.
	 */
	protected Collection<TableModel> getAllTableModels() {
		if (mTableModels == null) {
			mTableModels = new ArrayList<>();
		}
		if (!canUseCache()) {
			mTableModels.clear();
			for (String className : LitePalAttr.getInstance().getClassNames()) {
				mTableModels.add(getTableModel(className));
			}
		}
		return mTableModels;
	}

	/**
	 * This method is used to get all the association models which in the
	 * mapping list of litepal.xml file.
	 * 
	 * @return Collection of RelationModel for all the mapping classes.
	 */
	protected Collection<AssociationsModel> getAllAssociations() {
		if (mAllRelationModels == null || mAllRelationModels.isEmpty()) {
			mAllRelationModels = getAssociations(LitePalAttr.getInstance().getClassNames());
		}
		return mAllRelationModels;
	}

	/**
	 * Use the parameter SQLiteDatabase to execute the passing SQLs. Subclasses
	 * can add their own logic when do the executing job by overriding this
	 * method.
	 * 
	 * @param sqls
	 *            SQLs that want to execute.
	 * @param db
	 *            instance of SQLiteDatabase
	 */
	protected void execute(List<String> sqls, SQLiteDatabase db) {
		String throwSQL = "";
		try {
			if (sqls != null && !sqls.isEmpty()) {
				for (String sql : sqls) {
                    if (!TextUtils.isEmpty(sql)) {
                        throwSQL = BaseUtility.changeCase(sql);
                        db.execSQL(throwSQL);
                    }
				}
			}
		} catch (SQLException e) {
			throw new DatabaseGenerateException(DatabaseGenerateException.SQL_ERROR + throwSQL);
		}
	}

	/**
	 * Add association to all the tables based on the associations between class
	 * models.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	private static void addAssociation(SQLiteDatabase db, boolean force) {
		AssociationCreator associationsCreator = new Creator();
		associationsCreator.addOrUpdateAssociation(db, force);
	}

	/**
	 * Update associations to all the associated tables in the database. Remove
	 * dump foreign key columns and dump intermediate join tables.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	private static void updateAssociations(SQLiteDatabase db) {
		AssociationUpdater associationUpgrader = new Upgrader();
		associationUpgrader.addOrUpdateAssociation(db, false);
	}

	/**
	 * Upgrade all the tables in the database, including remove dump columns and
	 * add new columns.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	private static void upgradeTables(SQLiteDatabase db) {
		Upgrader upgrader = new Upgrader();
		upgrader.createOrUpgradeTable(db, false);
	}

	/**
	 * Create tables based on the class models defined in the litepal.xml file.
	 * After the tables are created, add association to these tables based on
	 * the associations between class models.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	private static void create(SQLiteDatabase db, boolean force) {
		Creator creator = new Creator();
		creator.createOrUpgradeTable(db, force);
	}

	/**
	 * Drop the tables which are no longer exist in the mapping list but created
	 * before.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	private static void drop(SQLiteDatabase db) {
		Dropper dropper = new Dropper();
		dropper.createOrUpgradeTable(db, false);
	}

	/**
	 * If the table models in the collection has the same size as classes
	 * defined in the mapping list, it means that the table models are exist.
	 * Can use cache.
	 * 
	 * @return Can use the cache for the table models or not.
	 */
	private boolean canUseCache() {
		if (mTableModels == null) {
			return false;
		}
		return mTableModels.size() == LitePalAttr.getInstance().getClassNames().size();
	}

	/**
	 * Create tables based on the class models defined in the litepal.xml file.
	 * After the tables are created, add association to these tables based on
	 * the associations between class models.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	static void create(SQLiteDatabase db) {
		create(db, true);
		addAssociation(db, true);
	}

	/**
	 * Upgrade tables to make sure when model classes are changed, the
	 * corresponding tables in the database should be always synchronized with
	 * them.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 */
	static void upgrade(SQLiteDatabase db) {
		drop(db);
		create(db, false);
		updateAssociations(db);
		upgradeTables(db);
		addAssociation(db, false);
	}

	/**
	 * Analysis the TableModel by the purpose of subclasses, and generate a SQL
	 * to do the intention job. The implementation of this method is totally
	 * delegated to the subclasses.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	protected abstract void createOrUpgradeTable(SQLiteDatabase db, boolean force);

	/**
	 * Analysis the {@link org.litepal.tablemanager.model.AssociationsModel} by the purpose of subclasses, and
	 * generate a SQL to do the intention job. The implementation of this method
	 * is totally delegated to the subclasses.
	 * 
	 * @param db
	 *            Instance of SQLiteDatabase.
	 * @param force
	 *            Drop the table first if it already exists.
	 */
	protected abstract void addOrUpdateAssociation(SQLiteDatabase db, boolean force);

}
