package org.litepal.crud;

import java.util.List;

import org.litepal.tablemanager.Connector;

public class ClusterQuery {

	String[] mColumns;

	String[] mConditions;

	String mOrderBy;

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

	public ClusterQuery order(String args) {
		mOrderBy = args;
		return this;
	}

	public ClusterQuery limit(int value) {
		mLimit = String.valueOf(value);
		return this;
	}

	public <T> List<T> execute(Class<T> modelClass) {
		QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
		return queryHandler.onFind(modelClass, mColumns, mConditions, mOrderBy, mLimit);
	}

}
