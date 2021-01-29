package com.litepaltest.test.crud.query

import androidx.test.filters.SmallTest
import com.litepaltest.model.Book
import com.litepaltest.test.LitePalTestCase
import junit.framework.TestCase.*
import org.junit.Test
import org.litepal.LitePal
import org.litepal.extension.*

@SmallTest
class QueryClusterKotlinTest : LitePalTestCase() {

    @Test
    fun testSelect() {
        val expectedBooks = getBooks(null, null, null, null, null, null, null)
        val books = LitePal.select("bookname", "price").find<Book>()
        assertEquals(expectedBooks.size, books.size)
        val firstBook = LitePal.select("bookname", "price").findFirst<Book>()
        val lastBook = LitePal.select("bookname", "price").findLast<Book>()
        assertNotNull(firstBook)
        for (i in books.indices) {
            val book = books[i]
            assertTrue(book.isSaved)
            assertEquals(expectedBooks[i].bookName, book.bookName)
            assertNull(book.pages)
            assertEquals(false, book.isPublished)
            assertEquals(0f, book.area)
            assertEquals(expectedBooks[i].price, book.price)
            assertEquals(0, book.isbn.toInt())
            assertEquals(0, book.level.toInt())
            assertEquals(expectedBooks[i].id, book.id)
            if (i == 0) {
                assertEquals(firstBook!!.isSaved, book.isSaved)
                assertEquals(firstBook.bookName, book.bookName)
                assertNull(firstBook.pages)
                assertEquals(firstBook.isPublished, book.isPublished)
                assertEquals(firstBook.price, book.price)
                assertEquals(firstBook.area, book.area)
                assertEquals(firstBook.isbn, book.isbn)
                assertEquals(firstBook.level, book.level)
                assertEquals(firstBook.id, book.id)
            }
            if (i == books.size - 1) {
                assertEquals(lastBook!!.isSaved, book.isSaved)
                assertEquals(lastBook.bookName, book.bookName)
                assertNull(lastBook.pages)
                assertEquals(lastBook.isPublished, book.isPublished)
                assertEquals(lastBook.price, book.price)
                assertEquals(lastBook.area, book.area)
                assertEquals(lastBook.isbn, book.isbn)
                assertEquals(lastBook.level, book.level)
                assertEquals(lastBook.id, book.id)
            }
        }
    }

    @Test
    fun testWhere() {
        val books = LitePal.where("bookname = ?", "Android First Line").find<Book>()
        val firstBook = LitePal.where("bookname = ?", "Android First Line").findFirst<Book>()
        val lastBook = LitePal.where("bookname = ?", "Android First Line").findLast<Book>()
        for (i in books.indices) {
            val book = books[i]
            assertTrue(book.isSaved)
            assertEquals("Android First Line", book.bookName)
            assertTrue(450 == book.pages)
            assertEquals(49.99, book.price)
            assertEquals(false, book.isPublished)
            assertEquals('A', book.level)
            assertEquals(10.5f, book.area)
            if (i == 0) {
                assertEquals(firstBook!!.isSaved, book.isSaved)
                assertEquals(firstBook.bookName, book.bookName)
                assertEquals(firstBook.pages, book.pages)
                assertEquals(firstBook.isPublished, book.isPublished)
                assertEquals(firstBook.price, book.price)
                assertEquals(firstBook.area, book.area)
                assertEquals(firstBook.isbn, book.isbn)
                assertEquals(firstBook.level, book.level)
                assertEquals(firstBook.id, book.id)
            }
            if (i == books.size - 1) {
                assertEquals(lastBook!!.isSaved, book.isSaved)
                assertEquals(lastBook.bookName, book.bookName)
                assertEquals(lastBook.pages, book.pages)
                assertEquals(lastBook.isPublished, book.isPublished)
                assertEquals(lastBook.price, book.price)
                assertEquals(lastBook.area, book.area)
                assertEquals(lastBook.isbn, book.isbn)
                assertEquals(lastBook.level, book.level)
                assertEquals(lastBook.id, book.id)
            }
        }
        val expectedBooks = getBooks(null, "bookname like ?",
                arrayOf("Android%Line"), null, null, null, null)
        val realBooks = LitePal.where("bookname like ?", "Android%Line").find<Book>()
        assertEquals(expectedBooks.size, realBooks.size)
    }

    @Test
    fun testOrder() {
        val books = LitePal.order("ID").find<Book>()
        val firstBook = LitePal.order("ID").findFirst<Book>()
        val lastBook = LitePal.order("ID").findLast<Book>()
        var preBook: Book? = null
        for (i in books.indices) {
            val book = books[i]
            assertTrue(book.isSaved)
            if (preBook != null) {
                assertTrue(book.id > preBook.id)
            }
            preBook = book
            if (i == 0) {
                assertEquals(firstBook!!.isSaved, book.isSaved)
                assertEquals(firstBook.bookName, book.bookName)
                assertEquals(firstBook.pages, book.pages)
                assertEquals(firstBook.isPublished, book.isPublished)
                assertEquals(firstBook.price, book.price)
                assertEquals(firstBook.area, book.area)
                assertEquals(firstBook.isbn, book.isbn)
                assertEquals(firstBook.level, book.level)
                assertEquals(firstBook.id, book.id)
            }
            if (i == books.size - 1) {
                assertEquals(lastBook!!.isSaved, book.isSaved)
                assertEquals(lastBook.bookName, book.bookName)
                assertEquals(lastBook.pages, book.pages)
                assertEquals(lastBook.isPublished, book.isPublished)
                assertEquals(lastBook.price, book.price)
                assertEquals(lastBook.area, book.area)
                assertEquals(lastBook.isbn, book.isbn)
                assertEquals(lastBook.level, book.level)
                assertEquals(lastBook.id, book.id)
            }
        }
        val inverseBooks = LitePal.order("ID desc").find<Book>()
        val inverseFirstBook = LitePal.order("ID desc").findFirst<Book>()
        val inverseLastBook = LitePal.order("ID desc").findLast<Book>()
        var inversePreBook: Book? = null
        for (i in inverseBooks.indices) {
            val book = inverseBooks[i]
            assertTrue(book.isSaved)
            if (inversePreBook != null) {
                assertTrue(book.id < inversePreBook.id)
            }
            inversePreBook = book
            if (i == 0) {
                assertEquals(inverseFirstBook!!.isSaved, book.isSaved)
                assertEquals(inverseFirstBook.bookName, book.bookName)
                assertEquals(inverseFirstBook.pages, book.pages)
                assertEquals(inverseFirstBook.isPublished, book.isPublished)
                assertEquals(inverseFirstBook.price, book.price)
                assertEquals(inverseFirstBook.area, book.area)
                assertEquals(inverseFirstBook.isbn, book.isbn)
                assertEquals(inverseFirstBook.level, book.level)
                assertEquals(inverseFirstBook.id, book.id)
            }
            if (i == books.size - 1) {
                assertEquals(inverseLastBook!!.isSaved, book.isSaved)
                assertEquals(inverseLastBook.bookName, book.bookName)
                assertEquals(inverseLastBook.pages, book.pages)
                assertEquals(inverseLastBook.isPublished, book.isPublished)
                assertEquals(inverseLastBook.price, book.price)
                assertEquals(inverseLastBook.area, book.area)
                assertEquals(inverseLastBook.isbn, book.isbn)
                assertEquals(inverseLastBook.level, book.level)
                assertEquals(inverseLastBook.id, book.id)
            }
        }
    }

    @Test
    fun testLimit() {
        var bookList = LitePal.limit(1).find<Book>()
        assertEquals(1, bookList.size)
        var book = bookList[0]
        assertTrue(book.isSaved)
        val firstBook = LitePal.findFirst<Book>()
        assertTrue(firstBook!!.isSaved)
        assertEquals(firstBook.bookName, book.bookName)
        assertEquals(firstBook.pages, book.pages)
        assertEquals(firstBook.isPublished, book.isPublished)
        assertEquals(firstBook.area, book.area)
        assertEquals(firstBook.price, book.price)
        assertEquals(firstBook.isbn, book.isbn)
        assertEquals(firstBook.level, book.level)
        assertEquals(firstBook.id, book.id)
        bookList = LitePal.order("id desc").limit(1).find()
        assertEquals(1, bookList.size)
        book = bookList[0]
        assertTrue(book.isSaved)
        val lastBook = LitePal.findLast(Book::class.java)
        assertTrue(lastBook!!.isSaved)
        assertEquals(lastBook.bookName, book.bookName)
        assertEquals(lastBook.pages, book.pages)
        assertEquals(lastBook.isPublished, book.isPublished)
        assertEquals(lastBook.area, book.area)
        assertEquals(lastBook.price, book.price)
        assertEquals(lastBook.isbn, book.isbn)
        assertEquals(lastBook.level, book.level)
        assertEquals(lastBook.id, book.id)
    }

    @Test
    fun testOffset() {
        val list = LitePal.offset(1).find<Book>()
        assertEquals(0, list.size)
        val bookList = LitePal.limit(1).offset(1).find<Book>()
        assertEquals(1, bookList.size)
        val book = bookList[0]
        assertTrue(book.isSaved)
        val expectedBooks = getBooks(null, null, null, null, null, null, null)
        val expectedBook = expectedBooks[1]
        assertEquals(expectedBook.bookName, book.bookName)
        assertEquals(expectedBook.pages, book.pages)
        assertEquals(expectedBook.isPublished, book.isPublished)
        assertEquals(expectedBook.area, book.area)
        assertEquals(expectedBook.price, book.price)
        assertEquals(expectedBook.isbn, book.isbn)
        assertEquals(expectedBook.level, book.level)
        assertEquals(expectedBook.id, book.id)
    }

    @Test
    fun testCluster() {
        val ids = LongArray(3)
        for (i in 0..2) {
            val book = Book()
            book.pages = 5555
            book.isPublished = true
            book.price = 40.99
            book.save()
            ids[i] = book.id
        }
        val books = LitePal
                .select("pages", "isPublished")
                .where("id=? or id=? or id=?", ids[0].toString(), ids[1].toString(),
                        ids[2].toString()).order("id").limit(2).offset(1).find<Book>()
        val firstBook = LitePal
                .select("pages", "isPublished")
                .where("id=? or id=? or id=?", ids[0].toString(), ids[1].toString(),
                        ids[2].toString()).order("id").limit(2).offset(1).findFirst<Book>()
        val lastBook = LitePal
                .select("pages", "isPublished")
                .where("id=? or id=? or id=?", ids[0].toString(), ids[1].toString(),
                        ids[2].toString()).order("id").limit(2).offset(1).findLast<Book>()
        assertEquals(2, books.size)
        assertTrue(books[0].id < books[1].id)
        for (i in 0..1) {
            val b = books[i]
            assertEquals(ids[i + 1], b.id)
            assertTrue(b.isSaved)
            assertNull(b.bookName)
            assertTrue(5555 == b.pages)
            assertEquals(true, b.isPublished)
            assertEquals(0f, b.area)
            assertEquals(0.0, b.price)
            assertEquals(0, b.isbn.toInt())
            assertEquals(0, b.level.toInt())
            if (i == 0) {
                assertEquals(firstBook!!.isSaved, b.isSaved)
                assertEquals(firstBook.bookName, b.bookName)
                assertEquals(firstBook.pages, b.pages)
                assertEquals(firstBook.isPublished, b.isPublished)
                assertEquals(firstBook.price, b.price)
                assertEquals(firstBook.area, b.area)
                assertEquals(firstBook.isbn, b.isbn)
                assertEquals(firstBook.level, b.level)
                assertEquals(firstBook.id, b.id)
            }
            if (i == books.size - 1) {
                assertEquals(lastBook!!.isSaved, b.isSaved)
                assertEquals(lastBook.bookName, b.bookName)
                assertEquals(lastBook.pages, b.pages)
                assertEquals(lastBook.isPublished, b.isPublished)
                assertEquals(lastBook.price, b.price)
                assertEquals(lastBook.area, b.area)
                assertEquals(lastBook.isbn, b.isbn)
                assertEquals(lastBook.level, b.level)
                assertEquals(lastBook.id, b.id)
            }
        }
    }

}
