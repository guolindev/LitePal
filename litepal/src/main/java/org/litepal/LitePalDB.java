package org.litepal;

import java.util.ArrayList;
import java.util.List;

/**
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

    public static void fromDefault(String dbName) {

    }

    public LitePalDB(String dbName, int version) {
        this.dbName = dbName;
        this.version = version;
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

}