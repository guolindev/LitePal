package com.litepaltest.test.crud.save;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.litepal.crud.DataSupport;
import org.litepal.util.DBUtility;

import com.litepaltest.model.Cellphone;
import com.litepaltest.model.Classroom;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class SaveAllTest extends AndroidTestCase {

    String classroomTable;

    String studentTable;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        classroomTable = DBUtility.getTableNameByClassName(Classroom.class.getName());
        studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
    }

    public void testSaveAll() {
		List<Cellphone> cellList = new ArrayList<Cellphone>();
		for (int i = 0; i < 50; i++) {
			Cellphone cellPhone = new Cellphone();
			cellPhone.setBrand("Samsung unique");
			cellPhone.setPrice(Math.random());
            cellPhone.setSerial(UUID.randomUUID().toString());
			cellList.add(cellPhone);
		}
		DataSupport.saveAll(cellList);
		for (Cellphone cell : cellList) {
			assertTrue(cell.isSaved());
		}
	}

	public void testSaveAllWithM2OOnOneSide() {
		Classroom classroom = new Classroom();
		classroom.setName("Music room");
		for (int i = 0; i < 50; i++) {
			Student student = new Student();
			student.setName("Tom");
			student.setAge(new Random().nextInt(20));
			classroom.getStudentCollection().add(student);
		}
		DataSupport.saveAll(classroom.getStudentCollection());
		classroom.save();
		List<Student> list = DataSupport.where(classroomTable + "_id = ?",
				String.valueOf(classroom.get_id())).find(Student.class);
		assertEquals(50, list.size());

	}

	public void testSaveAllWithM2OOnManySide() {
		Classroom classroom = new Classroom();
		classroom.setName("English room");
		List<Student> studentList = new ArrayList<Student>();
		for (int i = 0; i < 50; i++) {
			Student student = new Student();
			student.setName("Tom");
			student.setAge(new Random().nextInt(20));
			student.setClassroom(classroom);
			studentList.add(student);
		}
		DataSupport.saveAll(studentList);
		classroom.save();
		List<Student> list = DataSupport.where(classroomTable + "_id = ?",
				String.valueOf(classroom.get_id())).find(Student.class);
		assertEquals(50, list.size());
	}

	public void testSaveAllWithO2O() {
		List<IdCard> idcardList = new ArrayList<IdCard>();
		List<Student> studentList = new ArrayList<Student>();
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
		DataSupport.saveAll(idcardList);
		DataSupport.saveAll(studentList);
		for (Student student : studentList) {
			List<IdCard> result = DataSupport
					.where(studentTable + "_id=?", String.valueOf(student.getId())).find(IdCard.class);
			assertEquals(1, result.size());
		}
	}

	public void testSaveAllWithM2M() {
		List<Student> studentList = new ArrayList<Student>();
		List<Teacher> teacherList = new ArrayList<Teacher>();
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
		DataSupport.saveAll(studentList);
		DataSupport.saveAll(teacherList);
        String studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
        String teacherTable = DBUtility.getTableNameByClassName(Teacher.class.getName());
        String tableName = DBUtility.getIntermediateTableName(studentTable, teacherTable);
        for (Student student : studentList) {
			Cursor cursor = DataSupport.findBySQL(
					"select * from " + tableName + " where " + studentTable + "_id=?",
					String.valueOf(student.getId()));
			assertEquals(3, cursor.getCount());
			cursor.close();
		}
	}

    public void testSaveAllGenericData() {
        List<Classroom> classroomList = new ArrayList<Classroom>();
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
        DataSupport.saveAll(classroomList);
        assertEquals(50, classroomList.size());
        for (Classroom classroom : classroomList) {
            assertTrue(classroom.isSaved());
            Classroom c = DataSupport.find(Classroom.class, classroom.get_id());
            assertTrue(c.getName().startsWith("classroom"));
            assertEquals(20, c.getNews().size());
            assertEquals(13, c.getNumbers().size());
        }
    }

}