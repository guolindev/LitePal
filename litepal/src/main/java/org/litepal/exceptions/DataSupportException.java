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
 * When LitePal deals with CRUD actions of LitePalSupport, it may throw
 * DataSupportException for older versions API. The new CRUD APIs should throw
 * {@link LitePalSupportException}
 * 
 * @author Tony Green
 * @since 1.1
 */
public class DataSupportException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of LitePalSupportException.
	 *
	 * @param errorMessage
	 *            the description of this exception.
	 */
	public DataSupportException(String errorMessage) {
		super(errorMessage);
	}

    /**
     * Constructor of LitePalSupportException.
     *
     * @param errorMessage
     *            the description of this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public DataSupportException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }

}
