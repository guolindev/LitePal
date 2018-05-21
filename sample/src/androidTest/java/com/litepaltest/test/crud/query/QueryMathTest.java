package com.litepaltest.test.crud.query;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.litepal.util.DBUtility;

import com.litepaltest.model.Student;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class QueryMathTest extends AndroidTestCase {

    String studentTable;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studentTable = DBUtility.getTableNameByClassName(Student.class.getName());
    }

    public void testCount() {
		int result = LitePal.count(Student.class);
		int realResult = -100;
		Cursor cursor = LitePal.findBySQL("select count(1) from " + studentTable);
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		result = LitePal.where("id > ?", "99").count(studentTable);
		cursor = LitePal.findBySQL("select count(1) from " + studentTable + " where id > ?", "99");
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		try {
            LitePal.count("nosuchtable");
			fail();
		} catch (Exception e) {
		}
	}

	public void testAverage() {
		double result = LitePal.average(Student.class, "age");
		double realResult = -100;
		Cursor cursor = LitePal.findBySQL("select avg(age) from " + studentTable);
		if (cursor.moveToFirst()) {
			realResult = cursor.getDouble(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		result = LitePal.where("id > ?", "99").average(studentTable, "age");
		cursor = LitePal.findBySQL("select avg(age) from " + studentTable + " where id > ?", "99");
		if (cursor.moveToFirst()) {
			realResult = cursor.getDouble(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		try {
            LitePal.average(Student.class, "nosuchcolumn");
			fail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testMax() {
		int result = LitePal.max(Student.class, "age", Integer.TYPE);
		int realResult = -100;
		Cursor cursor = LitePal.findBySQL("select max(age) from " + studentTable);
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		result = LitePal.where("age < ?", "20").max(studentTable, "age", Integer.TYPE);
		cursor = LitePal.findBySQL("select max(age) from " + studentTable + " where age < ?", "20");
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
	}

	public void testMin() {
		int result = LitePal.min(Student.class, "age", Integer.TYPE);
		int realResult = -100;
		Cursor cursor = LitePal.findBySQL("select min(age) from " + studentTable);
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		result = LitePal.where("age > ?", "10").min(studentTable, "age", Integer.TYPE);
		cursor = LitePal.findBySQL("select min(age) from " + studentTable + " where age > ?", "10");
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
	}

	public void testSum() {
		int result = LitePal.sum(Student.class, "age", Integer.TYPE);
		int realResult = -100;
		Cursor cursor = LitePal.findBySQL("select sum(age) from " + studentTable);
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
		result = LitePal.where("age > ?", "15").sum(studentTable, "age", Integer.TYPE);
		cursor = LitePal.findBySQL("select sum(age) from " + studentTable + " where age > ?", "15");
		if (cursor.moveToFirst()) {
			realResult = cursor.getInt(0);
		}
		cursor.close();
		assertEquals(realResult, result);
	}

}