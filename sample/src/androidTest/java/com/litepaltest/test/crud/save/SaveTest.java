package com.litepaltest.test.crud.save;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.Book;
import com.litepaltest.model.Cellphone;
import com.litepaltest.model.Classroom;
import com.litepaltest.model.Computer;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Product;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;
import com.litepaltest.model.WeChatMessage;
import com.litepaltest.model.WeiboMessage;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class SaveTest extends LitePalTestCase {

    @Test
	public void testSave() {
		Cellphone cell = new Cellphone();
		cell.setBrand("iPhone");
		cell.setPrice(4998.01);
		cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
		assertTrue(cell.save());
		assertTrue(isDataExists(getTableName(cell), cell.getId()));
	}

    @Test
	public void testSaveWithConstructors() {
		Computer computer = new Computer("asus", 699.00);
		assertTrue(computer.save());
		assertTrue(isDataExists(getTableName(computer), computer.getId()));
		Computer c = getComputer(computer.getId());
		assertEquals("asus", c.getBrand());
		assertEquals(699.00, c.getPrice());
		Computer cc = LitePal.find(Computer.class, computer.getId());
		assertEquals("asus", cc.getBrand());
		assertEquals(699.00, cc.getPrice());
		Product p = new Product(null);
		p.setBrand("apple");
		p.setPrice(1222.33);
		p.save();
	}

    @Test
	public void testSaveAfterDelete() {
		Cellphone cell = new Cellphone();
		cell.setBrand("iPhone");
		cell.setPrice(4998.01);
		cell.setInStock('Y');
        cell.setSerial(UUID.randomUUID().toString());
		assertTrue(cell.save());
		assertTrue(isDataExists(getTableName(cell), cell.getId()));
		assertTrue(cell.delete() > 0);
		assertTrue(cell.save());
		assertTrue(isDataExists(getTableName(cell), cell.getId()));
		Student stu = new Student();
		stu.setName("Jimmy");
		IdCard idcard = new IdCard();
		idcard.setAddress("Washington");
		idcard.setNumber("123456");
		idcard.setStudent(stu);
		stu.setIdcard(idcard);
		stu.save();
		idcard.save();
		assertTrue(isDataExists(getTableName(stu), stu.getId()));
		assertTrue(isDataExists(getTableName(idcard), idcard.getId()));
		stu.delete();
		assertFalse(isDataExists(getTableName(stu), stu.getId()));
		assertFalse(isDataExists(getTableName(idcard), idcard.getId()));
		stu.save();
		idcard.save();
		assertTrue(isDataExists(getTableName(stu), stu.getId()));
		assertTrue(isDataExists(getTableName(idcard), idcard.getId()));
		Student danny = new Student();
		danny.setName("Danny");
		danny.setAge(14);
		Teacher cam = new Teacher();
		cam.setTeacherName("Cam");
		cam.setAge(33);
		cam.setSex(true);
		cam.setTeachYears(5);
		Teacher jack = new Teacher();
		jack.setTeacherName("Jack");
		jack.setAge(36);
		jack.setSex(false);
		jack.setTeachYears(11);
		danny.getTeachers().add(jack);
		danny.getTeachers().add(cam);
		cam.getStudents().add(danny);
		jack.getStudents().add(danny);
		danny.save();
		cam.save();
		jack.save();
		assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		danny.delete();
		assertFalse(isDataExists(getTableName(danny), danny.getId()));
		assertTrue(isDataExists(getTableName(cam), cam.getId()));
		assertTrue(isDataExists(getTableName(jack), jack.getId()));
		danny.save();
		assertTrue(isDataExists(getTableName(danny), danny.getId()));
		assertEquals(danny.getTeachers().size(), 2);
		Classroom c = new Classroom();
		c.setName("test classroom");
		Student s = new Student();
		s.setName("Tom");
		s.setClassroom(c);
		Student s2 = new Student();
		s2.setName("Tom");
		s2.setClassroom(c);
		assertTrue(c.save());
		assertTrue(s.save());
		assertTrue(s2.save());
		assertTrue(isDataExists(getTableName(c), c.get_id()));
		assertTrue(isDataExists(getTableName(s), s.getId()));
		assertTrue(isDataExists(getTableName(s), s2.getId()));
		c.delete();
		assertFalse(isDataExists(getTableName(c), c.get_id()));
		assertFalse(isDataExists(getTableName(s), s.getId()));
		assertFalse(isDataExists(getTableName(s), s2.getId()));
		c.save();
		s.save();
		s2.save();
		assertTrue(isDataExists(getTableName(c), c.get_id()));
		assertTrue(isDataExists(getTableName(s), s.getId()));
		assertTrue(isDataExists(getTableName(s), s2.getId()));
	}

    @Test
    public void testSaveInheritModels() {
        WeChatMessage weChatMessage = new WeChatMessage();
        weChatMessage.setFriend("Tom");
        weChatMessage.setContent("Hello nice to meet you");
        weChatMessage.setTitle("Greeting message");
        weChatMessage.setType(1);
        assertTrue(weChatMessage.save());
        assertTrue(weChatMessage.getId() > 0);
        WeChatMessage message1 = LitePal.find(WeChatMessage.class, weChatMessage.getId());
        assertEquals("Tom", message1.getFriend());
        assertEquals("Hello nice to meet you", message1.getContent());
        assertNull(message1.getTitle());
        assertEquals(1, message1.getType());

        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.setType(2);
        weiboMessage.setTitle("Following message");
        weiboMessage.setContent("Something big happens");
        weiboMessage.setFollower("Jimmy");
        weiboMessage.setNumber(123456);
        assertTrue(weiboMessage.save());
        assertTrue(weiboMessage.getId() > 0);
    }

    @Test
    public void testSaveInheritModelsWithAssociations() {
        Cellphone cellphone = new Cellphone();
        cellphone.setBrand("iPhone 7");
        cellphone.setInStock('N');
        cellphone.setPrice(6999.99);
        cellphone.setSerial(UUID.randomUUID().toString());
        cellphone.setMac("ff:3d:4a:99:76");
        cellphone.save();

        WeChatMessage weChatMessage = new WeChatMessage();
        weChatMessage.setFriend("Tom");
        weChatMessage.setContent("Hello nice to meet you");
        weChatMessage.setTitle("Greeting message");
        weChatMessage.setType(1);
        assertTrue(weChatMessage.save());
        assertTrue(weChatMessage.getId() > 0);
        WeChatMessage message1 = LitePal.find(WeChatMessage.class, weChatMessage.getId());
        assertEquals("Tom", message1.getFriend());
        assertEquals("Hello nice to meet you", message1.getContent());
        assertNull(message1.getTitle());
        assertEquals(1, message1.getType());

        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.setType(2);
        weiboMessage.setTitle("Following message");
        weiboMessage.setContent("Something big happens");
        weiboMessage.setFollower("Jimmy");
        weiboMessage.setNumber(123456);
        weiboMessage.setCellphone(cellphone);
        assertTrue(weiboMessage.save());
        assertTrue(weiboMessage.getId() > 0);
        WeiboMessage message2 = LitePal.find(WeiboMessage.class, weiboMessage.getId(), true);
        Cellphone result = message2.getCellphone();
        assertEquals(cellphone.getId(), result.getId());
        assertEquals(cellphone.getBrand(), result.getBrand());
        assertEquals(cellphone.getInStock(), result.getInStock());
        assertEquals(cellphone.getPrice(), result.getPrice());
        assertEquals(cellphone.getSerial(), result.getSerial());
        assertEquals(cellphone.getMac(), result.getMac());
    }

    @Test
    public void testSaveGenericData() {
        Classroom classroom = new Classroom();
        classroom.setName("classroom1");
        classroom.getNews().add("news1");
        classroom.getNews().add("news2");
        classroom.getNews().add("news3");
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        classroom.setNumbers(numbers);
        classroom.save();
        Classroom c = LitePal.find(Classroom.class, classroom.get_id());
        assertEquals("classroom1", c.getName());
        assertEquals(3, c.getNews().size());
        assertEquals(4, c.getNumbers().size());
        for (String news : c.getNews()) {
            assertTrue(news.equals("news1") || news.equals("news2") || news.equals("news3"));
        }
        for (int number : c.getNumbers()) {
            assertTrue(number == 1 || number == 2 || number == 3 || number == 4);
        }
    }

    @Test
    public void testSaveLongMaximumNumber() {
    	IdCard idCard = new IdCard();
    	idCard.setSerial(Long.MAX_VALUE);
    	idCard.setAddress("abczyx");
    	assertTrue(idCard.save());
    	IdCard idCardFromDB = LitePal.find(IdCard.class, idCard.getId());
    	assertEquals(Long.MAX_VALUE, idCardFromDB.getSerial());
	}

	@Test
	public void testNullValue() {
		Book book = new Book();
		book.setBookName("First Line of Android");
		assertTrue(book.save());
		Book bookFromDB = LitePal.find(Book.class, book.getId());
		assertNotNull(bookFromDB);
		assertNull(bookFromDB.getPages()); // pages should be null cause it's Integer type and assign no value.

		book.setPages(123); // assign pages
		assertTrue(book.save());
		bookFromDB = LitePal.find(Book.class, book.getId());
		assertNotNull(bookFromDB);
		assertNotNull(bookFromDB.getPages()); // now we should be pages value.
		assertEquals(Integer.valueOf(123), book.getPages());
	}

}