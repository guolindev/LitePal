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

import org.litepal.LitePalApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * LitePal used shared preferences a lot for storing versions and a lot of other
 * necessary values. This utility helps LitePal save and read each key-value
 * pairs from shared preferences file.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class SharedUtil {

	private static final String VERSION = "litepal_version";

	private static final String LITEPAL_PREPS = "litepal_prefs";

	/**
	 * Each time database upgrade, the version of database stored in shared
	 * preference will update.
	 * @param extraKeyName
	 * 			Pass the name of the using database usually. Pass null if it's default database.
	 * @param newVersion
     *          new version of database
	 */
	public static void updateVersion(String extraKeyName, int newVersion) {
		SharedPreferences.Editor sEditor = LitePalApplication.getContext()
				.getSharedPreferences(LITEPAL_PREPS, Context.MODE_PRIVATE).edit();
		if (TextUtils.isEmpty(extraKeyName)) {
			sEditor.putInt(VERSION, newVersion);
		} else {
            if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
            }
			sEditor.putInt(VERSION + "_" + extraKeyName, newVersion);
		}
		sEditor.apply();
	}

	/**
	 * Get the last database version.
	 * @param extraKeyName
	 * 			Pass the name of the using database usually. Pass null if it's default database.
	 * @return the last database version
	 */
	public static int getLastVersion(String extraKeyName) {
		SharedPreferences sPref = LitePalApplication.getContext().getSharedPreferences(
				LITEPAL_PREPS, Context.MODE_PRIVATE);
		if (TextUtils.isEmpty(extraKeyName)) {
			return sPref.getInt(VERSION, 0);
		} else {
            if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
            }
			return sPref.getInt(VERSION + "_" + extraKeyName, 0);
		}
	}

    /**
     * Remove the version with specified extra key name.
     * @param extraKeyName
     * 			Pass the name of the using database usually. Pass null if it's default database.
     */
    public static void removeVersion(String extraKeyName) {
        SharedPreferences.Editor sEditor = LitePalApplication.getContext()
                .getSharedPreferences(LITEPAL_PREPS, Context.MODE_PRIVATE).edit();
        if (TextUtils.isEmpty(extraKeyName)) {
            sEditor.remove(VERSION);
        } else {
            if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
            }
            sEditor.remove(VERSION + "_" + extraKeyName);
        }
        sEditor.apply();
    }

}
