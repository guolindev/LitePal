package org.litepal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalParser;
import org.litepal.tablemanager.Connector;

/**
 * LitePal is an Android library that allows developers to use SQLite database extremely easy.
 * You can initialized it by calling {@link #initialize(Context)} method to make LitePal ready to
 * work. Also you can switch the using database by calling {@link #use(LitePalDB)} and {@link #useDefault()}
 * methods.
 *
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

    /**
     * Switch the using database to the one specified by parameter.
     * @param litePalDB
     *          The database to switch to.
     */
    public static void use(LitePalDB litePalDB) {
        LitePalParser.parseLitePalConfiguration();
        LitePalAttr litePalAttr = LitePalAttr.getInstance();
        String dbNameInXML = litePalAttr.getDbName();
        litePalAttr.setDbName(litePalDB.getDbName());
        litePalAttr.setVersion(litePalDB.getVersion());
        litePalAttr.setStorage(litePalDB.isExternalStorage() ? "external" : "internal");
        litePalAttr.setClassNames(litePalDB.getClassNames());
        // set the extra key name only when use database other than default
        if (!dbNameInXML.equalsIgnoreCase(litePalDB.getDbName())) {
            litePalAttr.setExtraKeyName(litePalDB.getDbName());
            litePalAttr.setCases("lower");
        }
        Connector.clearLitePalOpenHelperInstance();
    }

    /**
     * Switch the using database to the one specified by litepal.xml.
     */
    public static void useDefault() {
        LitePalAttr.clearInstance();
        Connector.clearLitePalOpenHelperInstance();
    }

}
