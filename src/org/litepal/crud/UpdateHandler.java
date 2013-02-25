package org.litepal.crud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.litepal.exceptions.DataSupportException;
import org.litepal.util.BaseUtility;

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
	 * @throws DataSupportException
	 */
	int onUpdate(DataSupport baseObj, long id) {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		try {
			ContentValues values = new ContentValues();
			putFieldsValue(baseObj, supportedFields, values);
			putFieldsToDefaultValue(baseObj, values);
			if (values.size() > 0) {
				return mDatabase.update(baseObj.getTableName(), values, "id = " + id, null);
			}
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
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
	 * @throws DataSupportException
	 */
	int onUpdate(Class<?> modelClass, long id, ContentValues values) {
		try {
			if (values.size() > 0) {
				return mDatabase.update(getTableName(modelClass), values, "id = " + id, null);
			}
			return 0;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
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
	 * 
	 * @throws DataSupportException
	 */
	int onUpdateAll(DataSupport baseObj, String[] conditions) {
		List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
		try {
			checkConditionsCorrect(conditions);
			ContentValues values = new ContentValues();
			putFieldsValue(baseObj, supportedFields, values);
			putFieldsToDefaultValue(baseObj, values);
			if (values.size() > 0) {
				return mDatabase.update(baseObj.getTableName(), values, getWhereClause(conditions),
						getWhereArgs(conditions));
			}
			return 0;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
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
	 * 
	 * @throws DataSupportException
	 */
	int onUpdateAll(Class<?> modelClass, String[] conditions, ContentValues values) {
		try {
			checkConditionsCorrect(conditions);
			if (values.size() > 0) {
				return mDatabase.update(getTableName(modelClass), values,
						getWhereClause(conditions), getWhereArgs(conditions));
			}
			return 0;
		} catch (Exception e) {
			throw new DataSupportException(e.getMessage());
		}
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
	private void putFieldsToDefaultValue(DataSupport baseObj, ContentValues values)
			throws IllegalAccessException, SecurityException, IllegalArgumentException,
			NoSuchMethodException, InvocationTargetException {
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
		}
	}

	/**
	 * Check the number of question mark existed in conditions[0] equals the
	 * number of rest conditions elements or not. If not equals, throws
	 * DataSupportException.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @throws DataSupportException
	 */
	private void checkConditionsCorrect(String[] conditions) {
		if (conditions != null) {
			int conditionsSize = conditions.length;
			if (conditionsSize > 0) {
				String whereClause = conditions[0];
				int placeHolderSize = BaseUtility.count(whereClause, "?");
				if (conditionsSize != placeHolderSize + 1) {
					throw new DataSupportException(DataSupportException.UPDATE_CONDITIONS_EXCEPTION);
				}
			}
		}
	}

	/**
	 * Check the passing conditions represent to update all lines or not. <br>
	 * Here are the supported format means update all lines.
	 * 
	 * <pre>
	 * null
	 * new String[] {}
	 * new String[] { null }
	 * new String[] { "" }
	 * </pre>
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return Update all lines or not.
	 */
	private boolean isUpdateAllLines(String[] conditions) {
		if (conditions == null) {
			return true;
		}
		if (conditions.length == 0) {
			return true;
		}
		String whereClause = conditions[0];
		if (whereClause == null || "".equals(whereClause.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * Get the WHERE clause to apply when updating.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return The WHERE clause to apply when updating.
	 */
	private String getWhereClause(String[] conditions) {
		if (isUpdateAllLines(conditions)) {
			return null;
		}
		if (conditions != null && conditions.length > 0) {
			return conditions[0];
		}
		return null;
	}

	/**
	 * Get the WHERE arguments to fill into where clause when updating.
	 * 
	 * @param conditions
	 *            A string array representing the WHERE part of an SQL
	 *            statement.
	 * @return Where clause arguments.
	 */
	private String[] getWhereArgs(String[] conditions) {
		if (isUpdateAllLines(conditions)) {
			return null;
		}
		if (conditions != null && conditions.length > 1) {
			String[] whereArgs = new String[conditions.length - 1];
			System.arraycopy(conditions, 1, whereArgs, 0, conditions.length - 1);
			return whereArgs;
		}
		return null;
	}

}
