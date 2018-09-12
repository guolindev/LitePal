package org.litepal.tablemanager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.litepal.LitePal
import org.litepal.LitePalApplication
import org.litepal.parser.LitePalAttr
import org.litepal.util.SharedUtil

/**
 * The database helper to generate and manage the tables. It will automate
 * create or upgrade the database file depends on the parameters passed in.
 *
 * LitePal makes it easy for managing tables. It used the dynamic features of
 * Java with reflection API to achieve that. Developers won't need to write
 * their own SQL for managing tables, LitePal will do that for them. Developers
 * just need to write their model classes and add right associations. LitePal
 * will take all the rest job to manager tables in database.
 *
 * @author Tony Green
 * @since 1.0
 */
internal class LitePalOpenHelper
/**
 * The standard constructor for SQLiteOpenHelper.
 *
 * @param context
 * To use to open or create the database.
 * @param name
 * The database file.
 * @param factory
 * To use for creating cursor objects, or null for the default
 * version number of the database (starting at 1); if the
 * database is older, onUpgrade.
 * @param version
 * (SQLiteDatabase, int, int) will be used to upgrade the
 * database; if the database is newer,
 * onDowngrade(SQLiteDatabase, int, int) will be used to
 * downgrade the database
 */
(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    /**
     * A simple constructor for SQLiteOpenHelper with null for CursorFactory as
     * default.
     *
     * @param dbName
     * The database file.
     * @param version
     * (SQLiteDatabase, int, int) will be used to upgrade the
     * database; if the database is newer,
     * onDowngrade(SQLiteDatabase, int, int) will be used to
     * downgrade the database
     */
    constructor(dbName: String, version: Int) : this(LitePalApplication.getContext(), dbName, null, version)

    override fun onCreate(db: SQLiteDatabase) {
        Generator.create(db)
        LitePal.getDBListener()?.onCreate()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Generator.upgrade(db)
        SharedUtil.updateVersion(LitePalAttr.getInstance().extraKeyName, newVersion)
        LitePal.getDBListener()?.onUpgrade(oldVersion, newVersion)
    }

    companion object {
        const val TAG = "LitePalHelper"
    }

}