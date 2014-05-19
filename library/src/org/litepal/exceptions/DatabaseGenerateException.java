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
	 * Constructor of DatabaseGenerateException.
	 * 
	 * @param errorMessage
	 *            the description of this DatabaseGenerateException.
	 */
	public DatabaseGenerateException(String errorMessage) {
		super(errorMessage);
	}

}
