package win.zqxu.jxunits.jfx;

import win.zqxu.jxunits.jfx.XSummaryComber.XSummaryOrder;
import win.zqxu.jxunits.jfx.XSummaryComber.XSummarySummer;

/**
 * Interface for wrap source or summary item, for summary item has two types: subtotal or
 * total
 * 
 * @author zqxu
 */
public interface XSummaryItem<S> {
  /**
   * Get the source index if currently wrapped a source item
   * 
   * @return the source index
   */
  public int getSourceIndex();

  /**
   * Get the source item if currently wrapped a source item.
   * 
   * @return the source item
   */
  public S getSourceItem();

  /**
   * Determine whether currently wrapped a summary item
   * 
   * @return true if currently wrapped a summary item, otherwise false
   */
  public boolean isSummary();

  /**
   * Determine whether currently wrapped summary item is subtotal
   * 
   * @return true if currently wrapped summary item is subtotal
   */
  public boolean isSubtotal();

  /**
   * Get summary value for the order
   * 
   * @param <T>
   *          the value type
   * @param order
   *          the order
   * @return summary value
   */
  public <T> T getSummaryValue(XSummaryOrder<S, T> order);

  /**
   * Get summary value for the summer
   * 
   * @param <T>
   *          the value type
   * @param summer
   *          the summer
   * @return summary value
   */
  public <T> T getSummaryValue(XSummarySummer<S, T> summer);
}
