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

import android.util.Log;

public final class LitePalLog {
	
	public static final int DEBUG = 2;
	
	public static final int ERROR = 5;
	
	public static int level = ERROR;
	
	public static void d(String tagName, String message) {
		if (level <= DEBUG) {
			Log.d(tagName, message);
		}
	}
	
	public static void e(String tagName, Exception e){
		if (level <= ERROR) {
			Log.e(tagName, e.getMessage(), e);
		}
	}

}