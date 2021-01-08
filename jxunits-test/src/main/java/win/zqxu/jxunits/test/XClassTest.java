package win.zqxu.jxunits.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class XClassTest extends XBaseTest {
  /**
   * output class hierarchy
   * 
   * @param clazz
   *          the class
   */
  public static void outputHierarchy(Class<?> clazz) {
    outputHierarchy(clazz, System.out);
  }

  /**
   * output class hierarchy
   * 
   * @param clazz
   *          the class
   * @param stream
   *          the output stream
   */
  public static void outputHierarchy(Class<?> clazz, OutputStream stream) {
    try {
      outputHierarchy((Type) clazz, 0, stream);
      stream.write(System.lineSeparator().getBytes());
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private static void outputHierarchy(Type type, int indent, OutputStream stream)
      throws IOException {
    if (type == null || type == Object.class) return;
    outputType(type, indent, stream);
    stream.write(System.lineSeparator().getBytes());
    if (type instanceof Class) {
      outputAnnotations((Class<?>) type, indent + 4, stream);
    }
    if (type instanceof ParameterizedType) {
      type = ((ParameterizedType) type).getRawType();
    }
    if (type instanceof Class) {
      outputSuperTypes(((Class<?>) type), indent + 4, stream);
    }
  }

  /**
   * output annotations of the class
   * 
   * @param clazz
   *          the class
   * @param indent
   *          numbers of indent
   * @param stream
   *          the stream to output
   * @throws IOException
   *           if error occurs
   */
  static void outputAnnotations(Class<?> clazz, int indent,
      OutputStream stream) throws IOException {
    Annotation[] annotations = clazz.getAnnotations();
    if (annotations.length == 0) return;
    outputIndent(indent, stream);
    stream.write("annotations: ".getBytes());
    for (int i = 0; i < annotations.length; i++) {
      if (i > 0) stream.write(", ".getBytes());
      Class<?> type = annotations[i].annotationType();
      stream.write(type.getName().toString().getBytes());
    }
    stream.write(System.lineSeparator().getBytes());
  }

  /**
   * output type information
   * 
   * @param type
   *          the type to output
   * @param indent
   *          numbers of indent
   * @param stream
   *          the stream to output
   * @throws IOException
   *           if error occurs
   */
  static void outputType(Type type, int indent, OutputStream stream) throws IOException {
    outputIndent(indent, stream);
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      stream.write(clazz.toString().getBytes());
      outputTypeParameters(clazz.getTypeParameters(), stream);
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) type;
      stream.write(parameterized.getRawType().toString().getBytes());
      stream.write(parameterized.toString().replaceFirst(".*?<", "<").getBytes());
    } else {
      stream.write(type.toString().getBytes());
    }
  }

  private static void outputTypeParameters(TypeVariable<?>[] typeParameters,
      OutputStream stream) throws IOException {
    if (typeParameters.length == 0) return;
    stream.write("<".getBytes());
    for (int i = 0; i < typeParameters.length; i++) {
      if (i > 0) stream.write(", ".getBytes());
      stream.write(typeParameters[i].getName().getBytes());
    }
    stream.write(">".getBytes());
  }

  private static void outputSuperTypes(Class<?> clazz, int indent, OutputStream stream)
      throws IOException {
    outputHierarchy(clazz.getGenericSuperclass(), indent, stream);
    for (Type interf : clazz.getGenericInterfaces())
      outputHierarchy(interf, indent, stream);
  }

  /**
   * List all libraries (include folders) of the class loader
   * 
   * @param classLoader
   *          the class loader
   * @return All libraries (include folders) of the class loader
   */
  public static List<File> listLibraries(ClassLoader classLoader) {
    List<File> libraries = new ArrayList<>();
    while (classLoader != null) {
      if (classLoader instanceof URLClassLoader) {
        int index = 0;
        for (URL url : ((URLClassLoader) classLoader).getURLs())
          libraries.add(index++, new File(url.getFile()));
      }
      classLoader = classLoader.getParent();
    }
    File folder = new File(System.getProperty("java.home"), "lib");
    int index = 0;
    for (File file : folder.listFiles()) {
      if (file.isFile() && file.getName().matches("(?i).*\\.jar"))
        libraries.add(index++, file);
    }
    return libraries;
  }

  /**
   * List all functional interface match conditions in the class loader libraries (exclude
   * directories)
   * 
   * @param classLoader
   *          the class loader
   * @param paramCount
   *          parameter count, null for any case
   * @param returnValue
   *          has or has not return value, null for any case
   * @return function interface list found, empty list if not found
   */
  public static List<String> listFunctionalInterfaces(
      ClassLoader classLoader, Integer paramCount, Boolean returnValue) {
    List<String> interfaces = new ArrayList<>();
    for (File file : listLibraries(classLoader)) {
      if (!file.exists() || !file.isFile()) continue;
      try (JarFile jar = new JarFile(file)) {
        for (JarEntry entry : Collections.list(jar.entries())) {
          String name = entry.getName();
          if (!name.endsWith(".class")) continue;
          int length = name.length() - 6;
          name = name.replace('/', '.').substring(0, length);
          try {
            Class<?> clazz = Class.forName(name);
            if (!clazz.isInterface()) continue;
            if (!clazz.isAnnotationPresent(FunctionalInterface.class))
              continue;
            for (Method method : clazz.getMethods()) {
              if (!Modifier.isAbstract(method.getModifiers()))
                continue;
              int count = method.getParameterCount();
              if (paramCount != null && paramCount != count)
                break;
              Class<?> type = method.getReturnType();
              boolean result = type != void.class;
              if (returnValue != null && returnValue != result)
                break;
              String className = clazz.toGenericString()
                  .replaceFirst("^.* interface ", "");
              String methodName = method.toGenericString()
                  .replaceFirst(".* abstract ", "");
              interfaces.add(className + ", " + methodName);
              break;
            }
          } catch (Throwable ex) {
            ex.printStackTrace(); // ignore any exceptions
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace(); // ignore any exceptions
      }
    }
    return interfaces;
  }
}
