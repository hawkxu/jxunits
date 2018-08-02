package win.zqxu.jxunits.web.jnlp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import win.zqxu.jxunits.jre.XObjectUtils;

/**
 * Default store for JNLP deployment, allow all access and deployment, store files on
 * local drive
 * 
 * @author zqxu
 */
public class XJNLPDefaultStore extends XJNLPAbstractStore {
  private static final ObjectMapper JSONMapper = XJNLPDeployUtils.getJSONMapper();
  private static final Map<String, Object> transactionHolder = new HashMap<>();
  private static final Map<String, Object> committingHolder = new HashMap<>();
  private Map<String, Object> removingProjects = new HashMap<>();
  private Map<String, XJNLPProject> committingProjects = new HashMap<>();
  private Map<String, List<String>> removingLibraries = new HashMap<>();
  private Map<String, List<XJNLPLibrary>> committingLibraries = new HashMap<>();

  public XJNLPDefaultStore(String deployTask) {
    super(deployTask);
  }

  @Override
  public void initialize() {
    super.initialize();
    transactionHolder.clear();
    committingHolder.clear();
  }

  /**
   * Default allow all access
   */
  @Override
  public boolean isAccessAllowed(HttpServletRequest request, String projectName) {
    return true;
  }

  /**
   * Default allow all deployment
   */
  @Override
  public boolean isDeployAllowed(HttpServletRequest request, String projectName) {
    return true;
  }

  private File getDeployStoreFolder() {
    return new File(getDeployRootFolder(), "store");
  }

  private File getProjectFolder(String projectName) {
    return new File(getDeployStoreFolder(), projectName);
  }

  private File getProjectFile(String projectName) {
    return new File(getProjectFolder(projectName), projectName + ".proj");
  }

  private File getProjectJnlpFile(String projectName) {
    return new File(getProjectFolder(projectName), projectName + ".jnlp");
  }

  private File getProjectIconFile(String projectName, String kind) {
    return new File(getProjectFolder(projectName), projectName + "-" + kind + ".icon");
  }

  private File[] getLibraryFiles(String projectName) {
    return getProjectFolder(projectName).listFiles(
        f -> f.isFile() && f.getName().matches(".*\\.lib"));
  }

  private File getLibraryFile(String projectName, String libraryName) {
    return new File(getProjectFolder(projectName), libraryName + ".lib");
  }

  private File getLibraryJarFile(String projectName, String libraryName) {
    return new File(getProjectFolder(projectName), libraryName + ".jar");
  }

  private File getCommittingFolder(String projectName) {
    return new File(getDeployStoreFolder(), "." + projectName);
  }

  private File getCommittingJnlpFile(String projectName) {
    return new File(getCommittingFolder(projectName), projectName + ".jnlp");
  }

  private File getCommittingJarFile(String projectName, String libraryName) {
    return new File(getCommittingFolder(projectName), libraryName + ".jar");
  }

  @Override
  protected List<XJNLPProject> readStoredProjects() throws IOException {
    ArrayList<XJNLPProject> projectList = new ArrayList<>();
    File[] projectFolders = getDeployStoreFolder().listFiles(
        f -> f.isDirectory() && !f.getName().startsWith("."));
    if (projectFolders != null) {
      for (File folder : projectFolders) {
        File file = getProjectFile(folder.getName());
        if (file.exists())
          projectList.add(JSONMapper.readValue(file, XJNLPProject.class));
      }
    }
    return projectList;
  }

  /**
   * Check whether the project is in committing,
   * 
   * @param projectName
   *          the project name
   */
  protected void checkProjectInCommitting(String projectName) {
    synchronized (committingHolder) {
      if (committingHolder.containsKey(projectName))
        throw new IllegalStateException("the project is in committing");
    }
  }

  @Override
  protected XJNLPProject readStoredProject(String projectName) throws IOException {
    checkProjectInCommitting(projectName);
    File file = getProjectFile(projectName);
    return file.exists() ? JSONMapper.readValue(file, XJNLPProject.class) : null;
  }

  @Override
  protected String readStoredJnlpFile(String projectName) throws IOException {
    checkProjectInCommitting(projectName);
    return XJNLPDeployUtils.readFileString(getProjectJnlpFile(projectName));
  }

  @Override
  protected String readStoredIconFile(String projectName, String kind) throws IOException {
    checkProjectInCommitting(projectName);
    File file = getProjectIconFile(projectName, kind);
    return file.exists() ? XJNLPDeployUtils.readFileString(file) : null;
  }

  @Override
  protected List<XJNLPLibrary> readStoredLibraries(String projectName) throws IOException {
    checkProjectInCommitting(projectName);
    List<XJNLPLibrary> libraryList = new ArrayList<>();
    File[] libraryFiles = getLibraryFiles(projectName);
    if (libraryFiles != null) {
      for (File file : libraryFiles)
        libraryList.add(JSONMapper.readValue(file, XJNLPLibrary.class));
    }
    return libraryList;
  }

  @Override
  protected XJNLPLibrary readStoredLibrary(String projectName, String libraryName)
      throws IOException {
    checkProjectInCommitting(projectName);
    File file = getLibraryFile(projectName, libraryName);
    return file.exists() ? JSONMapper.readValue(file, XJNLPLibrary.class) : null;
  }

  @Override
  protected byte[] readStoredLibraryJar(String projectName, String libraryName) throws IOException {
    checkProjectInCommitting(projectName);
    return XJNLPDeployUtils.readFileBinary(getLibraryJarFile(projectName, libraryName));
  }

  @Override
  protected void beginTransaction(String projectName) throws IOException {
    synchronized (transactionHolder) {
      if (transactionHolder.containsKey(projectName))
        throw new IllegalStateException("the project was in another transaction");
      transactionHolder.put(projectName, null);
    }
    removingProjects.remove(projectName);
    committingProjects.remove(projectName);
    removingLibraries.put(projectName, new ArrayList<>());
    committingLibraries.put(projectName, new ArrayList<>());
    XJNLPDeployUtils.clearDirectory(getCommittingFolder(projectName), false);
  }

  @Override
  protected void commitTransaction(String projectName) throws IOException {
    synchronized (transactionHolder) {
      if (!transactionHolder.containsKey(projectName))
        throw new IllegalStateException("the project was not in transaction");
    }
    synchronized (committingHolder) {
      committingHolder.put(projectName, null);
    }
    try {
      if (removingProjects.containsKey(projectName)) {
        XJNLPDeployUtils.clearDirectory(getProjectFolder(projectName), true);
        return;
      }
      if (committingProjects.containsKey(projectName)) {
        updateProjectFile(projectName, committingProjects.get(projectName));
      }
      if (!removingLibraries.containsKey(projectName)
          && !committingLibraries.containsKey(projectName))
        return;
      for (String libraryName : removingLibraries.get(projectName)) {
        updateLibrary(projectName, libraryName, null, true);
      }
      for (XJNLPLibrary library : committingLibraries.get(projectName)) {
        updateLibrary(projectName, library.getName(), library, false);
      }
    } finally {
      clearTransaction(projectName);
      committingHolder.remove(projectName);
    }
  }

  private void updateProjectFile(String projectName, XJNLPProject project)
      throws IOException {
    File projectFile = getProjectFile(projectName);
    String content = JSONMapper.writeValueAsString(project);
    XJNLPDeployUtils.writeFileContent(projectFile, content);

    File commitingJnlp = getCommittingJnlpFile(projectName);
    if (commitingJnlp.exists()) {
      File storedJnlp = getProjectJnlpFile(projectName);
      if (!XJNLPDeployUtils.moveFile(commitingJnlp, storedJnlp))
        throw new IOException("Can not update project");
    }
  }

  private void updateLibrary(String projectName, String libraryName,
      XJNLPLibrary library, boolean remove) throws IOException {
    if (remove) {
      getLibraryFile(projectName, libraryName).delete();
      getLibraryJarFile(projectName, libraryName).delete();
    } else {
      File libraryFile = getLibraryFile(projectName, libraryName);
      String content = JSONMapper.writeValueAsString(library);
      XJNLPDeployUtils.writeFileContent(libraryFile, content);
      File committingJar = getCommittingJarFile(projectName, libraryName);
      if (committingJar.exists()) {
        File storedJar = getLibraryJarFile(projectName, libraryName);
        if (!XJNLPDeployUtils.moveFile(committingJar, storedJar))
          throw new IOException("Can not update library " + libraryName);
      }
    }
  }

  @Override
  protected void rollbackTransaction(String projectName) throws IOException {
    synchronized (transactionHolder) {
      if (!transactionHolder.containsKey(projectName))
        throw new IllegalStateException("the project was not in transaction");
    }
    clearTransaction(projectName);
  }

  private void clearTransaction(String projectName) {
    try {
      removingProjects.remove(projectName);
      committingProjects.remove(projectName);
      removingLibraries.remove(projectName);
      committingLibraries.remove(projectName);
      File commitingFolder = getCommittingFolder(projectName);
      XJNLPDeployUtils.clearDirectory(commitingFolder, true);
      commitingFolder.delete();
    } catch (Throwable ex) {
      // completely ignore any exceptions
    }
    synchronized (transactionHolder) {
      transactionHolder.remove(projectName);
    }
  }

  @Override
  protected void storeProject(XJNLPProject project, String jnlpContent) throws IOException {
    committingProjects.put(project.getName(), project);
    if (!XObjectUtils.isEmpty(jnlpContent)) {
      File jnlpFile = getCommittingJnlpFile(project.getName());
      XJNLPDeployUtils.writeFileContent(jnlpFile, jnlpContent);
    }
  }

  @Override
  protected void removeProject(String projectName) throws IOException {
    removingProjects.put(projectName, null);
  }

  @Override
  protected void storeIcon(String projectName, String kind, String iconContent) throws IOException {
    File file = getProjectIconFile(projectName, kind);
    XJNLPDeployUtils.writeFileContent(file, iconContent);
  }

  @Override
  protected void storeLibrary(String projectName, XJNLPLibrary library, byte[] jarContent)
      throws IOException {
    committingLibraries.get(projectName).add(library);
    if (!XObjectUtils.isEmpty(jarContent)) {
      File jarFile = getCommittingJarFile(projectName, library.getName());
      XJNLPDeployUtils.writeFileContent(jarFile, jarContent);
    }
  }

  @Override
  protected void removeLibrary(String projectName, String libraryName) throws IOException {
    removingLibraries.get(projectName).add(libraryName);
  }
}