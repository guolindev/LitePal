package org.litepal.crud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.litepal.exceptions.DataSupportException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is a component under DataSupport. It deals with the updating stuff as
 * primary task. Either updating specifying data with id or updating multiple
 * lines with conditions can be done here.
 * 
 * @author Tony Green
 * @since 1.1
 */
class UpdateHandler extends DataHandler {

	/**
	 * Initialize {@link DataHandler#mDatabase} for operating database. Do not
	 * allow to create instance of UpdateHandler out of CRUD package.
	 * 
	 * @param db
	 *            The instance of SQLiteDatabase.
	 */
	UpdateHandler(SQLiteDatabase db) {
		mDatabase = db;
	}

	/**
	 * The open interface for other classes in CRUD package to update. Using
	 * baseObj to decide which table to update, and id to decide a specific row.
	 * The value that need to update is stored in baseObj.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param id
	 *            Which record to update.
	 * @return The number of rows affected.
	 */
	int onUpdate(DataSupport baseObj, long id) {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values);
		if (values.size() > 0) {
			return mDatabase.update(baseObj.getTableName(), values, "id = " + id, null);
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to update. Using
	 * modelClass to decide which table to update, and id to decide a specific
	 * row. The value that need to update is stored in ContentValues.
	 * 
	 * @param modelClass
	 *            Which table to update by class.
	 * @param id
	 *            Which record to update.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * 
	 * @return The number of rows affected.
	 */
	int onUpdate(Class<?> modelClass, long id, ContentValues values) {
		if (values.size() > 0) {
			return mDatabase.update(getTableName(modelClass), values, "id = " + id, null);
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to update multiple
	 * rows. Using baseObj to decide which table to update, and conditions
	 * representing the WHERE part of an SQL statement. The value that need to
	 * update is stored in baseObj.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The number of rows affected.
	 */
	int onUpdateAll(DataSupport baseObj, String[] conditions) {
		checkConditionsCorrect(conditions);
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		ContentValues values = new ContentValues();
		putFieldsValue(baseObj, supportedFields, values);
		putFieldsToDefaultValue(baseObj, values);
		if (values.size() > 0) {
			return mDatabase.update(baseObj.getTableName(), values, getWhereClause(conditions),
					getWhereArgs(conditions));
		}
		return 0;
	}

	/**
	 * The open interface for other classes in CRUD package to update multiple
	 * lines. Using modelClass to decide which table to update, and conditions
	 * representing the WHERE part of an SQL statement. The value that need to
	 * update is stored in ContentValues.
	 * 
	 * @param modelClass
	 *            Which table to update by class.
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @param values
	 *            A map from column names to new column values. null is a valid
	 *            value that will be translated to NULL.
	 * @return The number of rows affected.
	 */
	int onUpdateAll(Class<?> modelClass, String[] conditions, ContentValues values) {
		checkConditionsCorrect(conditions);
		if (values.size() > 0) {
			return mDatabase.update(getTableName(modelClass), values, getWhereClause(conditions),
					getWhereArgs(conditions));
		}
		return 0;
	}

	/**
	 * Iterate all the fields that need to set to default value. If the field is
	 * id, ignore it. Or put the default value of field into ContentValues.
	 * 
	 * @param baseObj
	 *            Which table to update by model instance.
	 * @param values
	 *            To store data of current model for persisting or updating.
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private void putFieldsToDefaultValue(DataSupport baseObj, ContentValues values) {
		String fieldName = null;
		try {
			DataSupport emptyModel = getEmptyModel(baseObj);
			Class<?> emptyModelClass = emptyModel.getClass();
			for (String name : baseObj.getFieldsToSetToDefault()) {
				if (!isIdColumn(name)) {
					fieldName = name;
					Field field = emptyModelClass.getDeclaredField(fieldName);
					putContentValues(emptyModel, field, values);
				}
			}
		} catch (NoSuchFieldException e) {
			throw new DataSupportException(DataSupportException.noSuchFieldExceptioin(
					baseObj.getClassName(), fieldName));
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
	}

}
