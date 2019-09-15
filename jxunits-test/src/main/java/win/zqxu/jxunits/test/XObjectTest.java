package win.zqxu.jxunits.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class XObjectTest extends XBaseTest {
  /**
   * output object attributes to the "standard" output stream
   * 
   * @param object
   *          the object to output
   */
  public static void outputObject(Object object) {
    outputObject(object, System.out);
  }

  /**
   * output object attributes to the output stream
   * 
   * @param object
   *          the object to output
   * @param stream
   *          the stream to output
   */
  public static void outputObject(Object object, OutputStream stream) {
    try {
      outputObject(object.getClass(), object, 0, true, stream);
      stream.write(System.lineSeparator().getBytes());
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private static void outputObject(Type type, Object object, int indent,
      boolean typeIndent, OutputStream stream) throws IOException {
    if (typeIndent) outputIndent(indent, stream);
    if (type != null) XClassTest.outputType(type, 0, stream);
    if (isImmutable(object)) {
      outputImmutable(object, stream);
      stream.write(System.lineSeparator().getBytes());
    } else if (object instanceof Map) {
      stream.write(System.lineSeparator().getBytes());
      outputMap((Map<?, ?>) object, indent + 4, stream);
    } else if (object instanceof Iterable) {
      stream.write(System.lineSeparator().getBytes());
      outputIterable((Iterable<?>) object, indent + 4, stream);
    } else {
      stream.write(System.lineSeparator().getBytes());
      if (type instanceof ParameterizedType)
        type = ((ParameterizedType) type).getRawType();
      Class<?> clazz = (Class<?>) type;
      outputFields(clazz, object, indent + 4, stream);
      type = clazz.getGenericSuperclass();
      if (type instanceof Class && type != Object.class)
        outputObject(type, object, indent + 4, typeIndent, stream);
    }
  }

  private static boolean isImmutable(Object object) {
    return object == null || object.getClass().isPrimitive()
        || object instanceof Boolean || object instanceof Character
        || object instanceof Number || object instanceof String
        || object instanceof Date || object instanceof Temporal;
  }

  private static void outputImmutable(Object object, OutputStream stream)
      throws IOException {
    stream.write(", ".getBytes());
    if (object == null)
      stream.write("null".getBytes());
    else
      stream.write(object.toString().getBytes());
  }

  private static void outputMap(Map<?, ?> map, int indent, OutputStream stream)
      throws IOException {
    int index = 0;
    for (Entry<?, ?> entry : map.entrySet()) {
      outputIndex(index++, indent, stream);
      stream.write(System.lineSeparator().getBytes());
      Object key = entry.getKey();
      Class<?> keyType = key == null ? null : key.getClass();
      outputIndent(indent + 4, stream);
      stream.write("key: ".getBytes());
      outputObject(keyType, key, indent + 4, false, stream);
      Object val = entry.getValue();
      Class<?> valType = val == null ? null : val.getClass();
      outputIndent(indent + 4, stream);
      stream.write("val: ".getBytes());
      outputObject(valType, val, indent + 4, false, stream);
    }
  }

  private static void outputIterable(Iterable<?> iterable, int indent,
      OutputStream stream) throws IOException {
    Iterator<?> iterator = iterable.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      outputIndex(index++, indent, stream);
      Object value = iterator.next();
      Class<?> type = value == null ? null : value.getClass();
      outputObject(type, value, indent, false, stream);
    }
  }

  private static void outputIndex(int index, int indent,
      OutputStream stream) throws IOException {
    outputIndent(indent, stream);
    stream.write(String.valueOf(index).getBytes());
    stream.write(": ".getBytes());
  }

  private static void outputFields(Class<?> type, Object object, int indent,
      OutputStream stream) throws IOException {
    try {
      for (Field field : type.getDeclaredFields()) {
        outputIndent(indent, stream);
        stream.write(field.getName().getBytes());
        stream.write(": ".getBytes());
        field.setAccessible(true);
        Object value = field.get(object);
        Type clazz = value == null ? null : value.getClass();
        outputObject(clazz, value, indent, false, stream);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
