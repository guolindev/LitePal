package org.litepal.util;

import org.litepal.LitePalApplication;

import android.content.Context;
import android.content.SharedPreferences;

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
	 * 
	 * @param context
	 * @param newVersion
	 */
	public static void updateVersion(int newVersion) {
		SharedPreferences.Editor sEditor = LitePalApplication.getContext()
				.getSharedPreferences(LITEPAL_PREPS, Context.MODE_PRIVATE).edit();
		sEditor.putInt(VERSION, newVersion);
		sEditor.commit();
	}

	/**
	 * Get the last database version.
	 * 
	 * @return the last database version
	 */
	public static int getLastVersion() {
		SharedPreferences sPref = LitePalApplication.getContext().getSharedPreferences(
				LITEPAL_PREPS, Context.MODE_PRIVATE);
		int version = sPref.getInt(VERSION, 0);
		return version;
	}

}
