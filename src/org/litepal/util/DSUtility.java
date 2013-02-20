package org.litepal.util;

/**
 * A utility class to help LitePal with some data support actions. These actions
 * can help classes just do the jobs they care, and help them out of the trivial
 * work.
 * 
 * @author Tony Green
 * @since 1.1
 */
public class DSUtility {

	/**
	 * Disable to create an instance of DSUtility.
	 */
	private DSUtility() {
	}

	/**
	 * Each primitive type has a corresponding object type. For example int and
	 * Integer, boolean and Boolean. This method gives a way to turn primitive
	 * type into object type.
	 * 
	 * @param primitiveType
	 *            The class of primitive type.
	 * @return If the passed in parameter is primitive type, return a
	 *         corresponding object type. Otherwise return null.
	 */
	public static Class<?> getObjectType(Class<?> primitiveType) {
		if (primitiveType != null) {
			if (primitiveType.isPrimitive()) {
				String basicTypeName = primitiveType.getName();
				if ("int".equals(basicTypeName)) {
					return Integer.class;
				} else if ("short".equals(basicTypeName)) {
					return Short.class;
				} else if ("long".equals(basicTypeName)) {
					return Long.class;
				} else if ("float".equals(basicTypeName)) {
					return Float.class;
				} else if ("double".equals(basicTypeName)) {
					return Double.class;
				} else if ("boolean".equals(basicTypeName)) {
					return Boolean.class;
				} else if ("char".equals(basicTypeName)) {
					return Character.class;
				}
			}
		}
		return null;
	}
}
