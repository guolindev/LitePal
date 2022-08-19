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

package org.litepal.parser;

import java.util.ArrayList;
import java.util.List;

import org.litepal.exceptions.InvalidAttributesException;
import org.litepal.util.BaseUtility;
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
     * Define where the .db file should be. Option values: internal, external, or path in sdcard.
     */
    private String storage;

	/**
	 * All the model classes that want to map in the database. Each class should
	 * be given the full name including package name.
	 */
	private List<String> classNames;

    /**
     * Extra name as key for saving the database version in SharedUtil.
     */
	private String extraKeyName;

	/**
	 * Do not allow new a LitePalAttr object. Makes it a singleton class.
	 */
	private LitePalAttr() {
	}

	/**
	 * Provide a way to get the instance of LitePalAttr.
	 * @return the singleton instance of LitePalAttr
	 */
	public static LitePalAttr getInstance() {
		if (litePalAttr == null) {
			synchronized (LitePalAttr.class) {
				if (litePalAttr == null) {
					LitePalAttr litePalAttrTemp = new LitePalAttr();
					loadLitePalXMLConfiguration(litePalAttrTemp);
					litePalAttr = litePalAttrTemp;
				}
			}
		}
		return litePalAttr;
	}

	private static void loadLitePalXMLConfiguration(LitePalAttr litePalAttr) {
        if (BaseUtility.isLitePalXMLExists()) {
            LitePalConfig config = LitePalParser.parseLitePalConfiguration();
            litePalAttr.setDbName(config.getDbName());
            litePalAttr.setVersion(config.getVersion());
            litePalAttr.setClassNames(config.getClassNames());
            litePalAttr.setCases(config.getCases());
            litePalAttr.setStorage(config.getStorage());
        }
    }

	/**
	 * Clear the instance of LitePalAttr.
	 */
	public static void clearInstance() {
		litePalAttr = null;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

    public String getStorage() {
        return storage;
    }

	public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getExtraKeyName() {
        return extraKeyName;
    }

    public void setExtraKeyName(String extraKeyName) {
        this.extraKeyName = extraKeyName;
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
	 * Add a class name into the current mapping model list.
	 * 
	 * @param className
	 *            Full package class name.
	 */
	public void addClassName(String className) {
		getClassNames().add(className);
	}

	public void setClassNames(List<String> classNames) {
		this.classNames = classNames;
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
	 * If dbname is undefined, or version is less than 1, or version is earlier
	 * than current version, throw InvalidAttributesException.
	 * 
	 * @throws org.litepal.exceptions.InvalidAttributesException
	 */
	public void checkSelfValid() {
		if (TextUtils.isEmpty(dbName)) {
			loadLitePalXMLConfiguration(this);
            if (TextUtils.isEmpty(dbName)) {
                throw new InvalidAttributesException(
                        InvalidAttributesException.DBNAME_IS_EMPTY_OR_NOT_DEFINED);
            }
		}
		if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
			dbName = dbName + Const.Config.DB_NAME_SUFFIX;
		}
		if (version < 1) {
			throw new InvalidAttributesException(
					InvalidAttributesException.VERSION_OF_DATABASE_LESS_THAN_ONE);
		}
		if (version < SharedUtil.getLastVersion(extraKeyName)) {
			throw new InvalidAttributesException(
					InvalidAttributesException.VERSION_IS_EARLIER_THAN_CURRENT);
		}
		if (TextUtils.isEmpty(cases)) {
			cases = Const.Config.CASES_LOWER;
		} else {
			if (!cases.equals(Const.Config.CASES_UPPER)
					&& !cases.equals(Const.Config.CASES_LOWER)
					&& !cases.equals(Const.Config.CASES_KEEP)) {
				throw new InvalidAttributesException(cases
						+ InvalidAttributesException.CASES_VALUE_IS_INVALID);
			}
		}
	}

}
