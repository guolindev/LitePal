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
 * This is abstract super class to map the object field types to database column
 * types. The purpose of this class is to define a abstract method, and let all
 * subclasses implement it. Each subclass deals with a kind of changing and each
 * subclass will do their own logic to finish the changing job.
 * 
 * @author Tony Green
 * @since 1.0
 */
public abstract class OrmChange {

	/**
	 * Subclasses implement this method to do their own logic to change types.
	 *
	 * @param fieldType
	 *            The field type passed in.
	 * @return Column type.
	 */
	public abstract String object2Relation(String fieldType);

}
