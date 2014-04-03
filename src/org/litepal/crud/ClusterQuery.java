package org.litepal.crud;

import java.util.List;

import org.litepal.tablemanager.Connector;

/**
 * Allows developers to query tables with cluster style.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class ClusterQuery {

	/**
	 * Representing the selected columns in SQL.
	 */
	String[] mColumns;

	/**
	 * Representing the where clause in SQL.
	 */
	String[] mConditions;

	/**
	 * Representing the order by clause in SQL.
	 */
	String mOrderBy;

	/**
	 * Representing the limit clause in SQL.
	 */
	String mLimit;

	/**
	 * Representing the offset in SQL.
	 */
	String mOffset;

	/**
	 * Do not allow to create instance by developers.
	 */
	ClusterQuery() {
	}

	/**
	 * Declaring to query which columns in table.
	 * 
	 * <pre>
	 * DataSupport.select(&quot;name&quot;, &quot;age&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows with name and age columns in Person table.
	 * 
	 * @param columns
	 *            A String array of which columns to return. Passing null will
	 *            return all columns.
	 * 
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery select(String... columns) {
		mColumns = columns;
		return this;
	}

	/**
	 * Declaring to query which rows in table.
	 * 
	 * <pre>
	 * DataSupport.where(&quot;name = ? or age &gt; ?&quot;, &quot;Tom&quot;, &quot;14&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find rows which name is Tom or age greater than 14 in Person
	 * table.
	 * 
	 * @param conditions
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause. Passing null will return all rows.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery where(String... conditions) {
		mConditions = conditions;
		return this;
	}

	/**
	 * Declaring how to order the rows queried from table.
	 * 
	 * <pre>
	 * DataSupport.order(&quot;name desc&quot;).find(Person.class);
	 * </pre>
	 * 
	 * This will find all rows in Person table sorted by name with inverted
	 * order.
	 * 
	 * @param column
	 *            How to order the rows, formatted as an SQL ORDER BY clause.
	 *            Passing null will use the default sort order, which may be
	 *            unordered.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery order(String column) {
		mOrderBy = column;
		return this;
	}

	/**
	 * Limits the number of rows returned by the query.
	 * 
	 * <pre>
	 * DataSupport.limit(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the top 2 rows in Person table.
	 * 
	 * @param value
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery limit(int value) {
		mLimit = String.valueOf(value);
		return this;
	}

	/**
	 * Declaring the offset of rows returned by the query. This method must be
	 * used with {@link #limit(int)}, or nothing will return.
	 * 
	 * <pre>
	 * DataSupport.limit(1).offset(2).find(Person.class);
	 * </pre>
	 * 
	 * This will find the third row in Person table.
	 * 
	 * @param value
	 *            The offset amount of rows returned by the query.
	 * @return A ClusterQuery instance.
	 */
	public ClusterQuery offset(int value) {
		mOffset = String.valueOf(value);
		return this;
	}

	/**
	 * Finds multiple records by the cluster parameters. You can use the below
	 * way to finish a complicated query:
	 * 
	 * <pre>
	 * DataSupport.select(&quot;name&quot;).where(&quot;age &gt; ?&quot;, 14).order(&quot;age&quot;).limit(1).offset(2).find(Person.class);
	 * </pre>
	 * 
	 * You can also do the same job with SQLiteDatabase like this:
	 * 
	 * <pre>
	 * getSQLiteDatabase().query(&quot;Person&quot;, &quot;name&quot;, &quot;age &gt; ?&quot;, new String[] { &quot;14&quot; }, null, null, &quot;age&quot;,
	 * 		&quot;2,1&quot;);
	 * </pre>
	 * 
	 * Obviously, the first way is much more semantic.
	 * 
	 * @param modelClass
	 *            Which table to query and the object type to return as a list.
	 * @return An object list with founded data from database, or an empty list.
	 */
	public <T> List<T> find(Class<T> modelClass) {
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
