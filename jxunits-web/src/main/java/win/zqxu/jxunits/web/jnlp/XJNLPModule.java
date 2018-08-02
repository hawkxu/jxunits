package win.zqxu.jxunits.web.jnlp;

import java.util.Objects;

public class XJNLPModule {
  private String className;
  private String name;
  private String version;

  /**
   * Default constructor
   */
  public XJNLPModule() {
    this(null);
  }

  /**
   * Constructor with module class name
   * 
   * @param className
   *          the module class name
   */
  public XJNLPModule(String className) {
    this.className = className;
  }

  /**
   * Get module class name
   * 
   * @return the module class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Set module class name
   * 
   * @param className
   *          the module class name
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Get module name
   * 
   * @return the module name
   */
  public String getName() {
    return name;
  }

  /**
   * Set module name
   * 
   * @param name
   *          the module name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get module version
   * 
   * @return the module version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set module version
   * 
   * @param version
   *          the module version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    return 29 + Objects.hashCode(getClassName());
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof XJNLPModule
        || Objects.equals(getClassName(), ((XJNLPModule) obj).getClassName()));
  }
}
