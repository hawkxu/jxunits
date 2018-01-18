package win.zqxu.jxunits.jfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class XRangeField<T extends Comparable<? super T>> extends Control {
  private BooleanProperty editable = new SimpleBooleanProperty();
  private ObjectProperty<XPatternFormatter<T>> formatter = new SimpleObjectProperty<>();
  private ListProperty<XRangeItem<T>> items = new SimpleListProperty<>();
  private BooleanProperty autoTrim = new SimpleBooleanProperty();
  private ObjectProperty<XValueProvider<T>> provider = new SimpleObjectProperty<>();
  private IntegerProperty prefColumnCount = new SimpleIntegerProperty(12);

  public XRangeField() {
    this(null, null);
  }

  public XRangeField(XPatternFormatter<T> formatter) {
    this(formatter, null);
  }

  public XRangeField(XValueProvider<T> provider) {
    this(null, provider);
  }

  public XRangeField(XPatternFormatter<T> formatter, XValueProvider<T> provider) {
    setFormatter(formatter);
    setProvider(provider);
    setEditable(true);
    setAutoTrim(true);
    setItems(FXCollections.observableArrayList());
  }

  /**
   * editable property
   * 
   * @return editable property
   */
  public final BooleanProperty editableProperty() {
    return editable;
  }

  /**
   * Determine whether the field editable
   * 
   * @return true or false
   */
  public boolean isEditable() {
    return editableProperty().get();
  }

  /**
   * Set whether the field editable
   * 
   * @param editable
   *          true or false
   */
  public void setEditable(boolean editable) {
    editableProperty().set(editable);
  }

  /**
   * formatter property
   * 
   * @return formatter property
   */
  public final ObjectProperty<XPatternFormatter<T>> formatterProperty() {
    return formatter;
  }

  /**
   * Get formatter used in field
   * 
   * @return formatter
   */
  public XPatternFormatter<T> getFormatter() {
    return formatterProperty().get();
  }

  /**
   * Set formatter used in field
   * 
   * @param formatter
   *          the formatter
   */
  public void setFormatter(XPatternFormatter<T> formatter) {
    formatterProperty().set(formatter);
  }

  /**
   * items property
   * 
   * @return items property
   */
  public final ListProperty<XRangeItem<T>> itemsProperty() {
    return items;
  }

  /**
   * Get items
   * 
   * @return items
   */
  public ObservableList<XRangeItem<T>> getItems() {
    return itemsProperty().get();
  }

  /**
   * Set items
   * 
   * @param items
   *          the items
   */
  public void setItems(ObservableList<XRangeItem<T>> items) {
    itemsProperty().set(items);
  }

  /**
   * auto trim property
   * 
   * @return auto trim property
   */
  public final BooleanProperty autoTrimProperty() {
    return autoTrim;
  }

  /**
   * Determine whether trim input text automatically
   * 
   * @return true or false
   */
  public boolean isAutoTrim() {
    return autoTrimProperty().get();
  }

  /**
   * Set whether trim input text automatically
   * 
   * @param autoTrim
   *          true or false
   */
  public void setAutoTrim(boolean autoTrim) {
    autoTrimProperty().set(autoTrim);
  }

  /**
   * provider property
   * 
   * @return provider property
   */
  public final ObjectProperty<XValueProvider<T>> providerProperty() {
    return provider;
  }

  /**
   * Get value provider used in field
   * 
   * @return provider
   */
  public XValueProvider<T> getProvider() {
    return providerProperty().get();
  }

  /**
   * Set value provider used in field
   * 
   * @param provider
   *          the value provider
   */
  public void setProvider(XValueProvider<T> provider) {
    providerProperty().set(provider);
  }

  /**
   * preferred number of text columns
   * 
   * @return prefColumnCount property
   */
  public final IntegerProperty prefColumnCountProperty() {
    return prefColumnCount;
  }

  /**
   * preferred number of text columns
   * 
   * @return preferred column count
   */
  public int getPrefColumnCount() {
    return prefColumnCountProperty().get();
  }

  /**
   * preferred number of text columns
   * 
   * @param prefColumnCount
   *          preferred column count
   */
  public void setPrefColumnCount(int prefColumnCount) {
    prefColumnCountProperty().set(prefColumnCount);
  }

  /**
   * Get first range of this field, null if items is empty
   * 
   * @return first range or null
   */
  public XRangeItem<T> getFirstRange() {
    return itemsProperty().isEmpty() ? null : itemsProperty().get(0);
  }

  /**
   * Set first range of the field, add the range if items is empty currently
   * 
   * @param range
   *          the range
   */
  public void setFirstRange(XRangeItem<T> range) {
    if (itemsProperty().isEmpty())
      itemsProperty().add(range);
    else
      itemsProperty().set(0, range);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new XRangeFieldSkin<>(this);
  }
}
