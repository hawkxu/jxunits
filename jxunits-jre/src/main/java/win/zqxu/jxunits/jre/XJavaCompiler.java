package win.zqxu.jxunits.jre;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * In memory java compiler
 * 
 * @author zqxu
 */
public class XJavaCompiler {
  private XClassLoader classLoader;

  /**
   * Default constructor, use class loader which load this class as parent class loader
   * for compile class
   */
  public XJavaCompiler() {
    this(null);
  }

  /**
   * Constructor with parent class loader
   * 
   * @param parentClassLoader
   *          the parent class loader, the class loader which load this class will be used
   *          if the parentClassLoader is null
   */
  public XJavaCompiler(ClassLoader parentClassLoader) {
    if (parentClassLoader == null)
      parentClassLoader = getClass().getClassLoader();
    classLoader = new XClassLoader(parentClassLoader);
  }

  /**
   * Get class loader this compiler used for compile class
   * 
   * @return class loader
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * get already compiled class, returns null if no such class compiled
   * 
   * @param name
   *          the full qualified class name
   * @return class instance
   */
  public Class<?> getCompiledClass(String name) {
    return classLoader.findClass(name);
  }

  /**
   * Compile class from source code
   *
   * @param className
   *          full qualified class name
   * @param sourceCode
   *          the class source code
   * @return compiled class
   */
  public Class<?> compile(String className, String sourceCode) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    XFileManager fm = new XFileManager(compiler);
    URI uri = URI.create(className.replaceFirst(".*\\.", "") + ".java");
    SourceJavaFile source = new SourceJavaFile(uri, Kind.SOURCE, sourceCode);
    List<JavaFileObject> units = Arrays.asList(source);
    CompilationTask task = compiler.getTask(null, fm, null, null, null, units);
    if (task.call()) {
      byte[] binaryCode = fm.binaryJavaFile.buffer.toByteArray();
      return classLoader.defineClass(className, binaryCode);
    }
    throw new RuntimeException("compilation task execution failed");
  }

  private static class XClassLoader extends ClassLoader {
    public XClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    protected Class<?> findClass(String name) {
      return findLoadedClass(name);
    }

    protected Class<?> defineClass(String name, byte[] binaryCode) {
      return defineClass(name, binaryCode, 0, binaryCode.length);
    }
  }

  private static class XFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private BinaryJavaFile binaryJavaFile;

    protected XFileManager(JavaCompiler compiler) {
      super(compiler.getStandardFileManager(null, null, null));
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
        FileObject sibling) throws IOException {
      return binaryJavaFile = new BinaryJavaFile(className, kind);
    }
  }

  private static class SourceJavaFile extends SimpleJavaFileObject {
    private String content;

    protected SourceJavaFile(URI uri, Kind kind, String content) {
      super(uri, kind);
      this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return content;
    }
  }

  private static class BinaryJavaFile extends SimpleJavaFileObject {
    private ByteArrayOutputStream buffer;

    public BinaryJavaFile(String className, Kind kind) {
      super(URI.create(className + kind.extension), kind);
    }

    @Override
    public OutputStream openOutputStream() {
      return buffer = new ByteArrayOutputStream();
    }
  }
}
