package com.litepaltest.test.crud.query;

import android.database.Cursor;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Book;

import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;
import org.litepal.exceptions.DataSupportException;
import org.litepal.util.DBUtility;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

@SmallTest
public class QueryBySQLTest {

	private Book book;

    private String bookTable;

	@Before
	public void setUp() {
        bookTable = DBUtility.getTableNameByClassName(Book.class.getName());
		book = new Book();
		book.setBookName("数据库");
		book.setPages(300);
		book.save();
	}

	@Test
	public void testQueryBySQL() {
		Cursor cursor = LitePal.findBySQL("select * from " + bookTable);
		assertTrue(cursor.getCount() > 0);
		cursor.close();
	}

    @Test
	public void testQueryBySQLWithPlaceHolder() {
		Cursor cursor = LitePal.findBySQL(
				"select * from " + bookTable + " where id=? and bookname=? and pages=?",
				String.valueOf(book.getId()), "数据库", "300");
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		String bookName = cursor.getString(cursor.getColumnIndexOrThrow("bookname"));
		int pages = cursor.getInt(cursor.getColumnIndexOrThrow("pages"));
		assertEquals(bookName, "数据库");
		assertEquals(pages, 300);
		cursor.close();
	}

    @Test
	public void testQueryBySQLWithWrongParams() {
		try {
            LitePal.findBySQL("select * from " + bookTable + " where id=? and bookname=? and pages=?",
					String.valueOf(book.getId()), "数据库");
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		Cursor cursor = LitePal.findBySQL();
		assertNull(cursor);
		cursor = LitePal.findBySQL();
		assertNull(cursor);
	}

}
