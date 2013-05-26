package org.litepal.crud;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

class QueryHandler extends DataHandler {

	QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	<T> T onFind(Class<T> modelClass, long id) {
		List<T> dataList = query(modelClass, null, "id = ?", new String[] { String.valueOf(id) }, null,
				null, null);
		if (dataList.size() > 0) {
			return dataList.get(0);
		}
		return null;
	}
}
