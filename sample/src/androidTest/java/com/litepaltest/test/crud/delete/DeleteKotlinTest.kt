package com.litepaltest.test.crud.delete

import android.database.sqlite.SQLiteException
import androidx.test.filters.SmallTest
import com.litepaltest.model.Classroom
import com.litepaltest.model.IdCard
import com.litepaltest.model.Student
import com.litepaltest.model.Teacher
import com.litepaltest.test.LitePalTestCase
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.litepal.LitePal
import org.litepal.exceptions.DataSupportException
import org.litepal.extension.delete
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.util.DBUtility
import java.util.*

@SmallTest
class DeleteKotlinTest : LitePalTestCase() {

    private var gameRoom: Classroom? = null

    private var jude: Student? = null

    private var rose: Student? = null

    private var john: Teacher? = null

    private var mike: Teacher? = null

    private var judeCard: IdCard? = null

    private var roseCard: IdCard? = null

    private var johnCard: IdCard? = null

    private var mikeCard: IdCard? = null

    private var studentTable: String? = null

    private var teacherTable: String? = null

    @Before
    fun setUp() {
        studentTable = DBUtility.getTableNameByClassName(Student::class.java.name)
        teacherTable = DBUtility.getTableNameByClassName(Teacher::class.java.name)
    }

    @Test
    fun createClassroomStudentsTeachers() {
        initGameRoom()
        initRose()
        initJude()
        initMike()
        initJohn()
        val students = HashSet<Student>()
        students.add(rose!!)
        students.add(jude!!)
        gameRoom!!.studentCollection = students
        gameRoom!!.teachers.add(john)
        gameRoom!!.teachers.add(mike)
        gameRoom!!.save()
        rose!!.save()
        jude!!.save()
        john!!.save()
        mike!!.save()
    }

    @Test
    fun createStudentsTeachersWithIdCard() {
        initRose()
        initJude()
        initMike()
        initJohn()
        rose!!.save()
        jude!!.save()
        mike!!.save()
        john!!.save()
        roseCard!!.save()
        judeCard!!.save()
        mikeCard!!.save()
        johnCard!!.save()
    }

    @Test
    fun createStudentsTeachersWithAssociations() {
        initRose()
        initJude()
        initMike()
        initJohn()
        rose!!.teachers.add(john)
        rose!!.teachers.add(mike)
        jude!!.teachers.add(mike)
        rose!!.save()
        jude!!.save()
        john!!.save()
        mike!!.save()
    }

    @Test
    fun testDeleteWithNoParameter() {
        initJude()
        jude!!.save()
        val rowsAffected = jude!!.delete()
        assertEquals(1, rowsAffected)
        val s = getStudent(jude!!.id.toLong())
        assertNull(s)
    }

    @Test
    fun testDeleteById() {
        initJude()
        jude!!.save()
        val rowsAffected = LitePal.delete<Student>(jude!!.id.toLong())
        assertEquals(1, rowsAffected)
        val s = getStudent(jude!!.id.toLong())
        assertNull(s)
    }

    @Test
    fun testDeleteNoSavedModelWithNoParameter() {
        val tony = Student()
        tony.name = "Tony"
        tony.age = 23
        val rowsAffected = tony.delete()
        assertEquals(0, rowsAffected)
    }

    @Test
    fun testDeleteWithNotExistsRecordById() {
        val rowsAffected = LitePal.delete<Student>(998909)
        assertEquals(0, rowsAffected)
    }

    @Test
    fun testDeleteCascadeM2OAssociationsOnMSideWithNoParameter() {
        createClassroomStudentsTeachers()
        val rowsAffected = gameRoom!!.delete()
        assertEquals(5, rowsAffected)
        assertNull(getClassroom(gameRoom!!._id.toLong()))
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeM2OAssociationsOnMSideById() {
        createClassroomStudentsTeachers()
        val rowsAffected = LitePal.delete<Classroom>(gameRoom!!._id.toLong())
        assertEquals(5, rowsAffected)
        assertNull(getClassroom(gameRoom!!._id.toLong()))
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteAllCascadeM2OAssociationsOnMSide() {
        createClassroomStudentsTeachers()
        val rowsAffected = LitePal.deleteAll<Classroom>("id = ?", gameRoom!!._id.toString() + "")
        assertEquals(5, rowsAffected)
        assertNull(getClassroom(gameRoom!!._id.toLong()))
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeM2OAssociationsOnOSideWithNoParameter() {
        createClassroomStudentsTeachers()
        var rowsAffected = jude!!.delete()
        assertEquals(1, rowsAffected)
        assertNull(getStudent(jude!!.id.toLong()))
        rowsAffected = rose!!.delete()
        assertEquals(1, rowsAffected)
        assertNull(getStudent(rose!!.id.toLong()))
        rowsAffected = john!!.delete()
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(john!!.id.toLong()))
        rowsAffected = mike!!.delete()
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeM2OAssociationsOnOSideById() {
        createClassroomStudentsTeachers()
        var rowsAffected = LitePal.delete<Student>(jude!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertNull(getStudent(jude!!.id.toLong()))
        rowsAffected = LitePal.delete<Student>(rose!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertNull(getStudent(rose!!.id.toLong()))
        rowsAffected = LitePal.delete<Teacher>(john!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(john!!.id.toLong()))
        rowsAffected = LitePal.delete<Teacher>(mike!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteAllCascadeM2OAssociationsOnOSide() {
        createClassroomStudentsTeachers()
        var rowsAffected = LitePal.deleteAll<Student>("id = ?", jude!!.id.toString())
        assertEquals(1, rowsAffected)
        assertNull(getStudent(jude!!.id.toLong()))
        rowsAffected = LitePal.deleteAll<Student>("id = ?", rose!!.id.toString())
        assertEquals(1, rowsAffected)
        assertNull(getStudent(rose!!.id.toLong()))
        rowsAffected = LitePal.deleteAll<Teacher>("id = ?", john!!.id.toString())
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(john!!.id.toLong()))
        rowsAffected = LitePal.deleteAll<Teacher>("id = ?", mike!!.id.toString())
        assertEquals(1, rowsAffected)
        assertNull(getTeacher(mike!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeO2OAssociationsWithNoParameter() {
        createStudentsTeachersWithIdCard()
        var affectedRows = jude!!.delete()
        assertEquals(2, affectedRows)
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getIdCard(judeCard!!.id.toLong()))
        affectedRows = roseCard!!.delete()
        assertEquals(2, affectedRows)
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getIdCard(roseCard!!.id.toLong()))
        affectedRows = john!!.delete()
        assertEquals(2, affectedRows)
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getIdCard(johnCard!!.id.toLong()))
        affectedRows = mikeCard!!.delete()
        assertEquals(1, affectedRows)
        assertNull(getIdCard(mikeCard!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeO2OAssociationsById() {
        createStudentsTeachersWithIdCard()
        var affectedRows = LitePal.delete<Student>(jude!!.id.toLong())
        assertEquals(2, affectedRows)
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getIdCard(judeCard!!.id.toLong()))
        affectedRows = LitePal.delete<IdCard>(roseCard!!.id.toLong())
        assertEquals(2, affectedRows)
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getIdCard(roseCard!!.id.toLong()))
        affectedRows = LitePal.delete<Teacher>(john!!.id.toLong())
        assertEquals(2, affectedRows)
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getIdCard(johnCard!!.id.toLong()))
        affectedRows = LitePal.delete<IdCard>(mikeCard!!.id.toLong())
        assertEquals(1, affectedRows)
        assertNull(getIdCard(mikeCard!!.id.toLong()))
    }

    @Test
    fun testDeleteAllCascadeO2OAssociations() {
        createStudentsTeachersWithIdCard()
        var affectedRows = LitePal.deleteAll<Student>("id = ?", jude!!.id.toString())
        assertEquals(2, affectedRows)
        assertNull(getStudent(jude!!.id.toLong()))
        assertNull(getIdCard(judeCard!!.id.toLong()))
        affectedRows = LitePal.deleteAll<IdCard>("id = ?", roseCard!!.id.toString() + "")
        assertEquals(2, affectedRows)
        assertNull(getStudent(rose!!.id.toLong()))
        assertNull(getIdCard(roseCard!!.id.toLong()))
        affectedRows = LitePal.deleteAll<Teacher>("id = ?", "" + john!!.id)
        assertEquals(2, affectedRows)
        assertNull(getTeacher(john!!.id.toLong()))
        assertNull(getIdCard(johnCard!!.id.toLong()))
        affectedRows = LitePal.deleteAll<IdCard>("id=?", "" + mikeCard!!.id)
        assertEquals(1, affectedRows)
        assertNull(getIdCard(mikeCard!!.id.toLong()))
    }

    @Test
    fun testDeleteCascadeM2MAssociationsWithNoParameter() {
        createStudentsTeachersWithAssociations()
        var rowsAffected = jude!!.delete()
        assertEquals(2, rowsAffected)
        assertNull(getStudent(jude!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
        createStudentsTeachersWithAssociations()
        rowsAffected = rose!!.delete()
        assertEquals(3, rowsAffected)
        assertNull(getStudent(rose!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
        assertM2M(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
    }

    @Test
    fun testDeleteCascadeM2MAssociationsById() {
        createStudentsTeachersWithAssociations()
        var rowsAffected = LitePal.delete<Teacher>(john!!.id.toLong())
        assertEquals(2, rowsAffected)
        assertNull(getTeacher(john!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
        createStudentsTeachersWithAssociations()
        rowsAffected = LitePal.delete<Teacher>(mike!!.id.toLong())
        assertEquals(3, rowsAffected)
        assertNull(getTeacher(mike!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2MFalse(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
    }

    @Test
    fun testDeleteAllCascadeM2MAssociations() {
        createStudentsTeachersWithAssociations()
        var rowsAffected = LitePal.deleteAll<Teacher>("id=?", "" + john!!.id)
        assertEquals(2, rowsAffected)
        assertNull(getTeacher(john!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
        createStudentsTeachersWithAssociations()
        rowsAffected = LitePal.deleteAll<Teacher>("id=?", "" + mike!!.id)
        assertEquals(3, rowsAffected)
        assertNull(getTeacher(mike!!.id.toLong()))
        assertM2MFalse(studentTable, teacherTable, rose!!.id.toLong(), mike!!.id.toLong())
        assertM2MFalse(studentTable, teacherTable, jude!!.id.toLong(), mike!!.id.toLong())
        assertM2M(studentTable, teacherTable, rose!!.id.toLong(), john!!.id.toLong())
    }

    @Test
    fun testDeleteAllCascadeWithConditions() {
        val classroom = Classroom()
        classroom.name = "1" + System.currentTimeMillis()
        classroom.save()
        val classroom2 = Classroom()
        classroom2.name = "2" + System.currentTimeMillis()
        classroom2.save()
        val s1 = Student()
        s1.classroom = classroom
        s1.save()
        val s2 = Student()
        s2.classroom = classroom
        s2.save()
        val s3 = Student()
        s3.classroom = classroom2
        s3.save()
        var rows = LitePal.deleteAll<Classroom>("name = ?", classroom.name)
        assertEquals(3, rows)
        assertNull(getClassroom(classroom._id.toLong()))
        assertNull(getStudent(s1.id.toLong()))
        assertNull(getStudent(s2.id.toLong()))
        assertNotNull(getClassroom(classroom2._id.toLong()))
        assertNotNull(getStudent(s3.id.toLong()))
        rows = LitePal.deleteAll<Classroom>("name = ?", classroom2.name)
        assertEquals(2, rows)
        assertNull(getClassroom(classroom2._id.toLong()))
        assertNull(getStudent(s3.id.toLong()))
    }

    @Test
    fun testDeleteAll() {
        var s: Student
        val ids = IntArray(5)
        for (i in 0..4) {
            s = Student()
            s.name = "Dusting"
            s.age = i + 10086
            s.save()
            ids[i] = s.id
        }
        var affectedRows = LitePal.deleteAll<Student>("name = ? and age = ?", "Dusting", "10088")
        assertEquals(1, affectedRows)
        assertNull(getStudent(ids[2].toLong()))
        affectedRows = LitePal.deleteAll<Student>("name = ? and age > ? and age < ?", "Dusting", "10085", "10092")
        assertEquals(4, affectedRows)
    }

    @Test
    fun testDeleteAllRows() {
        createStudentsTeachersWithIdCard()
        var rowsCount = getRowsCount(teacherTable)
        var affectedRows = LitePal.deleteAll<Teacher>()
        assertTrue(rowsCount <= affectedRows)
        rowsCount = getRowsCount(studentTable)
        affectedRows = LitePal.deleteAll<Student>()
        assertTrue(rowsCount <= affectedRows)
        rowsCount = getRowsCount(DBUtility.getTableNameByClassName(IdCard::class.java.name))
        affectedRows = LitePal.deleteAll<IdCard>()
        assertTrue(rowsCount <= affectedRows)
        createStudentsTeachersWithAssociations()
        rowsCount = getRowsCount(teacherTable)
        affectedRows = LitePal.deleteAll<Teacher>()
        assertTrue(rowsCount <= affectedRows)
        rowsCount = getRowsCount(studentTable)
        affectedRows = LitePal.deleteAll<Student>()
        assertTrue(rowsCount <= affectedRows)
        rowsCount = getRowsCount(DBUtility.getIntermediateTableName(studentTable, teacherTable))
        affectedRows = LitePal.deleteAll(DBUtility.getIntermediateTableName(studentTable, teacherTable))
        assertTrue(rowsCount <= affectedRows)
    }

    @Test
    fun testMarkAsDeleted() {
        val students = ArrayList<Student>()
        for (i in 0..4) {
            val s = Student()
            s.name = "Dusting"
            s.age = i + 10
            students.add(s)
        }
        LitePal.saveAll(students)
        var list = LitePal.where("name=?", "Dusting").find<Student>()
        assertTrue(list.size >= 5)
        LitePal.deleteAll(Student::class.java, "name=?", "Dusting")
        list = LitePal.where("name=?", "Dusting").find()
        assertEquals(0, list.size)
        LitePal.saveAll(students)
        list = LitePal.where("name=?", "Dusting").find()
        assertEquals(0, list.size)
        LitePal.markAsDeleted(students)
        LitePal.saveAll(students)
        list = LitePal.where("name=?", "Dusting").find()
        assertEquals(5, list.size)
    }

    @Test
    fun testDeleteAllWithWrongConditions() {
        try {
            LitePal.deleteAll<Student>("name = 'Dustin'", "aaa")
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            LitePal.deleteAll<Student>(null, null)
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            LitePal.deleteAll<Student>("address = ?", "HK")
            fail()
        } catch (e: SQLiteException) {
        }

    }

    @Test
    fun testDeleteWithGenericData() {
        val classroom = Classroom()
        classroom.name = "classroom1"
        classroom.news.add("news1")
        classroom.news.add("news2")
        classroom.news.add("news3")
        classroom.save()
        val id = classroom._id
        val tableName = DBUtility.getGenericTableName(Classroom::class.java.name, "news")
        val column = DBUtility.getGenericValueIdColumnName(Classroom::class.java.name)
        var c = LitePal.findBySQL("select * from $tableName where $column = ?", id.toString())
        assertEquals(3, c!!.count)
        c.close()
        classroom.delete()
        c = LitePal.findBySQL("select * from $tableName where $column = ?", id.toString())
        assertEquals(0, c!!.count)
        c.close()
        assertFalse(classroom.isSaved)
        classroom.save()
        assertTrue(classroom.isSaved)
        c = LitePal.findBySQL("select * from $tableName where $column = ?", classroom._id.toString())
        assertEquals(3, c!!.count)
        c.close()
        LitePal.deleteAll<Classroom>("id = ?", classroom._id.toString())
        c = LitePal.findBySQL("select * from $tableName where $column = ?", classroom._id.toString())
        assertEquals(0, c!!.count)
        c.close()
    }

    private fun initGameRoom() {
        gameRoom = Classroom()
        gameRoom!!.name = "Game room"
    }

    private fun initJude() {
        jude = Student()
        jude!!.name = "Jude"
        jude!!.age = 13
        judeCard = IdCard()
        judeCard!!.address = "Jude Street"
        judeCard!!.number = "123456"
        jude!!.idcard = judeCard
        judeCard!!.student = jude
    }

    private fun initRose() {
        rose = Student()
        rose!!.name = "Rose"
        rose!!.age = 15
        roseCard = IdCard()
        roseCard!!.address = "Rose Street"
        roseCard!!.number = "123457"
        roseCard!!.student = rose
    }

    private fun initJohn() {
        john = Teacher()
        john!!.teacherName = "John"
        john!!.age = 33
        john!!.teachYears = 13
        johnCard = IdCard()
        johnCard!!.address = "John Street"
        johnCard!!.number = "123458"
        john!!.idCard = johnCard
    }

    private fun initMike() {
        mike = Teacher()
        mike!!.teacherName = "Mike"
        mike!!.age = 36
        mike!!.teachYears = 16
        mikeCard = IdCard()
        mikeCard!!.address = "Mike Street"
        mikeCard!!.number = "123459"
        mike!!.idCard = mikeCard
    }

}