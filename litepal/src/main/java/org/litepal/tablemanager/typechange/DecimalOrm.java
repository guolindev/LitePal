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
	 * real as column type. Column name will be same as field name. Then return
	 * an array of the combination.
	 */
	@Override
	public String[] object2Relation(String className, String fieldName, String fieldType) {
		if (fieldName != null && fieldType != null) {
			String[] relations = new String[2];
			relations[0] = fieldName;
			if (fieldType.equals("float") || fieldType.equals("java.lang.Float")) {
				relations[1] = "REAL";
				return relations;
			}
			if (fieldType.equals("double") || fieldType.equals("java.lang.Double")) {
				relations[1] = "REAL";
				return relations;
			}
		}
		return null;
	}

}
