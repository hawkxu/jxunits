package win.zqxu.jxunits.web.jnlp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JNLP project descriptor
 * 
 * @author zqxu
 *
 */
public class XJNLPProject {
  private String name;
  private String version;
  private String title;
  private boolean disabled;
  private String moduleBaseClass;
  private String moduleNameGetter;
  private String moduleVersionGetter;
  private LocalDateTime lastModified;

  /**
   * Default constructor
   */
  public XJNLPProject() {
    this(null);
  }

  /**
   * Constructor with project name
   * 
   * @param name
   *          the project name
   */
  public XJNLPProject(String name) {
    this.name = name;
  }

  /**
   * Get project name
   * 
   * @return the project name
   */
  public String getName() {
    return name;
  }

  /**
   * Set project name
   * 
   * @param name
   *          the project name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get project version
   * 
   * @return the project version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set project version
   * 
   * @param version
   *          the project version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Get project title
   * 
   * @return the project title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set project title
   * 
   * @param title
   *          the project title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get whether the project is disabled
   * 
   * @return true for disabled or false not
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Set whether the project is disabled
   * 
   * @param disabled
   *          true for disabled or false not
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * Get base class name for module
   * 
   * @return base class name for module
   */
  public String getModuleBaseClass() {
    return moduleBaseClass;
  }

  /**
   * Set base class name for module
   * 
   * @param moduleBaseClass
   *          base class name for module
   */
  public void setModuleBaseClass(String moduleBaseClass) {
    this.moduleBaseClass = moduleBaseClass;
  }

  /**
   * Get method name for get module title
   * 
   * @return method name for get module title
   */
  public String getModuleNameGetter() {
    return moduleNameGetter;
  }

  /**
   * Set method name for get module title
   * 
   * @param moduleNameGetter
   *          method name for get module title
   */
  public void setModuleNameGetter(String moduleNameGetter) {
    this.moduleNameGetter = moduleNameGetter;
  }

  /**
   * Get method name for get module version
   * 
   * @return method name for get module version
   */
  public String getModuleVersionGetter() {
    return moduleVersionGetter;
  }

  /**
   * Set method name for get module version
   * 
   * @param moduleVersionGetter
   *          method name for get module version
   */
  public void setModuleVersionGetter(String moduleVersionGetter) {
    this.moduleVersionGetter = moduleVersionGetter;
  }

  /**
   * Get last modified time of the project
   * 
   * @return last modified time
   */
  public LocalDateTime getLastModified() {
    return lastModified;
  }

  /**
   * Set last modified time of the project
   * 
   * @param lastModified
   *          last modified time
   */
  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public int hashCode() {
    return 31 + Objects.hashCode(getName());
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof XJNLPProject
        && Objects.equals(getName(), ((XJNLPProject) obj).getName()));
  }
}
