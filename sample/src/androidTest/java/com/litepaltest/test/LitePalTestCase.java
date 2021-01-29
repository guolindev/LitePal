package com.litepaltest.test;

import java.util.ArrayList;
import java.util.List;

import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.filters.SmallTest;

import com.litepaltest.model.Book;
import com.litepaltest.model.Cellphone;
import com.litepaltest.model.Classroom;
import com.litepaltest.model.Computer;
import com.litepaltest.model.IdCard;
import com.litepaltest.model.Student;
import com.litepaltest.model.Teacher;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@SmallTest
public class LitePalTestCase {

	protected void assertM2M(String table1, String table2, long id1, long id2) {
		assertTrue(isIntermediateDataCorrect(table1, table2, id1, id2));
	}

	protected void assertM2MFalse(String table1, String table2, long id1, long id2) {
		assertFalse(isIntermediateDataCorrect(table1, table2, id1, id2));
	}

	/**
	 * 
	 * @param table1
	 *            Table without foreign key.
	 * @param table2
	 *            Table with foreign key.
	 * @param table1Id
	 *            id of table1.
	 * @param table2Id
	 *            id of table2.
	 * @return success or failed.
	 */
	protected boolean isFKInsertCorrect(String table1, String table2, long table1Id, long table2Id) {
		SQLiteDatabase db = Connector.getDatabase();
		try (Cursor cursor = db.query(table2, null, "id = ?", new String[]{String.valueOf(table2Id)},
				null, null, null)) {
			cursor.moveToFirst();
			long fkId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseUtility.changeCase(table1
					+ "_id")));
			return fkId == table1Id;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean isIntermediateDataCorrect(String table1, String table2, long table1Id,
			long table2Id) {
		SQLiteDatabase db = Connector.getDatabase();
		Cursor cursor = null;
		try {
			String where = table1 + "_id = ? and " + table2 + "_id = ?";
			cursor = db.query(DBUtility.getIntermediateTableName(table1, table2), null, where,
					new String[] { String.valueOf(table1Id), String.valueOf(table2Id) }, null,
					null, null);
			return cursor.getCount() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	protected long getForeignKeyValue(String tableWithFK, String tableWithoutFK, long id) {
		Cursor cursor = Connector.getDatabase().query(tableWithFK, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
		long foreignKeyId = 0;
		if (cursor.moveToFirst()) {
			foreignKeyId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseUtility
					.changeCase(tableWithoutFK + "_id")));
		}
		cursor.close();
		return foreignKeyId;
	}

	protected boolean isDataExists(String table, long id) {
		SQLiteDatabase db = Connector.getDatabase();
		try (Cursor cursor = db.query(table, null, "id = ?", new String[]{String.valueOf(id)}, null,
				null, null)) {
			return cursor.getCount() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected String getTableName(Object object) {
		return DBUtility.getTableNameByClassName(object.getClass().getName());
	}

    protected String getTableName(Class<?> c) {
        return DBUtility.getTableNameByClassName(c.getName());
    }

	protected int getRowsCount(String tableName) {
		int count = 0;
		Cursor c = Connector.getDatabase().query(tableName, null, null, null, null, null, null);
		count = c.getCount();
		c.close();
		return count;
	}

	protected List<Book> getBooks(String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {
		List<Book> books = new ArrayList<Book>();
		Cursor cursor = Connector.getDatabase().query(getTableName(Book.class), columns, selection, selectionArgs,
				groupBy, having, orderBy, limit);
		if (cursor.moveToFirst()) {
			do {
				long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
				String bookName = cursor.getString(cursor.getColumnIndexOrThrow("bookname"));
				Integer pages = null;
				if (!cursor.isNull(cursor.getColumnIndexOrThrow("pages"))) {
					pages = cursor.getInt(cursor.getColumnIndexOrThrow("pages"));
				}
				double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
				char level = cursor.getString(cursor.getColumnIndexOrThrow("level")).charAt(0);
				short isbn = cursor.getShort(cursor.getColumnIndexOrThrow("isbn"));
				float area = cursor.getFloat(cursor.getColumnIndexOrThrow("area"));
				boolean isPublished = cursor.getInt(cursor.getColumnIndexOrThrow("ispublished")) == 1;
				Book book = new Book();
				book.setId(id);
				book.setBookName(bookName);
				book.setPages(pages);
				book.setPrice(price);
				book.setLevel(level);
				book.setIsbn(isbn);
				book.setArea(area);
				book.setPublished(isPublished);
				books.add(book);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return books;
	}

	protected Classroom getClassroom(long id) {
		Classroom c = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(Classroom.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			c = new Classroom();
			String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			c.setName(name);
		}
		cursor.close();
		return c;
	}

	protected IdCard getIdCard(long id) {
		IdCard card = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(IdCard.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			card = new IdCard();
			String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
			String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
			card.setAddress(address);
			card.setNumber(number);
		}
		cursor.close();
		return card;
	}

	protected Computer getComputer(long id) {
		Computer computer = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(Computer.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			computer = new Computer("", 0);
			double newPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
			String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
			computer.setBrand(brand);
			computer.setPrice(newPrice);
		}
		cursor.close();
		return computer;
	}

	protected Cellphone getCellPhone(long id) {
		Cellphone cellPhone = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(Cellphone.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			cellPhone = new Cellphone();
			double newPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
			char inStock = cursor.getString(cursor.getColumnIndexOrThrow("instock")).charAt(0);
			String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
			cellPhone.setBrand(brand);
			cellPhone.setInStock(inStock);
			cellPhone.setPrice(newPrice);
		}
		cursor.close();
		return cellPhone;
	}

	protected Teacher getTeacher(long id) {
		Teacher teacher = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(Teacher.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			teacher = new Teacher();
			String teacherName = cursor.getString(cursor.getColumnIndexOrThrow("teachername"));
			int teachYears = cursor.getInt(cursor.getColumnIndexOrThrow("teachyears"));
			int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
			int sex = cursor.getInt(cursor.getColumnIndexOrThrow("sex"));
			teacher.setTeacherName(teacherName);
			teacher.setTeachYears(teachYears);
			teacher.setAge(age);
			if (sex == 0) {
				teacher.setSex(false);
			} else if (sex == 1) {
				teacher.setSex(true);
			}
		}
		cursor.close();
		return teacher;
	}

	protected Student getStudent(long id) {
		Student student = null;
		Cursor cursor = Connector.getDatabase().query(getTableName(Student.class), null, "id = ?",
				new String[] { String.valueOf(id) }, null, null, null);
		if (cursor.moveToFirst()) {
			student = new Student();
			String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
			student.setName(name);
			student.setAge(age);
		}
		cursor.close();
		return student;
	}

	protected List<Teacher> getTeachers(int[] ids) {
		List<Teacher> teachers = new ArrayList<>();
		Cursor cursor = Connector.getDatabase().query(getTableName(Teacher.class), null, getWhere(ids), null, null,
				null, null);
		if (cursor.moveToFirst()) {
			Teacher t = new Teacher();
			String teacherName = cursor.getString(cursor.getColumnIndexOrThrow("teachername"));
			int teachYears = cursor.getInt(cursor.getColumnIndexOrThrow("teachyears"));
			int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
			int sex = cursor.getInt(cursor.getColumnIndexOrThrow("sex"));
			t.setTeacherName(teacherName);
			t.setTeachYears(teachYears);
			t.setAge(age);
			if (sex == 0) {
				t.setSex(false);
			} else if (sex == 1) {
				t.setSex(true);
			}
			teachers.add(t);
		}
		cursor.close();
		return teachers;
	}

	protected List<Student> getStudents(int[] ids) {
		List<Student> students = new ArrayList<>();
		Cursor cursor = Connector.getDatabase().query(getTableName(Student.class), null, getWhere(ids), null, null,
				null, null);
		if (cursor.moveToFirst()) {
			Student s = new Student();
			String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
			s.setName(name);
			s.setAge(age);
			students.add(s);
		}
		cursor.close();
		return students;
	}

	private String getWhere(int[] ids) {
		StringBuilder where = new StringBuilder();
		boolean needOr = false;
		for (int id : ids) {
			if (needOr) {
				where.append(" or ");
			}
			where.append("id = ").append(id);
			needOr = true;
		}
		return where.toString();
	}

}
