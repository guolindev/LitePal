package com.litepaltest.test.crud.save;

import junit.framework.Assert;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.Teacher;
import com.litepaltest.test.LitePalTestCase;

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

	public void testCase1() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		c1.save();
		t1.save();
		t2.save();
		assertFK(c1, t1, t2);
	}

	public void testCase2() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		t1.save();
		t2.save();
		c1.save();
		assertFK(c1, t1, t2);
	}

	public void testCase3() {
		init();
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		t1.save();
		c1.save();
		t2.save();
		assertFK(c1, t1, t2);
	}

	public void testCase4() {
		init();
		t1 = null;
		t2 = null;
		c1.getTeachers().add(t1);
		c1.getTeachers().add(t2);
		c1.save();
		isDataExists(getTableName(c1), c1.get_id());
	}

    public void testSaveFast() {
        init();
        c1.getTeachers().add(t1);
        c1.getTeachers().add(t2);
        c1.saveFast();
        t1.saveFast();
        t2.saveFast();
        isDataExists(getTableName(c1), c1.get_id());
        isDataExists(getTableName(t1), t1.getId());
        isDataExists(getTableName(t2), t2.getId());
        Assert.assertFalse(isFKInsertCorrect(getTableName(c1), getTableName(t1), c1.get_id(),
                t1.getId()));
        Assert.assertFalse(isFKInsertCorrect(getTableName(c1), getTableName(t2), c1.get_id(),
                t2.getId()));
    }

	private void assertFK(Classroom c1, Teacher t1, Teacher t2) {
		Assert.assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(t1), c1.get_id(),
				t1.getId()));
		Assert.assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(t2), c1.get_id(),
				t2.getId()));
	}

}
