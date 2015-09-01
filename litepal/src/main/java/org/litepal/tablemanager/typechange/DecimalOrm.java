/*
 * Copyright (C)  Tony Green, Litepal Framework Open Source Project
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

package org.litepal.tablemanager.typechange;

/**
 * This class deals with decimal type.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class DecimalOrm extends OrmChange {

	/**
	 * If the field type passed in is float or double, it will change it into
	 * real as column type.
	 */
	@Override
	public String object2Relation(String fieldType) {
		if (fieldType != null) {
			if (fieldType.equals("float") || fieldType.equals("java.lang.Float")) {
				return "real";
			}
			if (fieldType.equals("double") || fieldType.equals("java.lang.Double")) {
				return "real";
			}
		}
		return null;
	}

}
