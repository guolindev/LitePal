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

import java.util.Collection;
import java.util.Locale;

import org.litepal.exceptions.DataSupportException;
import org.litepal.parser.LitePalAttr;

import android.text.TextUtils;

/**
 * A utility class to help LitePal with some base actions that might through any
 * components. These actions can help classes just do the jobs they care, and
 * help them out of the trivial work.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class BaseUtility {

	/**
	 * Disable to create an instance of BaseUtility.
	 */
	private BaseUtility() {
	}

	/**
	 * It will change the case of the passing parameter into the case defined in
	 * litepal.xml file.
	 * 
	 * @param string
	 *            The string want to change case.
	 * @return The string after changing case. If the name is null, then simply
	 *         return null.
	 */
	public static String changeCase(String string) {
		if (string != null) {
			LitePalAttr litePalAttr = LitePalAttr.getInstance();
			String cases = litePalAttr.getCases();
			if (Const.LitePal.CASES_KEEP.equals(cases)) {
				return string;
			} else if (Const.LitePal.CASES_UPPER.equals(cases)) {
				return string.toUpperCase(Locale.US);
			}
			return string.toLowerCase(Locale.US);
		}
		return null;
	}

	/**
	 * This helper method makes up the shortage of contains method in Collection
	 * to support the function of case insensitive contains. It only supports
	 * the String generic type of collection, cause other types have no cases
	 * concept.
	 * 
	 * @param collection
	 *            The collection contains string data.
	 * @param string
	 *            The string want to look for in the collection.
	 * @return If the string is in the collection without case concern return
	 *         true, otherwise return false. If the collection is null, return
	 *         false.
	 */
	public static boolean containsIgnoreCases(Collection<String> collection, String string) {
		if (collection == null) {
			return false;
		}
		if (string == null) {
			return collection.contains(null);
		}
		boolean contains = false;
		for (String element : collection) {
			if (string.equalsIgnoreCase(element)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	/**
	 * Capitalize make the first letter of the word be upper case.
	 * 
	 * @param string
	 *            The word to capitalize.
	 * @return The word after capitalize.
	 */
	public static String capitalize(String string) {
		if (!TextUtils.isEmpty(string)) {
			return string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1);
		}
		return string == null ? null : "";
	}

	/**
	 * Count how many marks existed in string.
	 * 
	 * @param string
	 *            The source sentence.
	 * @param mark
	 *            The specific substring to count.
	 * @return The number of marks existed in string.
	 */
	public static int count(String string, String mark) {
		if (!TextUtils.isEmpty(string) && !TextUtils.isEmpty(mark)) {
			int count = 0;
			int index = string.indexOf(mark);
			while (index != -1) {
				count++;
				string = string.substring(index + mark.length());
				index = string.indexOf(mark);
			}
			return count;
		}
		return 0;
	}

	/**
	 * Check the number of question mark existed in conditions[0] equals the
	 * number of rest conditions elements or not. If not equals, throws
	 * DataSupportException.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @throws org.litepal.exceptions.DataSupportException
	 */
	public static void checkConditionsCorrect(String... conditions) {
		if (conditions != null) {
			int conditionsSize = conditions.length;
			if (conditionsSize > 0) {
				String whereClause = conditions[0];
				int placeHolderSize = BaseUtility.count(whereClause, "?");
				if (conditionsSize != placeHolderSize + 1) {
					throw new DataSupportException(DataSupportException.UPDATE_CONDITIONS_EXCEPTION);
				}
			}
		}
	}

	/**
	 * Judge a field type is supported or not. Currently only basic data types
	 * and String are supported.
	 * 
	 * @param fieldType
	 *            Text field type.
	 * @return Supported return true, not supported return false.
	 */
	public static boolean isFieldTypeSupported(String fieldType) {
		if ("boolean".equals(fieldType) || "java.lang.Boolean".equals(fieldType)) {
			return true;
		}
		if ("float".equals(fieldType) || "java.lang.Float".equals(fieldType)) {
			return true;
		}
		if ("double".equals(fieldType) || "java.lang.Double".equals(fieldType)) {
			return true;
		}
		if ("int".equals(fieldType) || "java.lang.Integer".equals(fieldType)) {
			return true;
		}
		if ("long".equals(fieldType) || "java.lang.Long".equals(fieldType)) {
			return true;
		}
		if ("short".equals(fieldType) || "java.lang.Short".equals(fieldType)) {
			return true;
		}
		if ("char".equals(fieldType) || "java.lang.Character".equals(fieldType)) {
			return true;
		}
		if ("[B".equals(fieldType) || "[Ljava.lang.Byte;".equals(fieldType)) {
			return true;
		}
		if ("java.lang.String".equals(fieldType) || "java.util.Date".equals(fieldType)) {
			return true;
		}
		return false;
	}

}
