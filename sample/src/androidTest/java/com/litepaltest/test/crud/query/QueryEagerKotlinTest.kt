package com.litepaltest.test.crud.query

import androidx.test.filters.SmallTest
import com.litepaltest.model.Classroom
import com.litepaltest.model.IdCard
import com.litepaltest.model.Student
import com.litepaltest.model.Teacher
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.extension.findAll
import org.litepal.extension.findLast
import java.util.*

@SmallTest
class QueryEagerKotlinTest {

    private var classroom: Classroom? = null

    private var student1: Student? = null

    private var student2: Student? = null

    private var student3: Student? = null

    private var teacher1: Teacher? = null

    private var teacher2: Teacher? = null

    private var idcard1: IdCard? = null

    private var idcard2: IdCard? = null

    @Before
    fun setUp() {
        val calendar = Calendar.getInstance()
        classroom = Classroom()
        classroom!!.name = "Classroom 11"
        idcard1 = IdCard()
        idcard1!!.number = "320311"
        idcard2 = IdCard()
        idcard2!!.number = "320322"
        calendar.clear()
        calendar.set(1990, 9, 16, 0, 0, 0)
        student1 = Student()
        student1!!.name = "Student 1"
        student1!!.classroom = classroom
        student1!!.idcard = idcard1
        student1!!.birthday = calendar.time
        calendar.clear()
        calendar.set(1989, 7, 7, 0, 0, 0)
        student2 = Student()
        student2!!.name = "Student 2"
        student2!!.classroom = classroom
        student2!!.birthday = calendar.time
        student3 = Student()
        student3!!.name = "Student 3"
        teacher1 = Teacher()
        teacher1!!.teacherName = "Teacher 1"
        teacher1!!.teachYears = 3
        teacher1!!.idCard = idcard2
        teacher2 = Teacher()
        teacher2!!.isSex = false
        teacher2!!.teacherName = "Teacher 2"
        student1!!.teachers.add(teacher1)
        student1!!.teachers.add(teacher2)
        student2!!.teachers.add(teacher2)
        classroom!!.teachers.add(teacher1)
        classroom!!.save()
        student1!!.save()
        student2!!.save()
        student3!!.save()
        idcard1!!.save()
        idcard2!!.save()
        teacher1!!.save()
        teacher2!!.save()
    }

    @Test
    fun testEagerFind() {
        var s1 = LitePal.find<Student>(student1!!.id.toLong(), true)
        var c: Classroom? = s1!!.classroom
        val ic = s1.idcard
        val tList = s1.teachers
        assertNotNull(c)
        assertNotNull(ic)
        assertEquals(classroom!!._id, c!!._id)
        assertEquals("Classroom 11", c.name)
        assertEquals(idcard1!!.id, ic.id)
        assertEquals("320311", ic.number)
        assertEquals(student1!!.teachers.size, tList.size)
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(1990, 9, 16, 0, 0, 0)
        assertEquals(calendar.time.time, s1.birthday.time)
        for (t in tList) {
            if (t.id == teacher1!!.id) {
                assertEquals("Teacher 1", t.teacherName)
                assertEquals(teacher1!!.teachYears, t.teachYears)
                assertTrue(t.isSex)
                continue
            }
            if (t.id == teacher2!!.id) {
                assertEquals("Teacher 2", t.teacherName)
                assertFalse(t.isSex)
                continue
            }
            fail()
        }
        s1 = LitePal.find<Student>(student1!!.id.toLong())
        c = s1!!.classroom
        assertNull(c)
        assertNull(s1.idcard)
        assertEquals(0, s1.teachers.size)
        c = LitePal.find<Classroom>(classroom!!._id.toLong(), true)
        assertEquals(2, c!!.studentCollection.size)
        assertEquals(1, c.teachers.size)
        for (s in c.studentCollection) {
            if (s.id == student1!!.id) {
                assertEquals("Student 1", s.name)
                continue
            }
            if (s.id == student2!!.id) {
                assertEquals("Student 2", s.name)
                calendar.clear()
                calendar.set(1989, 7, 7, 0, 0, 0)
                assertEquals(calendar.time.time, s.birthday.time)
                continue
            }
            fail()
        }
        val t1 = LitePal.find<Teacher>(teacher2!!.id.toLong(), true)
        val sList = t1!!.students
        assertEquals(teacher2!!.students.size, sList.size)
        for (s in sList) {
            if (s.id == student1!!.id) {
                assertEquals("Student 1", s.name)
                calendar.clear()
                calendar.set(1990, 9, 16, 0, 0, 0)
                assertEquals(calendar.time.time, s.birthday.time)
                continue
            }
            if (s.id == student2!!.id) {
                assertEquals("Student 2", s.name)
                continue
            }
            fail()
        }
        val s3 = LitePal.find<Student>(student3!!.id.toLong())
        assertNull(s3!!.birthday)
    }

    private fun resetData() {
        LitePal.deleteAll<Student>()
        LitePal.deleteAll<Classroom>()
        LitePal.deleteAll<Teacher>()
        LitePal.deleteAll<IdCard>()
        setUp()
    }

    @Test
    fun testEagerFindFirst() {
        resetData()
        var s1 = LitePal.findFirst(Student::class.java)
        assertNull(s1!!.classroom)
        s1 = LitePal.findFirst(Student::class.java, true)
        assertNotNull(s1)
    }

    @Test
    fun testEagerFindLast() {
        resetData()
        var t1 = LitePal.findLast<Teacher>()
        assertEquals(0, t1!!.students.size)
        t1 = LitePal.findLast<Teacher>(true)
        assertTrue(0 < t1!!.students.size)
    }

    @Test
    fun testEagerFindAll() {
        resetData()
        var sList = LitePal.findAll<Student>()
        for (s in sList) {
            assertNull(s.classroom)
            assertEquals(0, s.teachers.size)
        }
        sList = LitePal.findAll(true)
        for (s in sList) {
            if (s.classroom == null) {
                continue
            }
            assertEquals("Classroom 11", s.classroom.name)
            assertTrue(s.teachers.size > 0)
            val tList = s.teachers
            for (t in tList) {
                if (t.id == teacher1!!.id) {
                    assertEquals("Teacher 1", t.teacherName)
                    assertEquals(teacher1!!.teachYears, t.teachYears)
                    assertTrue(t.isSex)
                    continue
                }
                if (t.id == teacher2!!.id) {
                    assertEquals("Teacher 2", t.teacherName)
                    assertFalse(t.isSex)
                    continue
                }
                fail()
            }
        }
    }

    @Test
    fun testEagerClusterQuery() {
        resetData()
        var sList = LitePal.where("id = ?", student1!!.id.toString()).find<Student>()
        assertEquals(1, sList.size)
        var s = sList[0]
        assertNull(s.classroom)
        sList = LitePal.where("id = ?", student1!!.id.toString()).find(true)
        assertEquals(1, sList.size)
        s = sList[0]
        assertNotNull(s.classroom)
        val c = s.classroom
        assertEquals("Classroom 11", c.name)
    }

}