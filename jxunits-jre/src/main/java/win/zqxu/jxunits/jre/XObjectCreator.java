package win.zqxu.jxunits.jre;

/**
 * Object creator
 * 
 * @author zqxu
 */
@FunctionalInterface
public interface XObjectCreator {
  /**
   * Create an instance for the type
   * 
   * @param <T>
   *          the object type
   * @param type
   *          the type
   * @return A new instance of the type
   * @throws ReflectiveOperationException
   *           if can not create instance for the type
   */
  public <T> T create(Class<T> type) throws ReflectiveOperationException;
}
