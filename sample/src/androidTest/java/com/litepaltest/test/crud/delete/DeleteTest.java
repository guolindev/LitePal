package com.litepaltest.test.crud.delete;

import android.database.Cursor;

import android.database.sqlite.SQLiteException;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Classroom;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;
import com.litepaltest.test.LitePalTestCase;

import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;
import org.litepal.exceptions.DataSupportException;
import org.litepal.util.DBUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

@SmallTest
public class DeleteTest extends LitePalTestCase {

	private Classroom gameRoom;

	private Student jude;

	private Student rose;

	private Teacher john;

	private Teacher mike;

	private IdCard judeCard;

	private IdCard roseCard;

	private IdCard johnCard;

	private IdCard mikeCard;
    
    private String studentTable;
    
    private String teacherTable;

    @Before
    public void setUp() {
        studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
        teacherTable = DBUtility.getTableNameByClassName(Teacher.class.getName());
    }

    @Test
    public void createClassroomStudentsTeachers() {
		initGameRoom();
		initRose();
		initJude();
		initMike();
		initJohn();
		Set<Student> students = new HashSet<Student>();
		students.add(rose);
		students.add(jude);
		gameRoom.setStudentCollection(students);
		gameRoom.getTeachers().add(john);
		gameRoom.getTeachers().add(mike);
		gameRoom.save();
		rose.save();
		jude.save();
		john.save();
		mike.save();
	}

    @Test
	public void createStudentsTeachersWithIdCard() {
		initRose();
		initJude();
		initMike();
		initJohn();
		rose.save();
		jude.save();
		mike.save();
		john.save();
		roseCard.save();
		judeCard.save();
		mikeCard.save();
		johnCard.save();
	}

    @Test
	public void createStudentsTeachersWithAssociations() {
		initRose();
		initJude();
		initMike();
		initJohn();
		rose.getTeachers().add(john);
		rose.getTeachers().add(mike);
		jude.getTeachers().add(mike);
		rose.save();
		jude.save();
		john.save();
		mike.save();
	}

    @Test
	public void testDeleteWithNoParameter() {
		initJude();
		jude.save();
		int rowsAffected = jude.delete();
		assertEquals(1, rowsAffected);
		Student s = getStudent(jude.getId());
		assertNull(s);
	}

    @Test
	public void testDeleteById() {
		initJude();
		jude.save();
		int rowsAffected = LitePal.delete(Student.class, jude.getId());
		assertEquals(1, rowsAffected);
		Student s = getStudent(jude.getId());
		assertNull(s);
	}

    @Test
	public void testDeleteNoSavedModelWithNoParameter() {
		Student tony = new Student();
		tony.setName("Tony");
		tony.setAge(23);
		int rowsAffected = tony.delete();
		assertEquals(0, rowsAffected);
	}

    @Test
	public void testDeleteWithNotExistsRecordById() {
		int rowsAffected = LitePal.delete(Student.class, 998909);
		assertEquals(0, rowsAffected);
	}

    @Test
	public void testDeleteCascadeM2OAssociationsOnMSideWithNoParameter() {
		createClassroomStudentsTeachers();
		int rowsAffected = gameRoom.delete();
		assertEquals(5, rowsAffected);
		assertNull(getClassroom(gameRoom.get_id()));
		assertNull(getStudent(jude.getId()));
		assertNull(getStudent(rose.getId()));
		assertNull(getTeacher(john.getId()));
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteCascadeM2OAssociationsOnMSideById() {
		createClassroomStudentsTeachers();
		int rowsAffected = LitePal.delete(Classroom.class, gameRoom.get_id());
		assertEquals(5, rowsAffected);
		assertNull(getClassroom(gameRoom.get_id()));
		assertNull(getStudent(jude.getId()));
		assertNull(getStudent(rose.getId()));
		assertNull(getTeacher(john.getId()));
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteAllCascadeM2OAssociationsOnMSide() {
		createClassroomStudentsTeachers();
		int rowsAffected = LitePal.deleteAll(Classroom.class, "id = ?", gameRoom.get_id() + "");
		assertEquals(5, rowsAffected);
		assertNull(getClassroom(gameRoom.get_id()));
		assertNull(getStudent(jude.getId()));
		assertNull(getStudent(rose.getId()));
		assertNull(getTeacher(john.getId()));
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteCascadeM2OAssociationsOnOSideWithNoParameter() {
		createClassroomStudentsTeachers();
		int rowsAffected = jude.delete();
		assertEquals(1, rowsAffected);
		assertNull(getStudent(jude.getId()));
		rowsAffected = rose.delete();
		assertEquals(1, rowsAffected);
		assertNull(getStudent(rose.getId()));
		rowsAffected = john.delete();
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(john.getId()));
		rowsAffected = mike.delete();
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteCascadeM2OAssociationsOnOSideById() {
		createClassroomStudentsTeachers();
		int rowsAffected = LitePal.delete(Student.class, jude.getId());
		assertEquals(1, rowsAffected);
		assertNull(getStudent(jude.getId()));
		rowsAffected = LitePal.delete(Student.class, rose.getId());
		assertEquals(1, rowsAffected);
		assertNull(getStudent(rose.getId()));
		rowsAffected = LitePal.delete(Teacher.class, john.getId());
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(john.getId()));
		rowsAffected = LitePal.delete(Teacher.class, mike.getId());
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteAllCascadeM2OAssociationsOnOSide() {
		createClassroomStudentsTeachers();
		int rowsAffected = LitePal.deleteAll(Student.class, "id = ?", String.valueOf(jude.getId()));
		assertEquals(1, rowsAffected);
		assertNull(getStudent(jude.getId()));
		rowsAffected = LitePal.deleteAll(Student.class, "id = ?", String.valueOf(rose.getId()));
		assertEquals(1, rowsAffected);
		assertNull(getStudent(rose.getId()));
		rowsAffected = LitePal.deleteAll(Teacher.class, "id = ?", String.valueOf(john.getId()));
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(john.getId()));
		rowsAffected = LitePal.deleteAll(Teacher.class, "id = ?", String.valueOf(mike.getId()));
		assertEquals(1, rowsAffected);
		assertNull(getTeacher(mike.getId()));
	}

    @Test
	public void testDeleteCascadeO2OAssociationsWithNoParameter() {
		createStudentsTeachersWithIdCard();
		int affectedRows = jude.delete();
		assertEquals(2, affectedRows);
		assertNull(getStudent(jude.getId()));
		assertNull(getIdCard(judeCard.getId()));
		affectedRows = roseCard.delete();
		assertEquals(2, affectedRows);
		assertNull(getStudent(rose.getId()));
		assertNull(getIdCard(roseCard.getId()));
		affectedRows = john.delete();
		assertEquals(2, affectedRows);
		assertNull(getTeacher(john.getId()));
		assertNull(getIdCard(johnCard.getId()));
		affectedRows = mikeCard.delete();
		assertEquals(1, affectedRows);
		assertNull(getIdCard(mikeCard.getId()));
	}

    @Test
	public void testDeleteCascadeO2OAssociationsById() {
		createStudentsTeachersWithIdCard();
		int affectedRows = LitePal.delete(Student.class, jude.getId());
		assertEquals(2, affectedRows);
		assertNull(getStudent(jude.getId()));
		assertNull(getIdCard(judeCard.getId()));
		affectedRows = LitePal.delete(IdCard.class, roseCard.getId());
		assertEquals(2, affectedRows);
		assertNull(getStudent(rose.getId()));
		assertNull(getIdCard(roseCard.getId()));
		affectedRows = LitePal.delete(Teacher.class, john.getId());
		assertEquals(2, affectedRows);
		assertNull(getTeacher(john.getId()));
		assertNull(getIdCard(johnCard.getId()));
		affectedRows = LitePal.delete(IdCard.class, mikeCard.getId());
		assertEquals(1, affectedRows);
		assertNull(getIdCard(mikeCard.getId()));
	}

    @Test
	public void testDeleteAllCascadeO2OAssociations() {
		createStudentsTeachersWithIdCard();
		int affectedRows = LitePal.deleteAll(Student.class, "id = ?", String.valueOf(jude.getId()));
		assertEquals(2, affectedRows);
		assertNull(getStudent(jude.getId()));
		assertNull(getIdCard(judeCard.getId()));
		affectedRows = LitePal.deleteAll(IdCard.class, "id = ?", roseCard.getId() + "");
		assertEquals(2, affectedRows);
		assertNull(getStudent(rose.getId()));
		assertNull(getIdCard(roseCard.getId()));
		affectedRows = LitePal.deleteAll(Teacher.class, "id = ?", "" + john.getId());
		assertEquals(2, affectedRows);
		assertNull(getTeacher(john.getId()));
		assertNull(getIdCard(johnCard.getId()));
		affectedRows = LitePal.deleteAll(IdCard.class, "id=?", "" + mikeCard.getId());
		assertEquals(1, affectedRows);
		assertNull(getIdCard(mikeCard.getId()));
	}

    @Test
	public void testDeleteCascadeM2MAssociationsWithNoParameter() {
		createStudentsTeachersWithAssociations();
		int rowsAffected = jude.delete();
		assertEquals(2, rowsAffected);
		assertNull(getStudent(jude.getId()));
		assertM2MFalse(studentTable, teacherTable, jude.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), john.getId());
		createStudentsTeachersWithAssociations();
		rowsAffected = rose.delete();
		assertEquals(3, rowsAffected);
		assertNull(getStudent(rose.getId()));
		assertM2MFalse(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2MFalse(studentTable, teacherTable, rose.getId(), john.getId());
		assertM2M(studentTable, teacherTable, jude.getId(), mike.getId());
	}

    @Test
	public void testDeleteCascadeM2MAssociationsById() {
		createStudentsTeachersWithAssociations();
		int rowsAffected = LitePal.delete(Teacher.class, john.getId());
		assertEquals(2, rowsAffected);
		assertNull(getTeacher(john.getId()));
		assertM2MFalse(studentTable, teacherTable, rose.getId(), john.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, jude.getId(), mike.getId());
		createStudentsTeachersWithAssociations();
		rowsAffected = LitePal.delete(Teacher.class, mike.getId());
		assertEquals(3, rowsAffected);
		assertNull(getTeacher(mike.getId()));
		assertM2MFalse(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2MFalse(studentTable, teacherTable, jude.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), john.getId());
	}

    @Test
	public void testDeleteAllCascadeM2MAssociations() {
		createStudentsTeachersWithAssociations();
		int rowsAffected = LitePal.deleteAll(Teacher.class, "id=?", "" + john.getId());
		assertEquals(2, rowsAffected);
		assertNull(getTeacher(john.getId()));
		assertM2MFalse(studentTable, teacherTable, rose.getId(), john.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, jude.getId(), mike.getId());
		createStudentsTeachersWithAssociations();
		rowsAffected = LitePal.deleteAll(Teacher.class, "id=?", "" + mike.getId());
		assertEquals(3, rowsAffected);
		assertNull(getTeacher(mike.getId()));
		assertM2MFalse(studentTable, teacherTable, rose.getId(), mike.getId());
		assertM2MFalse(studentTable, teacherTable, jude.getId(), mike.getId());
		assertM2M(studentTable, teacherTable, rose.getId(), john.getId());
	}

    @Test
	public void testDeleteAllCascadeWithConditions() {
		Classroom classroom = new Classroom();
		classroom.setName("1"+System.currentTimeMillis());
		classroom.save();
		Classroom classroom2 = new Classroom();
		classroom2.setName("2"+ System.currentTimeMillis());
		classroom2.save();
		Student s1 = new Student();
		s1.setClassroom(classroom);
		s1.save();
		Student s2 = new Student();
		s2.setClassroom(classroom);
		s2.save();
		Student s3 = new Student();
		s3.setClassroom(classroom2);
		s3.save();
		int rows = LitePal.deleteAll(Classroom.class, "name = ?", classroom.getName());
		assertEquals(3, rows);
		assertNull(getClassroom(classroom.get_id()));
		assertNull(getStudent(s1.getId()));
		assertNull(getStudent(s2.getId()));
		assertNotNull(getClassroom(classroom2.get_id()));
		assertNotNull(getStudent(s3.getId()));
		rows = LitePal.deleteAll(Classroom.class, "name = ?", classroom2.getName());
		assertEquals(2, rows);
		assertNull(getClassroom(classroom2.get_id()));
		assertNull(getStudent(s3.getId()));
	}

    @Test
	public void testDeleteAll() {
		Student s;
		int[] ids = new int[5];
		for (int i = 0; i < 5; i++) {
			s = new Student();
			s.setName("Dusting");
			s.setAge(i + 10086);
			s.save();
			ids[i] = s.getId();
		}
		int affectedRows = LitePal.deleteAll(Student.class, "name = ? and age = ?", "Dusting",
				"10088");
		assertEquals(1, affectedRows);
		assertNull(getStudent(ids[2]));
		affectedRows = LitePal.deleteAll(Student.class, "name = ? and age > ? and age < ?", "Dusting", "10085", "10092");
		assertEquals(4, affectedRows);
	}

    @Test
	public void testDeleteAllRows() {
		createStudentsTeachersWithIdCard();
		int rowsCount = getRowsCount(teacherTable);
		int affectedRows = 0;
		affectedRows = LitePal.deleteAll(Teacher.class);
		assertTrue(rowsCount <= affectedRows);
		rowsCount = getRowsCount(studentTable);
		affectedRows = LitePal.deleteAll(Student.class);
		assertTrue(rowsCount<= affectedRows);
		rowsCount = getRowsCount(DBUtility.getTableNameByClassName(IdCard.class.getName()));
		affectedRows = LitePal.deleteAll(IdCard.class);
		assertTrue(rowsCount<=affectedRows);
		createStudentsTeachersWithAssociations();
		rowsCount = getRowsCount(teacherTable);
		affectedRows = LitePal.deleteAll(Teacher.class);
		assertTrue(rowsCount<=affectedRows);
		rowsCount = getRowsCount(studentTable);
		affectedRows = LitePal.deleteAll(Student.class);
		assertTrue(rowsCount<=affectedRows);
		rowsCount = getRowsCount(DBUtility.getIntermediateTableName(studentTable, teacherTable));
		affectedRows = LitePal.deleteAll(DBUtility.getIntermediateTableName(studentTable, teacherTable));
		assertTrue(rowsCount<=affectedRows);
	}

    @Test
    public void testMarkAsDeleted() {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Student s = new Student();
            s.setName("Dusting");
            s.setAge(i + 10);
            students.add(s);
        }
        LitePal.saveAll(students);
        List<Student> list = LitePal.where("name=?", "Dusting").find(Student.class);
        assertTrue(list.size() >= 5);
        LitePal.deleteAll(Student.class, "name=?", "Dusting");
        list = LitePal.where("name=?", "Dusting").find(Student.class);
        assertEquals(0, list.size());
        LitePal.saveAll(students);
        list = LitePal.where("name=?", "Dusting").find(Student.class);
        assertEquals(0, list.size());
        LitePal.markAsDeleted(students);
        LitePal.saveAll(students);
        list = LitePal.where("name=?", "Dusting").find(Student.class);
        assertEquals(5, list.size());
    }

    @Test
	public void testDeleteAllWithWrongConditions() {
		try {
            LitePal.deleteAll(Student.class, "name = 'Dustin'", "aaa");
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
            LitePal.deleteAll(Student.class, null, null);
			fail();
		} catch (DataSupportException e) {
			assertEquals("The parameters in conditions are incorrect.", e.getMessage());
		}
		try {
            LitePal.deleteAll(Student.class, "address = ?", "HK");
			fail();
		} catch (SQLiteException e) {
		}
	}

    @Test
    public void testDeleteWithGenericData() {
        Classroom classroom = new Classroom();
        classroom.setName("classroom1");
        classroom.getNews().add("news1");
        classroom.getNews().add("news2");
        classroom.getNews().add("news3");
        classroom.save();
        int id = classroom.get_id();
        String tableName = DBUtility.getGenericTableName(Classroom.class.getName(), "news");
        String column = DBUtility.getGenericValueIdColumnName(Classroom.class.getName());
        Cursor c = LitePal.findBySQL("select * from " + tableName + " where " + column + " = ?", String.valueOf(id));
        assertEquals(3, c.getCount());
        c.close();
        classroom.delete();
        c = LitePal.findBySQL("select * from " + tableName + " where " + column + " = ?", String.valueOf(id));
        assertEquals(0, c.getCount());
        c.close();
        assertFalse(classroom.isSaved());
        classroom.save();
        assertTrue(classroom.isSaved());
        c = LitePal.findBySQL("select * from " + tableName + " where " + column + " = ?", String.valueOf(classroom.get_id()));
        assertEquals(3, c.getCount());
        c.close();
        LitePal.deleteAll(Classroom.class, "id = ?", String.valueOf(classroom.get_id()));
        c = LitePal.findBySQL("select * from " + tableName + " where " + column + " = ?", String.valueOf(classroom.get_id()));
        assertEquals(0, c.getCount());
        c.close();
    }

    private void initGameRoom() {
		gameRoom = new Classroom();
		gameRoom.setName("Game room");
	}

	private void initJude() {
		jude = new Student();
		jude.setName("Jude");
		jude.setAge(13);
		judeCard = new IdCard();
		judeCard.setAddress("Jude Street");
		judeCard.setNumber("123456");
		jude.setIdcard(judeCard);
		judeCard.setStudent(jude);
	}

	private void initRose() {
		rose = new Student();
		rose.setName("Rose");
		rose.setAge(15);
		roseCard = new IdCard();
		roseCard.setAddress("Rose Street");
		roseCard.setNumber("123457");
		roseCard.setStudent(rose);
	}

	private void initJohn() {
		john = new Teacher();
		john.setTeacherName("John");
		john.setAge(33);
		john.setTeachYears(13);
		johnCard = new IdCard();
		johnCard.setAddress("John Street");
		johnCard.setNumber("123458");
		john.setIdCard(johnCard);
	}

	private void initMike() {
		mike = new Teacher();
		mike.setTeacherName("Mike");
		mike.setAge(36);
		mike.setTeachYears(16);
		mikeCard = new IdCard();
		mikeCard.setAddress("Mike Street");
		mikeCard.setNumber("123459");
		mike.setIdCard(mikeCard);
	}

}
