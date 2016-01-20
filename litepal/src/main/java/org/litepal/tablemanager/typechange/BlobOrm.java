package org.litepal.tablemanager.typechange;

/**
 * This class deals with byte type.
 *
 * @author Tony Green
 * @since 1.3.1
 */
public class BlobOrm extends OrmChange{

    /**
     * If the field type passed in is byte, it will change it into blob as
     * column type.
     */
    @Override
    public String object2Relation(String fieldType) {
        if (fieldType != null) {
            if (fieldType.equals("byte") || fieldType.equals("java.lang.Byte")) {
                return "blob";
            }
        }
        return null;
    }

}
