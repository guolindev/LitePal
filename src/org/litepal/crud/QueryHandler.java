package org.litepal.crud;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

class QueryHandler extends DataHandler {

	QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	<T> T onFind(Class<T> modelClass, long id) {
		List<T> dataList = query(modelClass, null, "id = ?", new String[] { String.valueOf(id) },
				null, null, null, null);
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	<T> T onFindFirst(Class<T> modelClass) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id", "1");
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	<T> T onFindLast(Class<T> modelClass) {
		List<T> dataList = query(modelClass, null, null, null, null, null, "id desc", "1");
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}

	<T> List<T> onFindAll(Class<T> modelClass, long[] ids) {
		List<T> dataList;
		if (isAffectAllLines(ids)) {
			dataList = query(modelClass, null, null, null, null, null, "id", null);
		} else {
			dataList = query(modelClass, null, getWhereOfIdsWithOr(ids), null, null, null, "id",
					null);
		}
		return dataList;
	}

}