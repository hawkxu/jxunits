package win.zqxu.jxunits.jre;

import java.util.List;

/**
 * Utility functions for List objects
 * 
 * @author zqxu
 */
public class XListUtils {
  /**
   * Move the item at index in the list one step up
   * 
   * @param list
   *          the list
   * @param index
   *          the item index
   * @return true if the item moved
   */
  public static boolean moveUp(List<?> list, int index) {
    return move(list, index, -1);
  }

  /**
   * Move the item at index in the list one step down
   * 
   * @param list
   *          the list
   * @param index
   *          the item index
   * @return true if the item moved
   */
  public static boolean moveDown(List<?> list, int index) {
    return move(list, index, 1);
  }

  /**
   * move the item at index in the list by specified steps
   * 
   * @param list
   *          the list
   * @param index
   *          the item index
   * @param steps
   *          how many steps to move, positive number to move down, negative number to
   *          move up
   * @return true if the item moved
   */
  public static boolean move(List<?> list, int index, int steps) {
    if (index < 0 || index >= list.size()) return false;
    return swap(list, index, index + steps);
  }

  /**
   * swap two item in the list, returns false if index1=index2
   * 
   * @param <T> item value type of the list
   * @param list
   *          the list
   * @param index1
   *          one item index to swap
   * @param index2
   *          another item index to swap
   * @return true if two item index are valid and swapped
   */
  public static <T> boolean swap(List<T> list, int index1, int index2) {
    if (index1 < 0 || index1 >= list.size()
        || index2 < 0 || index2 >= list.size() || index1 == index2)
      return false;
    list.add(index2, list.remove(index1));
    return true;
  }
}
