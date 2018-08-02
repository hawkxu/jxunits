package win.zqxu.jxunits.web.jnlp;

/**
 * Library descriptor for deploy
 * 
 * @author zqxu
 */
public class XJNLPLibraryDeploy extends XJNLPLibrary {
  private boolean major;
  private boolean deleted;

  public XJNLPLibraryDeploy() {
  }

  public XJNLPLibraryDeploy(XJNLPLibrary library) {
    if (library != null) {
      setName(library.getName());
      setSequence(library.getSequence());
      setVersion(library.getVersion());
      setDisabled(library.isDisabled());
      setFileSize(library.getFileSize());
      setFileHash(library.getFileHash());
      setLastModified(library.getLastModified());
      setModules(library.getModules());
    }
  }

  /**
   * Get whether the library is main jar of the project
   * 
   * @return true or false
   */
  public boolean isMajor() {
    return major;
  }

  /**
   * Set whether the library is main jar of the project
   * 
   * @param major
   *          true or false
   */
  public void setMajor(boolean major) {
    this.major = major;
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
}
