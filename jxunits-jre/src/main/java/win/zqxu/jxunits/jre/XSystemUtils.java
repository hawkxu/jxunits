package win.zqxu.jxunits.jre;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Some utilities methods for Java environment and system
 * 
 * @author zqxu
 */
public class XSystemUtils {

  /**
   * Get root path of the class, if the class was in jar, then the path was the jar file
   * path
   * 
   * @param clazz
   *          the class
   * @return root path of the class
   */
  public static File getClassRoot(Class<?> clazz) {
    String path = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
    try {
      return new File(URLDecoder.decode(path, "UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("error get class root path", ex);
    }
  }

  /**
   * Get root folder of the class, if the class was in jar, then the path was the folder
   * contains the jar file
   * 
   * @param clazz
   *          the class
   * @return root folder of the class
   */
  public static File getClassFolder(Class<?> clazz) {
    File root = getClassRoot(clazz);
    return root.isDirectory() ? root : root.getParentFile();
  }

  /**
   * Get user local application data path
   * 
   * @return user local application data path
   */
  public static File getUserLocalPath() {
    if (File.separatorChar == '\\')
      return new File(System.getenv("LOCALAPPDATA"));
    else
      return new File(System.getProperty("user.dir") + "./local");
  }

  /**
   * list all class name in the root (a folder or jar file)
   * 
   * @param root
   *          the root to search in
   * @return all class name (except nested class)
   */
  public static String[] listClass(File root) {
    return listClass(root, ".*");
  }

  /**
   * list all class name in the root (a folder or jar file)
   * 
   * @param root
   *          the root to search in
   * @param regex
   *          regular expression to filter class name
   * @return all class name (except nested class)
   */
  public static String[] listClass(File root, String regex) {
    if (root.isFile())
      return listClassInJar(root, regex);
    else
      return listClassInFolder(root, regex);
  }

  private static String[] listClassInJar(File jar, String regex) {
    try (JarFile _jar = new JarFile(jar)) {
      List<String> classList = new ArrayList<>();
      Enumeration<JarEntry> entries = _jar.entries();
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.matches("[^$]*\\.class")) {
          name = pathToClassName(name);
          if (name.matches(regex)) classList.add(name);
        }
      }
      return classList.toArray(new String[0]);
    } catch (IOException ex) {
      throw new RuntimeException("list class in jar", ex);
    }
  }

  private static String[] listClassInFolder(File folder, String regex) {
    List<String> classList = new ArrayList<>();
    int start = folder.getAbsolutePath().length();
    for (File file : listClassFiles(folder)) {
      String name = pathToClassName(file.getAbsolutePath().substring(start));
      if (name.matches(regex)) classList.add(name);
    }
    return classList.toArray(new String[0]);
  }

  private static List<File> listClassFiles(File folder) {
    List<File> files = new ArrayList<>();
    for (File file : folder.listFiles(f -> f.isDirectory()
        || f.getName().matches("[^$]*\\.class"))) {
      if (file.isFile())
        files.add(file);
      else
        files.addAll(listClassFiles(file));
    }
    return files;
  }

  private static String pathToClassName(String path) {
    return path.replaceAll("^/|^\\\\|\\.class", "").replaceAll("/|\\\\", ".");
  }
}
