package win.zqxu.jxunits.jfx;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;

/**
 * apply pseudo class 'summary' to summary row.
 * 
 * @author zqxu
 */
public class XSummaryTableRow<S> extends TableRow<XSummaryItem<S>> {
  private static final PseudoClass SUMMARY = PseudoClass.getPseudoClass("summary");

  @Override
  protected void updateItem(XSummaryItem<S> item, boolean empty) {
    super.updateItem(item, empty);
    pseudoClassStateChanged(SUMMARY, item != null && item.isSummary());
  }
}
