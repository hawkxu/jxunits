package win.zqxu.jxunits.jfx;

import java.io.BufferedReader;
import java.io.StringReader;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import win.zqxu.jxunits.jre.XResource;

public class XRangeEditor<T extends Comparable<? super T>> {
  private ObservableList<XRangeItem<T>> ranges;
  private XPatternFormatter<T> formatter;
  private XValueProvider<T> provider;
  private boolean editable = true;
  private boolean interval = true;
  private XRangeOption fixedOption;
  private DialogPane dialogPane;
  private Node[] buttons;
  private TabPane tabPane;
  private TableView<XRangeItem<T>> includeTable;
  private TableView<XRangeItem<T>> excludeTable;

  public XRangeEditor() {
    this(null);
  }

  public XRangeEditor(ObservableList<XRangeItem<T>> ranges) {
    setRanges(ranges);
  }

  /**
   * Get ranges in editor
   * 
   * @return ranges
   */
  public ObservableList<XRangeItem<T>> getRanges() {
    return ranges;
  }

  /**
   * Set ranges to editor
   * 
   * @param ranges
   *          the ranges
   */
  public void setRanges(ObservableList<XRangeItem<T>> ranges) {
    this.ranges = ranges;
  }

  /**
   * get formatter used by this range editor
   * 
   * @return formatter
   */
  public XPatternFormatter<T> getFormatter() {
    return formatter;
  }

  /**
   * set formatter used by this range editor, the range editor will used a cloned copy of
   * this formatter
   * 
   * @param formatter
   *          the formatter
   */
  public void setFormatter(XPatternFormatter<T> formatter) {
    this.formatter = formatter;
  }

  /**
   * get provider used by this range editor, the range editor will used a cloned copy of
   * this formatter
   * 
   * @return provider
   */
  public XValueProvider<T> getProvider() {
    return provider;
  }

  /**
   * set provider used by this range editor, the range editor will used a cloned copy of
   * this provider
   * 
   * @param provider
   *          the provider
   */
  public void setProvider(XValueProvider<T> provider) {
    this.provider = provider;
  }

  /**
   * range editor is editable, default is true
   * 
   * @return true or false
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * range editor is editable, default is true
   * 
   * @param editable
   *          true or false
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /**
   * interval allowed, default is true
   * 
   * @return true or false
   */
  public boolean isInterval() {
    return interval;
  }

  /**
   * interval allowed, default is true
   * 
   * @param interval
   *          true or false
   */
  public void setInterval(boolean interval) {
    this.interval = interval;
  }

  /**
   * fixed option, default is null
   * 
   * @return fixed option
   */
  public XRangeOption getFixedOption() {
    return fixedOption;
  }

  /**
   * fixed option, default is null
   * 
   * @param fixedOption
   *          the fixed option
   */
  public void setFixedOption(XRangeOption fixedOption) {
    this.fixedOption = fixedOption;
  }

  /**
   * show modal dialog for this editor, return true if user confirmed
   * 
   * @param owner
   *          the owner of dialog
   * @param title
   *          dialog title, use default title for null
   * @return true or false
   */
  public boolean show(Node owner, String title) {
    if (dialogPane == null) dialogPane = createDialogPane();
    updateRangeEditor();
    if (title == null || title.isEmpty()) {
      if (isEditable())
        title = XResource.getString("RANGE.EDITOR");
      else
        title = XResource.getString("RANGE.VIEWER");
    }
    return XJfxUtils.showDialog(owner, dialogPane, title) == ButtonType.OK;
  }

  private DialogPane createDialogPane() {
    DialogPane dlpEditor = new DialogPane();
    // because the default JavaFX style added 10 pixels padding
    // for .graphic-container in dialog pane without header
    // it make the context menu item height abnormally
    // so set dialog pane header to a empty node to avoid this
    dlpEditor.setHeader(new ImageView());
    includeTable = createRangeTable();
    includeTable.getItems().addListener(
        (ListChangeListener<XRangeItem<T>>) c -> updateTabTitle(0));
    excludeTable = createRangeTable();
    excludeTable.getItems().addListener(
        (ListChangeListener<XRangeItem<T>>) c -> updateTabTitle(1));
    Tab includeTab = new Tab(null, new VBox(includeTable));
    Tab excludeTab = new Tab(null, new VBox(excludeTable));
    tabPane = new TabPane(includeTab, excludeTab);
    tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    updateTabTitle(0);
    updateTabTitle(1);
    dlpEditor.setContent(new VBox(6, createEditorBar(), tabPane));
    Control buttonBar = (Control) dlpEditor.lookup(".button-bar");
    buttonBar.setVisible(false);
    buttonBar.setPrefHeight(0);
    dlpEditor.getButtonTypes().add(ButtonType.OK);
    dlpEditor.getButtonTypes().add(ButtonType.CANCEL);
    return dlpEditor;
  }

  private void updateRangeEditor() {
    buttons[0].setDisable(!editable);
    buttons[3].setDisable(!editable);
    buttons[4].setDisable(!editable);
    buttons[5].setDisable(!editable || formatter == null);
    buttons[6].setDisable(!editable);
    updateRangeTable(includeTable, XRangeSign.I);
    updateRangeTable(excludeTable, XRangeSign.E);
  }

  private void updateRangeTable(TableView<XRangeItem<T>> table, XRangeSign sign) {
    table.setEditable(editable);
    table.getColumns().clear();
    table.getColumns().add(createOptionColumn());
    table.getColumns().add(createValueColumn("low"));
    if (isInterval2()) {
      table.getColumns().add(createValueColumn("high"));
    }
    table.getItems().clear();
    if (ranges == null || ranges.isEmpty()) return;
    for (XRangeItem<T> range : ranges)
      if (range.getSign() == sign) table.getItems().add(range.clone());
  }

  private TableView<XRangeItem<T>> createRangeTable() {
    TableView<XRangeItem<T>> table = new TableView<>();
    table.setPrefSize(500, 300);
    return table;
  }

  private void updateTabTitle(int index) {
    Tab tab = tabPane.getTabs().get(index);
    String key = index == 0 ? "RANGE.INCLUDES" : "RANGE.EXCLUDES";
    TableView<?> table = index == 0 ? includeTable : excludeTable;
    int size = table.getItems().size();
    tab.setText(XResource.getString(key) + "(" + size + ")");
  }

  private TableColumn<XRangeItem<T>, ?> createOptionColumn() {
    TableColumn<XRangeItem<T>, XRangeOption> column;
    column = createColumn("option");
    column.setEditable(fixedOption == null);
    column.setCellFactory(c -> new XRangeOptionTableCell<>());
    return column;
  }

  private TableColumn<XRangeItem<T>, ?> createValueColumn(String field) {
    TableColumn<XRangeItem<T>, T> column = createColumn(field);
    XValueField<T> editor = new XValueField<>();
    if (formatter != null)
      editor.setFormatter(formatter.clone());
    if (provider != null)
      editor.setProvider(provider.clone());
    column.setCellFactory(c -> new XValueTableCell<>(editor));
    return column;
  }

  @SuppressWarnings("deprecation")
  private <S, V> TableColumn<S, V> createColumn(String field) {
    TableColumn<S, V> column = new TableColumn<>();
    column = new TableColumn<>();
    column.impl_setReorderable(false);
    column.setPrefWidth(field.equals("option") ? 50 : 200);
    column.setSortable(false);
    column.setText(XResource.getString("RANGE.COLUMN." + field));
    column.setCellValueFactory(new PropertyValueFactory<>(field));
    return column;
  }

  private Node createEditorBar() {
    buttons = new Node[7];
    buttons[0] = createButton("OK", "accept.png");
    buttons[1] = createButton("CANCEL", "cross.png");
    buttons[2] = new Separator(Orientation.VERTICAL);
    buttons[3] = createButton("ADD", "table_add.png");
    buttons[4] = createButton("DELETE", "table_delete.png");
    buttons[5] = createButton("PASTE", "paste_plain.png");
    buttons[6] = createButton("CLEAR", "recycle.png");
    return new HBox(1, buttons);
  }

  private Button createButton(String id, String icon) {
    Button button = new Button();
    button.setId(id);
    button.setPadding(new Insets(3, 4, 4, 4));
    Image image = XImageLoader.get(icon);
    button.setGraphic(new ImageView(image));
    button.setOnAction(event -> handleEditorAction(event));
    return button;
  }

  private void handleEditorAction(ActionEvent event) {
    String id = ((Button) event.getSource()).getId();
    switch (id) {
    case "ADD":
      doAdd();
      break;
    case "DELETE":
      doDelete();
      break;
    case "PASTE":
      doPaste();
      break;
    case "CLEAR":
      doClear();
      break;
    case "OK":
      doConfirm();
      dialogPane.lookupButton(ButtonType.OK).fireEvent(new ActionEvent());
      break;
    default:
      dialogPane.lookupButton(ButtonType.CANCEL).fireEvent(new ActionEvent());
      break;
    }
  }

  private TableView<XRangeItem<T>> getActiveTable() {
    int tab = tabPane.getSelectionModel().getSelectedIndex();
    return tab == 0 ? includeTable : excludeTable;
  }

  private void doAdd() {
    TableView<XRangeItem<T>> table = getActiveTable();
    XRangeSign sign = table == includeTable ? XRangeSign.I : XRangeSign.E;
    XRangeOption option = fixedOption;
    if (option == null) option = XRangeOption.EQ;
    table.getItems().add(new XRangeItem<>(sign, option, null, null));
  }

  private void doDelete() {
    TableView<XRangeItem<T>> table = getActiveTable();
    int index = table.getSelectionModel().getSelectedIndex();
    if (index != -1) table.getItems().remove(index);
  }

  private void doPaste() {
    String string = Clipboard.getSystemClipboard().getString();
    if (string == null || string.isEmpty()) return;
    TableView<XRangeItem<T>> table = getActiveTable();
    XRangeSign sign = table == includeTable ? XRangeSign.I : XRangeSign.E;
    StringConverter<T> converter = null;
    // has checked the formatter not be null before
    converter = formatter.getValueConverter();
    BufferedReader reader = new BufferedReader(new StringReader(string));
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) continue;
        try {
          T low = null, high = null;
          String[] parts = line.split("\t");
          parts[0] = parts[0].trim();
          parts[0] = formatter.apply(parts[0]);
          low = converter.fromString(parts[0]);
          if (isInterval2() && parts.length > 1 && !parts[1].isEmpty()) {
            parts[1] = parts[1].trim();
            parts[1] = formatter.apply(parts[1]);
            high = converter.fromString(parts[1]);
          }
          XRangeOption option = fixedOption;
          XRangeItem<T> range = new XRangeItem<>(sign, option, low, high);
          if (!range.isEmpty()) table.getItems().add(range);
        } catch (Exception ex) {
          // safely ignored any exception
        }
      }
      reader.close();
    } catch (Exception ex) {
      // safely ignored any exception
    }
  }

  private boolean isInterval2() {
    return interval && fixedOption == null;
  }

  private void doClear() {
    getActiveTable().getItems().clear();
  }

  private void doConfirm() {
    ranges = FXCollections.observableArrayList();
    for (XRangeItem<T> range : includeTable.getItems())
      if (!XRangeItem.isEmpty(range)) ranges.add(range);
    for (XRangeItem<T> range : excludeTable.getItems())
      if (!XRangeItem.isEmpty(range)) ranges.add(range);
  }

  private static class XRangeOptionTableCell<T extends Comparable<? super T>>
      extends TableCell<XRangeItem<T>, XRangeOption> {
    private XRangeOptionMenu optionMenu;
    private ImageView optionImage;

    private XRangeOptionTableCell() {
      setAlignment(Pos.CENTER);
      setOnMouseClicked(event -> handleMouseClicked(event));
    }

    @SuppressWarnings("unchecked")
    private XRangeItem<T> getRange() {
      return (XRangeItem<T>) getTableRow().getItem();
    }

    private void handleMouseClicked(MouseEvent event) {
      if (!isColumnEditable()) return;
      if (event.getButton().equals(MouseButton.PRIMARY)) {
        XRangeItem<T> range = getRange();
        if (range == null) return;
        if (optionMenu == null) {
          optionMenu = new XRangeOptionMenu();
          optionMenu.setOnAction(evt -> handleOptionMenu(evt));
        }
        optionMenu.show(optionImage, range);
      }
    }

    private boolean isColumnEditable() {
      return getTableView().isEditable() && getTableColumn().isEditable();
    }

    private void handleOptionMenu(ActionEvent event) {
      String menuId = ((MenuItem) event.getTarget()).getId();
      if (menuId.equals("RM"))
        getTableView().getItems().remove(getIndex());
      else
        getRange().setOption(XRangeOption.valueOf(menuId));
    }

    @Override
    protected void updateItem(XRangeOption item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(getOptionImage(item));
    }

    private Node getOptionImage(XRangeOption item) {
      if (optionImage == null) optionImage = new ImageView();
      XRangeItem<T> range = getRange();
      XRangeSign sign = range == null ? null : range.getSign();
      optionImage.setImage(XRangeItem.getOptionImage(sign, item));
      return optionImage;
    }
  }
}
