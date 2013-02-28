package org.litepal;

import org.litepal.exceptions.GlobalException;

import android.app.Application;
import android.content.Context;

/**
 * Base class of LitePal to make things easier when developers need to use
 * context. When you need context, just use
 * <b>LitePalApplication.getContext()</b>. To make this function work, you need
 * to configure your AndroidManifest.xml. Specifying
 * <b>"org.litepal.LitePalApplication"</b> as the application name in your
 * &lt;application&gt; tag to enable LitePal get the context. Of course if you
 * need to write your own Application class, LitePal can still live with that.
 * But just remember make your own Application class inherited from
 * LitePalApplication instead of inheriting from Application directly. This can
 * make all things work without side effects.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class LitePalApplication extends Application {

	/**
	 * Global application context.
	 */
	private static Context mContext;

	/**
	 * Construct of LitePalApplication. Initialize application context.
	 */
	public LitePalApplication() {
		mContext = this;
	}

	/**
	 * Get the global application context.
	 * 
	 * @return Application context.
	 * @throws GlobalException
	 */
	public static Context getContext() {
		if (mContext == null) {
			throw new GlobalException(GlobalException.APPLICATION_CONTEXT_IS_NULL);
		}
		return mContext;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mContext = getApplicationContext();
	}

}
