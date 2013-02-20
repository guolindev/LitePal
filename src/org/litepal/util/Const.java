package org.litepal.util;

public interface Const {

	public interface Model {
		/**
		 * One2One constant value.
		 */
		public static final int ONE_TO_ONE = 1;

		/**
		 * Many2One constant value.
		 */
		public static final int MANY_TO_ONE = 2;

		/**
		 * Many2Many constant value.
		 */
		public static final int MANY_TO_MANY = 3;
	}

	public interface LitePal {
		/**
		 * The suffix for each database file.
		 */
		public static final String DB_NAME_SUFFIX = ".db";

		/**
		 * Constant for upper case.
		 */
		public static final String CASES_UPPER = "upper";

		/**
		 * Constant for lower case.
		 */
		public static final String CASES_LOWER = "lower";

		/**
		 * Constant for keep case.
		 */
		public static final String CASES_KEEP = "keep";

		/**
		 * Constant configuration file name.
		 */
		public static final String CONFIGURATION_FILE_NAME = "litepal.xml";
	}

	public interface TableSchema {
		/**
		 * Table name in database.
		 */
		public static final String TABLE_NAME = "table_schema";

		/**
		 * The name column in table_schema.
		 */
		public static final String COLUMN_NAME = "name";

		/**
		 * The type column in table_schema.
		 */
		public static final String COLUMN_TYPE = "type";

		/**
		 * Constant for normal table.
		 */
		public static final int NORMAL_TABLE = 0;

		/**
		 * Constant for intermediate join table.
		 */
		public static final int INTERMEDIATE_JOIN_TABLE = 1;
	}

}
