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

package org.litepal.exceptions;

/**
 * When LitePal generate or update tables, it may throw DatabaseGenerateException.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class DatabaseGenerateException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Can not find a class with the passing class name.
	 */
	public static final String CLASS_NOT_FOUND = "can not find a class named ";

	/**
	 * An exception that indicates there was an error with SQL parsing or
	 * execution.
	 */
	public static final String SQL_ERROR = "An exception that indicates there was an error with SQL parsing or execution. ";

	/**
	 * SQL syntax error when executing generation job.
	 */
	public static final String SQL_SYNTAX_ERROR = "SQL syntax error happens while executing ";

	/**
	 * Can not find a table with the passing table name when executing SQL.
	 */
	public static final String TABLE_DOES_NOT_EXIST_WHEN_EXECUTING = "Table doesn't exist when executing ";

	/**
	 * Can not find a table with the passing table name.
	 */
	public static final String TABLE_DOES_NOT_EXIST = "Table doesn't exist with the name of ";

    /**
     * Don't have permission to create database on sdcard.
     */
    public static final String EXTERNAL_STORAGE_PERMISSION_DENIED = "You don't have permission to access database at %1$s. Make sure you handled WRITE_EXTERNAL_STORAGE runtime permission correctly.";

	/**
	 * Constructor of DatabaseGenerateException.
	 * 
	 * @param errorMessage
	 *            the description of this DatabaseGenerateException.
	 */
	public DatabaseGenerateException(String errorMessage) {
		super(errorMessage);
	}

}
