package com.litepaltest.test.crud.save;

import androidx.test.filters.SmallTest;

import java.util.HashSet;
import java.util.Set;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.Student;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

@SmallTest
public class Many2OneBiSaveTest extends LitePalTestCase {

	private Classroom c1;

	private Student s1;

	private Student s2;

	public void init() {
		c1 = new Classroom();
		c1.setName("Computer room");
		s1 = new Student();
		s1.setName("Tom");
		s2 = new Student();
		s2.setName("Lily");
	}

    @Test
	public void testCase1() {
		init();
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		c1.save();
		s1.save();
		s2.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase2() {
		init();
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		s1.save();
		s2.save();
		c1.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase3() {
		init();
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		s2.save();
		c1.save();
		s1.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase4() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		c1.save();
		s1.save();
		s2.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase5() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		s1.save();
		s2.save();
		c1.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase6() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		s1.save();
		c1.save();
		s2.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase7() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		c1.save();
		s1.save();
		s2.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase8() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		s1.save();
		s2.save();
		c1.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase9() {
		init();
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		s1.save();
		c1.save();
		s2.save();
		assertFK(c1, s1, s2);
	}

    @Test
	public void testCase10() {
		init();
		s1 = null;
		s2 = null;
		Set<Student> ss = new HashSet<>();
		ss.add(s1);
		ss.add(s2);
		c1.setStudentCollection(ss);
		c1.save();
		isDataExists(getTableName(c1), c1.get_id());
		init();
		c1 = null;
		s1.setClassroom(c1);
		s2.setClassroom(c1);
		s1.save();
		isDataExists(getTableName(s1), s1.getId());
		s2.save();
		isDataExists(getTableName(s2), s2.getId());
	}

	private void assertFK(Classroom c1, Student s1, Student s2) {
		assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(s1), c1.get_id(),
				s1.getId()));
		assertTrue(isFKInsertCorrect(getTableName(c1), getTableName(s2), c1.get_id(),
				s2.getId()));
	}

}
