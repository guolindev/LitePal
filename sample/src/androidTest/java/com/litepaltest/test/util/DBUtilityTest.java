package com.litepaltest.test.util;

import android.database.sqlite.SQLiteDatabase;

import com.litepaltest.model.Book;
import com.litepaltest.model.Cellphone;
import com.litepaltest.test.LitePalTestCase;

import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import java.util.List;

public class DBUtilityTest extends LitePalTestCase {

    SQLiteDatabase db;

    @Override
    protected void setUp() throws Exception {
        db = Connector.getDatabase();
    }

    public void testFindUniqueColumns() {
        List<String> uniqueColumns = DBUtility.findUniqueColumns(DBUtility.getTableNameByClassName(
                        Cellphone.class.getName()), db);
        assertEquals(1, uniqueColumns.size());
        assertTrue(uniqueColumns.contains("serial"));
        uniqueColumns = DBUtility.findUniqueColumns(DBUtility.getTableNameByClassName(Book.class.getName()), db);
        assertEquals(0, uniqueColumns.size());
    }

}
