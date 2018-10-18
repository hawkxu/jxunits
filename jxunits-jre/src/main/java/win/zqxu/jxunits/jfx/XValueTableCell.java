package win.zqxu.jxunits.jfx;

import javafx.application.Platform;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

/**
 * Represents table cell support using XPatternFormatter to format cell data and restrict
 * user input
 * 
 * @author zqxu
 */
public class XValueTableCell<S, T> extends XTableCell<S, T> {
  private XValueField<T> editor;

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
   * Constructor XValueTableCell with value field editor
   * 
   * @param editor
   *          the value field editor
   */
  public XValueTableCell(XValueField<T> editor) {
    super();
    this.editor = editor;
    this.getStyleClass().add("x-value-table-cell");
  }

  /**
   * register event listener for the editor
   */
  @Override
  protected XValueField<T> createEditor() {
    editor.setOnKeyReleased(evt -> {
      if (evt.getCode() == KeyCode.ESCAPE) {
        cancelEdit();
        evt.consume();
      }
    });
    editor.setOnAction(evt -> {
      editor.commitValue();
      commitEdit(editor.getValue());
      evt.consume();
    });
    editor.focusedProperty().addListener((v, o, n) -> {
      if (!n) commitWhenLostFocus(editor.getValue());
    });
    Platform.runLater(() -> editor.selectAll());
    return editor;
  }

  /**
   * get editor belong to the cell
   * 
   * @return editor
   */
  protected XValueField<T> getEditor() {
    return editor;
  }

  /**
   * update editor value
   */
  @Override
  protected void updateEditor(T item) {
    editor.setValue(item);
  }

  /**
   * convert item to text through editor
   */
  @Override
  protected String getItemText(T item) {
    return editor.convertToString(item);
  }
}
