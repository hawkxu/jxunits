package win.zqxu.jxunits.jre;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * to create object
 * 
 * @author zqxu
 */
public class XDefaultObjectCreator implements XObjectCreator {
  private static Map<Class<?>, Class<?>> IMPLEMENTED_MAP;
  private static XDefaultObjectCreator instance;

  static {
    IMPLEMENTED_MAP = new HashMap<>();
    setDefaultImplementedClass(Collection.class, ArrayList.class);
    setDefaultImplementedClass(AbstractCollection.class, ArrayList.class);
    setDefaultImplementedClass(Set.class, HashSet.class);
    setDefaultImplementedClass(AbstractSet.class, HashSet.class);
    setDefaultImplementedClass(SortedSet.class, TreeSet.class);
    setDefaultImplementedClass(List.class, ArrayList.class);
    setDefaultImplementedClass(AbstractList.class, ArrayList.class);
    setDefaultImplementedClass(Map.class, HashMap.class);
    setDefaultImplementedClass(AbstractMap.class, HashMap.class);
  }

  /**
   * Get default implemented class for the base class used to create object. e.g.
   * ArrayList for List
   * 
   * @param baseClass
   *          the base class
   * @return default implemented class or null if no default implemented class
   */
  public static Class<?> getDefaultImplementedClass(Class<?> baseClass) {
    return IMPLEMENTED_MAP.get(baseClass);
  }

  /**
   * Set default implemented class for the base class used to create object. e.g.
   * ArrayList for List
   * 
   * @param baseClass
   *          the base class, must be interface or abstract class
   * @param implementedClass
   *          must be implemented class of the base class and not abstract
   */
  public static void setDefaultImplementedClass(Class<?> baseClass, Class<?> implementedClass) {
    if (!Modifier.isAbstract(baseClass.getModifiers()) || baseClass.isAnnotation())
      throw new IllegalArgumentException(baseClass + " isn't interface or abstract class");
    if (Modifier.isAbstract(implementedClass.getModifiers())
        || !baseClass.isAssignableFrom(implementedClass))
      throw new IllegalArgumentException(implementedClass + " is abstract or not extends "
          + baseClass);
    IMPLEMENTED_MAP.put(baseClass, implementedClass);
  }

  /**
   * get DefaultObjectCreator instance
   * 
   * @return DefaultObjectCreator instance
   */
  public static XObjectCreator getInstance() {
    if (instance == null)
      instance = new XDefaultObjectCreator();
    return instance;
  }

  protected XDefaultObjectCreator() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T create(Class<T> type) throws ReflectiveOperationException {
    T object = tryCreate(type);
    if (object != null) return object;
    throw new IllegalAccessException("Can not create instance for " + type);
  }

  /**
   * try create instance for the type through its constructors, if the type has no
   * constructor, return null
   * 
   * @param <T>
   *          the object type
   * @param type
   *          the type
   * @return a new instance of the type or null
   * @throws ReflectiveOperationException
   *           invoke constructor of the type error
   */
  protected <T> T tryCreate(Class<?> type) throws ReflectiveOperationException {
    if (IMPLEMENTED_MAP.containsKey(type))
      type = IMPLEMENTED_MAP.get(type);
    Constructor<T> c = getMinimalArgumentsConstructor(type);
    if (c == null) return null;
    c.setAccessible(true);
    Class<?>[] types = c.getParameterTypes();
    Object[] params = new Object[types.length];
    for (int i = 0; i < params.length; i++)
      params[i] = getDefaultValue(types[i]);
    return c.newInstance(params);
  }

  @SuppressWarnings("unchecked")
  private <T> Constructor<T> getMinimalArgumentsConstructor(Class<?> type) {
    Constructor<?> minimal = null;
    for (Constructor<?> c : type.getConstructors()) {
      Parameter[] params = c.getParameters();
      int paramCount = params.length;
      // ignore those constructors reference the type itself
      for (Parameter p : params)
        if (p.getType() == type) {
          paramCount = Integer.MAX_VALUE;
          break;
        }
      if (minimal == null || minimal.getParameterCount() > paramCount)
        minimal = c;
    }
    return (Constructor<T>) minimal;
  }

  private Object getDefaultValue(Class<?> type) throws ReflectiveOperationException {
    if (type == boolean.class || type == Boolean.class)
      return false;
    if (type.isPrimitive())
      return 0;
    if (type == Character.class || type == Byte.class || type == Short.class
        || type == Integer.class || type == Long.class || type == Float.class
        || type == Double.class)
      return 0;
    if (type == String.class)
      return "";
    if (type == Collections.class)
      return Collections.EMPTY_LIST;
    if (type == List.class)
      return Collections.EMPTY_LIST;
    if (type == Set.class)
      return Collections.EMPTY_SET;
    if (type == Map.class)
      return Collections.EMPTY_MAP;
    if (type.isArray())
      return Array.newInstance(type.getComponentType(), 0);
    return tryCreate(type);
  }
}