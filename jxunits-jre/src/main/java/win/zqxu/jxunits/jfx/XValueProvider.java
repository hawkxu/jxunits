package win.zqxu.jxunits.jfx;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import com.sun.javafx.scene.control.skin.DatePickerSkin;
import com.sun.javafx.util.Utils;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 * Base class of value provider, the implementation classes should use getValue and
 * commitValue method in this base class instead of the value field
 * 
 * @author zqxu
 */
@SuppressWarnings("restriction")
public abstract class XValueProvider<T> {
  /**
   * Create value provider for select value from items
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items for select
   * @return value provider
   */
  @SuppressWarnings("unchecked")
  public static <T> XValueProvider<T> LIST(T... items) {
    return LIST(FXCollections.observableArrayList(items));
  }

  /**
   * Create value provider for select value from items
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items for select
   * @return value provider
   */
  public static <T> XValueProvider<T> LIST(List<T> items) {
    return LIST(FXCollections.observableList(items));
  }

  /**
   * Create value provider for select value from items
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items
   * @return value provider
   */
  public static <T> XValueProvider<T> LIST(ObservableList<T> items) {
    return new XListValueProvider<>(XImageLoader.get("pickdown.png"), items);
  }

  /**
   * Create value provider for select key value from map
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items
   * @return value provider
   */
  public static <T> XValueProvider<T> KEYS(Map<T, String> items) {
    return KEYS(FXCollections.observableMap(items));
  }

  /**
   * Create value provider for select key value from map
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items
   * @return value provider
   */
  public static <T> XValueProvider<T> KEYS(ObservableMap<T, String> items) {
    return new XKeyValueProvider<>(XImageLoader.get("pickdown.png"), items);
  }

  /**
   * Create value provider for select key value from items
   * 
   * @param <T>
   *          the value type
   * @param items
   *          the items
   * @return value provider
   */
  public static <T> XValueProvider<T> KEYS(ObservableList<Entry<T, String>> items) {
    return new XKeyValueProvider<>(XImageLoader.get("pickdown.png"), items);
  }

  /**
   * Create value provider for select date value
   * 
   * @return value provider
   */
  public static XValueProvider<LocalDate> DATE() {
    return new XDateValueProvider(XImageLoader.get("pickdown.png"));
  }

  /**
   * Create value provider for get value from the picker
   * 
   * @param <T>
   *          the value type
   * @param picker
   *          the picker
   * @return value provider
   */
  public static <T> XValueProvider<T> PICK(Callback<T, T> picker) {
    return new XPickValueProvider<>(XImageLoader.get("pickup.png"), picker);
  }

  /**
   * Create a free value provider just call the consumer
   * 
   * @param <T>
   *          the value type
   * @param consumer
   *          the consumer to call
   * @return value provider
   */
  public static <T> XValueProvider<T> FREE(Consumer<XValueField<T>> consumer) {
    return new XFreeValueProvider<>(XImageLoader.get("pickup.png"), consumer);
  }

  /** the value field this provider bind to */
  protected XValueField<T> field;

  /**
   * get node of this provider to add to value field
   * 
   * @return the provider node
   */
  protected abstract Node getNode();

  /**
   * bind this value provider to a value field
   * 
   * @param field
   *          the value field
   */
  public void bindToField(XValueField<T> field) {
    if (this.field != null) {
      throw new IllegalStateException("Provider is already used in other control");
    }
    this.field = field;
  }

  /**
   * unbound this value provider from the value field previously bound to
   */
  public void unbindFromField() {
    this.field = null;
  }

  /**
   * Get editing value from field
   * 
   * @return editing value
   */
  protected T getEditingValue() {
    try {
      return field.convertToValue(field.getText());
    } catch (Exception ex) {
    }
    return field.getValue();
  }

  /**
   * Commit provider value to field
   * 
   * @param value
   *          the value to commit
   */
  protected void commitProviderValue(T value) {
    field.setValue(value);
    field.cancelEdit(); // make sure text match value
  }

  /**
   * invoke value provider
   */
  public abstract void invoke();

  /**
   * Clone value provider
   */
  @Override
  public abstract XValueProvider<T> clone();

  private static class LabelButton extends Label {
    public LabelButton(Image image) {
      super(null, new ImageView(image));
      setCursor(Cursor.DEFAULT);
      hoverProperty().addListener(evt -> updateShadow());
      pressedProperty().addListener(evt -> updateShadow());
      disabledProperty().addListener(evt -> updateOpacity());
    }

    private void updateShadow() {
      if (isPressed())
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX,
            new Color(0, 0, 0, 0.9), 8, 0, 0, 0));
      else if (isHover())
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX,
            new Color(0, 0, 0, 0.5), 8, 0, 0, 0));
      else
        setEffect(null);
    }

    private void updateOpacity() {
      setOpacity(isDisabled() ? 0.5 : 1);
    }
  }

  /**
   * Base provider class shows a button with icon in value field
   * 
   * @author zqxu
   */
  public static abstract class XAbstractValueProvider<T> extends XValueProvider<T> {
    protected final Image icon;
    private LabelButton button;

    public XAbstractValueProvider(Image icon) {
      this.icon = icon;
      button = new LabelButton(icon);
      button.setOnMouseClicked(event -> invokeProvider());
    }

    @Override
    public Node getNode() {
      return button;
    }

    @Override
    public void invoke() {
      if (field != null && field.isEditable() && !field.isDisabled()) invokeProvider();
    }

    /**
     * invoke value provider
     */
    protected abstract void invokeProvider();
  }

  /**
   * Base provider class shows pop up when button clicked
   * 
   * @author zqxu
   */
  public static abstract class XPopupValueProvider<T> extends XAbstractValueProvider<T> {
    private PopupControl popup;

    public XPopupValueProvider(Image icon) {
      super(icon);
    }

    private PopupControl getPopup() {
      if (popup == null) {
        popup = new PopupControl();
        popup.getStyleClass().add("combo-box-popup");
        popup.setAutoHide(true);
        popup.setOnShown(event -> handleShown());
      }
      return popup;
    }

    protected boolean isShowing() {
      return popup.isShowing();
    }

    protected void hide() {
      PopupControl _popup = getPopup();
      if (_popup != null) _popup.hide();
    }

    @Override
    protected void invokeProvider() {
      PopupControl _popup = getPopup();
      _popup.getStyleClass().add("combo-box-popup");
      final Node content = getPopupContent();
      _popup.setSkin(new Skin<Skinnable>() {
        @Override
        public Skinnable getSkinnable() {
          return field;
        }

        @Override
        public Node getNode() {
          return content;
        }

        @Override
        public void dispose() {
        }
      });
      Point2D position = Utils.pointRelativeTo(field, content,
          HPos.CENTER, VPos.BOTTOM, 0, 0, true);
      _popup.show(field.getScene().getWindow(), position.getX(), position.getY());
    }

    protected void handleShown() {
    }

    protected abstract Node getPopupContent();
  }

  /**
   * A provider shows a pop up list view to select value
   * 
   * @author zqxu
   */
  public static class XListValueProvider<T> extends XPopupValueProvider<T> {
    private ListProperty<T> items = new SimpleListProperty<>();
    private ListView<T> itemsList;

    public XListValueProvider(Image icon) {
      this(icon, FXCollections.observableArrayList());
    }

    public XListValueProvider(Image icon, ObservableList<T> items) {
      super(icon);
      setItems(items);
    }

    /**
     * items property
     * 
     * @return items property
     */
    public final ListProperty<T> itemsProperty() {
      return items;
    }

    /**
     * get items to select
     * 
     * @return items to select
     */
    public ObservableList<T> getItems() {
      return itemsProperty().get();
    }

    /**
     * set items to select
     * 
     * @param items
     *          items to select
     */
    public void setItems(ObservableList<T> items) {
      itemsProperty().set(items);
    }

    private ListView<T> getListView() {
      if (itemsList == null) {
        itemsList = new ListView<>();
        itemsList.itemsProperty().bind(itemsProperty());
        itemsList.getSelectionModel().selectedItemProperty()
            .addListener((v, o, n) -> handleSelected());
      }
      return itemsList;
    }

    @Override
    protected Node getPopupContent() {
      ListView<T> listView = getListView();
      int index = itemsProperty().indexOf(getEditingValue());
      listView.scrollTo(index);
      if (index != -1)
        listView.getSelectionModel().select(index);
      else
        listView.getSelectionModel().clearSelection();
      return listView;
    }

    @Override
    protected void handleShown() {
      itemsList.setPrefWidth(field.getWidth());
      Node cell = itemsList.lookup(".list-cell");
      double height = cell.prefHeight(-1);
      int rows = Math.min(8, getItems().size());
      Insets insets = itemsList.getInsets();
      itemsList.setPrefHeight(height * rows
          + insets.getTop() + insets.getBottom());
    }

    private void handleSelected() {
      if (isShowing()) {
        hide();
        commitProviderValue(itemsList.getSelectionModel().getSelectedItem());
      }
    }

    @Override
    public XListValueProvider<T> clone() {
      return new XListValueProvider<>(icon, items);
    }
  }

  /**
   * A provider shows a pop up table view with key-names to select value
   * 
   * @author zqxu
   */
  public static class XKeyValueProvider<T> extends XPopupValueProvider<T> {
    private ListProperty<Entry<T, String>> items = new SimpleListProperty<>();
    private BooleanProperty showNameOnly = new SimpleBooleanProperty();
    private TableView<Entry<T, String>> itemsTable;

    public XKeyValueProvider(Image icon) {
      this(icon, FXCollections.observableHashMap());
    }

    public XKeyValueProvider(Image icon, ObservableList<Entry<T, String>> items) {
      super(icon);
      setItems(items);
    }

    public XKeyValueProvider(Image icon, ObservableMap<T, String> items) {
      super(icon);
      setItems(items);
    }

    /**
     * items property
     * 
     * @return items property
     */
    public final ListProperty<Entry<T, String>> itemsProperty() {
      return items;
    }

    /**
     * Get items to select
     * 
     * @return items to select
     */
    public ObservableList<Entry<T, String>> getItems() {
      return itemsProperty().get();
    }

    /**
     * Set items to select
     * 
     * @param items
     *          items to select
     */
    public void setItems(ObservableList<Entry<T, String>> items) {
      itemsProperty().set(items);
    }

    /**
     * Set items through ObservableMap object
     * 
     * @param items
     *          items to select
     */
    public void setItems(ObservableMap<T, String> items) {
      setItems(XJfxUtils.observableMapList(items));
    }

    /**
     * show name only property
     * 
     * @return show name only property
     */
    public final BooleanProperty showNameOnlyProperty() {
      return showNameOnly;
    }

    /**
     * Whether show name only, default is false
     * 
     * @return true or false
     */
    public boolean isShowNameOnly() {
      return showNameOnlyProperty().get();
    }

    /**
     * Whether show name only, default is false
     * 
     * @param showNameOnly
     *          true or false
     */
    public void setShowNameOnly(boolean showNameOnly) {
      showNameOnlyProperty().set(showNameOnly);
    }

    @Override
    protected Node getPopupContent() {
      TableView<Entry<T, String>> tableView = getTableView();
      int index = indexOf(getEditingValue());
      tableView.scrollTo(index);
      if (index != -1)
        tableView.getSelectionModel().select(index);
      else
        tableView.getSelectionModel().clearSelection();
      return tableView;
    }

    private TableView<Entry<T, String>> getTableView() {
      if (itemsTable == null) {
        itemsTable = new TableView<>();
        itemsTable.itemsProperty().bind(itemsProperty());
        TableColumn<Entry<T, String>, String> keyColumn = new TableColumn<>();
        keyColumn.setCellValueFactory(data -> {
          T value = data.getValue().getKey();
          return new SimpleStringProperty(field.convertToString(value));
        });
        keyColumn.visibleProperty().bind(showNameOnlyProperty().not());
        itemsTable.getColumns().add(keyColumn);
        TableColumn<Entry<T, String>, String> nameColumn = new TableColumn<>();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        itemsTable.getColumns().add(nameColumn);
        itemsTable.getSelectionModel().selectedItemProperty()
            .addListener((v, o, n) -> handleSelected());
        itemsTable.setMinWidth(field.getWidth());
        itemsTable.setMaxWidth(field.getWidth() * 2);
      }
      return itemsTable;
    }

    private int indexOf(T value) {
      int index = -1;
      for (Entry<T, String> item : itemsProperty()) {
        index++;
        if (Objects.equals(value, item.getKey())) return index;
      }
      return -1;
    }

    @Override
    protected void handleShown() {
      Region header = (Region) itemsTable.lookup("TableHeaderRow");
      header.setPrefHeight(0);
      header.setVisible(false);
      XJfxUtils.resizeColumnToFitContent(itemsTable.getColumns().get(0));
      XJfxUtils.resizeColumnToFitContent(itemsTable.getColumns().get(1));
      Insets insets = itemsTable.getInsets();
      double prefWidth = insets.getLeft() + insets.getRight();
      for (TableColumn<?, ?> column : itemsTable.getColumns()) {
        if (column.isVisible()) prefWidth += column.getWidth();
      }
      itemsTable.setPrefWidth(prefWidth);
      TableRow<?> tableRow = (TableRow<?>) itemsTable.lookup("TableRow");
      double rowHeight = tableRow.getHeight();
      int rows = Math.min(8, itemsTable.getItems().size());
      double insetsHeight = insets.getTop() + insets.getBottom();
      itemsTable.setPrefHeight(rowHeight * rows + insetsHeight);
      Platform.runLater(() -> {
        XJfxUtils.resizeColumnToFitTable(itemsTable.getColumns().get(1));
      });
    }

    private void handleSelected() {
      if (isShowing()) {
        hide();
        commitProviderValue(itemsTable.getSelectionModel().getSelectedItem().getKey());
      }
    }

    @Override
    public XKeyValueProvider<T> clone() {
      return new XKeyValueProvider<>(icon, items);
    }
  }

  public static class XDateValueProvider extends XPopupValueProvider<LocalDate> {
    private DatePicker picker = new DatePicker();
    private Node pane;

    public XDateValueProvider(Image icon) {
      super(icon);
    }

    @Override
    protected Node getPopupContent() {
      if (pane == null) {
        DatePickerSkin skin = new DatePickerSkin(picker);
        pane = skin.getPopupContent();
        picker.valueProperty().addListener((v, o, n) -> handleSelected());
      }
      picker.setValue(getEditingValue());
      return pane;
    }

    private void handleSelected() {
      if (isShowing()) {
        hide();
        commitProviderValue(picker.getValue());
      }
    }

    @Override
    public XDateValueProvider clone() {
      return new XDateValueProvider(icon);
    }
  }

  /**
   * pick value provider, set field value if the picker returns a non-null value
   * 
   * @author zqxu
   */
  public static class XPickValueProvider<T> extends XAbstractValueProvider<T> {
    private final Callback<T, T> picker;

    public XPickValueProvider(Image icon, Callback<T, T> picker) {
      super(icon);
      this.picker = picker;
    }

    @Override
    protected void invokeProvider() {
      T value = picker.call(getEditingValue());
      if (value != null) commitProviderValue(value);
    }

    @Override
    public XPickValueProvider<T> clone() {
      return new XPickValueProvider<>(icon, picker);
    }
  }

  /**
   * Free value provider, just call the consumer
   * 
   * @author zqxu
   */
  public static class XFreeValueProvider<T> extends XAbstractValueProvider<T> {
    private final Consumer<XValueField<T>> consumer;

    public XFreeValueProvider(Image icon, Consumer<XValueField<T>> consumer) {
      super(icon);
      this.consumer = consumer;
    }

    @Override
    protected void invokeProvider() {
      consumer.accept(field);
    }

    @Override
    public XFreeValueProvider<T> clone() {
      return new XFreeValueProvider<>(icon, consumer);
    }
  }
}
