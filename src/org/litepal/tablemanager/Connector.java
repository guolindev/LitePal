package org.litepal.tablemanager;

import org.litepal.exceptions.InvalidAttributesException;
import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalParser;

import android.database.sqlite.SQLiteDatabase;

/**
 * The connector to connect database provided by LitePal. Users can use this
 * class to get the instance of SQLiteDatabase. But users still need to write
 * their own CRUD logic by the returned SQLiteDatabase. It will be improved in
 * the future.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class Connector {

	/**
	 * LitePalAttr model.
	 */
	private static LitePalAttr mLitePalAttr;

	/**
	 * The quote of LitePalHelper.
	 */
	private static LitePalOpenHelper mLitePalHelper;

	/**
	 * Get a writable SQLiteDatabase.
	 * 
	 * There're a lot of ways to operate database in android. But LitePal
	 * doesn't support using ContentProvider currently. The best way to use
	 * LitePal well is get the SQLiteDatabase instance and use the methods like
	 * SQLiteDatabase#save, SQLiteDatabase#update, SQLiteDatabase#delete,
	 * SQLiteDatabase#query in the SQLiteDatabase class to do the database
	 * operation. It will be improved in the future.
	 * 
	 * @return A writable SQLiteDatabase instance
	 * 
	 * @throws InvalidAttributesException
	 * @throws ParseConfigurationFileException
	 */
	public synchronized static SQLiteDatabase getWritableDatabase() {
		LitePalOpenHelper litePalHelper = buildConnection();
		return litePalHelper.getWritableDatabase();
	}

	/**
	 * Get a readable SQLiteDatabase.
	 * 
	 * There're a lot of ways to operate database in android. But LitePal
	 * doesn't support using ContentProvider currently. The best way to use
	 * LitePal well is get the SQLiteDatabase instance and use the methods like
	 * SQLiteDatabase#query in the SQLiteDatabase class to do the database
	 * query. It will be improved in the future.
	 * 
	 * @return A readable SQLiteDatabase instance.
	 * 
	 * @throws InvalidAttributesException
	 * @throws ParseConfigurationFileException
	 */
	public synchronized static SQLiteDatabase getReadableDatabase() {
		LitePalOpenHelper litePalHelper = buildConnection();
		return litePalHelper.getReadableDatabase();
	}

	/**
	 * Call getDatabase directly will invoke the getWritableDatabase method by
	 * default.
	 * 
	 * This is method is alias of getWritableDatabase.
	 * 
	 * @return A writable SQLiteDatabase instance
	 * 
	 * @throws InvalidAttributesException
	 * @throws ParseConfigurationFileException
	 */
	public static SQLiteDatabase getDatabase() {
		return getWritableDatabase();
	}

	/**
	 * Build a connection to the database. This progress will analysis the
	 * litepal.xml file, and will check if the fields in LitePalAttr are valid,
	 * and it will open a SQLiteOpenHelper to decide to create tables or update
	 * tables or doing nothing depends on the version attributes.
	 * 
	 * After all the stuffs above are finished. This method will return a
	 * LitePalHelper object.Notes this method could throw a lot of exceptions.
	 * 
	 * @return LitePalHelper object.
	 * 
	 * @throws InvalidAttributesException
	 * @throws ParseConfigurationFileException
	 */
	private static LitePalOpenHelper buildConnection() {
		if (mLitePalAttr == null) {
			LitePalParser.parseLitePalConfiguration();
			mLitePalAttr = LitePalAttr.getInstance();
		}
		if (mLitePalAttr.checkSelfValid()) {
			if (mLitePalHelper == null) {
				mLitePalHelper = new LitePalOpenHelper(mLitePalAttr.getDbName(),
						mLitePalAttr.getVersion());
			}
			return mLitePalHelper;
		} else {
			throw new InvalidAttributesException("Uncaught invalid attributes exception happened");
		}
	}

}
