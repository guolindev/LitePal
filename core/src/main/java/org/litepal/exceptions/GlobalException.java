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
 * This is where all the global exceptions declared of LitePal.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class GlobalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Application context is null.
	 */
	public static final String APPLICATION_CONTEXT_IS_NULL = "Application context is null. Maybe you neither configured your application name with \"org.litepal.LitePalApplication\" in your AndroidManifest.xml, nor called LitePal.initialize(Context) method.";

	/**
	 * Constructor of GlobalException.
	 * 
	 * @param errorMessage
	 *            the description of this exception.
	 */
	public GlobalException(String errorMessage) {
		super(errorMessage);
	}
}
