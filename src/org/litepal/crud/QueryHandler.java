package org.litepal.crud;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
			DataSupport modelInstance = (DataSupport) constructor
					.newInstance(getConstructorParams(constructor));
			List<Field> supportedFields = getSupportedFields(modelClass.getName());
			String tableName = getTableName(modelClass);
			Cursor cursor = mDatabase.query(tableName, null, "id = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				for (Field field : supportedFields) {
					String methodName = genGetColumnMethod(field);
					LogUtil.d(TAG, "method name is " + methodName);
					int columnIndex = cursor
							.getColumnIndex(BaseUtility.changeCase(field.getName()));
					LogUtil.d(TAG, "field name is " + field.getName() + " column index is "
							+ columnIndex);
					if (columnIndex != -1) {
						Class<?> cursorClass = cursor.getClass();
						Method method = cursorClass.getMethod(methodName, int.class);
						Object value = method.invoke(cursor, columnIndex);
						LogUtil.d(TAG, "value is " + value);
						LogUtil.d(TAG, "type is " + field.getType());
						if (field.getType() == boolean.class || field.getType() == Boolean.class) {
							if ("0".equals(String.valueOf(value))) {
								value = false;
							} else if ("1".equals(String.valueOf(value))) {
								value = true;
							}
						} else if (field.getType() == char.class
								|| field.getType() == Character.class) {
							value = ((String) value).charAt(0);
						}
						putSetMethodValueByField(modelInstance, field, value);
					}
				}
			}
			cursor.close();
			return (T) modelInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String genGetColumnMethod(Field field) {
		String typeName;
		Class<?> fieldType = field.getType();
		if (fieldType.isPrimitive()) {
			typeName = BaseUtility.capitalize(fieldType.getName());
		} else {
			typeName = fieldType.getSimpleName();
		}
		String methodName = "get" + typeName;
		if ("getBoolean".equals(methodName)) {
			methodName = "getInt";
		} else if ("getChar".equals(methodName)) {
			methodName = "getString";
		}
		return methodName;
	}

}
