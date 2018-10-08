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

	public interface Config {
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

        /**
         * Constant for generic table.
         */
        public static final int GENERIC_TABLE = 2;
	}

}
