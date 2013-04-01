package org.litepal.crud;

import android.database.sqlite.SQLiteDatabase;

class QueryHandler extends DataHandler {

	QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	<T> T onFind(Class<T> modelClass, long id) {
		return null;
	}

}
