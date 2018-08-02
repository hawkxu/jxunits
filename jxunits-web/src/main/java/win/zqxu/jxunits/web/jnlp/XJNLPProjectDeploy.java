package win.zqxu.jxunits.web.jnlp;

/**
 * Project descriptor for deploy
 * 
 * @author zqxu
 */
public class XJNLPProjectDeploy extends XJNLPProject {
  private boolean initial;
  private boolean deleted;
  private boolean deployAllowed;

  public XJNLPProjectDeploy() {
    // keep initial false
  }

  public XJNLPProjectDeploy(XJNLPProject project) {
    initial = project == null;
    if (project != null) {
      setName(project.getName());
      setVersion(project.getVersion());
      setTitle(project.getTitle());
      setDisabled(project.isDisabled());
      setModuleBaseClass(project.getModuleBaseClass());
      setModuleNameGetter(project.getModuleNameGetter());
      setModuleVersionGetter(project.getModuleVersionGetter());
      setLastModified(project.getLastModified());
    }
  }

  /**
   * Get initial indicator
   * 
   * @return true or false
   */
  public boolean isInitial() {
    return initial;
  }

  /**
   * Do nothing to prevent JSON deserialization
   * 
   * @param initial
   *          true or false
   */
  public void setInitial(boolean initial) {
  }

  /**
   * Get deletion indicator
   * 
   * @return true or false
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * Set deletion indicator
   * 
   * @param deleted
   *          true or false
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * Get whether deploy to this project is allowed
   * 
   * @return true or false
   */
  public boolean isDeployAllowed() {
    return deployAllowed;
  }

  /**
   * Set whether deploy to this project is allowed
   * 
   * @param deployAllowed
   *          true or false
   */
  public void setDeployAllowed(boolean deployAllowed) {
    this.deployAllowed = deployAllowed;
  }
}
