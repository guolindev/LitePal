package org.litepal.crud;

import java.lang.reflect.Field;
import java.util.List;

import org.litepal.util.BaseUtility;
import org.litepal.util.LogUtil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class QueryHandler extends DataHandler {

	QueryHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	<T> T onFind(Class<T> modelClass, long id) {
		List<Field> supportedFields = getSupportedFields(modelClass.getName());
		String tableName = getTableName(modelClass);
		Cursor cursor = mDatabase.query(tableName, null, "id = " + id, null, null, null, null);
		if (cursor.moveToFirst()) {
			for (Field field : supportedFields) {
				String methodName = genGetColumnMethod(field);
				LogUtil.d(TAG, "method name is " + methodName);
				int columnIndex = cursor.getColumnIndex(BaseUtility.changeCase(field.getName()));
				LogUtil.d(TAG, "field name is " + field.getName() + " column index is "
						+ columnIndex);
				if (columnIndex != -1) {
					Class<?> cursorClass = cursor.getClass();
					try {
						cursorClass.getMethod(methodName, int.class);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
		cursor.close();
		return null;
	}

	private String genGetColumnMethod(Field field) {
		String fieldType;
		if (field.getType().isPrimitive()) {
			fieldType = BaseUtility.capitalize(field.getType().getName());
		} else {
			fieldType = field.getType().getSimpleName();
		}
		String methodName = "get" + fieldType;
		return methodName;
	}

}
