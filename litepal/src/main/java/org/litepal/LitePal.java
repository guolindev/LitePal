package org.litepal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.Connector;

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

    /**
     * Get a writable SQLiteDatabase.
     *
     * @return A writable SQLiteDatabase instance
     */
    public static SQLiteDatabase getDatabase() {
        return Connector.getDatabase();
    }

    public static void use(LitePalDB litePalDB) {
        LitePalAttr litePalAttr = LitePalAttr.getInstance();
        litePalAttr.setDbName(litePalDB.getDbName());
        litePalAttr.setVersion(litePalDB.getVersion());
        litePalAttr.setStorage(litePalDB.isExternalStorage() ? "external" : "internal");
        litePalAttr.setClassNames(litePalDB.getClassNames());
        litePalAttr.setExtraKeyName(litePalDB.getDbName());
        litePalAttr.setCases("lower");
        Connector.clearLitePalOpenHelperInstance();
    }

    public static void useDefault() {
        LitePalAttr.clearInstance();
        Connector.clearLitePalOpenHelperInstance();
    }

}
