package win.zqxu.jxunits.jfx;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public abstract class XAbstractTextTableCell<S, T> extends TableCell<S, T> {
  private TextField textField;

  public XAbstractTextTableCell() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startEdit() {
    super.startEdit();
    if (!isEditing()) return;
    if (textField == null)
      initTextField();
    Platform.runLater(() -> {
      textField.selectAll();
      textField.requestFocus();
    });
    setText(null);
    setGraphic(textField);
    textField.setText(getCellText());
  }

  private void initTextField() {
    textField = getTextField();
    textField.setOnKeyReleased(evt -> {
      if (evt.getCode() == KeyCode.ESCAPE) {
        cancelEdit();
        evt.consume();
      }
    });
    textField.setOnAction(evt -> {
      commitEdit(getEditValue());
      evt.consume();
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setGraphic(null);
    setText(getCellText());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else if (isEditing()) {
      setText(null);
      setGraphic(textField);
      textField.setText(getCellText());
    } else {
      setGraphic(null);
      setText(getCellText());
    }
  }

  /**
   * Create text field for table cell, invoke once on start edit
   * 
   * @return Created text field
   */
  protected abstract TextField getTextField();

  /**
   * Get text for current cell value
   * 
   * @return cell text
   */
  protected abstract String getCellText();

  /**
   * Get value of current editing text field
   * 
   * @return edit value
   */
  protected abstract T getEditValue();
}
