package com.litepaltest.test.crud.query

import androidx.test.filters.SmallTest
import com.litepaltest.model.Book
import com.litepaltest.test.LitePalTestCase
import junit.framework.TestCase.*
import org.junit.Test
import org.litepal.LitePal
import org.litepal.extension.*

@SmallTest
class QueryBasicKotlinTest : LitePalTestCase() {

    @Test
    fun testFind() {
        val isbn: Short = 30013
        val book = Book()
        book.area = 10.5f
        book.bookName = "Android Second Line"
        book.isbn = isbn
        book.level = 'A'
        book.pages = 450
        book.price = 49.99
        book.isPublished = false
        book.save()
        val b: Book? = LitePal.find(book.id)
        assertNotNull(b)
        assertEquals(book.id, b!!.id)
        assertEquals(10.5f, b.area)
        assertEquals("Android Second Line", b.bookName)
        assertEquals(isbn, b.isbn)
        assertEquals('A', b.level)
        assertTrue(450 == b.pages)
        assertEquals(49.99, b.price)
        assertFalse(b.isPublished)
        assertTrue(b.isSaved)
    }

    @Test
    fun testFindMul() {
        val isbn1: Short = 30017
        val book1 = Book()
        book1.area = 1.5f
        book1.bookName = "Android Second Line"
        book1.isbn = isbn1
        book1.level = 'B'
        book1.pages = 434
        book1.price = 40.99
        book1.isPublished = true
        book1.save()
        val isbn2: Short = 30014
        val book2 = Book()
        book2.area = 8.8f
        book2.bookName = "Android Third Line"
        book2.isbn = isbn2
        book2.level = 'C'
        book2.pages = 411
        book2.price = 35.99
        book2.isPublished = false
        book2.save()
        val bookList = LitePal.findAll<Book>(book1.id, book2.id)
        assertEquals(2, bookList.size)
        for (book in bookList) {
            if (book.id == book1.id) {
                assertEquals(1.5f, book.area)
                assertEquals("Android Second Line", book.bookName)
                assertEquals(isbn1, book.isbn)
                assertEquals('B', book.level)
                assertTrue(434 == book.pages)
                assertEquals(40.99, book.price)
                assertTrue(book.isPublished)
                assertTrue(book.isSaved)
                continue
            } else if (book.id == book2.id) {
                assertEquals(8.8f, book.area)
                assertEquals("Android Third Line", book.bookName)
                assertEquals(isbn2, book.isbn)
                assertEquals('C', book.level)
                assertTrue(411 == book.pages)
                assertEquals(35.99, book.price)
                assertFalse(book.isPublished)
                assertTrue(book.isSaved)
                continue
            }
            fail()
        }
    }

    @Test
    fun testFindAll() {
        val expectBooks = getBooks(null, null, null, null, null, null, null)
        val realBooks = LitePal.findAll<Book>()
        assertEquals(expectBooks.size, realBooks.size)
        for (i in expectBooks.indices) {
            val expectBook = expectBooks[i]
            val realBook = realBooks[i]
            assertEquals(expectBook.id, realBook.id)
            assertEquals(expectBook.bookName, realBook.bookName)
            assertEquals(expectBook.pages, realBook.pages)
            assertEquals(expectBook.price, realBook.price)
            assertEquals(expectBook.area, realBook.area)
            assertEquals(expectBook.isbn, realBook.isbn)
            assertEquals(expectBook.level, realBook.level)
            assertEquals(expectBook.isPublished, realBook.isPublished)
            assertTrue(realBook.isSaved)
        }
    }

    @Test
    fun testFindFirst() {
        val expectedBooks = getBooks(null, null, null, null, null, null, null)
        val expectedFirstBook = expectedBooks[0]
        val realFirstBook = LitePal.findFirst<Book>()
        assertEquals(expectedFirstBook.id, realFirstBook!!.id)
        assertEquals(expectedFirstBook.bookName, realFirstBook.bookName)
        assertEquals(expectedFirstBook.pages, realFirstBook.pages)
        assertEquals(expectedFirstBook.price, realFirstBook.price)
        assertEquals(expectedFirstBook.area, realFirstBook.area)
        assertEquals(expectedFirstBook.isbn, realFirstBook.isbn)
        assertEquals(expectedFirstBook.level, realFirstBook.level)
        assertEquals(expectedFirstBook.isPublished, realFirstBook.isPublished)
        assertTrue(realFirstBook.isSaved)
    }

    @Test
    fun testFindLast() {
        val expectedBooks = getBooks(null, null, null, null, null, null, null)
        val expectedLastBook = expectedBooks[expectedBooks.size - 1]
        val realLastBook = LitePal.findLast<Book>()
        assertEquals(expectedLastBook.id, realLastBook!!.id)
        assertEquals(expectedLastBook.bookName, realLastBook.bookName)
        assertEquals(expectedLastBook.pages, realLastBook.pages)
        assertEquals(expectedLastBook.price, realLastBook.price)
        assertEquals(expectedLastBook.area, realLastBook.area)
        assertEquals(expectedLastBook.isbn, realLastBook.isbn)
        assertEquals(expectedLastBook.level, realLastBook.level)
        assertEquals(expectedLastBook.isPublished, realLastBook.isPublished)
        assertTrue(realLastBook.isSaved)
    }

    @Test
    fun testIsExist() {
        val book = Book()
        book.area = 10.5f
        book.bookName = "Android Third Line"
        book.pages = 556
        book.price = 49.99
        book.isPublished = false
        book.save()

        val book2 = Book()
        book2.area = 10.5f
        book2.bookName = "Android Fourth Line"
        book2.pages = 818
        book2.price = 59.99
        book2.isPublished = false
        book2.save()

        assertTrue(LitePal.isExist<Book>("bookname = ? and pages = ?", "Android Third Line", "556"))
        assertFalse(LitePal.isExist<Book>("bookname = ? and pages = ?", "Android Third Lines", "556"))
        assertTrue(LitePal.isExist<Book>("bookname = ? and pages = ?", "Android Fourth Line", "818"))
        assertFalse(LitePal.isExist<Book>("bookname = ? and pages = ?", "Android Fourth Line", "813"))
    }

}