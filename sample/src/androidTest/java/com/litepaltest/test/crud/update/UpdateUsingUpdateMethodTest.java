package com.litepaltest.test.crud.update;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;
import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.DBUtility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.Computer;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;
import com.litepaltest.test.LitePalTestCase;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;

@SmallTest
public class UpdateUsingUpdateMethodTest extends LitePalTestCase {

	private Teacher teacher;

	private Teacher t1;

	private Teacher t2;

	private Student student;

	private Student s1;

	private Student s2;

	private Student s3;

	private Classroom classroom;

	private Classroom c1;

	private Classroom c2;

    private String studentTable;

	@Before
	public void setUp() {
        studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
		init();
	}

	private void init() {
		classroom = new Classroom();
		classroom.setName("English room");
        classroom.getNews().add("hello");
        classroom.getNews().add("world");
        classroom.getNumbers().add(123);
        classroom.getNumbers().add(456);
		teacher = new Teacher();
		teacher.setTeacherName("Tony");
		teacher.setTeachYears(3);
		teacher.setAge(23);
		teacher.setSex(false);
		student = new Student();
		student.setName("Jonny");
		student.setAge(13);
		student.setClassroom(classroom);
		student.setBirthday(new Date());
		student.getTeachers().add(teacher);
		teacher.getStudents().add(student);
		student.save();
		teacher.save();
		classroom.save();
	}

	private void initForAssociations() {
		c1 = new Classroom();
		c1.setName("Working room");
		c2 = new Classroom();
		c2.setName("Resting room");
		s1 = new Student();
		s1.setName("Parker");
		s1.setAge(18);
		s2 = new Student();
		s2.setName("Peter");
		s2.setAge(19);
		s3 = new Student();
		s3.setName("Miley");
		s3.setAge(16);
		t1 = new Teacher();
		t1.setTeacherName("Jackson");
		t1.setTeachYears(3);
		t1.setAge(28);
		t2 = new Teacher();
		t2.setTeacherName("Rose");
		t2.setTeachYears(12);
		t2.setAge(34);
	}

    @Test
	public void testUpdateWithStaticUpdate() {
		ContentValues values = new ContentValues();
		values.put("TEACHERNAME", "Toy");
		int rowsAffected = LitePal.update(Teacher.class, values, teacher.getId());
		assertEquals(1, rowsAffected);
		assertEquals("Toy", getTeacher(teacher.getId()).getTeacherName());
		values.clear();
		values.put("aGe", 15);
		rowsAffected = LitePal.update(Student.class, values, student.getId());
		assertEquals(1, rowsAffected);
		assertEquals(15, getStudent(student.getId()).getAge());
	}

    @Test
	public void testUpdateWithStaticUpdateButWrongClass() {
		ContentValues values = new ContentValues();
		values.put("TEACHERNAME", "Toy");
		try {
            LitePal.update(Object.class, values, teacher.getId());
		} catch (SQLiteException ignored) {
		}
	}

    @Test
	public void testUpdateWithStaticUpdateButWrongColumn() {
		ContentValues values = new ContentValues();
		values.put("TEACHERYEARS", 13);
		try {
            LitePal.update(Teacher.class, values, teacher.getId());
			fail("no such column: TEACHERYEARS");
		} catch (SQLiteException ignored) {
		}
	}

    @Test
	public void testUpdateWithStaticUpdateButNotExistsRecord() {
		ContentValues values = new ContentValues();
		values.put("TEACHERNAME", "Toy");
		int rowsAffected = LitePal.update(Teacher.class, values, 998909);
		assertEquals(0, rowsAffected);
	}

    @Test
	public void testUpdateWithInstanceUpdate() {
		Teacher t = new Teacher();
		t.setAge(66);
		t.setTeacherName("Jobs");
		t.setTeachYears(33);
		t.setSex(false);
		int rowsAffected = t.update(teacher.getId());
		assertEquals(1, rowsAffected);
		Teacher newTeacher = getTeacher(teacher.getId());
		assertEquals("Jobs", newTeacher.getTeacherName());
		assertEquals(33, newTeacher.getTeachYears());
		assertEquals(66, newTeacher.getAge());
	}

    @Test
	public void testUpdateWithDefaultValueWithInstanceUpdate() {
		Teacher t = new Teacher();
		t.setTeacherName("");
		t.setTeachYears(0);
		t.setSex(true);
		t.setAge(22);
		int affectedTeacher = t.update(teacher.getId());
		assertEquals(0, affectedTeacher);
		Teacher newTeacher = getTeacher(teacher.getId());
		assertEquals(teacher.getAge(), newTeacher.getAge());
		assertEquals(teacher.getTeacherName(), newTeacher.getTeacherName());
		assertEquals(teacher.getTeachYears(), newTeacher.getTeachYears());
		assertEquals(teacher.isSex(), newTeacher.isSex());
		Student s = new Student();
		s.setName(null);
		s.setAge(0);
		int affectedStudent = s.update(student.getId());
		assertEquals(0, affectedStudent);
		Student newStudent = getStudent(student.getId());
		assertEquals(student.getName(), newStudent.getName());
		assertEquals(student.getAge(), newStudent.getAge());
	}

    @Test
	public void testUpdateToDefaultValueWithInstanceUpdate() {
		Student s = new Student();
		s.setToDefault("age");
		s.setToDefault("name");
		s.setToDefault("birthday");
		int affectedStudent = s.update(student.getId());
		assertEquals(1, affectedStudent);
		Student newStudent = LitePal.find(Student.class, student.getId());
		assertNull(newStudent.getBirthday());
		assertNull(newStudent.getName());
		assertEquals(0, newStudent.getAge());
		Teacher t = new Teacher();
		t.setAge(45);
		t.setTeachYears(5);
		t.setTeacherName("John");
		t.setToDefault("teacherName");
		t.setToDefault("age");
		int affectedTeacher = t.update(teacher.getId());
		assertEquals(1, affectedTeacher);
		Teacher newTeacher = getTeacher(teacher.getId());
		assertEquals(22, newTeacher.getAge());
		assertEquals("", newTeacher.getTeacherName());
		assertEquals(5, newTeacher.getTeachYears());
	}

    @Test
	public void testUpdateToDefaultValueWithInstanceUpdateButWrongField() {
		try {
			Teacher t = new Teacher();
			t.setToDefault("name");
			t.update(t.getId());
			fail();
		} catch (DataSupportException e) {
			assertEquals(
					"The name field in com.litepaltest.model.Teacher class is necessary which does not exist.",
					e.getMessage());
		}
	}

    @Test
	public void testUpdateWithInstanceUpdateWithConstructor() {
		try {
			Computer computer = new Computer("ACER", 5444);
			computer.save();
			computer.update(computer.getId());
			fail();
		} catch (DataSupportException e) {
			assertEquals("com.litepaltest.model.Computer needs a default constructor.",
					e.getMessage());
		}
	}

    @Test
	public void testUpdateWithInstanceUpdateButNotExistsRecord() {
		Teacher t = new Teacher();
		t.setTeacherName("Johnny");
		int rowsAffected = t.update(189876465);
		assertEquals(0, rowsAffected);
	}

	// public void testUpdateM2OAssociationsOnMSideWithInstanceUpdate() {
	// initForAssociations();
	// s1.setClassroom(c1);
	// s2.setClassroom(c1);
	// assertTrue(c1.save());
	// assertTrue(c2.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// Student st = new Student();
	// st.setClassroom(c2);
	// int rowsAffected = st.update(s1.getId());
	// assertEquals(1, rowsAffected);
	// rowsAffected = st.update(s2.getId());
	// assertEquals(1, rowsAffected);
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s2.getId()));
	// }
	//
	// public void
	// testUpdateM2OAssociationsAndOtherFieldsOnMSideWithInstanceUpdate() {
	// initForAssociations();
	// s1.setClassroom(c1);
	// s2.setClassroom(c1);
	// assertTrue(c1.save());
	// assertTrue(c2.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// Student st = new Student();
	// st.setName("Jackson");
	// st.setClassroom(c2);
	// int rowsAffected = st.update(s1.getId());
	// assertEquals(1, rowsAffected);
	// rowsAffected = st.update(s2.getId());
	// assertEquals(1, rowsAffected);
	// assertEquals("Jackson", getStudent(s1.getId()).getName());
	// assertEquals("Jackson", getStudent(s2.getId()).getName());
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s2.getId()));
	// }
	//
	// public void testUpdateM2OAssociationsOnOSideWithInstanceUpdate() {
	// initForAssociations();
	// c1.getStudentCollection().add(s1);
	// c1.getStudentCollection().add(s2);
	// assertTrue(c1.save());
	// assertTrue(c2.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// Classroom c = new Classroom();
	// c.getStudentCollection().add(s1);
	// c.getStudentCollection().add(s2);
	// int rowsAffected = c.update(c2.get_id());
	// assertEquals(2, rowsAffected);
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s2.getId()));
	// }
	//
	// public void
	// testUpdateM2OAssociationsAndOtherFieldsOnOSideWithInstanceUpdate() {
	// initForAssociations();
	// c1.getStudentCollection().add(s1);
	// c1.getStudentCollection().add(s2);
	// assertTrue(c1.save());
	// assertTrue(c2.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// Classroom c = new Classroom();
	// c.setName("Game room");
	// c.getStudentCollection().add(s1);
	// c.getStudentCollection().add(s2);
	// int rowsAffected = c.update(c2.get_id());
	// assertEquals(3, rowsAffected);
	// assertEquals("Game room", getClassroom(c2.get_id()).getName());
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s2.getId()));
	// }
	//
	// public void
	// testUpdateM2OAssociationsOnMSideWithNotExistsRecordWithInstanceUpdate() {
	// initForAssociations();
	// s1.setClassroom(c1);
	// s2.setClassroom(c1);
	// assertTrue(c1.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// Student s = new Student();
	// s.setClassroom(c2);
	// int rowsAffected = s.update(s1.getId());
	// assertEquals(0, rowsAffected);
	// s.update(s2.getId());
	// assertEquals(0, rowsAffected);
	// assertEquals(c1.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// assertEquals(c1.get_id(), getForeignKeyValue("student", "classroom",
	// s2.getId()));
	// }
	//
	// public void
	// testUpdateM2OAssociationsOnOSideWithNotExistsRecordWithInstanceUpdate() {
	// initForAssociations();
	// c1.getStudentCollection().add(s1);
	// c1.getStudentCollection().add(s2);
	// assertTrue(c1.save());
	// assertTrue(c2.save());
	// assertTrue(s1.save());
	// Classroom c = new Classroom();
	// c.getStudentCollection().add(s1);
	// c.getStudentCollection().add(s2);
	// c.update(c2.get_id());
	// assertEquals(c2.get_id(), getForeignKeyValue("student", "classroom",
	// s1.getId()));
	// }
	//
	// public void testUpdateM2OAssociationsOnMSideWithNullWithInstanceUpdate()
	// {
	// initForAssociations();
	// s1.setClassroom(c1);
	// s2.setClassroom(c1);
	// assertTrue(c1.save());
	// assertTrue(s1.save());
	// assertTrue(s2.save());
	// }

    @Test
	public void testUpdateAllWithStaticUpdate() {
		Student s;
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++) {
			s = new Student();
			s.setName("Dusting");
			s.setAge(i + 10);
			s.save();
			ids[i] = s.getId();
		}
		ContentValues values = new ContentValues();
		values.put("age", 24);
		int affectedRows = LitePal.updateAll(Student.class, values, "name = ? and age = ?",
				"Dusting", "13");
		assertEquals(1, affectedRows);
		Student updatedStu = getStudent(ids[3]);
		assertEquals(24, updatedStu.getAge());
		values.clear();
		values.put("name", "Dustee");
		affectedRows = LitePal.updateAll(Student.class, values, "name = ?", "Dusting");
		assertEquals(5, affectedRows);
		List<Student> students = getStudents(ids);
		for (Student updatedStudent : students) {
			assertEquals("Dustee", updatedStudent.getName());
		}
	}

    @Test
	public void testUpdateAllRowsWithStaticUpdate() {
		int allRows = getRowsCount(studentTable);
		ContentValues values = new ContentValues();
		values.put("name", "Zuckerburg");
		int affectedRows = LitePal.updateAll(Student.class, values);
		assertEquals(allRows, affectedRows);
        String table = DBUtility.getIntermediateTableName(studentTable, DBUtility.getTableNameByClassName(Teacher.class.getName()));
		allRows = getRowsCount(table);
		values.clear();
		values.putNull(studentTable + "_id");
		affectedRows = LitePal.updateAll(table, values);
		assertEquals(allRows, affectedRows);
	}

    @Test
	public void testUpdateAllWithStaticUpdateButWrongConditions() {
		ContentValues values = new ContentValues();
		values.put("name", "Dustee");
		try {
            LitePal.updateAll(Student.class, values, "name = 'Dustin'", "aaa");
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
            LitePal.updateAll(Student.class, values, null, null);
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
            LitePal.updateAll(Student.class, values, "address = ?", "HK");
			fail();
		} catch (SQLiteException ignored) {
		}
	}

    @Test
	public void testUpdateAllWithInstanceUpdate() {
		Student s;
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++) {
			s = new Student();
			s.setName("Jessica");
			s.setAge(i + 10);
			s.save();
			ids[i] = s.getId();
		}
		Date date = new Date();
		Student toUpdate = new Student();
		toUpdate.setAge(24);
		toUpdate.setBirthday(date);
		int affectedRows = toUpdate.updateAll("name = ? and age = ?", "Jessica", "13");
		assertEquals(1, affectedRows);
		Student updatedStu = LitePal.find(Student.class, ids[3]);
		assertEquals(24, updatedStu.getAge());
		assertEquals(date.getTime(), updatedStu.getBirthday().getTime());
		toUpdate.setAge(18);
		toUpdate.setName("Jess");
		affectedRows = toUpdate.updateAll("name = ?", "Jessica");
		assertEquals(5, affectedRows);
		List<Student> students = getStudents(ids);
		for (Student updatedStudent : students) {
			assertEquals("Jess", updatedStudent.getName());
			assertEquals(18, updatedStudent.getAge());
		}
	}

    @Test
	public void testUpdateAllRowsWithInstanceUpdate() {
		Cursor c = Connector.getDatabase().query(studentTable, null, null, null, null, null, null);
		int allRows = c.getCount();
		c.close();
		Student student = new Student();
		student.setName("Zuckerburg");
		int affectedRows = student.updateAll();
		assertEquals(allRows, affectedRows);
	}

    @Test
	public void testUpdateAllWithDefaultValueWithInstanceUpdate() {
		Teacher tea;
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++) {
			tea = new Teacher();
			tea.setTeacherName("Rose Jackson");
			tea.setAge(50);
			tea.setTeachYears(15);
			tea.setSex(false);
			tea.save();
			ids[i] = tea.getId();
		}
		Teacher t = new Teacher();
		t.setTeacherName("");
		t.setTeachYears(0);
		t.setSex(true);
		t.setAge(22);
		int affectedTeacher = t.updateAll("teachername = 'Rose Jackson'");
		assertEquals(0, affectedTeacher);
		List<Teacher> teachers = getTeachers(ids);
		for (Teacher updatedTeacher : teachers) {
			assertEquals("Rose Jackson", updatedTeacher.getTeacherName());
			assertEquals(50, updatedTeacher.getAge());
			assertEquals(15, updatedTeacher.getTeachYears());
			assertFalse(updatedTeacher.isSex());
		}
	}

    @Test
	public void testUpdateAllToDefaultValueWithInstanceUpdate() {
		Student stu;
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++) {
			stu = new Student();
			stu.setName("Michael Jackson");
			stu.setAge(18);
			stu.save();
			ids[i] = stu.getId();
		}
		Student s = new Student();
		s.setToDefault("age");
		s.setToDefault("name");
		int affectedStudent = s.updateAll("name = 'Michael Jackson'");
		assertEquals(5, affectedStudent);
		List<Student> students = getStudents(ids);
		for (Student updatedStudent : students) {
			assertNull(updatedStudent.getName());
			assertEquals(0, updatedStudent.getAge());
		}
	}

    @Test
	public void testUpdateAllToDefaultValueWithInstanceUpdateButWrongField() {
		try {
			Teacher t = new Teacher();
			t.setToDefault("name");
			t.updateAll("");
			fail();
		} catch (DataSupportException e) {
			assertEquals(
					"The name field in com.litepaltest.model.Teacher class is necessary which does not exist.",
					e.getMessage());
		}
	}

    @Test
	public void testUpdateAllWithInstanceUpdateButWrongConditions() {
		Student student = new Student();
		student.setName("Dustee");
		try {
			student.updateAll("name = 'Dustin'", "aaa");
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
			student.updateAll(null, null);
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
			student.updateAll("address = ?", "HK");
			fail();
		} catch (Exception ignored) {
		}
	}

    @Test
    public void testUpdateGenericData() {
        Classroom c = new Classroom();
        c.setName("Math room");
        c.getNews().add("news");
        c.getNews().add("paper");
        c.update(classroom.get_id());
        Classroom result = LitePal.find(Classroom.class, classroom.get_id());
        assertEquals("Math room", result.getName());
        StringBuilder builder = new StringBuilder();
        for (String s : result.getNews()) {
            builder.append(s);
        }
        assertEquals("newspaper", builder.toString());
        assertEquals(2, result.getNumbers().size());
        Classroom c2 = new Classroom();
        c2.setToDefault("numbers");
        c2.update(classroom.get_id());
        result = LitePal.find(Classroom.class, classroom.get_id());
        assertEquals("Math room", result.getName());
        assertEquals(2, result.getNews().size());
        assertEquals(0, result.getNumbers().size());
    }

}
