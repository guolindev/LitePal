package com.litepaltest.test.crud.save;

import androidx.test.filters.SmallTest;

import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

@SmallTest
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

    @Test
	public void testO2OBiSaveStudentFirst() {
		init();
		s.setIdcard(i);
		i.setStudent(s);
		s.save();
		i.save();
		assertFK(s, i);
	}

    @Test
	public void testO2OBiSaveIdCardFirst() {
		init();
		s.setIdcard(i);
		i.setStudent(s);
		i.save();
		s.save();
		assertFK(s, i);
	}

    @Test
	public void testO2OBiBuildNullAssocations() {
		init();
		s.setIdcard(null);
		i.setStudent(null);
		i.save();
		s.save();
		isDataExists(getTableName(s), s.getId());
		isDataExists(getTableName(i), i.getId());
	}

    @Test
	public void testO2OBiBuildUniAssociationsSaveStudentFirst() {
		init();
		s.setIdcard(i);
		s.save();
		i.save();
		assertFK(s, i);
	}

    @Test
	public void testO2OBiBuildUniAssociationsSaveIdCardFirst() {
		init();
		s.setIdcard(i);
		i.save();
		s.save();
		assertFK(s, i);
	}

	private void assertFK(Student s, IdCard i) {
		assertTrue(isFKInsertCorrect(getTableName(s), getTableName(i), s.getId(), i.getId()));
		assertTrue(isFKInsertCorrect(getTableName(i), getTableName(s), i.getId(), s.getId()));
	}

}
