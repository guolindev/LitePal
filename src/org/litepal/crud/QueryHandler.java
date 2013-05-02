package org.litepal.crud;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		try {
			Constructor<?> constructor = findBestSuitConstructor(modelClass);
			T con = (T)constructor.newInstance(getConstructorParams(constructor));
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
						Method method = cursorClass.getMethod(methodName, int.class);
						Object object = method.invoke(cursor, columnIndex);
						LogUtil.d(TAG, "object is " + object);
					}
				}
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
