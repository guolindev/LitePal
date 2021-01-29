package com.litepaltest.test.crud.save;

import android.database.Cursor;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Cellphone;
import com.litepaltest.model.Classroom;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;

import org.junit.Before;
import org.junit.Test;
import org.litepal.LitePal;
import org.litepal.util.DBUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class SaveAllTest {

    String classroomTable;

    String studentTable;

    @Before
    public void setUp() {
        classroomTable = DBUtility.getTableNameByClassName(Classroom.class.getName());
        studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
    }

    @Test
    public void testSaveAll() {
		List<Cellphone> cellList = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			Cellphone cellPhone = new Cellphone();
			cellPhone.setBrand("Samsung unique");
			cellPhone.setPrice(Math.random());
            cellPhone.setSerial(UUID.randomUUID().toString());
			cellList.add(cellPhone);
		}
		assertTrue(LitePal.saveAll(cellList));
		for (Cellphone cell : cellList) {
			assertTrue(cell.isSaved());
		}
	}

    @Test
	public void testSaveAllWithM2OOnOneSide() {
		Classroom classroom = new Classroom();
		classroom.setName("Music room");
		for (int i = 0; i < 50; i++) {
			Student student = new Student();
			student.setName("Tom");
			student.setAge(new Random().nextInt(20));
			classroom.getStudentCollection().add(student);
		}
		assertTrue(LitePal.saveAll(classroom.getStudentCollection()));
		classroom.save();
		List<Student> list = LitePal.where(classroomTable + "_id = ?",
				String.valueOf(classroom.get_id())).find(Student.class);
		assertEquals(50, list.size());

	}

    @Test
	public void testSaveAllWithM2OOnManySide() {
		Classroom classroom = new Classroom();
		classroom.setName("English room");
		List<Student> studentList = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			Student student = new Student();
			student.setName("Tom");
			student.setAge(new Random().nextInt(20));
			student.setClassroom(classroom);
			studentList.add(student);
		}
		assertTrue(LitePal.saveAll(studentList));
		classroom.save();
		List<Student> list = LitePal.where(classroomTable + "_id = ?",
				String.valueOf(classroom.get_id())).find(Student.class);
		assertEquals(50, list.size());
	}

    @Test
	public void testSaveAllWithO2O() {
		List<IdCard> idcardList = new ArrayList<>();
		List<Student> studentList = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			IdCard idcard = new IdCard();
			idcard.setNumber(String.valueOf(new Random().nextInt(2000000)));
			Student student = new Student();
			student.setName("Jim");
			student.setAge(new Random().nextInt(20));
			student.setIdcard(idcard);
			idcardList.add(idcard);
			studentList.add(student);
		}
		assertTrue(LitePal.saveAll(idcardList));
		assertTrue(LitePal.saveAll(studentList));
		for (Student student : studentList) {
			List<IdCard> result = LitePal
					.where(studentTable + "_id=?", String.valueOf(student.getId())).find(IdCard.class);
			assertEquals(1, result.size());
		}
	}

    @Test
	public void testSaveAllWithM2M() {
		List<Student> studentList = new ArrayList<>();
		List<Teacher> teacherList = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			Teacher teacher = new Teacher();
			teacher.setTeacherName("Lucy");
			teacher.setTeachYears(new Random().nextInt(10));
			teacherList.add(teacher);
		}
		for (int i = 0; i < 50; i++) {
			Student student = new Student();
			student.setName("Timmy");
			student.setAge(new Random().nextInt(20));
			int index1 = new Random().nextInt(50);
			student.getTeachers().add(teacherList.get(index1));
			int index2 = index1;
			while (index2 == index1) {
				index2 = new Random().nextInt(50);
			}
			student.getTeachers().add(teacherList.get(index2));
			int index3 = index2;
			while (index3 == index2 || index3 == index1) {
				index3 = new Random().nextInt(50);
			}
			student.getTeachers().add(teacherList.get(index3));
			studentList.add(student);
		}
		assertTrue(LitePal.saveAll(studentList));
		assertTrue(LitePal.saveAll(teacherList));
        String studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
        String teacherTable = DBUtility.getTableNameByClassName(Teacher.class.getName());
        String tableName = DBUtility.getIntermediateTableName(studentTable, teacherTable);
        for (Student student : studentList) {
			Cursor cursor = LitePal.findBySQL(
					"select * from " + tableName + " where " + studentTable + "_id=?",
					String.valueOf(student.getId()));
			assertEquals(3, cursor.getCount());
			cursor.close();
		}
	}

    @Test
    public void testSaveAllGenericData() {
        List<Classroom> classroomList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Classroom classroom = new Classroom();
            classroom.setName("classroom " + i);
            for (int j = 0; j < 20; j++) {
                classroom.getNews().add("news " + i);
            }
            for (int k = 0; k < 13; k++) {
                classroom.getNumbers().add(k);
            }
            classroomList.add(classroom);
        }
		assertTrue(LitePal.saveAll(classroomList));
        assertEquals(50, classroomList.size());
        for (Classroom classroom : classroomList) {
            assertTrue(classroom.isSaved());
            Classroom c = LitePal.find(Classroom.class, classroom.get_id());
            assertTrue(c.getName().startsWith("classroom"));
            assertEquals(20, c.getNews().size());
            assertEquals(13, c.getNumbers().size());
        }
    }

    @Test
    public void testSaveAllFailed() {
    	List<Cellphone> cellphones = new ArrayList<>();
    	String serial = UUID.randomUUID().toString();
		for (int i = 0; i < 20; i++) {
			Cellphone cellphone = new Cellphone();
			cellphone.setBrand("Apple");
			cellphone.setSerial(serial + (i % 10)); // serial is unique, so this should save failed
			cellphones.add(cellphone);
		}
		assertFalse(LitePal.saveAll(cellphones));
		List<Cellphone> list = LitePal.where("serial like ?", serial + "%").find(Cellphone.class);
		assertTrue(list.isEmpty());
	}

}