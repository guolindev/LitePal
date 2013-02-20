package org.litepal.exceptions;

/**
 * Reading the attributes in the litepal.xml file. Check all the attributes if they
 * are valid value. If anyone of them is not under rules, throw
 * InvalidAttributesException exception.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class InvalidAttributesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * dbname is empty or not defined in litepal.xml file.
	 */
	public static final String DBNAME_IS_EMPTY_OR_NOT_DEFINED = "dbname is empty or not defined in litepal.xml file";

	/**
	 * the version of database can not be less than 1.
	 */
	public static final String VERSION_OF_DATABASE_LESS_THAN_ONE = "the version of database can not be less than 1";

	/**
	 * the version in litepal.xml is earlier than the current version.
	 */
	public static final String VERSION_IS_EARLIER_THAN_CURRENT = "the version in litepal.xml is earlier than the current version";
	
	/**
	 * There's an invalid value in cases mark. Only keep, upper, lower allowed.
	 */
	public static final String CASES_VALUE_IS_INVALID = " is an invalid value for <cases></cases>";

	/**
	 * Constructor of InvalidAttributesException.
	 * 
	 * @param errorMessage
	 *            the description of this InvalidAttributesException.
	 */
	public InvalidAttributesException(String errorMessage) {
		super(errorMessage);
	}

}
