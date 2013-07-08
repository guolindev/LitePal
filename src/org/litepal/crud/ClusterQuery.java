package org.litepal.crud;

public class ClusterQuery {

	String[] mColumns;

	String[] mConditions;

	String mGroupBy;

	String mHaving;

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

	public ClusterQuery group(String args) {
		mGroupBy = args;
		return this;
	}

	public ClusterQuery having(String options) {
		mHaving = options;
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

	public <T> T execute() {
		return null;
	}

}
