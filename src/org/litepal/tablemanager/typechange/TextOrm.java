package org.litepal.tablemanager.typechange;

/**
 * This class deals with text type.
 * 
 * @author Tony Green
 * @since 1.0
 */
public class TextOrm extends OrmChange {

	/**
	 * If the field type passed in is char or String, it will change it into
	 * text as column type. Column name will be same as field name. Then return
	 * an array of the combination.
	 */
	@Override
	public String[] object2Relation(String className, String fieldName, String fieldType) {
		if (fieldName != null && fieldType != null) {
			String[] relations = new String[2];
			relations[0] = fieldName;
			if (fieldType.equals("char") || fieldType.equals("java.lang.Character")) {
				relations[1] = "TEXT";
				return relations;
			}
			if (fieldType.equals("java.lang.String")) {
				relations[1] = "TEXT";
				return relations;
			}
		}
		return null;
	}

}
