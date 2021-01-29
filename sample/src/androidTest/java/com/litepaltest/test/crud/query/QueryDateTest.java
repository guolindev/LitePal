package com.litepaltest.test.crud.query;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.Student;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;
import org.litepal.LitePal;

import java.util.Calendar;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class QueryDateTest extends LitePalTestCase {

	@Test
	public void testQueryDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(1990, 9, 16, 0, 0, 0);
		Student student1 = new Student();
		student1.setName("Student 1");
		student1.setBirthday(calendar.getTime());
		student1.save();
		Student studentFromDB = LitePal.find(Student.class, student1.getId());
		assertEquals("Student 1", studentFromDB.getName());
		assertEquals(calendar.getTimeInMillis(), studentFromDB.getBirthday().getTime());
	}

@Test
	public void testQueryDateBefore1970() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(1920, 6, 3, 0, 0, 0);
		Student student1 = new Student();
		student1.setName("Student 2");
		student1.setBirthday(calendar.getTime());
		student1.save();
		Student studentFromDB = LitePal.find(Student.class, student1.getId());
		assertEquals("Student 2", studentFromDB.getName());
		assertEquals(calendar.getTimeInMillis(), studentFromDB.getBirthday().getTime());
	}

	@Test
	public void testQueryDateWithDefaultValue() {
		Student student = new Student();
		student.setName("School Student");
		assertTrue(student.save());
		Student studentFromDB = LitePal.find(Student.class, student.getId());
		assertEquals(1589203961859L, studentFromDB.getSchoolDate().getTime());
	}

}