/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.litepal.annotation.Column;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.tablemanager.typechange.BooleanOrm;
import org.litepal.tablemanager.typechange.DateOrm;
import org.litepal.tablemanager.typechange.DecimalOrm;
import org.litepal.tablemanager.typechange.NumericOrm;
import org.litepal.tablemanager.typechange.OrmChange;
import org.litepal.tablemanager.typechange.TextOrm;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;

/**
 * Base class of all the LitePal components. If each component need to
 * interactive with other components or they have some same logic with duplicate
 * codes, LitePalBase may be the solution.
 * 
 * @author Tony Green
 * @since 1.1
 */
public abstract class LitePalBase {

	public static final String TAG = "LitePalBase";

	/**
	 * Action to get associations.
	 */
	private static final int GET_ASSOCIATIONS_ACTION = 1;

	/**
	 * Action to get association info.
	 */
	private static final int GET_ASSOCIATION_INFO_ACTION = 2;

	/**
	 * All the supporting mapping types currently in the array.
	 */
	private OrmChange[] typeChangeRules = { new NumericOrm(), new TextOrm(), new BooleanOrm(),
			new DecimalOrm(), new DateOrm() };

	/**
	 * The collection contains all association models.
	 */
	private Collection<AssociationsModel> mAssociationModels;

	/**
	 * The collection contains all association info.
	 */
	private Collection<AssociationsInfo> mAssociationInfos;

	/**
	 * This method is used to get the table model by the class name passed
	 * in. The principle to generate table model is that each field in the class
	 * with non-static modifier and has a type among int/Integer, long/Long,
	 * short/Short, float/Float, double/Double, char/Character, boolean/Boolean
	 * or String, would generate a column with same name as corresponding field.
	 * If users don't want some of the fields map a column, declare an ignore
     * annotation with {@link Column#ignore()}.
	 * 
	 * @param className
	 *            The full name of the class to map in database.
	 * @return A table model with table name, class name and the map of column
	 *         name and column type.
	 */
	protected TableModel getTableModel(String className) {
		String tableName = DBUtility.getTableNameByClassName(className);
		TableModel tableModel = new TableModel();
		tableModel.setTableName(tableName);
		tableModel.setClassName(className);
		List<Field> supportedFields = getSupportedFields(className);
		for (Field field : supportedFields) {
            ColumnModel columnModel = convertFieldToColumnModel(field);
            tableModel.addColumnModel(columnModel);
		}
		return tableModel;
	}

	/**
	 * This method is used to get association models depends on the given class
	 * name list.
	 * 
	 * @param classNames
	 *            The names of the classes that want to get their associations.
	 * @return Collection of association models.
	 */
	protected Collection<AssociationsModel> getAssociations(List<String> classNames) {
		if (mAssociationModels == null) {
			mAssociationModels = new HashSet<AssociationsModel>();
		}
		mAssociationModels.clear();
		for (String className : classNames) {
			analyzeClassFields(className, GET_ASSOCIATIONS_ACTION);
		}
		return mAssociationModels;
	}

	/**
	 * Get the association info model by the class name.
	 * 
	 * @param className
	 *            The class name to introspection.
	 * @return Collection of association info.
	 */
	protected Collection<AssociationsInfo> getAssociationInfo(String className) {
		if (mAssociationInfos == null) {
			mAssociationInfos = new HashSet<AssociationsInfo>();
		}
		mAssociationInfos.clear();
		analyzeClassFields(className, GET_ASSOCIATION_INFO_ACTION);
		return mAssociationInfos;
	}

	/**
	 * Find all the fields in the class. But not each field is supported to add
	 * a column to the table. Only the basic data types and String are
	 * supported. This method will intercept all the types which are not
	 * supported and return a new list of supported fields.
	 * 
	 * @param className
	 *            The full name of the class.
	 * @return A list of supported fields
	 */
	protected List<Field> getSupportedFields(String className) {
		List<Field> supportedFields = new ArrayList<Field>();
		Class<?> dynamicClass;
		try {
			dynamicClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
		}
		Field[] fields = dynamicClass.getDeclaredFields();
		for (Field field : fields) {
            Column annotation = field.getAnnotation(Column.class);
            if (annotation != null && annotation.ignore()) {
                continue;
            }
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				Class<?> fieldTypeClass = field.getType();
				String fieldType = fieldTypeClass.getName();
				if (BaseUtility.isFieldTypeSupported(fieldType)) {
					supportedFields.add(field);
				}
			}
		}
		return supportedFields;
	}

	/**
	 * If the field type implements from List or Set, regard it as a collection.
	 * 
	 * @param fieldType
	 *            The field type.
	 * @return True if the field type is collection, false otherwise.
	 */
	protected boolean isCollection(Class<?> fieldType) {
		return isList(fieldType) || isSet(fieldType);
	}

	/**
	 * If the field type implements from List, regard it as a list.
	 * 
	 * @param fieldType
	 *            The field type.
	 * @return True if the field type is List, false otherwise.
	 */
	protected boolean isList(Class<?> fieldType) {
		return List.class.isAssignableFrom(fieldType);
	}

	/**
	 * If the field type implements from Set, regard it as a set.
	 * 
	 * @param fieldType
	 *            The field type.
	 * @return True if the field type is Set, false otherwise.
	 */
	protected boolean isSet(Class<?> fieldType) {
		return Set.class.isAssignableFrom(fieldType);
	}

	/**
	 * Judge the passed in column is an id column or not. The column named id or
	 * _id will be considered as id column.
	 * 
	 * @param columnName
	 *            The name of column.
	 * @return Return true if it's id column, otherwise return false.
	 */
	protected boolean isIdColumn(String columnName) {
		return "_id".equalsIgnoreCase(columnName) || "id".equalsIgnoreCase(columnName);
	}

	/**
	 * If two tables are associated, one table have a foreign key column. The
	 * foreign key column name will be the associated table name with _id
	 * appended.
	 * 
	 * @param associatedTableName
	 *            The associated table name.
	 * @return The foreign key column name.
	 */
	protected String getForeignKeyColumnName(String associatedTableName) {
		return BaseUtility.changeCase(associatedTableName + "_id");
	}

	/**
	 * Introspection of the passed in class. Analyze the fields of current class
	 * and find out the associations of it.
	 * 
	 * @param className
	 *            The class name to introspection.
	 * @param action
	 *            Between {@link org.litepal.LitePalBase#GET_ASSOCIATIONS_ACTION} and
	 *            {@link org.litepal.LitePalBase#GET_ASSOCIATION_INFO_ACTION}
	 */
	private void analyzeClassFields(String className, int action) {
		try {
            Class<?> dynamicClass = Class.forName(className);
			Field[] fields = dynamicClass.getDeclaredFields();
			for (Field field : fields) {
				if (isPrivateAndNonPrimitive(field)) {
					oneToAnyConditions(className, field, action);
					manyToAnyConditions(className, field, action);
				}
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
		}
	}

	/**
	 * Judge the field is a private non primitive field or not.
	 * 
	 * @param field
	 *            The field to judge.
	 * @return True if the field is <b>private</b> and <b>non primitive</b>,
	 *         false otherwise.
	 */
	private boolean isPrivateAndNonPrimitive(Field field) {
		return Modifier.isPrivate(field.getModifiers()) && !field.getType().isPrimitive();
	}

	/**
	 * Deals with one to any association conditions. e.g. Song and Album. An
	 * album have many songs, and a song belongs to one album. So if there's an
	 * Album model defined in Song with private modifier, and in Album there's a
	 * List or Set with generic type of Song and declared as private modifier,
	 * they are one2many association. If there's no List or Set defined in
	 * Album, they will become one2one associations. If there's also a Song
	 * model defined in Album with private modifier, maybe the album just have
	 * one song, they are one2one association too.
	 * 
	 * When it's many2one association, it's easy to just simply add a foreign id
	 * column to the many side model's table. But when it comes to many2many
	 * association, it can not be done without intermediate join table in
	 * database. LitePal assumes that this join table's name is the
	 * concatenation of the two target table names in alphabetical order.
	 * 
	 * @param className
	 *            Source class name.
	 * @param field
	 *            A field of source class.
	 * @param action
	 *            Between {@link org.litepal.LitePalBase#GET_ASSOCIATIONS_ACTION} and
	 *            {@link org.litepal.LitePalBase#GET_ASSOCIATION_INFO_ACTION}
	 * 
	 * @throws ClassNotFoundException
	 */
	private void oneToAnyConditions(String className, Field field, int action) throws ClassNotFoundException {
		Class<?> fieldTypeClass = field.getType();
		// If the mapping list contains the class name
		// defined in one class.
		if (LitePalAttr.getInstance().getClassNames().contains(fieldTypeClass.getName())) {
			Class<?> reverseDynamicClass = Class.forName(fieldTypeClass.getName());
			Field[] reverseFields = reverseDynamicClass.getDeclaredFields();
			// Look up if there's a reverse association
			// definition in the reverse class.
			boolean reverseAssociations = false;
			// Begin to check the fields of the defined
			// class.
			for (int i = 0; i < reverseFields.length; i++) {
				Field reverseField = reverseFields[i];
				if (Modifier.isPrivate(reverseField.getModifiers())) {
					Class<?> reverseFieldTypeClass = reverseField.getType();
					// If there's the from class name in the
					// defined class, they are one2one bidirectional
					// associations.
					if (className.equals(reverseFieldTypeClass.getName())) {
						if (action == GET_ASSOCIATIONS_ACTION) {
							addIntoAssociationModelCollection(className, fieldTypeClass.getName(),
									fieldTypeClass.getName(), Const.Model.ONE_TO_ONE);
						} else if (action == GET_ASSOCIATION_INFO_ACTION) {
							addIntoAssociationInfoCollection(className, fieldTypeClass.getName(),
									fieldTypeClass.getName(), field, reverseField, Const.Model.ONE_TO_ONE);
						}
						reverseAssociations = true;
					}
					// If there's the from class Set or List in
					// the defined class, they are many2one bidirectional
					// associations.
					else if (isCollection(reverseFieldTypeClass)) {
						String genericTypeName = getGenericTypeName(reverseField);
						if (className.equals(genericTypeName)) {
							if (action == GET_ASSOCIATIONS_ACTION) {
								addIntoAssociationModelCollection(className, fieldTypeClass.getName(),
										className, Const.Model.MANY_TO_ONE);
							} else if (action == GET_ASSOCIATION_INFO_ACTION) {
								addIntoAssociationInfoCollection(className, fieldTypeClass.getName(),
										className, field, reverseField, Const.Model.MANY_TO_ONE);
							}
							reverseAssociations = true;
						}
					}
					// If there's no from class in the defined class, they are
					// one2one unidirectional associations.
					if ((i == reverseFields.length - 1) && !reverseAssociations) {
						if (action == GET_ASSOCIATIONS_ACTION) {
							addIntoAssociationModelCollection(className, fieldTypeClass.getName(),
									fieldTypeClass.getName(), Const.Model.ONE_TO_ONE);
						} else if (action == GET_ASSOCIATION_INFO_ACTION) {
							addIntoAssociationInfoCollection(className, fieldTypeClass.getName(),
									fieldTypeClass.getName(), field, null, Const.Model.ONE_TO_ONE);
						}
					}
				}
			}
		}
	}

	/**
	 * Deals with one to any association conditions. e.g. Song and Album. An
	 * album have many songs, and a song belongs to one album. So if there's an
	 * Album model defined in Song with private modifier, and in Album there's a
	 * List or Set with generic type of Song and declared as private modifier,
	 * they are one2many association. If there's no List or Set defined in
	 * Album, they will become one2one associations. If there's also a Song
	 * model defined in Album with private modifier, maybe the album just have
	 * one song, they are one2one association too.
	 * 
	 * When it's many2one association, it's easy to just simply add a foreign id
	 * column to the many side model's table. But when it comes to many2many
	 * association, it can not be done without intermediate join table in
	 * database. LitePal assumes that this join table's name is the
	 * concatenation of the two target table names in alphabetical order.
	 * 
	 * @param className
	 *            Source class name.
	 * @param field
	 *            A field of source class.
	 * @param action
	 *            Between {@link org.litepal.LitePalBase#GET_ASSOCIATIONS_ACTION} and
	 *            {@link org.litepal.LitePalBase#GET_ASSOCIATION_INFO_ACTION}
	 * 
	 * @throws ClassNotFoundException
	 */
	private void manyToAnyConditions(String className, Field field, int action) throws ClassNotFoundException {
		if (isCollection(field.getType())) {
			String genericTypeName = getGenericTypeName(field);
			// If the mapping list contains the genericTypeName, begin to check
			// this genericTypeName class.
			if (LitePalAttr.getInstance().getClassNames().contains(genericTypeName)) {
				Class<?> reverseDynamicClass = Class.forName(genericTypeName);
				Field[] reverseFields = reverseDynamicClass.getDeclaredFields();
				// Look up if there's a reverse association
				// definition in the reverse class.
				boolean reverseAssociations = false;
				for (int i = 0; i < reverseFields.length; i++) {
					Field reverseField = reverseFields[i];
					// Only map private fields
					if (Modifier.isPrivate(reverseField.getModifiers())) {
						Class<?> reverseFieldTypeClass = reverseField.getType();
						// If there's a from class name defined in the reverse
						// class, they are many2one bidirectional
						// associations.
						if (className.equals(reverseFieldTypeClass.getName())) {
							if (action == GET_ASSOCIATIONS_ACTION) {
								addIntoAssociationModelCollection(className, genericTypeName,
										genericTypeName, Const.Model.MANY_TO_ONE);
							} else if (action == GET_ASSOCIATION_INFO_ACTION) {
								addIntoAssociationInfoCollection(className, genericTypeName, genericTypeName,
										field, reverseField, Const.Model.MANY_TO_ONE);
							}
							reverseAssociations = true;
						}
						// If there's a List or Set contains from class name
						// defined in the reverse class, they are many2many
						// association.
						else if (isCollection(reverseFieldTypeClass)) {
							String reverseGenericTypeName = getGenericTypeName(reverseField);
							if (className.equals(reverseGenericTypeName)) {
								if (action == GET_ASSOCIATIONS_ACTION) {
									addIntoAssociationModelCollection(className, genericTypeName, null,
											Const.Model.MANY_TO_MANY);
								} else if (action == GET_ASSOCIATION_INFO_ACTION) {
									addIntoAssociationInfoCollection(className, genericTypeName, null, field,
											reverseField, Const.Model.MANY_TO_MANY);
								}
								reverseAssociations = true;
							}
						}
						// If there's no from class in the defined class, they
						// are many2one unidirectional associations.
						if ((i == reverseFields.length - 1) && !reverseAssociations) {
							if (action == GET_ASSOCIATIONS_ACTION) {
								addIntoAssociationModelCollection(className, genericTypeName,
										genericTypeName, Const.Model.MANY_TO_ONE);
							} else if (action == GET_ASSOCIATION_INFO_ACTION) {
								addIntoAssociationInfoCollection(className, genericTypeName, genericTypeName,
										field, null, Const.Model.MANY_TO_ONE);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Package a {@link org.litepal.tablemanager.model.AssociationsModel}, and add it into
	 * {@link #mAssociationModels} Collection.
	 * 
	 * @param className
	 *            The class name for {@link org.litepal.tablemanager.model.AssociationsModel}.
	 * @param associatedClassName
	 *            The associated class name for {@link org.litepal.tablemanager.model.AssociationsModel}.
	 * @param classHoldsForeignKey
	 *            The class which holds foreign key.
	 * @param associationType
	 *            The association type for {@link org.litepal.tablemanager.model.AssociationsModel}.
	 */
	private void addIntoAssociationModelCollection(String className, String associatedClassName,
			String classHoldsForeignKey, int associationType) {
		AssociationsModel associationModel = new AssociationsModel();
		associationModel.setTableName(DBUtility.getTableNameByClassName(className));
		associationModel.setAssociatedTableName(DBUtility.getTableNameByClassName(associatedClassName));
		associationModel.setTableHoldsForeignKey(DBUtility.getTableNameByClassName(classHoldsForeignKey));
		associationModel.setAssociationType(associationType);
		mAssociationModels.add(associationModel);
	}

	/**
	 * Package a {@link org.litepal.crud.model.AssociationsInfo}, and add it into
	 * {@link #mAssociationInfos} Collection.
	 * 
	 * @param selfClassName
	 *            The class name of self model.
	 * @param associatedClassName
	 *            The class name of the class which associated with self class.
	 * @param classHoldsForeignKey
	 *            The class which holds foreign key.
	 * @param associateOtherModelFromSelf
	 *            The field of self class to declare has association with other
	 *            class.
	 * @param associateSelfFromOtherModel
	 *            The field of the associated class to declare has association
	 *            with self class.
	 * @param associationType
	 *            The association type.
	 */
	private void addIntoAssociationInfoCollection(String selfClassName, String associatedClassName,
			String classHoldsForeignKey, Field associateOtherModelFromSelf,
			Field associateSelfFromOtherModel, int associationType) {
		AssociationsInfo associationInfo = new AssociationsInfo();
		associationInfo.setSelfClassName(selfClassName);
		associationInfo.setAssociatedClassName(associatedClassName);
		associationInfo.setClassHoldsForeignKey(classHoldsForeignKey);
		associationInfo.setAssociateOtherModelFromSelf(associateOtherModelFromSelf);
		associationInfo.setAssociateSelfFromOtherModel(associateSelfFromOtherModel);
		associationInfo.setAssociationType(associationType);
		mAssociationInfos.add(associationInfo);
	}

	/**
	 * Get the generic type name of List or Set. If there's no generic type of
	 * List or Set return null.
	 * 
	 * @param field
	 *            A generic type field.
	 * @return The name of generic type of List of Set.
	 */
	private String getGenericTypeName(Field field) {
		Type genericType = field.getGenericType();
		if (genericType != null) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) genericType;
				Class<?> genericArg = (Class<?>) parameterizedType.getActualTypeArguments()[0];
				return genericArg.getName();
			}
		}
		return null;
	}

    /**
     * Convert a field instance into A ColumnModel instance. ColumnModel can provide information
     * when creating table.
     * @param field
     *          A supported field to map into column.
     * @return ColumnModel instance contains column information.
     */
    private ColumnModel convertFieldToColumnModel(Field field) {
        String columnType = null;
        String fieldType = field.getType().getName();
        for (OrmChange ormChange : typeChangeRules) {
            columnType = ormChange.object2Relation(fieldType);
            if (columnType != null) {
                break;
            }
        }
        boolean nullable = true;
        boolean unique = false;
        String defaultValue = "";
        Column annotation = field.getAnnotation(Column.class);
        if (annotation != null) {
            nullable = annotation.nullable();
            unique = annotation.unique();
            defaultValue = annotation.defaultValue();
        }
        ColumnModel columnModel = new ColumnModel();
        columnModel.setColumnName(field.getName());
        columnModel.setColumnType(columnType);
        columnModel.setIsNullable(nullable);
        columnModel.setIsUnique(unique);
        columnModel.setDefaultValue(defaultValue);
        return columnModel;
    }

}
