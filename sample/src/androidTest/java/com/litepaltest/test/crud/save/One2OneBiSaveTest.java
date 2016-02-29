package com.litepaltest.test.crud.save;

import junit.framework.Assert;

import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.test.LitePalTestCase;

public class One2OneBiSaveTest extends LitePalTestCase {

	private Student s;

	private IdCard i;

	private void init() {
		s = new Student();
		s.setName("Jimmy");
		s.setAge(18);
		i = new IdCard();
		i.setNumber("9997777112");
		i.setAddress("Nanjing road");
	}

	public void testO2OBiSaveStudentFirst() {
		init();
		s.setIdcard(i);
		i.setStudent(s);
		s.save();
		i.save();
		assertFK(s, i);
	}

	public void testO2OBiSaveIdCardFirst() {
		init();
		s.setIdcard(i);
		i.setStudent(s);
		i.save();
		s.save();
		assertFK(s, i);
	}

	public void testO2OBiBuildNullAssocations() {
		init();
		s.setIdcard(null);
		i.setStudent(null);
		i.save();
		s.save();
		isDataExists(getTableName(s), s.getId());
		isDataExists(getTableName(i), i.getId());
	}

	public void testO2OBiBuildUniAssociationsSaveStudentFirst() {
		init();
		s.setIdcard(i);
		s.save();
		i.save();
		assertFK(s, i);
	}

	public void testO2OBiBuildUniAssociationsSaveIdCardFirst() {
		init();
		s.setIdcard(i);
		i.save();
		s.save();
		assertFK(s, i);
	}

    public void testSaveFast() {
        init();
        s.setIdcard(i);
        i.setStudent(s);
        i.saveFast();
        s.saveFast();
        isDataExists(getTableName(s), s.getId());
        isDataExists(getTableName(i), i.getId());
        Assert.assertFalse(isFKInsertCorrect(getTableName(s), getTableName(i), s.getId(), i.getId()));
        Assert.assertFalse(isFKInsertCorrect(getTableName(i), getTableName(s), i.getId(), s.getId()));
    }

	private void assertFK(Student s, IdCard i) {
		Assert.assertTrue(isFKInsertCorrect(getTableName(s), getTableName(i), s.getId(), i.getId()));
		Assert.assertTrue(isFKInsertCorrect(getTableName(i), getTableName(s), i.getId(), s.getId()));
	}

}
