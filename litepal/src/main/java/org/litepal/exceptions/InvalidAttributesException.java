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
