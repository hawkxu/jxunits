package win.zqxu.jxunits.web.jnlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import win.zqxu.jxunits.jre.XObjectUtils;
import win.zqxu.jxunits.jre.XSystemUtils;

/**
 * Base class for JNLP project store, the servlet will create store instance for each
 * request.
 * 
 * @author zqxu
 */
public abstract class XJNLPAbstractStore {
  private static final ObjectMapper JSONMapper = XJNLPDeployUtils.getJSONMapper();
  private static final Map<String, Object> syncHolder = new HashMap<>();
  private static final String[] iconKinds = new String[]{"default", "shortcut", "splash"};
  private final String deployTask;

  /**
   * Constructor with deploy task number, maybe null if not in deploying
   * 
   * @param deployTask
   *          the deploy task number returned by
   *          {@link #startDeployingTask(String, boolean)} or null if not in deploying
   */
  public XJNLPAbstractStore(String deployTask) {
    this.deployTask = deployTask;
  }

  /**
   * Get current deploy task number
   * 
   * @return deploy task number or null if not in deploying
   */
  public String getDeployTask() {
    return deployTask;
  }

  private boolean isProjectTaskOwner(String projectName) throws IOException {
    String task1 = readProjectTask(projectName);
    String task2 = getDeployTask();
    return !XObjectUtils.isEmpty(task1) && Objects.equals(task1, task2);
  }

  /**
   * the servlet will call this method once while servlet initialization
   */
  public void initialize() {
    syncHolder.clear();
    new Thread(() -> {
      try {
        XJNLPDeployUtils.clearDirectory(getDeployingRootFolder(), false);
      } catch (IOException ex) {
        Logger.getLogger(XJNLPAbstractStore.class.getName()).log(Level.WARNING, null, ex);
      }
    }).start();
  }

  /**
   * Determine whether the request access to the project is allowed
   * 
   * @param request
   *          the request instance
   * @param projectName
   *          the project name
   * @return true or false
   */
  public abstract boolean isAccessAllowed(HttpServletRequest request, String projectName);

  /**
   * Determine whether the request deploy to the project is allowed, also used for new
   * project checking.
   * 
   * @param request
   *          the request instance
   * @param projectName
   *          the project name, null for determine whether allowed to add new project
   * 
   * @return true or false
   */
  public abstract boolean isDeployAllowed(HttpServletRequest request, String projectName);

  /**
   * Read stored projects information
   * 
   * @return project list
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract List<XJNLPProject> readStoredProjects() throws IOException;

  /**
   * Read stored project information by name
   * 
   * @param projectName
   *          the project name
   * @return the project or null if no such project
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract XJNLPProject readStoredProject(String projectName) throws IOException;

  /**
   * Read stored JNLP file content of the project
   * 
   * @param projectName
   *          the project name
   * @return JNLP file content
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract String readStoredJnlpFile(String projectName) throws IOException;

  /**
   * Read stored icon file content of the project
   * 
   * @param projectName
   *          the project name
   * @param kind
   *          icon kind
   * @return icon file content or null if no such icon file
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract String readStoredIconFile(String projectName, String kind) throws IOException;

  /**
   * Read store libraries information of the project
   * 
   * @param projectName
   *          the project name
   * @return libraries of the project
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract List<XJNLPLibrary> readStoredLibraries(String projectName) throws IOException;

  /**
   * Read stored library information for the project and library name
   * 
   * @param projectName
   *          the project name
   * @param libraryName
   *          the library name
   * @return library or null if no such library
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract XJNLPLibrary readStoredLibrary(String projectName, String libraryName)
      throws IOException;

  /**
   * Read stored library file content for the project and library name
   * 
   * @param projectName
   *          the project name
   * @param libraryName
   *          the library name
   * @return library file content or null if no such library
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract byte[] readStoredLibraryJar(String projectName, String libraryName)
      throws IOException;

  /**
   * Begin deployment transaction
   * 
   * @param projectName
   *          the project name
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void beginTransaction(String projectName) throws IOException;

  /**
   * Commit deployment transaction
   * 
   * @param projectName
   *          the project name
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void commitTransaction(String projectName) throws IOException;

  /**
   * Rollback deployment transaction
   * 
   * @param projectName
   *          the project name
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void rollbackTransaction(String projectName) throws IOException;

  /**
   * Store project with or without JNLP file content change
   * 
   * @param project
   *          the project to store
   * @param jnlpContent
   *          the JNLP file content of the project or null if not changed
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void storeProject(XJNLPProject project, String jnlpContent)
      throws IOException;

  /**
   * Remove project, the store should remove all libraries belong this project
   * 
   * @param projectName
   *          the project name to remove
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void removeProject(String projectName) throws IOException;

  /**
   * Store icon for the project
   * 
   * @param projectName
   *          the project name
   * @param kind
   *          the icon kind
   * @param iconContent
   *          the icon content
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void storeIcon(String projectName, String kind, String iconContent)
      throws IOException;

  /**
   * Store library with or without JAR file content change
   * 
   * @param projectName
   *          the project name
   * @param library
   *          the library
   * @param jarContent
   *          new JAR file content or null if not changed
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void storeLibrary(String projectName, XJNLPLibrary library,
      byte[] jarContent) throws IOException;

  /**
   * Remove library from project
   * 
   * @param projectName
   *          the project name
   * @param libraryName
   *          the library name
   * @throws IOException
   *           if any IO errors occur
   */
  protected abstract void removeLibrary(String projectName, String libraryName)
      throws IOException;

  /**
   * Get root folder for store JNLP deployment files, must has privileges to create
   * sub-folder or there is already has a sub-folder named XJNLPDeploy and has full
   * privileges. default is XJNLPDeploy under /opt for Linux or env:ALLUSERSPROFILE for
   * Windows
   * 
   * @return root folder for store JNP deployment files
   */
  protected File getDeployRootFolder() {
    String root = System.getenv("ALLUSERSPROFILE");
    if (File.separatorChar == '/') root = "/opt";
    return new File(root, "XJNLPDeploy");
  }

  private File getDeployRuntimeFolder() {
    return new File(getDeployRootFolder(), "runtime");
  }

  /*
   * Get folder for project deploying
   */
  private File getDeployingRootFolder() {
    return new File(getDeployRuntimeFolder(), ".deploying");
  }

  private File getDeployingFolder(String projectName) {
    return new File(getDeployingRootFolder(), projectName);
  }

  private File getDeployingTaskFile(String projectName) {
    return new File(getDeployingFolder(projectName), projectName + ".task");
  }

  private File getDeployingProjectFile(String projectName) {
    return new File(getDeployingFolder(projectName), projectName + ".proj");
  }

  private File getDeployingIconFile(String projectName, String kind) {
    return new File(getDeployingFolder(projectName), projectName + "-" + kind + ".icon");
  }

  private File getDeployingJNLPFile(String projectName) {
    return new File(getDeployingFolder(projectName), projectName + ".jnlp");
  }

  private File getDeployingLibraryFile(String projectName, String libraryName) {
    return new File(getDeployingFolder(projectName), libraryName + ".lib");
  }

  private File[] getDeployingLibraryFiles(String projectName) {
    return getDeployingFolder(projectName).listFiles(
        f -> f.isFile() && f.getName().matches(".*\\.lib"));
  }

  private File getDeployingJarFile(String projectName, XJNLPLibrary library) {
    return new File(getDeployingFolder(projectName), getLibraryFileName(library));
  }

  private String getLibraryFileName(XJNLPLibrary library) {
    String version = library.getVersion();
    if (XObjectUtils.isEmpty(version))
      version = "";
    else
      version = "-" + version;
    return library.getName() + version + ".jar";
  }

  /*
   * Get task number for the project
   */
  private String readProjectTask(String projectName) throws IOException {
    File taskFile = getDeployingTaskFile(projectName);
    if (!taskFile.exists()) return null;
    return XJNLPDeployUtils.readFileString(taskFile);
  }

  /**
   * Get deployed projects
   * 
   * @return deployed projects, empty list if no deployed project
   * @throws IOException
   *           if any IO errors occur
   */
  public List<XJNLPProject> getProjects() throws IOException {
    List<XJNLPProject> projects = readStoredProjects();
    return projects == null ? new ArrayList<>() : projects;
  }

  /**
   * Get deploying project, returns deployed project if the project not in deploying, all
   * properties directly declared in {@link XJNLPProjectDeploy} are not set in this case
   * 
   * @param projectName
   *          the project name
   * @return the project, initial set to true if no such project
   * @throws IOException
   *           if any IO errors occur
   */
  public XJNLPProjectDeploy getDeployingProject(String projectName) throws IOException {
    XJNLPProjectDeploy project;
    File file = getDeployingProjectFile(projectName);
    if (isProjectTaskOwner(projectName) && file.exists())
      project = JSONMapper.readValue(file, XJNLPProjectDeploy.class);
    else
      project = new XJNLPProjectDeploy(readStoredProject(projectName));
    project.setName(projectName); // force project name
    return project;
  }

  /**
   * Get deploying project icons, returns deployed project icons if project not in
   * deploying, returns empty map if no any icons uploaded
   * 
   * @param projectName
   *          the project name
   * @return icons map contains kind and icon data text (base64 formatted)
   * @throws IOException
   *           if any IO errors occur
   */
  public Map<String, String> getDeployingIcons(String projectName) throws IOException {
    Map<String, String> icons = new HashMap<>();
    for (String kind : iconKinds) {
      File file = getDeployingIconFile(projectName, kind);
      String content;
      if (file.exists())
        content = XJNLPDeployUtils.readFileString(file);
      else
        content = readStoredIconFile(projectName, kind);
      if (content != null) icons.put(kind, content);
    }
    return icons;
  }

  /**
   * Get deploying libraries of the project, returns stored libraries if currently no any
   * changes made
   * 
   * @param projectName
   *          the project name
   * @return libraries of the project, empty list if no any library
   * @throws IOException
   *           if any IO errors occur
   */
  public List<XJNLPLibraryDeploy> getDeployingLibraries(String projectName) throws IOException {
    List<XJNLPLibraryDeploy> deployingList = new ArrayList<>();
    List<XJNLPLibrary> storedList = readStoredLibraries(projectName);
    if (storedList != null) {
      for (XJNLPLibrary library : storedList)
        deployingList.add(new XJNLPLibraryDeploy(library));
    }
    File[] deployingFiles = getDeployingLibraryFiles(projectName);
    if (deployingFiles != null) {
      for (File file : deployingFiles) {
        XJNLPLibraryDeploy library = JSONMapper.readValue(file, XJNLPLibraryDeploy.class);
        int index = deployingList.indexOf(library);
        if (index == -1)
          deployingList.add(library);
        else
          deployingList.set(index, library);
      }
    }
    deployingList.sort((o1, o2) -> o1.getSequence() - o2.getSequence());
    if (!deployingList.isEmpty()) deployingList.get(0).setMajor(true);
    return deployingList;
  }

  /**
   * Update deploying project with or without JNLP file content change
   * 
   * @param project
   *          the project to update
   * @param jnlpContent
   *          new JNLP file content or null if not changed
   * @return updated project
   * @throws IOException
   *           if any IO errors occur
   */
  public XJNLPProjectDeploy updateDeployingProject(XJNLPProjectDeploy project,
      String jnlpContent) throws IOException {
    assert !XObjectUtils.isEmpty(project.getName());
    String projectName = project.getName();
    checkRequestTaskNumber(projectName);
    if (jnlpContent != null) jnlpContent = jnlpContent.trim();
    if (!XObjectUtils.isEmpty(jnlpContent)) {
      project.setTitle(parseTitle(jnlpContent));
      writeDeployingJNLP(projectName, jnlpContent);
    }
    XJNLPProjectDeploy old = getDeployingProject(projectName);
    String oldBaseClass = getModuleBaseClassNoEmpty(old);
    String newBaseClass = getModuleBaseClassNoEmpty(project);
    if (!Objects.equals(oldBaseClass, newBaseClass)) {
      updateDeployingModules(project);
    }
    File projectFile = getDeployingProjectFile(project.getName());
    String content = JSONMapper.writeValueAsString(project);
    XJNLPDeployUtils.writeFileContent(projectFile, content);
    return project;
  }

  private String parseTitle(String jnlpContent) throws IOException {
    try {
      Document document = XJNLPDeployUtils.parseXMLDocument(jnlpContent);
      Element jnlpElement = document.getDocumentElement();
      if (!jnlpElement.getTagName().equals("jnlp"))
        throw new SAXException("Can not found root jnlp node");
      return jnlpElement.getElementsByTagName("title").item(0).getTextContent();
    } catch (Exception ex) {
      throw new IOException("Invalid JNLP file format", ex);
    }
  }

  private void writeDeployingJNLP(String projectName, String jnlpContent) throws IOException {
    File jnlpFile = getDeployingJNLPFile(projectName);
    XJNLPDeployUtils.writeFileContent(jnlpFile, jnlpContent);
  }

  private String getModuleBaseClassNoEmpty(XJNLPProject project) {
    String baseClass = project == null ? null : project.getModuleBaseClass();
    return (baseClass == null || baseClass.isEmpty()) ? null : baseClass;
  }

  private void updateDeployingModules(XJNLPProjectDeploy project) throws IOException {
    String projectName = project.getName();
    List<XJNLPLibraryDeploy> libraries = getDeployingLibraries(projectName);
    URLClassLoader loader = null;
    boolean checking = !XObjectUtils.isEmpty(project.getModuleBaseClass());
    if (checking) loader = createDeployingLoader(projectName, null);
    try {
      for (XJNLPLibraryDeploy library : libraries) {
        library.setModules(parseModules(loader, project, library));
        File file = getDeployingLibraryFile(projectName, library.getName());
        String content = JSONMapper.writeValueAsString(library);
        XJNLPDeployUtils.writeFileContent(file, content);
      }
    } finally {
      closeLoaderAndCallGC(loader);
    }
  }

  /**
   * Update deploying project icon
   * 
   * @param projectName
   *          the project name
   * @param kind
   *          the icon kind
   * @param iconContent
   *          the icon content (base64 formatted)
   * @throws IOException
   *           if any IO errors occur
   */
  public void updateDeployingIcon(String projectName, String kind,
      String iconContent) throws IOException {
    if (!Arrays.asList(iconKinds).contains(kind))
      throw new IOException("Unknown icon kind: " + kind);
    if (getIconExtension(iconContent) == null)
      throw new IOException("Invalid image format");
    File file = getDeployingIconFile(projectName, kind);
    XJNLPDeployUtils.writeFileContent(file, iconContent);
  }

  /**
   * Update deploying library with or without JAR file content change
   * 
   * @param projectName
   *          the project name
   * @param library
   *          the library
   * @param jarContent
   *          new JAR file content or null if not changed
   * @return updated library
   * @throws IOException
   *           if any IO errors occur
   */
  public XJNLPLibraryDeploy updateDeployingLibrary(String projectName,
      XJNLPLibraryDeploy library, byte[] jarContent) throws IOException {
    assert !XObjectUtils.isEmpty(projectName);
    assert !XObjectUtils.isEmpty(library.getName());
    checkRequestTaskNumber(projectName);
    if (!XObjectUtils.isEmpty(jarContent)) {
      File jarFile = getDeployingJarFile(projectName, library);
      checkSignedAndSave(jarFile, jarContent);
      library.setDeleted(false);
      library.setDisabled(false);
      library.setFileSize(jarContent.length);
      library.setFileHash(calcFileHash(jarContent));
      library.setModules(parseModules(projectName, library));
    }
    File file = getDeployingLibraryFile(projectName, library.getName());
    String content = JSONMapper.writeValueAsString(library);
    XJNLPDeployUtils.writeFileContent(file, content);
    return library;
  }

  private void checkSignedAndSave(File jarFile, byte[] jarContent)
      throws IOException {
    File tmp = new File(jarFile.getAbsolutePath() + ".tmp");
    XJNLPDeployUtils.writeFileContent(tmp, jarContent);
    if (isJarSigned(tmp))
      XJNLPDeployUtils.moveFile(tmp, jarFile);
    else
      throw new IOException("the jar file has not been signed");
  }

  private boolean isJarSigned(File file) throws IOException {
    try (JarFile jar = new JarFile(file)) {
      if (jar.getEntry("META-INF/SIGNATURE.BSF") != null)
        return true; // Blob Signing
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory()) continue;
        try (InputStream is = jar.getInputStream(entry)) {
          XJNLPDeployUtils.readInputStream(is);
        }
        if (!XObjectUtils.isEmpty(entry.getCodeSigners()))
          return true;
      }
    }
    return false;
  }

  private String calcFileHash(byte[] jarContent) throws IOException {
    try {
      return XJNLPDeployUtils.calcHashSHA256(jarContent);
    } catch (NoSuchAlgorithmException ex) {
      throw new IOException("Calculate file hash failed", ex);
    }
  }

  private List<XJNLPModule> parseModules(String projectName, XJNLPLibraryDeploy library)
      throws IOException {
    XJNLPProjectDeploy project = getDeployingProject(projectName);
    if (XObjectUtils.isEmpty(project.getModuleBaseClass())) return null;
    URLClassLoader loader = createDeployingLoader(projectName, library);
    try {
      return parseModules(loader, project, library);
    } finally {
      closeLoaderAndCallGC(loader);
    }
  }

  /*
   * Get class loader for deploying
   */
  private URLClassLoader createDeployingLoader(String projectName,
      XJNLPLibraryDeploy deploying) throws IOException {
    List<XJNLPLibraryDeploy> libraries = getDeployingLibraries(projectName);
    if (deploying != null) {
      int index = libraries.indexOf(deploying);
      if (index == -1)
        libraries.add(deploying);
      else
        libraries.set(index, deploying);
    }
    List<URL> urlList = new ArrayList<>();
    for (XJNLPLibraryDeploy library : libraries) {
      if (library.isDisabled() || library.isDeleted()) continue;
      File jarFile = getDeployingJarFile(projectName, library);
      if (!jarFile.exists()) {
        jarFile = getRuntimeJarFile(projectName, library);
      }
      urlList.add(jarFile.toURI().toURL());
    }
    return new URLClassLoader(urlList.toArray(new URL[0]));
  }

  private List<XJNLPModule> parseModules(URLClassLoader loader, XJNLPProject project,
      XJNLPLibrary library) {
    List<XJNLPModule> moduleList = new ArrayList<>();
    try {
      Class<?> baseClass = loader.loadClass(project.getModuleBaseClass());
      String nameGetter = project.getModuleNameGetter();
      String versionGetter = project.getModuleVersionGetter();
      File jarFile = getDeployingJarFile(project.getName(), library);
      for (String className : XSystemUtils.listClass(jarFile)) {
        try {
          Class<?> checking = loader.loadClass(className);
          int modifiers = checking.getModifiers();
          if (!baseClass.isAssignableFrom(checking) ||
              Modifier.isAbstract(modifiers) || !Modifier.isPublic(modifiers))
            continue;
          Object instance = checking.newInstance();
          XJNLPModule module = new XJNLPModule(className);
          moduleList.add(module);
          try {
            module.setName(getAttributeValue(instance, checking, nameGetter));
          } catch (Throwable ex) {
            // safely ignore any exceptions
          }
          try {
            module.setVersion(getAttributeValue(instance, checking, versionGetter));
          } catch (Throwable ex) {
            // safely ignore any exceptions
          }
          if (XObjectUtils.isEmpty(module.getName())) module.setName(className);
        } catch (Throwable ex) {
          ex.printStackTrace();
          // safely ignore any exceptions
        }
      }
    } catch (Throwable ex) {
      // safely ignore any exceptions
    }
    return moduleList.isEmpty() ? null : moduleList;
  }

  private String getAttributeValue(Object object, Class<?> clazz, String getter)
      throws Exception {
    Object value = null;
    if (getter != null && !getter.isEmpty()) {
      Method method = clazz.getMethod(getter);
      if (method != null) value = method.invoke(object);
    }
    return value == null ? null : value.toString();
  }

  private void closeLoaderAndCallGC(URLClassLoader loader) {
    try {
      try {
        loader.close();
      } finally {
        System.gc();
      }
    } catch (Throwable ex) {
      // completely ignore any exceptions
    }
  }

  /**
   * Start deploy task if possible
   * 
   * @param projectName
   *          the project name
   * @param forceDeploy
   *          whether force start deploy
   * @return task number or null if deploy rejected
   * @throws IOException
   *           if any IO errors occur
   */
  public String startDeployingTask(String projectName,
      boolean forceDeploy) throws IOException {
    assert !XObjectUtils.isEmpty(projectName);
    synchronized (getSyncKey(projectName)) {
      if (!XObjectUtils.isEmpty(getDeployTask()))
        throw new IOException("Current request already in deploy");
      String projectTask = readProjectTask(projectName);
      if (!XObjectUtils.isEmpty(projectTask)) {
        if (!forceDeploy) return null; // reject
        File deployingFolder = getDeployingFolder(projectName);
        XJNLPDeployUtils.clearDirectory(deployingFolder, false);
      }
      projectTask = UUID.randomUUID().toString();
      File taskFile = getDeployingTaskFile(projectName);
      XJNLPDeployUtils.writeFileContent(taskFile, projectTask);
      return projectTask;
    }
  }

  private Object getSyncKey(String object) {
    synchronized (syncHolder) {
      if (!syncHolder.containsKey(object))
        syncHolder.put(object, new Object());
      return syncHolder.get(object);
    }
  }

  /**
   * Finish deploy task, commit all deploy operation in task
   * 
   * @param projectName
   *          the Project Name
   * @throws IOException
   *           if any IO errors occur
   */
  public void finishDeployingTask(String projectName) throws IOException {
    assert !XObjectUtils.isEmpty(projectName);
    synchronized (getSyncKey(projectName)) {
      checkRequestTaskNumber(projectName);
      beginTransaction(projectName);
      boolean transactionActive = true;
      try {
        XJNLPProjectDeploy project = getDeployingProject(projectName);
        if (project.isDeleted())
          removeProject(projectName);
        else
          performStoreProject(project);
        commitTransaction(projectName);
        clearDeployingTask(projectName);
        transactionActive = false;
      } finally {
        if (transactionActive) rollbackTransaction(projectName);
      }
    }
  }

  /**
   * Cancel deploy task, No-OP if current request no valid task
   * 
   * @param projectName
   *          the ProjectName
   * @throws IOException
   *           if any IO errors occur
   */
  public void cancelDeployingTask(String projectName) throws IOException {
    assert !XObjectUtils.isEmpty(projectName);
    synchronized (getSyncKey(projectName)) {
      String projectTask = readProjectTask(projectName);
      String deployTask = getDeployTask();
      if (XObjectUtils.isEmpty(deployTask)
          || !Objects.equals(deployTask, projectTask))
        return;
      clearDeployingTask(projectName);
    }
  }

  private void clearDeployingTask(String projectName) throws IOException {
    XJNLPDeployUtils.clearDirectory(getDeployingFolder(projectName), true);
  }

  private void performStoreProject(XJNLPProjectDeploy project) throws IOException {
    String projectName = project.getName();
    File projFile = getDeployingProjectFile(projectName);
    Map<String, String> commitingIcons = getCommittingIcons(projectName);
    List<XJNLPLibraryDeploy> committingLibraries = getCommittingLibraries(projectName);
    // force project update if any icon or library changed
    if (projFile.exists() || !commitingIcons.isEmpty() || !committingLibraries.isEmpty()) {
      project.setLastModified(LocalDateTime.now());
      File jnlpFile = getDeployingJNLPFile(projectName);
      String jnlpContent = null;
      if (jnlpFile.exists()) {
        jnlpContent = XJNLPDeployUtils.readFileString(jnlpFile);
      }
      storeProject(project, jnlpContent);
    }
    for (String kind : commitingIcons.keySet()) {
      storeIcon(projectName, kind, commitingIcons.get(kind));
    }
    for (XJNLPLibraryDeploy library : getCommittingLibraries(projectName)) {
      if (library.isDeleted()) {
        removeLibrary(projectName, library.getName());
        continue;
      }
      XJNLPLibrary storedLib = readStoredLibrary(projectName, library.getName());
      File jarFile = getDeployingJarFile(projectName, library);
      byte[] jarContent = null;
      if (!jarFile.exists() && storedLib == null) {
        throw new IOException("New library " + library.getName() + " withou jar file");
      }
      if (jarFile.exists() && !isLibraryJarSame(library, storedLib)) {
        library.setLastModified(LocalDateTime.now());
        jarContent = XJNLPDeployUtils.readFileBinary(jarFile);
      } else {
        library.setFileSize(storedLib.getFileSize());
        library.setFileHash(storedLib.getFileHash());
        library.setLastModified(storedLib.getLastModified());
      }
      storeLibrary(projectName, library, jarContent);
    }
  }

  private Map<String, String> getCommittingIcons(String projectName) throws IOException {
    Map<String, String> icons = new HashMap<>();
    for (String kind : iconKinds) {
      File file = getDeployingIconFile(projectName, kind);
      if (file.exists()) icons.put(kind, XJNLPDeployUtils.readFileString(file));
    }
    return icons;
  }

  private List<XJNLPLibraryDeploy> getCommittingLibraries(String projectName) throws IOException {
    List<XJNLPLibraryDeploy> committingList = new ArrayList<>();
    File[] deployingFiles = getDeployingLibraryFiles(projectName);
    if (deployingFiles != null) {
      for (File file : deployingFiles)
        committingList.add(JSONMapper.readValue(file, XJNLPLibraryDeploy.class));
    }
    return committingList;
  }

  private boolean isLibraryJarSame(XJNLPLibraryDeploy library, XJNLPLibrary storedLib) {
    return storedLib != null && Objects.equals(library.getFileHash(), storedLib.getFileHash());
  }

  private void checkRequestTaskNumber(String projectName) throws IOException {
    String projectTask = readProjectTask(projectName);
    String deployTask = getDeployTask();
    if (XObjectUtils.isEmpty(deployTask))
      throw new IOException("Current request no deploy task");
    if (!Objects.equals(deployTask, projectTask))
      throw new IOException("Current request task was invalid");
  }

  /**
   * Get runtime project for the name
   * 
   * @param projectName
   *          the project name
   * @return project or null if no such project
   * @throws IOException
   *           if any IO errors occur
   */
  public XJNLPProject getRuntimeProject(String projectName) throws IOException {
    return readStoredProject(projectName);
  }

  /**
   * Read runtime project JNLP file content with some attributes changed to fit the
   * project
   * 
   * @param baseURL
   *          the base URL
   * @param projectName
   *          the project name
   * @return JNLP file content of the project
   * @throws IOException
   *           if any IO errors occur
   */
  public String readRuntimeJNLP(String baseURL, String projectName) throws IOException {
    try {
      String jnlpContent = readStoredJnlpFile(projectName);
      Document document = XJNLPDeployUtils.parseXMLDocument(jnlpContent);
      fillCodebase(document, baseURL, projectName);
      fillIcons(document, projectName);
      fillResources(document, projectName);
      return XJNLPDeployUtils.getXMLContent(document);
    } catch (SAXException | ParserConfigurationException | TransformerException ex) {
      throw new IOException("Generate JNLP file failed", ex);
    }
  }

  private void fillCodebase(Document document, String baseURL, String projectName) {
    Element jnlp = (Element) document.getElementsByTagName("jnlp").item(0);
    String codebase = baseURL + projectName;
    jnlp.setAttribute("codebase", codebase);
    jnlp.setAttribute("href", projectName + ".jnlp");
  }

  private void fillIcons(Document document, String projectName) throws IOException {
    Element info = (Element) document.getElementsByTagName("information").item(0);
    NodeList iconList = info.getElementsByTagName("icon");
    Node before = removeChildren(info, iconList);
    for (String kind : iconKinds) {
      String iconContent = readStoredIconFile(projectName, kind);
      String ext = getIconExtension(iconContent);
      if (ext == null) continue;
      Element icon = document.createElement("icon");
      icon.setAttribute("kind", kind);
      icon.setAttribute("href", projectName + "-" + kind + ext);
      info.insertBefore(icon, before);
    }
  }

  private String getIconExtension(String iconContent) {
    String mimeType = XJNLPDeployUtils.getMimeTypeFromContent(iconContent);
    if (mimeType == null) return null;
    if (mimeType.equals("image/jpeg")) return ".jpg";
    if (mimeType.equals("image/png")) return ".png";
    if (mimeType.equals("image/gif")) return ".gif";
    return mimeType.matches(".*icon") ? ".ico" : null;
  }

  private void fillResources(Document document, String projectName) throws IOException {
    Element resources = (Element) document.getElementsByTagName("resources").item(0);
    NodeList jarList = resources.getElementsByTagName("jar");
    Node before = removeChildren(resources, jarList);
    boolean versionEnabled = determineVersionEnabled(resources);
    boolean first = true;
    List<XJNLPLibrary> libraries = readStoredLibraries(projectName);
    libraries.sort((o1, o2) -> o1.getSequence() - o2.getSequence());
    for (XJNLPLibrary library : libraries) {
      if (library.isDisabled()) continue;
      Element jar = document.createElement("jar");
      String jarName = getLibraryFileName(library);
      if (first)
        jar.setAttribute("main", "true");
      else
        jarName = "lib/" + jarName;
      first = false;
      jar.setAttribute("href", jarName);
      jar.setAttribute("size", String.valueOf(library.getFileSize()));
      if (versionEnabled) jar.setAttribute("version", library.getVersion());
      resources.insertBefore(jar, before);
    }
  }

  private Node removeChildren(Element owner, NodeList childList) {
    List<Node> removing = new ArrayList<>();
    Node before = null;
    for (int i = 0; i < childList.getLength(); i++) {
      Node child = childList.item(i);
      if (i == 0) {
        Node prev = child.getPreviousSibling();
        while (prev instanceof Text) {
          removing.add(0, prev);
          prev = prev.getPreviousSibling();
        }
      }
      removing.add(child);
      Node next = child.getNextSibling();
      while (next instanceof Text) {
        removing.add(next);
        next = next.getNextSibling();
      }
      before = next;
    }
    for (int i = 0; i < removing.size(); i++) {
      owner.removeChild(removing.get(i));
    }
    return before;
  }

  private boolean determineVersionEnabled(Element resources) {
    NodeList properties = resources.getElementsByTagName("property");
    for (int i = 0; i < properties.getLength(); i++) {
      NamedNodeMap attributes = properties.item(i).getAttributes();
      Node nameNode = attributes.getNamedItem("name");
      String name = nameNode == null ? null : nameNode.getNodeValue();
      if (!"jnlp.versionEnabled".equalsIgnoreCase(name)) continue;
      Node valueNode = attributes.getNamedItem("value");
      if (valueNode != null && Boolean.valueOf(valueNode.getNodeValue()))
        return true;
    }
    return false;
  }

  /**
   * Read runtime icon for the project
   * 
   * @param projectName
   *          the project name
   * @param fileName
   *          the icon file name
   * @return icon content (base64 formatted) or null if no such icon
   * @throws IOException
   *           if any IO errors occur
   */
  public String readRuntimeIcon(String projectName, String fileName) throws IOException {
    for (String kind : iconKinds) {
      if (fileName.contains("-" + kind + "."))
        return readStoredIconFile(projectName, kind);
    }
    return null;
  }

  /**
   * Read runtime modules in the project
   * 
   * @param projectName
   *          the project name
   * @return modules
   * @throws IOException
   *           if any IO errors occur
   */
  public List<XJNLPModule> readRuntimeModules(String projectName) throws IOException {
    List<XJNLPModule> modules = new ArrayList<>();
    for (XJNLPLibrary library : readStoredLibraries(projectName)) {
      if (library.isDisabled()) continue;
      if (library.getModules() != null)
        modules.addAll(library.getModules());
    }
    return modules;
  }

  /**
   * Get runtime library for the file name
   * 
   * @param projectName
   *          the project name
   * @param fileName
   *          the file name
   * @return library or null if no such library
   * @throws IOException
   *           if any IO errors occur
   */
  public XJNLPLibrary getRuntimeLibrary(String projectName, String fileName) throws IOException {
    for (XJNLPLibrary library : readStoredLibraries(projectName)) {
      if (getLibraryFileName(library).equals(fileName)) {
        return library;
      }
    }
    return null;
  }

  /**
   * Get project library jar file content for the file name
   * 
   * @param projectName
   *          the project name
   * @param fileName
   *          the file name
   * @return file content or null if no such library
   * @throws IOException
   *           if any IO errors occur
   */
  public byte[] readRuntimeJar(String projectName, String fileName) throws IOException {
    XJNLPLibrary library = getRuntimeLibrary(projectName, fileName);
    if (library == null) return null;
    return XJNLPDeployUtils.readFileBinary(getRuntimeJarFile(projectName, library));
  }

  private File getRuntimeJarFile(String projectName, XJNLPLibrary library) throws IOException {
    synchronized (getSyncKey(projectName + "#" + library.getName())) {
      File projectFolder = new File(getDeployRuntimeFolder(), projectName);
      File jarFile = new File(projectFolder, getLibraryFileName(library));
      File hashFile = new File(jarFile.getAbsolutePath() + ".hash");
      if (jarFile.exists() && hashFile.exists()) {
        String fileHash = XJNLPDeployUtils.readFileString(hashFile);
        if (fileHash != null && fileHash.equals(library.getFileHash()))
          return jarFile;
      }
      if (!projectFolder.exists()) projectFolder.mkdirs();
      byte[] jarContent = readStoredLibraryJar(projectName, library.getName());
      XJNLPDeployUtils.writeFileContent(jarFile, jarContent);
      String fileHash = calcFileHash(jarContent);
      XJNLPDeployUtils.writeFileContent(hashFile, fileHash);
      return jarFile;
    }
  }
}
