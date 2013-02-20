package org.litepal.parser;

import java.util.ArrayList;
import java.util.List;

import org.litepal.exceptions.InvalidAttributesException;
import org.litepal.util.Const;
import org.litepal.util.SharedUtil;

import android.text.TextUtils;

/**
 * The object model for the litepal.xml file. Once database connection happens,
 * LitePal will try to analysis the litepal.xml, and read all the attribute into
 * the LitePalAttr model for further usage.
 * 
 * @author Tony Green
 * @since 1.0
 */
public final class LitePalAttr {

	/**
	 * Static litePalAttr object.
	 */
	private static LitePalAttr litePalAttr;

	/**
	 * The version of database.
	 */
	private int version;

	/**
	 * The name of database.
	 */
	private String dbName;

	/**
	 * The case of table names and column names and SQL.
	 */
	private String cases;

	/**
	 * All the model classes that want to map in the database. Each class should
	 * be given the full name including package name.
	 */
	private List<String> classNames;

	/**
	 * Do not allow new a LitePalAttr object. Makes it a singleton class.
	 */
	private LitePalAttr() {
	}

	/**
	 * Provide a way to get the object of LitePalAttr class.
	 * 
	 * @return the singleton object of LitePalAttr
	 */
	public static LitePalAttr getInstance() {
		if (litePalAttr == null) {
			synchronized (LitePalAttr.class) {
				if (litePalAttr == null) {
					litePalAttr = new LitePalAttr();
				}
			}
		}
		return litePalAttr;
	}

	public int getVersion() {
		return version;
	}

	void setVersion(int version) {
		this.version = version;
	}

	public String getDbName() {
		return dbName;
	}

	void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * Get the class name list. Always add table_schema as a value.
	 * 
	 * @return The class name list.
	 */
	public List<String> getClassNames() {
		if (classNames == null) {
			classNames = new ArrayList<String>();
			classNames.add("org.litepal.model.Table_Schema");
		} else if (classNames.isEmpty()) {
			classNames.add("org.litepal.model.Table_Schema");
		}
		return classNames;
	}

	/**
	 * Add a class name into the current list.
	 * 
	 * @param className
	 *            The class name.
	 */
	public void addClassName(String className) {
		getClassNames().add(className);
	}

	public String getCases() {
		return cases;
	}

	public void setCases(String cases) {
		this.cases = cases;
	}

	/**
	 * Before application build the connection with database, check the fields
	 * in LitePalAttr. If all of the fields are passed, the connection will be
	 * continued.If anyone of them doesn't pass, an exception will be thrown.
	 * 
	 * @return If all of the fields are passed, return true. If dbname is
	 *         undefined, or version is less than 1, or version is earlier than
	 *         current version, throw InvalidAttributesException
	 * 
	 * @throws InvalidAttributesException
	 */
	public boolean checkSelfValid() {
		if (TextUtils.isEmpty(dbName)) {
			throw new InvalidAttributesException(
					InvalidAttributesException.DBNAME_IS_EMPTY_OR_NOT_DEFINED);
		}
		if (!dbName.endsWith(Const.LitePal.DB_NAME_SUFFIX)) {
			dbName = dbName + Const.LitePal.DB_NAME_SUFFIX;
		}
		if (version < 1) {
			throw new InvalidAttributesException(
					InvalidAttributesException.VERSION_OF_DATABASE_LESS_THAN_ONE);
		}
		if (version < SharedUtil.getLastVersion()) {
			throw new InvalidAttributesException(
					InvalidAttributesException.VERSION_IS_EARLIER_THAN_CURRENT);
		}
		if (TextUtils.isEmpty(cases)) {
			cases = Const.LitePal.CASES_LOWER;
		} else {
			if (!cases.equals(Const.LitePal.CASES_UPPER)
					&& !cases.equals(Const.LitePal.CASES_LOWER)
					&& !cases.equals(Const.LitePal.CASES_KEEP)) {
				throw new InvalidAttributesException(cases
						+ InvalidAttributesException.CASES_VALUE_IS_INVALID);
			}
		}
		return true;
	}

}
