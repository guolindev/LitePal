package com.litepaltest.test.crud.query;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.Book;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;
import org.litepal.LitePal;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

@SmallTest
public class QueryBasicTest extends LitePalTestCase {

    @Test
	public void testFind() {
		short isbn = 30013;
		Book book = new Book();
		book.setArea(10.5f);
		book.setBookName("Android First Line");
		book.setIsbn(isbn);
		book.setLevel('A');
		book.setPages(450);
		book.setPrice(49.99);
		book.setPublished(false);
		book.save();
		Book b = LitePal.find(Book.class, book.getId());
		assertEquals(book.getId(), b.getId());
		assertEquals(10.5f, b.getArea());
		assertEquals("Android First Line", b.getBookName());
		assertEquals(isbn, b.getIsbn());
		assertEquals('A', b.getLevel());
		assertTrue(450 == b.getPages());
		assertEquals(49.99, b.getPrice());
		assertFalse(b.isPublished());
		assertTrue(b.isSaved());
	}

    @Test
	public void testFindMul() {
		short isbn1 = 30017;
		Book book1 = new Book();
		book1.setArea(1.5f);
		book1.setBookName("Android Second Line");
		book1.setIsbn(isbn1);
		book1.setLevel('B');
		book1.setPages(434);
		book1.setPrice(40.99);
		book1.setPublished(true);
		book1.save();
		short isbn2 = 30014;
		Book book2 = new Book();
		book2.setArea(8.8f);
		book2.setBookName("Android Third Line");
		book2.setIsbn(isbn2);
		book2.setLevel('C');
		book2.setPages(411);
		book2.setPrice(35.99);
		book2.setPublished(false);
		book2.save();
		List<Book> bookList = LitePal.findAll(Book.class, book1.getId(), book2.getId());
		assertEquals(2, bookList.size());
		for (Book book : bookList) {
			if (book.getId() == book1.getId()) {
				assertEquals(1.5f, book.getArea());
				assertEquals("Android Second Line", book.getBookName());
				assertEquals(isbn1, book.getIsbn());
				assertEquals('B', book.getLevel());
				assertTrue(434 == book.getPages());
				assertEquals(40.99, book.getPrice());
				assertTrue(book.isPublished());
				assertTrue(book.isSaved());
				continue;
			} else if (book.getId() == book2.getId()) {
				assertEquals(8.8f, book.getArea());
				assertEquals("Android Third Line", book.getBookName());
				assertEquals(isbn2, book.getIsbn());
				assertEquals('C', book.getLevel());
				assertTrue(411 == book.getPages());
				assertEquals(35.99, book.getPrice());
				assertFalse(book.isPublished());
				assertTrue(book.isSaved());
				continue;
			}
			fail();
		}
	}

    @Test
	public void testFindAll() {
		List<Book> expectBooks = getBooks(null, null, null, null, null, null, null);
		List<Book> realBooks = LitePal.findAll(Book.class);
		assertEquals(expectBooks.size(), realBooks.size());
		for (int i = 0; i < expectBooks.size(); i++) {
			Book expectBook = expectBooks.get(i);
			Book realBook = realBooks.get(i);
			assertEquals(expectBook.getId(), realBook.getId());
			assertEquals(expectBook.getBookName(), realBook.getBookName());
			assertEquals(expectBook.getPages(), realBook.getPages());
			assertEquals(expectBook.getPrice(), realBook.getPrice());
			assertEquals(expectBook.getArea(), realBook.getArea());
			assertEquals(expectBook.getIsbn(), realBook.getIsbn());
			assertEquals(expectBook.getLevel(), realBook.getLevel());
			assertEquals(expectBook.isPublished(), realBook.isPublished());
			assertTrue(realBook.isSaved());
		}
	}

    @Test
	public void testFindFirst() {
		List<Book> expectedBooks = getBooks(null, null, null, null, null, null, null);
		Book expectedFirstBook = expectedBooks.get(0);
		Book realFirstBook = LitePal.findFirst(Book.class);
		assertEquals(expectedFirstBook.getId(), realFirstBook.getId());
		assertEquals(expectedFirstBook.getBookName(), realFirstBook.getBookName());
		assertEquals(expectedFirstBook.getPages(), realFirstBook.getPages());
		assertEquals(expectedFirstBook.getPrice(), realFirstBook.getPrice());
		assertEquals(expectedFirstBook.getArea(), realFirstBook.getArea());
		assertEquals(expectedFirstBook.getIsbn(), realFirstBook.getIsbn());
		assertEquals(expectedFirstBook.getLevel(), realFirstBook.getLevel());
		assertEquals(expectedFirstBook.isPublished(), realFirstBook.isPublished());
		assertTrue(realFirstBook.isSaved());
	}

    @Test
	public void testFindLast() {
		List<Book> expectedBooks = getBooks(null, null, null, null, null, null, null);
		Book expectedLastBook = expectedBooks.get(expectedBooks.size() - 1);
		Book realLastBook = LitePal.findLast(Book.class);
		assertEquals(expectedLastBook.getId(), realLastBook.getId());
		assertEquals(expectedLastBook.getBookName(), realLastBook.getBookName());
		assertEquals(expectedLastBook.getPages(), realLastBook.getPages());
		assertEquals(expectedLastBook.getPrice(), realLastBook.getPrice());
		assertEquals(expectedLastBook.getArea(), realLastBook.getArea());
		assertEquals(expectedLastBook.getIsbn(), realLastBook.getIsbn());
		assertEquals(expectedLastBook.getLevel(), realLastBook.getLevel());
		assertEquals(expectedLastBook.isPublished(), realLastBook.isPublished());
		assertTrue(realLastBook.isSaved());
	}

    @Test
    public void testIsExist() {
        Book book = new Book();
        book.setArea(10.5f);
        book.setBookName("Android Third Line");
        book.setPages(556);
        book.setPrice(49.99);
        book.setPublished(false);
        book.save();

        Book book2 = new Book();
        book2.setArea(10.5f);
        book2.setBookName("Android Fourth Line");
        book2.setPages(818);
        book2.setPrice(59.99);
        book2.setPublished(false);
        book2.save();

        assertTrue(LitePal.isExist(Book.class, "bookname = ? and pages = ?", "Android Third Line", "556"));
        assertFalse(LitePal.isExist(Book.class, "bookname = ? and pages = ?", "Android Third Lines", "556"));
        assertTrue(LitePal.isExist(Book.class, "bookname = ? and pages = ?", "Android Fourth Line", "818"));
        assertFalse(LitePal.isExist(Book.class, "bookname = ? and pages = ?", "Android Fourth Line", "813"));
    }

}