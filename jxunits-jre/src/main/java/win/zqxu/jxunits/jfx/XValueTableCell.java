package win.zqxu.jxunits.jfx;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Represents table cell support using XPatternFormatter to format cell data and restrict
 * user input
 * 
 * @author zqxu
 */
public class XValueTableCell<S, T> extends XAbstractTextTableCell<S, T> {
  private XValueField<T> field;

  /**
   * Constructor XValueTableCell with formatter
   * 
   * @param formatter
   *          the text formatter
   */
  public XValueTableCell(TextFormatter<T> formatter) {
    this(new XValueField<>(formatter, null));
  }

  /**
   * Constructor XValueTableCell with provider
   * 
   * @param provider
   *          the value provider
   */
  public XValueTableCell(XValueProvider<T> provider) {
    this(new XValueField<>(null, provider));
  }

  /**
   * Constructor XValueTableCell with formatter and provider
   * 
   * @param formatter
   *          the text formatter
   * @param provider
   *          the value provider
   */
  public XValueTableCell(TextFormatter<T> formatter, XValueProvider<T> provider) {
    this(new XValueField<>(formatter, provider));
  }

  /**
   * Constructor XValueTableCell with value field
   * 
   * @param field
   *          the value field
   */
  public XValueTableCell(XValueField<T> field) {
    super();
    this.field = field;
  }

  /**
   * Get value field used by this table cell
   * 
   * @return value field
   */
  public XValueField<T> getValueField() {
    return field;
  }

  @Override
  protected TextField getTextField() {
    return field;
  }

  @Override
  public void startEdit() {
    super.startEdit();
    field.setValue(getItem());
  }

  @Override
  protected String getCellText() {
    return field.convertToString(getItem());
  }

  @Override
  protected T getEditValue() {
    field.commitValue();
    return field.getValue();
  }
}
