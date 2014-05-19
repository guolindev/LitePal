package org.litepal.util;

import android.util.Log;

public final class LogUtil {

	public static void d(String tagName, String message) {
		Log.d(tagName, message);
	}
	
	public static void e(String tagName, Exception e){
		e.printStackTrace();
	}

}
