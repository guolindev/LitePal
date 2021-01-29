package com.litepaltest.test;

import android.database.sqlite.SQLiteDatabase;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.Computer;
import com.litepaltest.model.Headset;
import com.litepaltest.model.Product;

import org.junit.Test;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.util.DBUtility;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * @author guolin
 * @since 2016/11/10
 */
@SmallTest
public class MultiDatabaseTest extends LitePalTestCase {

    @Test
    public void testMultiDatabase() {
        LitePal.deleteDatabase("db2");
        SQLiteDatabase db = LitePal.getDatabase();
        assertTrue(DBUtility.isTableExists("Album", db));
        assertTrue(DBUtility.isTableExists("Song", db));
        assertTrue(DBUtility.isTableExists("Singer", db));
        assertTrue(DBUtility.isTableExists("Classroom", db));
        assertTrue(DBUtility.isTableExists("Teacher", db));
        assertTrue(DBUtility.isTableExists("IdCard", db));
        assertTrue(DBUtility.isTableExists("Student", db));
        assertTrue(DBUtility.isTableExists("Cellphone", db));
        assertTrue(DBUtility.isTableExists("Computer", db));
        assertTrue(DBUtility.isTableExists("Book", db));
        assertTrue(DBUtility.isTableExists("Product", db));
        assertTrue(DBUtility.isTableExists("Headset", db));
        assertTrue(DBUtility.isTableExists("WeChatMessage", db));
        assertTrue(DBUtility.isTableExists("WeiboMessage", db));

        LitePalDB litePalDB = new LitePalDB("db2", 1);
        litePalDB.addClassName(Classroom.class.getName());
        litePalDB.addClassName(Product.class.getName());
        litePalDB.setExternalStorage(true);
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
        assertFalse(DBUtility.isTableExists("Album", db));
        assertFalse(DBUtility.isTableExists("Song", db));
        assertFalse(DBUtility.isTableExists("Singer", db));
        assertTrue(DBUtility.isTableExists("Classroom", db));
        assertFalse(DBUtility.isTableExists("Teacher", db));
        assertFalse(DBUtility.isTableExists("IdCard", db));
        assertFalse(DBUtility.isTableExists("Student", db));
        assertFalse(DBUtility.isTableExists("Cellphone", db));
        assertFalse(DBUtility.isTableExists("Computer", db));
        assertFalse(DBUtility.isTableExists("Book", db));
        assertTrue(DBUtility.isTableExists("Product", db));
        assertFalse(DBUtility.isTableExists("Headset", db));
        assertFalse(DBUtility.isTableExists("WeChatMessage", db));
        assertFalse(DBUtility.isTableExists("WeiboMessage", db));

        litePalDB = new LitePalDB("db2", 2);
        litePalDB.addClassName(Computer.class.getName());
        litePalDB.addClassName(Product.class.getName());
        litePalDB.addClassName(Headset.class.getName());
        litePalDB.setExternalStorage(true);
        LitePal.use(litePalDB);
        db = LitePal.getDatabase();
        assertFalse(DBUtility.isTableExists("Album", db));
        assertFalse(DBUtility.isTableExists("Song", db));
        assertFalse(DBUtility.isTableExists("Singer", db));
        assertFalse(DBUtility.isTableExists("Classroom", db));
        assertFalse(DBUtility.isTableExists("Teacher", db));
        assertFalse(DBUtility.isTableExists("IdCard", db));
        assertFalse(DBUtility.isTableExists("Student", db));
        assertFalse(DBUtility.isTableExists("Cellphone", db));
        assertTrue(DBUtility.isTableExists("Computer", db));
        assertFalse(DBUtility.isTableExists("Book", db));
        assertTrue(DBUtility.isTableExists("Product", db));
        assertTrue(DBUtility.isTableExists("Headset", db));
        assertFalse(DBUtility.isTableExists("WeChatMessage", db));
        assertFalse(DBUtility.isTableExists("WeiboMessage", db));

        LitePal.useDefault();
        db = LitePal.getDatabase();
        assertTrue(DBUtility.isTableExists("Album", db));
        assertTrue(DBUtility.isTableExists("Song", db));
        assertTrue(DBUtility.isTableExists("Singer", db));
        assertTrue(DBUtility.isTableExists("Classroom", db));
        assertTrue(DBUtility.isTableExists("Teacher", db));
        assertTrue(DBUtility.isTableExists("IdCard", db));
        assertTrue(DBUtility.isTableExists("Student", db));
        assertTrue(DBUtility.isTableExists("Cellphone", db));
        assertTrue(DBUtility.isTableExists("Computer", db));
        assertTrue(DBUtility.isTableExists("Book", db));
        assertTrue(DBUtility.isTableExists("Product", db));
        assertTrue(DBUtility.isTableExists("Headset", db));
        assertTrue(DBUtility.isTableExists("WeChatMessage", db));
        assertTrue(DBUtility.isTableExists("WeiboMessage", db));
    }

}
