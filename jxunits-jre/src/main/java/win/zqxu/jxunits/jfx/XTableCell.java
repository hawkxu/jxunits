package win.zqxu.jxunits.jfx;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;

public class XTableCell<S, T> extends TableCell<S, T> {
  private Node editor;

  public XTableCell() {
    this.getStyleClass().add("x-table-cell");
  }

  /**
   * Get row item, null if no table row associated
   * 
   * @return row item
   */
  public S getRowItem() {
    @SuppressWarnings("unchecked")
    TableRow<S> row = getTableRow();
    return row == null ? null : row.getItem();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startEdit() {
    super.startEdit();
    if (!isEditing()) return;
    if (editor == null)
      editor = createEditor();
    if (editor == null) {
      super.cancelEdit();
      return;
    }
    setText(null);
    setGraphic(editor);
    updateEditor(getItem());
    Platform.runLater(() -> {
      editor.requestFocus();
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setGraphic(null);
    setText(getItemText(getItem()));
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
      setGraphic(editor);
      updateEditor(getItem());
    } else {
      setGraphic(null);
      setText(getItemText(item));
    }
  }

  /**
   * create editor for the cell, the implemented class should add listener to invoke
   * commitEdit and cancelEdit here. this method will be called only once. default returns
   * null.
   * 
   * @return created editor
   */
  protected Node createEditor() {
    return null;
  }

  /**
   * update editor value with cell item. default do nothing
   * 
   * @param item
   *          the cell item
   */
  protected void updateEditor(T item) {
  }

  /**
   * get display text for the cell item, the implemented class should overwrite this
   * method to provide formatted text
   * 
   * @param item
   *          the cell item
   * @return display text for the cell item
   */
  protected String getItemText(T item) {
    return item == null ? null : item.toString();
  }

  /**
   * Used to commit when the editor lost focus
   * 
   * @param newValue
   *          the new value
   */
  protected void commitWhenLostFocus(T newValue) {
    TableView<S> table = getTableView();
    TableRow<?> row = getTableRow();
    TableColumn<S, T> column = getTableColumn();
    if (table != null && row != null && column != null) {
      CellEditEvent<S, T> editEvent = new CellEditEvent<>(table,
          new TablePosition<>(table, row.getIndex(), column),
          TableColumn.editCommitEvent(), newValue);
      Event.fireEvent(column, editEvent);
      table.edit(-1, null);
    }
  }

  @Override
  public String getUserAgentStylesheet() {
    return XTableCell.class.getResource("x-styles.css").toExternalForm();
  }
}
