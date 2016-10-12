package org.litepal;

import android.content.Context;

/**
 * @author Tony Green
 * @since 1.4
 */

public class LitePal {

    /**
     * Initialize to make LitePal ready to work. If you didn't configure LitePalApplication
     * in the AndroidManifest.xml, make sure you call this method as soon as possible. In
     * Application's onCreate() method will be fine.
     *
     * @param context
     * 		Application context.
     */
    public static void initialize(Context context) {
        LitePalApplication.sContext = context;
    }

    public static void use(LitePalDB litePalDB) {

    }

    public static void useLitePalXML() {

    }

}
