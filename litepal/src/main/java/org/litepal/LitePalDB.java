package org.litepal;

import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of LitePal database. It's similar to litepal.xml configuration, but allows to
 * configure database details at runtime. This is very important when comes to support multiple
 * databases functionality.
 *
 * @author Tony Green
 * @since 1.4
 */
public class LitePalDB {

    /**
     * The version of database.
     */
    private int version;

    /**
     * The name of database.
     */
    private String dbName;

    /**
     * Indicates that the database file stores in external storage or not.
     */
    private boolean isExternalStorage = false;

    /**
     * All the model classes that want to map in the database. Each class should
     * be given the full name including package name.
     */
    private List<String> classNames;

    /**
     * Construct a LitePalDB instance from the default configuration by litepal.xml. But database
     * name must be different than the default.
     * @param dbName
     *          Name of database.
     * @return A LitePalDB instance which used the default configuration in litepal.xml but with a specified database name.
     */
    public static LitePalDB fromDefault(String dbName) {
        LitePalParser.parseLitePalConfiguration();
        LitePalAttr attr = LitePalAttr.getInstance();
        LitePalDB litePalDB = new LitePalDB(dbName, attr.getVersion());
        litePalDB.setExternalStorage("external".equals(attr.getStorage()));
        litePalDB.setClassNames(attr.getClassNames());
        return litePalDB;
    }

    /**
     * Construct a LitePalDB instance. Database name and version are necessary fields.
     * @param dbName
     *          Name of database.
     * @param version
     *          Version of database.
     */
    public LitePalDB(String dbName, int version) {
        this.dbName = dbName;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public String getDbName() {
        return dbName;
    }

    public boolean isExternalStorage() {
        return isExternalStorage;
    }

    public void setExternalStorage(boolean isExternalStorage) {
        this.isExternalStorage = isExternalStorage;
    }

    /**
     * Get the class name list. Always add table_schema as a value.
     *
     * @return The class name list.
     */
    public List<String> getClassNames() {
        if (classNames == null) {
            classNames = new ArrayList<>();
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

    void setClassNames(List<String> className) {
        this.classNames = className;
    }

}