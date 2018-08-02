package win.zqxu.jxunits.web.jnlp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * JNLP library descriptor
 * 
 * @author zqxu
 */
public class XJNLPLibrary {
  private String name;
  private int sequence;
  private String version;
  private boolean disabled;
  private int fileSize;
  private String fileHash;
  private LocalDateTime lastModified;
  private List<XJNLPModule> modules;

  /**
   * Default constructor
   */
  public XJNLPLibrary() {
    this(null);
  }

  /**
   * Constructor with library name
   * 
   * @param name
   *          the library name
   */
  public XJNLPLibrary(String name) {
    this.name = name;
  }

  /**
   * Get library name
   * 
   * @return the library name
   */
  public String getName() {
    return name;
  }

  /**
   * Set library name
   * 
   * @param name
   *          the library name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get library sequence in project
   * 
   * @return library sequence in project
   */
  public int getSequence() {
    return sequence;
  }

  /**
   * Set library sequence in project
   * 
   * @param sequence
   *          library sequence in project
   */
  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  /**
   * Get library version
   * 
   * @return the library version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set library version
   * 
   * @param version
   *          the library version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Get whether the library is disabled
   * 
   * @return true for disabled or false not
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Set whether the library is disabled
   * 
   * @param disabled
   *          true for disabled or false not
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * Get library file size in bytes
   * 
   * @return library file size in bytes
   */
  public int getFileSize() {
    return fileSize;
  }

  /**
   * Set library file size in bytes
   * 
   * @param fileSize
   *          library file size in bytes
   */
  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }

  /**
   * Get hash code for the library file
   * 
   * @return hash code for the library file
   */
  public String getFileHash() {
    return fileHash;
  }

  /**
   * Get hash code for the library file
   * 
   * @param fileHash
   *          hash code for the library file
   */
  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  /**
   * Get last modified time of the library jar file
   * 
   * @return last modified time
   */
  public LocalDateTime getLastModified() {
    return lastModified;
  }

  /**
   * Set last modified time of the library jar file
   * 
   * @param lastModified
   *          last modified time
   */
  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * get modules in this library
   * 
   * @return modules
   */
  public List<XJNLPModule> getModules() {
    return modules;
  }

  /**
   * set modules in this library
   * 
   * @param modules
   *          modules
   */
  public void setModules(List<XJNLPModule> modules) {
    this.modules = modules;
  }

  @Override
  public int hashCode() {
    return 25 + Objects.hashCode(getName());
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof XJNLPLibrary
        && Objects.equals(getName(), ((XJNLPLibrary) obj).getName()));
  }
}