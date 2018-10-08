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
 * LitePalSupportException.
 * 
 * @author Tony Green
 * @since 2.0
 */
public class LitePalSupportException extends DataSupportException {
	private static final long serialVersionUID = 1L;

	/**
	 * Thrown when models have invalid type for id fields. Only int or long is
	 * supported.
	 */
	public static final String ID_TYPE_INVALID_EXCEPTION = "id type is not supported. Only int or long is acceptable for id";

	/**
	 * Thrown when the saving model is not an instance of LitePalSupport.
	 */
	public static final String MODEL_IS_NOT_AN_INSTANCE_OF_LITE_PAL_SUPPORT = " should be inherited from LitePalSupport";

	/**
	 * Thrown when developers use wrong field to declare many2one or many2many
	 * associations.
	 */
	public static final String WRONG_FIELD_TYPE_FOR_ASSOCIATIONS = "The field to declare many2one or many2many associations should be List or Set.";

	/**
	 * Thrown when fail to save a model.
	 */
	public static final String SAVE_FAILED = "Save current model failed.";

	/**
	 * Thrown when there is no default constructor in model class to update.
	 */
	public static final String INSTANTIATION_EXCEPTION = " needs a default constructor.";

	/**
	 * Thrown when the parameters in conditions are incorrect.
	 */
	public static final String UPDATE_CONDITIONS_EXCEPTION = "The parameters in conditions are incorrect.";

	/**
	 * Constructor of LitePalSupportException.
	 * 
	 * @param errorMessage
	 *            the description of this exception.
	 */
	public LitePalSupportException(String errorMessage) {
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
    public LitePalSupportException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }

	/**
	 * Thrown when the VM notices that a program tries to reference, on a class
	 * or object, a method that does not exist.
	 * 
	 * @param className
	 *            The class name.
	 * @param methodName
	 *            The method name which is missing.
	 * @return Exception message.
	 */
	public static String noSuchMethodException(String className, String methodName) {
		return "The " + methodName + " method in " + className
				+ " class is necessary which does not exist.";
	}

	/**
	 * Thrown when the virtual machine notices that a program tries to
	 * reference, on a class or object, a field that does not exist.
	 * 
	 * @param className
	 *            The class name.
	 * @param fieldName
	 *            The field name which is missing.
	 * @return Exception message.
	 */
	public static String noSuchFieldExceptioin(String className, String fieldName) {
		return "The " + fieldName + " field in " + className
				+ " class is necessary which does not exist.";
	}

}
