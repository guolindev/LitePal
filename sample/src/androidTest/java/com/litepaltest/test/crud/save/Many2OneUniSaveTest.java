package com.litepaltest.test.crud.save;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.Teacher;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

@SmallTest
public class Many2OneUniSaveTest extends LitePalTestCase {

	private Classroom c1;

	private Teacher t1;

	private Teacher t2;

	public void init() {
		c1 = new Classroom();
		c1.setName("Music room");
		t1 = new Teacher();
		t1.setTeacherName("John");
		t1.setAge(25);
		t2 = new Teacher();
		t2.setTeacherName("Sean");
		t2.setAge(35);
	}

    @Test
	public void testCase1() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		c1.save();
		t1.save();
		t2.save();
		assertFK(c1, t1, t2);
	}

    @Test
	public void testCase2() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		t1.save();
		t2.save();
		c1.save();
		assertFK(c1, t1, t2);
	}

    @Test
	public void testCase3() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		t1.save();
		c1.save();
		t2.save();
		assertFK(c1, t1, t2);
	}

    @Test
	public void testCase4() {
		init();
		t1 = null;
		t2 = null;
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		c1.save();
		isDataExists(getTableName(c1), c1.get_id());
	}

	private void assertFK(Classroom c1, Teacher t1, Teacher t2) {
		assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(t1), c1.get_id(),
				t1.getId()));
		assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(t2), c1.get_id(),
				t2.getId()));
	}

}
