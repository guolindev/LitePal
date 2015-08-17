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
