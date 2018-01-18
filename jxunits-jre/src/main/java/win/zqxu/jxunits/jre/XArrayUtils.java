package win.zqxu.jxunits.jre;

/**
 * Utility functions for Array objects
 * 
 * @author zqxu
 */
public class XArrayUtils {
  /**
   * Get array dimensions for the object, returns 0 if the object is not an array. e.g.
   * String[] is 1, String[][] is 2.
   * 
   * @param object
   *          the array object
   * @return array dimensions
   */
  public static int getDimensions(Object object) {
    return object == null ? 0 : getDimensions(object.getClass());
  }

  /**
   * Get array dimensions for the type, returns 0 if the type is not an array type. e.g.
   * String[] is 1, String[][] is 2.
   * 
   * @param type
   *          the type
   * @return array dimensions
   */
  public static int getDimensions(Class<?> type) {
    int dimensions = 0;
    if (type != null) {
      while (type.isArray()) {
        dimensions++;
        type = type.getComponentType();
      }
    }
    return dimensions;
  }

  /**
   * Get final component type (not array type) of the array, if the object is null, then
   * null returned, if the object is not an array, then the object class returned
   * 
   * @param object
   *          the object
   * @return final component type
   */
  public static Class<?> getFinalComponentType(Object object) {
    return object == null ? null : getFinalComponentType(object.getClass());
  }

  /**
   * Get final component type (not array type) of the type, if the type is null, then null
   * returned, if the type is not an array type, then the type itself returned, e.g. the
   * final component type is String of String[][]
   * 
   * @param type
   *          the type
   * @return final component type
   */
  public static Class<?> getFinalComponentType(Class<?> type) {
    if (type == null) return null;
    while (type.isArray())
      type = type.getComponentType();
    return type;
  }
}