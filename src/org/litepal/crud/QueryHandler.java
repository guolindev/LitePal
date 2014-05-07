package org.litepal.crud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DataSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This is a component under DataSupport. It deals with query stuff as primary
 * task.
 * 
 * @author Tony Green
 * @since 1.1
 */
class QueryHandler extends DataHandler {

	/**
	 * Initialize {@link DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of QueryHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
	QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to query a record
	 * based on id. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @param id
	 *            Which record to query.
	 * @return An object with founded data from database, or null.
	 */
	<T> T onFind(Class<T> modelClass, long id, boolean isEager) {
		if (!isEager) {
			List<T> dataList = query(modelClass, null, "id = ?", new String[] { String.valueOf(id) }, null,
					null, null, null, null);
			if (dataList.size() > 0) {
				return dataList.get(0);
			}
		} else {
			Collection<AssociationsInfo> associationInfos = getAssociationInfo(modelClass.getName());
			try {
				List<AssociationsInfo> fkInCurrentModel = new ArrayList<AssociationsInfo>();
				List<AssociationsInfo> fkInOtherModel = new ArrayList<AssociationsInfo>();
				for (AssociationsInfo associationInfo : associationInfos) {
					Log.d("TAG", "association type " + associationInfo.getAssociationType()
							+ " class hold fk " + associationInfo.getClassHoldsForeignKey());
					if (associationInfo.getAssociationType() == Const.Model.MANY_TO_ONE
							|| associationInfo.getAssociationType() == Const.Model.ONE_TO_ONE) {
						if (modelClass.getName().equals(associationInfo.getClassHoldsForeignKey())) {
							fkInCurrentModel.add(associationInfo);
						} else {
							fkInOtherModel.add(associationInfo);
						}
					} else if (associationInfo.getAssociationType() == Const.Model.MANY_TO_MANY) {
					}
				}
				Log.d("TAG", fkInCurrentModel.toString());
				List<T> dataList = query(modelClass, null, "id = ?", new String[] { String.valueOf(id) },
						null, null, null, null, fkInCurrentModel);
				DataSupport baseObj = null;
				if (dataList.size() > 0) {
					baseObj = (DataSupport) dataList.get(0);
				}
				if (baseObj != null) {
					for (AssociationsInfo info : fkInOtherModel) {
						String foreignKeyColumn = getForeignKeyColumnName(DBUtility
								.getTableNameByClassName(info.getSelfClassName()));
//						List<T> list = (List<T>) DataSupport.where("? = ?", foreignKeyColumn,
//								String.valueOf(baseObj.getBaseObjId())).find(
//								Class.forName(info.getAssociatedClassName()));
					}
				}
				return (T) baseObj;
			} catch (Exception e) {
				throw new DataSupportException(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query the first
	 * record in a table. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @return An object with data of first row, or null.
	 */
	<T> T onFindFirst(Class<T> modelClass) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id", "1", null);
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query the last
	 * record in a table. If the result set is empty, gives null back.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return.
	 * @return An object with data of last row, or null.
	 */
	<T> T onFindLast(Class<T> modelClass) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id desc", "1", null);
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	/**
	 * The open interface for other classes in CRUD package to query multiple
	 * records by an id array. Pass no ids means query all rows.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param ids
	 *            Which records to query. Or do not pass it to find all records.
	 * @return An object list with founded data from database, or an empty list.
	 */
	<T> List<T> onFindAll(Class<T> modelClass, long... ids) {
		List<T> dataList;
		if (isAffectAllLines(ids)) {
			dataList = query(modelClass, null, null, null, null, null, "id", null, null);
		} else {
			dataList = query(modelClass, null, getWhereOfIdsWithOr(ids), null, null, null, "id", null, null);
		}
		return dataList;
	}

	/**
	 * The open interface for other classes in CRUD package to query multiple
	 * records by parameters.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @return
	 */
	<T> List<T> onFind(Class<T> modelClass, String[] columns, String[] conditions, String orderBy,
			String limit, boolean isEager) {
		BaseUtility.checkConditionsCorrect(conditions);
		if (!isEager) {
			List<T> dataList = query(modelClass, columns, getWhereClause(conditions),
					getWhereArgs(conditions), null, null, orderBy, limit, null);
			return dataList;
		}
		return null;
	}

}