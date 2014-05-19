package org.litepal.tablemanager.typechange;

/**
 * This class deals with numeric type.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class NumericOrm extends OrmChange {

	/**
	 * If the field type passed in is int, long or short, it will change it into
	 * integer as column type. Column name will be same as field name. Then return
	 * an array of the combination.
	 */
	@Override
	public String[] object2Relation(String className, String fieldName, String fieldType) {
		if (fieldName != null && fieldType != null) {
			String[] relations = new String[2];
			relations[0] = fieldName;
			if (fieldType.equals("int") || fieldType.equals("java.lang.Integer")) {
				relations[1] = "INTEGER";
				return relations;
			}
			if (fieldType.equals("long") || fieldType.equals("java.lang.Long")) {
				relations[1] = "INTEGER";
				return relations;
			}
			if (fieldType.equals("short") || fieldType.equals("java.lang.Short")) {
				relations[1] = "INTEGER";
				return relations;
			}
		}
		return null;
	}

}
