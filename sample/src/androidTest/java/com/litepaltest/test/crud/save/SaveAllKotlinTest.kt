package com.litepaltest.test.crud.save

import androidx.test.filters.SmallTest
import com.litepaltest.model.*
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.litepal.LitePal.find
import org.litepal.LitePal.findBySQL
import org.litepal.LitePal.where
import org.litepal.extension.saveAll
import org.litepal.util.DBUtility
import java.util.*

@SmallTest
class SaveAllKotlinTest {

    var classroomTable: String? = null

    var studentTable: String? = null

    @Before
    fun setUp() {
        classroomTable = DBUtility.getTableNameByClassName(Classroom::class.java.name)
        studentTable = DBUtility.getTableNameByClassName(Student::class.java.name)
    }

    @Test
    fun testSaveAll() {
        val cellList: MutableList<Cellphone> = ArrayList()
        for (i in 0..49) {
            val cellPhone = Cellphone()
            cellPhone.setBrand("Samsung unique")
            cellPhone.price = Math.random()
            cellPhone.serial = UUID.randomUUID().toString()
            cellList.add(cellPhone)
        }
        TestCase.assertTrue(cellList.saveAll())
        for (cell in cellList) {
            TestCase.assertTrue(cell.isSaved)
        }
    }

    @Test
    fun testSaveAllWithM2OOnOneSide() {
        val classroom = Classroom()
        classroom.name = "Music room"
        for (i in 0..49) {
            val student = Student()
            student.name = "Tom"
            student.age = Random().nextInt(20)
            classroom.studentCollection.add(student)
        }
        TestCase.assertTrue(classroom.studentCollection.saveAll())
        classroom.save()
        val list = where(classroomTable + "_id = ?", classroom._id.toString()).find(Student::class.java)
        assertEquals(50, list.size)
    }

    @Test
    fun testSaveAllWithM2OOnManySide() {
        val classroom = Classroom()
        classroom.name = "English room"
        val studentList: MutableList<Student> = ArrayList()
        for (i in 0..49) {
            val student = Student()
            student.name = "Tom"
            student.age = Random().nextInt(20)
            student.classroom = classroom
            studentList.add(student)
        }
        TestCase.assertTrue(studentList.saveAll())
        classroom.save()
        val list = where(classroomTable + "_id = ?", classroom._id.toString()).find(Student::class.java)
        assertEquals(50, list.size)
    }

    @Test
    fun testSaveAllWithO2O() {
        val idcardList: MutableList<IdCard> = ArrayList()
        val studentList: MutableList<Student> = ArrayList()
        for (i in 0..49) {
            val idcard = IdCard()
            idcard.number = Random().nextInt(2000000).toString()
            val student = Student()
            student.name = "Jim"
            student.age = Random().nextInt(20)
            student.idcard = idcard
            idcardList.add(idcard)
            studentList.add(student)
        }
        TestCase.assertTrue(idcardList.saveAll())
        TestCase.assertTrue(studentList.saveAll())
        for (student in studentList) {
            val result = where(studentTable + "_id=?", student.id.toString()).find(IdCard::class.java)
            assertEquals(1, result.size)
        }
    }

    @Test
    fun testSaveAllWithM2M() {
        val studentList: MutableList<Student> = ArrayList()
        val teacherList: MutableList<Teacher> = ArrayList()
        for (i in 0..49) {
            val teacher = Teacher()
            teacher.teacherName = "Lucy"
            teacher.teachYears = Random().nextInt(10)
            teacherList.add(teacher)
        }
        for (i in 0..49) {
            val student = Student()
            student.name = "Timmy"
            student.age = Random().nextInt(20)
            val index1 = Random().nextInt(50)
            student.teachers.add(teacherList[index1])
            var index2 = index1
            while (index2 == index1) {
                index2 = Random().nextInt(50)
            }
            student.teachers.add(teacherList[index2])
            var index3 = index2
            while (index3 == index2 || index3 == index1) {
                index3 = Random().nextInt(50)
            }
            student.teachers.add(teacherList[index3])
            studentList.add(student)
        }
        TestCase.assertTrue(studentList.saveAll())
        TestCase.assertTrue(teacherList.saveAll())
        val studentTable = DBUtility.getTableNameByClassName(Student::class.java.name)
        val teacherTable = DBUtility.getTableNameByClassName(Teacher::class.java.name)
        val tableName = DBUtility.getIntermediateTableName(studentTable, teacherTable)
        for (student in studentList) {
            val cursor = findBySQL(
                    "select * from " + tableName + " where " + studentTable + "_id=?", student.id.toString())
            assertEquals(3, cursor.count)
            cursor.close()
        }
    }

    @Test
    fun testSaveAllGenericData() {
        val classroomList: MutableList<Classroom> = ArrayList()
        for (i in 0..49) {
            val classroom = Classroom()
            classroom.name = "classroom $i"
            for (j in 0..19) {
                classroom.news.add("news $i")
            }
            for (k in 0..12) {
                classroom.numbers.add(k)
            }
            classroomList.add(classroom)
        }
        TestCase.assertTrue(classroomList.saveAll())
        assertEquals(50, classroomList.size)
        for (classroom in classroomList) {
            TestCase.assertTrue(classroom.isSaved)
            val c = find(Classroom::class.java, classroom._id.toLong())
            TestCase.assertTrue(c.name.startsWith("classroom"))
            assertEquals(20, c.news.size)
            assertEquals(13, c.numbers.size)
        }
    }

    @Test
    fun testSaveAllFailed() {
        val cellphones: MutableList<Cellphone> = ArrayList()
        val serial = UUID.randomUUID().toString()
        for (i in 0..19) {
            val cellphone = Cellphone()
            cellphone.setBrand("Apple")
            cellphone.serial = serial + i % 10 // serial is unique, so this should save failed
            cellphones.add(cellphone)
        }
        TestCase.assertFalse(cellphones.saveAll())
        val list = where("serial like ?", "$serial%").find(Cellphone::class.java)
        TestCase.assertTrue(list.isEmpty())
    }

}