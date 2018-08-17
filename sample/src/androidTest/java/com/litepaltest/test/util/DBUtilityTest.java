package com.litepaltest.test.util;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.filters.SmallTest;

import com.litepaltest.model.Book;
import com.litepaltest.model.Cellphone;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Before;
import org.junit.Test;
import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class DBUtilityTest extends LitePalTestCase {

    SQLiteDatabase db;

    @Before
    public void setUp() {
        db = Connector.getDatabase();
    }

    @Test
    public void testFindUniqueColumns() {
        List<String> uniqueColumns = DBUtility.findUniqueColumns(DBUtility.getTableNameByClassName(
                        Cellphone.class.getName()), db);
        assertEquals(1, uniqueColumns.size());
        assertTrue(uniqueColumns.contains("serial"));
        uniqueColumns = DBUtility.findUniqueColumns(DBUtility.getTableNameByClassName(Book.class.getName()), db);
        assertEquals(0, uniqueColumns.size());
    }

}
