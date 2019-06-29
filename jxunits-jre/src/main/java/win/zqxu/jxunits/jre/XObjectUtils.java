package win.zqxu.jxunits.jre;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Utility functions for common Object
 * 
 * @author zqxu
 */
public class XObjectUtils {
  /**
   * Determine whether the object is empty. an object considered as empty if
   * <ul>
   * <li>null value</li>
   * <li>empty string</li>
   * <li>zero length array</li>
   * <li>empty collection (list, set...)</li>
   * <li>empty map</li>
   * </ul>
   * 
   * @param object
   *          the object to check
   * 
   * @return true if the object is empty
   */
  public static boolean isEmpty(Object object) {
    return object == null
        || (object instanceof String && ((String) object).isEmpty())
        || (object.getClass().isArray() && Array.getLength(object) == 0)
        || (object instanceof Collection && ((Collection<?>) object).isEmpty())
        || (object instanceof Map && ((Map<?, ?>) object).isEmpty());
  }

  /**
   * Get attribute value of the object, the attribute should be field or getter method.
   * returns null for null object or null attribute
   * 
   * @param object
   *          the entity
   * @param attribute
   *          the java member for the attribute
   * @return attribute value of the object
   * @throws UnsupportedOperationException
   *           if get attribute value failed
   */
  public static Object getAttributeValue(Object object, Member attribute) {
    if (object == null || attribute == null) return null;
    try {
      if (attribute instanceof Field) {
        ((Field) attribute).setAccessible(true);
        return ((Field) attribute).get(object);
      } else {
        ((Method) attribute).setAccessible(true);
        return ((Method) attribute).invoke(object);
      }
    } catch (Exception ex) {
      throw new UnsupportedOperationException("get attribute value", ex);
    }
  }

  /**
   * Set attribute value of the object, the attribute should be field or setter method,
   * and the value must be type-compatible. no operation if the object is null.
   * 
   * @param object
   *          the object to set attribute for
   * @param attribute
   *          the java member for the attribute
   * @param value
   *          attribute value to set
   * @throws UnsupportedOperationException
   *           if set attribute value failed
   */
  public static void setAttributeValue(Object object, Member attribute, Object value) {
    if (object == null) return;
    try {
      if (attribute instanceof Field) {
        ((Field) attribute).setAccessible(true);
        ((Field) attribute).set(object, value);
      } else {
        ((Method) attribute).setAccessible(true);
        ((Method) attribute).invoke(object, value);
      }
    } catch (Exception ex) {
      throw new UnsupportedOperationException("set attribute value", ex);
    }
  }

  /**
   * Get declared method in object's class and its super classes
   * 
   * @param object
   *          the object
   * @param name
   *          the method name
   * @param parameterTypes
   *          the parameter types of the method
   * @return the declared method or null if not found
   */
  public static Method getMethod(Object object, String name, Class<?>... parameterTypes) {
    return getMethod(object.getClass(), name, parameterTypes);
  }

  /**
   * Get declared method in class and its super classes
   * 
   * @param clazz
   *          the class
   * @param name
   *          the method name
   * @param parameterTypes
   *          the parameter types of the method
   * @return the declared method or null if not found
   */
  public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
    while (clazz != null) {
      try {
        Method method = clazz.getDeclaredMethod(name, parameterTypes);
        if (method != null) return method;
      } catch (Exception ex) {
        // safely ignored any exception
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

  /**
   * Get default value for specified type. default primitive value or null if the type if
   * not a primitive type.
   * 
   * @param <T>
   *          generic type
   * @param type
   *          the value type
   * @return default value for the type
   */
  @SuppressWarnings("unchecked")
  public static <T> T getDefaultValue(Class<T> type) {
    if (type == null || !type.isPrimitive()) return null;
    return (T) Array.get(Array.newInstance(type, 1), 0);
  }
}
