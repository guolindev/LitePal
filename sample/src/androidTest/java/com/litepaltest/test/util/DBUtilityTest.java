package com.litepaltest.test.util;

import android.database.sqlite.SQLiteDatabase;
import androidx.test.filters.SmallTest;
import android.util.Pair;

import com.litepaltest.model.Book;
import com.litepaltest.model.Cellphone;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Before;
import org.junit.Test;
import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import java.util.Set;

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
    public void testFindIndexedColumns() {
        Pair<Set<String>, Set<String>> pair = DBUtility.findIndexedColumns(DBUtility.getTableNameByClassName(Cellphone.class.getName()), db);
        Set<String> indexColumns = pair.first;
        Set<String> uniqueColumns = pair.second;
        assertEquals(1, indexColumns.size());
        assertEquals(1, uniqueColumns.size());
        assertTrue(indexColumns.contains("brand"));
        assertTrue(uniqueColumns.contains("serial"));
        pair = DBUtility.findIndexedColumns(DBUtility.getTableNameByClassName(Book.class.getName()), db);
        indexColumns = pair.first;
        uniqueColumns = pair.second;
        assertEquals(0, indexColumns.size());
        assertEquals(0, uniqueColumns.size());
    }

}
