package org.litepal.exceptions;

/**
 * Using SAX way parsing XML by default. If any problem happens when parsing the
 * litepal.xml file, ParseConfigurationFileException will be thrown.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class ParseConfigurationFileException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * can not find the litepal.xml file by the given id.
	 */
	public static final String CAN_NOT_FIND_LITEPAL_FILE = "litepal.xml file is missing. Please ensure it under assets folder.";

	/**
	 * can not parse the litepal.xml, check if it's in correct format.
	 */
	public static final String FILE_FORMAT_IS_NOT_CORRECT = "can not parse the litepal.xml, check if it's in correct format";

	/**
	 * parse configuration is failed.
	 */
	public static final String PARSE_CONFIG_FAILED = "parse configuration is failed";

	/**
	 * IO exception happened.
	 */
	public static final String IO_EXCEPTION = "IO exception happened";

	/**
	 * Constructor of ParseConfigurationFileException.
	 * 
	 * @param errorMessage
	 *            the description of this exception.
	 */
	public ParseConfigurationFileException(String errorMessage) {
		super(errorMessage);
	}

}
