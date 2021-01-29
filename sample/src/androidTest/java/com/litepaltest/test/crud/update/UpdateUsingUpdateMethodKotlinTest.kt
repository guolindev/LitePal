package com.litepaltest.test.crud.update

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.test.filters.SmallTest
import com.litepaltest.model.*
import com.litepaltest.test.LitePalTestCase
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.litepal.LitePal
import org.litepal.exceptions.DataSupportException
import org.litepal.extension.find
import org.litepal.extension.update
import org.litepal.extension.updateAll
import org.litepal.tablemanager.Connector
import org.litepal.util.DBUtility
import java.util.*

@SmallTest
class UpdateUsingUpdateMethodKotlinTest : LitePalTestCase() {

    private var teacher: Teacher? = null

    private var student: Student? = null

    private var classroom: Classroom? = null

    private var studentTable: String? = null

    @Before
    fun setUp() {
        studentTable = DBUtility.getTableNameByClassName(Student::class.java.name)
        init()
    }

    private fun init() {
        classroom = Classroom()
        classroom!!.name = "English room"
        classroom!!.news.add("hello")
        classroom!!.news.add("world")
        classroom!!.numbers.add(123)
        classroom!!.numbers.add(456)
        teacher = Teacher()
        teacher!!.teacherName = "Tony"
        teacher!!.teachYears = 3
        teacher!!.age = 23
        teacher!!.isSex = false
        student = Student()
        student!!.name = "Jonny"
        student!!.age = 13
        student!!.classroom = classroom
        student!!.birthday = Date()
        student!!.teachers.add(teacher)
        teacher!!.students.add(student)
        student!!.save()
        teacher!!.save()
        classroom!!.save()
    }

    @Test
    fun testUpdateWithStaticUpdate() {
        val values = ContentValues()
        values.put("TEACHERNAME", "Toy")
        var rowsAffected = LitePal.update<Teacher>(values, teacher!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertEquals("Toy", getTeacher(teacher!!.id.toLong())!!.teacherName)
        values.clear()
        values.put("aGe", 15)
        rowsAffected = LitePal.update<Student>(values, student!!.id.toLong())
        assertEquals(1, rowsAffected)
        assertEquals(15, getStudent(student!!.id.toLong())!!.age)
    }

    @Test
    fun testUpdateWithStaticUpdateButWrongClass() {
        val values = ContentValues()
        values.put("TEACHERNAME", "Toy")
        try {
            LitePal.update<Any>(values, teacher!!.id.toLong())
        } catch (e: SQLiteException) {
        }

    }

    @Test
    fun testUpdateWithStaticUpdateButWrongColumn() {
        val values = ContentValues()
        values.put("TEACHERYEARS", 13)
        try {
            LitePal.update(Teacher::class.java, values, teacher!!.id.toLong())
            fail("no such column: TEACHERYEARS")
        } catch (e: SQLiteException) {
        }

    }

    @Test
    fun testUpdateWithStaticUpdateButNotExistsRecord() {
        val values = ContentValues()
        values.put("TEACHERNAME", "Toy")
        val rowsAffected = LitePal.update(Teacher::class.java, values, 998909)
        assertEquals(0, rowsAffected)
    }

    @Test
    fun testUpdateWithInstanceUpdate() {
        val t = Teacher()
        t.age = 66
        t.teacherName = "Jobs"
        t.teachYears = 33
        t.isSex = false
        val rowsAffected = t.update(teacher!!.id.toLong())
        assertEquals(1, rowsAffected)
        val newTeacher = getTeacher(teacher!!.id.toLong())
        assertEquals("Jobs", newTeacher!!.teacherName)
        assertEquals(33, newTeacher.teachYears)
        assertEquals(66, newTeacher.age)
    }

    @Test
    fun testUpdateWithDefaultValueWithInstanceUpdate() {
        val t = Teacher()
        t.teacherName = ""
        t.teachYears = 0
        t.isSex = true
        t.age = 22
        val affectedTeacher = t.update(teacher!!.id.toLong())
        assertEquals(0, affectedTeacher)
        val newTeacher = getTeacher(teacher!!.id.toLong())
        assertEquals(teacher!!.age, newTeacher!!.age)
        assertEquals(teacher!!.teacherName, newTeacher.teacherName)
        assertEquals(teacher!!.teachYears, newTeacher.teachYears)
        assertEquals(teacher!!.isSex, newTeacher.isSex)
        val s = Student()
        s.name = null
        s.age = 0
        val affectedStudent = s.update(student!!.id.toLong())
        assertEquals(0, affectedStudent)
        val newStudent = getStudent(student!!.id.toLong())
        assertEquals(student!!.name, newStudent!!.name)
        assertEquals(student!!.age, newStudent.age)
    }

    @Test
    fun testUpdateToDefaultValueWithInstanceUpdate() {
        val s = Student()
        s.setToDefault("age")
        s.setToDefault("name")
        s.setToDefault("birthday")
        val affectedStudent = s.update(student!!.id.toLong())
        assertEquals(1, affectedStudent)
        val newStudent = LitePal.find<Student>(student!!.id.toLong())
        assertNull(newStudent!!.birthday)
        assertNull(newStudent.name)
        assertEquals(0, newStudent.age)
        val t = Teacher()
        t.age = 45
        t.teachYears = 5
        t.teacherName = "John"
        t.setToDefault("teacherName")
        t.setToDefault("age")
        val affectedTeacher = t.update(teacher!!.id.toLong())
        assertEquals(1, affectedTeacher)
        val newTeacher = getTeacher(teacher!!.id.toLong())
        assertEquals(22, newTeacher!!.age)
        assertEquals("", newTeacher.teacherName)
        assertEquals(5, newTeacher.teachYears)
    }

    @Test
    fun testUpdateToDefaultValueWithInstanceUpdateButWrongField() {
        try {
            val t = Teacher()
            t.setToDefault("name")
            t.update(t.id.toLong())
            fail()
        } catch (e: DataSupportException) {
            assertEquals(
                    "The name field in com.litepaltest.model.Teacher class is necessary which does not exist.",
                    e.message)
        }

    }

    @Test
    fun testUpdateWithInstanceUpdateWithConstructor() {
        try {
            val computer = Computer("ACER", 5444.0)
            computer.save()
            computer.update(computer.id)
            fail()
        } catch (e: DataSupportException) {
            assertEquals("com.litepaltest.model.Computer needs a default constructor.",
                    e.message)
        }

    }

    @Test
    fun testUpdateWithInstanceUpdateButNotExistsRecord() {
        val t = Teacher()
        t.teacherName = "Johnny"
        val rowsAffected = t.update(189876465)
        assertEquals(0, rowsAffected)
    }

    @Test
    fun testUpdateAllWithStaticUpdate() {
        var s: Student
        val ids = IntArray(5)
        for (i in 0..4) {
            s = Student()
            s.name = "Dusting"
            s.age = i + 10
            s.save()
            ids[i] = s.id
        }
        val values = ContentValues()
        values.put("age", 24)
        var affectedRows = LitePal.updateAll<Student>(values, "name = ? and age = ?",
                "Dusting", "13")
        assertEquals(1, affectedRows)
        val updatedStu = getStudent(ids[3].toLong())
        assertEquals(24, updatedStu!!.age)
        values.clear()
        values.put("name", "Dustee")
        affectedRows = LitePal.updateAll<Student>(values, "name = ?", "Dusting")
        assertEquals(5, affectedRows)
        val students = getStudents(ids)
        for (updatedStudent in students) {
            assertEquals("Dustee", updatedStudent.name)
        }
    }

    @Test
    fun testUpdateAllRowsWithStaticUpdate() {
        var allRows = getRowsCount(studentTable)
        val values = ContentValues()
        values.put("name", "Zuckerburg")
        var affectedRows = LitePal.updateAll<Student>(values)
        assertEquals(allRows, affectedRows)
        val table = DBUtility.getIntermediateTableName(studentTable, DBUtility.getTableNameByClassName(Teacher::class.java.name))
        allRows = getRowsCount(table)
        values.clear()
        values.putNull(studentTable!! + "_id")
        affectedRows = LitePal.updateAll(table, values)
        assertEquals(allRows, affectedRows)
    }

    @Test
    fun testUpdateAllWithStaticUpdateButWrongConditions() {
        val values = ContentValues()
        values.put("name", "Dustee")
        try {
            LitePal.updateAll<Student>(values, "name = 'Dustin'", "aaa")
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            LitePal.updateAll<Student>(values, null, null)
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            LitePal.updateAll<Student>(values, "address = ?", "HK")
            fail()
        } catch (e: SQLiteException) {
        }

    }

    @Test
    fun testUpdateAllWithInstanceUpdate() {
        var s: Student
        val ids = IntArray(5)
        for (i in 0..4) {
            s = Student()
            s.name = "Jessica"
            s.age = i + 10
            s.save()
            ids[i] = s.id
        }
        val date = Date()
        val toUpdate = Student()
        toUpdate.age = 24
        toUpdate.birthday = date
        var affectedRows = toUpdate.updateAll("name = ? and age = ?", "Jessica", "13")
        assertEquals(1, affectedRows)
        val updatedStu = LitePal.find(Student::class.java, ids[3].toLong())
        assertEquals(24, updatedStu!!.age)
        assertEquals(date.time, updatedStu.birthday.time)
        toUpdate.age = 18
        toUpdate.name = "Jess"
        affectedRows = toUpdate.updateAll("name = ?", "Jessica")
        assertEquals(5, affectedRows)
        val students = getStudents(ids)
        for (updatedStudent in students) {
            assertEquals("Jess", updatedStudent.name)
            assertEquals(18, updatedStudent.age)
        }
    }

    @Test
    fun testUpdateAllRowsWithInstanceUpdate() {
        val c = Connector.getDatabase().query(studentTable, null, null, null, null, null, null)
        val allRows = c.count
        c.close()
        val student = Student()
        student.name = "Zuckerburg"
        val affectedRows = student.updateAll()
        assertEquals(allRows, affectedRows)
    }

    @Test
    fun testUpdateAllWithDefaultValueWithInstanceUpdate() {
        var tea: Teacher?
        val ids = IntArray(5)
        for (i in 0..4) {
            tea = Teacher()
            tea.teacherName = "Rose Jackson"
            tea.age = 50
            tea.teachYears = 15
            tea.isSex = false
            tea.save()
            ids[i] = tea.id
        }
        val t = Teacher()
        t.teacherName = ""
        t.teachYears = 0
        t.isSex = true
        t.age = 22
        val affectedTeacher = t.updateAll("teachername = 'Rose Jackson'")
        assertEquals(0, affectedTeacher)
        val teachers = getTeachers(ids)
        for (updatedTeacher in teachers) {
            assertEquals("Rose Jackson", updatedTeacher.teacherName)
            assertEquals(50, updatedTeacher.age)
            assertEquals(15, updatedTeacher.teachYears)
            assertEquals(false, updatedTeacher.isSex)
        }
    }

    @Test
    fun testUpdateAllToDefaultValueWithInstanceUpdate() {
        var stu: Student
        val ids = IntArray(5)
        for (i in 0..4) {
            stu = Student()
            stu.name = "Michael Jackson"
            stu.age = 18
            stu.save()
            ids[i] = stu.id
        }
        val s = Student()
        s.setToDefault("age")
        s.setToDefault("name")
        val affectedStudent = s.updateAll("name = 'Michael Jackson'")
        assertEquals(5, affectedStudent)
        val students = getStudents(ids)
        for (updatedStudent in students) {
            assertEquals(null, updatedStudent.name)
            assertEquals(0, updatedStudent.age)
        }
    }

    @Test
    fun testUpdateAllToDefaultValueWithInstanceUpdateButWrongField() {
        try {
            val t = Teacher()
            t.setToDefault("name")
            t.updateAll("")
            fail()
        } catch (e: DataSupportException) {
            assertEquals(
                    "The name field in com.litepaltest.model.Teacher class is necessary which does not exist.",
                    e.message)
        }

    }

    @Test
    fun testUpdateAllWithInstanceUpdateButWrongConditions() {
        val student = Student()
        student.name = "Dustee"
        try {
            student.updateAll("name = 'Dustin'", "aaa")
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            student.updateAll(null, null)
            fail()
        } catch (e: DataSupportException) {
            assertEquals("The parameters in conditions are incorrect.", e.message)
        }

        try {
            student.updateAll("address = ?", "HK")
            fail()
        } catch (e: Exception) {
        }

    }

    @Test
    fun testUpdateGenericData() {
        val c = Classroom()
        c.name = "Math room"
        c.news.add("news")
        c.news.add("paper")
        c.update(classroom!!._id.toLong())
        var result = LitePal.find<Classroom>(classroom!!._id.toLong())
        assertEquals("Math room", result!!.name)
        val builder = StringBuilder()
        for (s in result.news) {
            builder.append(s)
        }
        assertEquals("newspaper", builder.toString())
        assertEquals(2, result.numbers.size)
        val c2 = Classroom()
        c2.setToDefault("numbers")
        c2.update(classroom!!._id.toLong())
        result = LitePal.find<Classroom>(classroom!!._id.toLong())
        assertEquals("Math room", result!!.name)
        assertEquals(2, result.news.size)
        assertEquals(0, result.numbers.size)
    }


}