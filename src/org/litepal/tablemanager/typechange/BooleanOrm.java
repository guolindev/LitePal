package org.litepal.tablemanager.typechange;

/**
 * This class deals with boolean type.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class BooleanOrm extends OrmChange {

	/**
	 * If the field type passed in is boolean, it will change it into integer as
	 * column type. Column name will be same as field name. Then return an array
	 * of the combination.
	 */
	@Override
	public String[] object2Relation(String className, String fieldName, String fieldType) {
		if (fieldName != null && fieldType != null) {
			String columnName = fieldName;
			if (fieldType.equals("boolean") || fieldType.equals("java.lang.Boolean")) {
				String[] relations = { columnName, "INTEGER" };
				return relations;
			}
		}
		return null;
	}

}
