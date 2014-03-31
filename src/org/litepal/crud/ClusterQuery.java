package org.litepal.crud;

import java.util.List;

import org.litepal.tablemanager.Connector;

public class ClusterQuery {

	String[] mColumns;

	String[] mConditions;

	String mOrderBy;
	
	String mOffset;

	String mLimit;

	ClusterQuery() {
	}

	public ClusterQuery select(String... columns) {
		mColumns = columns;
		return this;
	}

	public ClusterQuery where(String... conditions) {
		mConditions = conditions;
		return this;
	}

	public ClusterQuery order(String column) {
		mOrderBy = column;
		return this;
	}
	
	public ClusterQuery offset(int value) {
		mOffset = String.valueOf(value);
		return this;
	}

	public ClusterQuery limit(int value) {
		mLimit = String.valueOf(value);
		return this;
	}

	public <T> List<T> run(Class<T> modelClass) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		String limit;
		if (mOffset == null) {
			limit = mLimit;
		} else {
			if (mLimit == null) {
				mLimit = "0";
			}
			limit = mOffset + "," + mLimit;
		}
		return queryHandler.onFind(modelClass, mColumns, mConditions, mOrderBy, limit);
	}

}
