package win.zqxu.jxunits.jfx;

import java.security.InvalidParameterException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
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
  private BooleanProperty interval = new SimpleBooleanProperty(true);
  private BooleanProperty multiple = new SimpleBooleanProperty(true);
  private ObjectProperty<XRangeOption> fixedOption;

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
    getStyleClass().add("x-range-field");
    setFormatter(formatter);
    setProvider(provider);
    setEditable(true);
    setAutoTrim(true);
    setFocusTraversable(false);
    setItems(FXCollections.observableArrayList());
    getProperties().addListener((MapChangeListener<Object, Object>) change -> {
      handlePropertiesChanged(change);
    });
  }

  private void handlePropertiesChanged(Change<?, ?> change) {
    if (change.wasAdded() && "FOCUSED".equals(change.getKey())) {
      setFocused(Boolean.TRUE.equals(change.getValueAdded()));
      getProperties().remove("FOCUSED");
    }
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
  public final boolean isEditable() {
    return editableProperty().get();
  }

  /**
   * Set whether the field editable
   * 
   * @param editable
   *          true or false
   */
  public final void setEditable(boolean editable) {
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
  public final XPatternFormatter<T> getFormatter() {
    return formatterProperty().get();
  }

  /**
   * Set formatter used in field
   * 
   * @param formatter
   *          the formatter
   */
  public final void setFormatter(XPatternFormatter<T> formatter) {
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
  public final ObservableList<XRangeItem<T>> getItems() {
    return itemsProperty().get();
  }

  /**
   * Set items
   * 
   * @param items
   *          the items
   */
  public final void setItems(ObservableList<XRangeItem<T>> items) {
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
  public final boolean isAutoTrim() {
    return autoTrimProperty().get();
  }

  /**
   * Set whether trim input text automatically
   * 
   * @param autoTrim
   *          true or false
   */
  public final void setAutoTrim(boolean autoTrim) {
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
  public final XValueProvider<T> getProvider() {
    return providerProperty().get();
  }

  /**
   * Set value provider used in field
   * 
   * @param provider
   *          the value provider
   */
  public final void setProvider(XValueProvider<T> provider) {
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
  public final int getPrefColumnCount() {
    return prefColumnCountProperty().get();
  }

  /**
   * preferred number of text columns
   * 
   * @param prefColumnCount
   *          preferred column count
   */
  public final void setPrefColumnCount(int prefColumnCount) {
    prefColumnCountProperty().set(prefColumnCount);
  }

  /**
   * interval property
   * 
   * @return interval property
   */
  public final BooleanProperty intervalProperty() {
    return interval;
  }

  /**
   * Determine whether interval allowed
   * 
   * @return true or false
   */
  public final boolean isInterval() {
    return intervalProperty().get();
  }

  /**
   * Set whether interval allowed
   * 
   * @param interval
   *          true or false
   */
  public final void setInterval(boolean interval) {
    intervalProperty().set(interval);
  }

  /**
   * multiple property
   * 
   * @return multiple property
   */
  public final BooleanProperty multipleProperty() {
    return multiple;
  }

  /**
   * Determine whether multiple ranges allowed
   * 
   * @return true or false
   */
  public final boolean isMultiple() {
    return multipleProperty().get();
  }

  /**
   * Set whether multiple ranges allowed
   * 
   * @param multiple
   *          true or false
   */
  public final void setMultiple(boolean multiple) {
    multipleProperty().set(multiple);
  }

  /**
   * fixed option property
   * 
   * @return fixed option property
   */
  public final ObjectProperty<XRangeOption> fixedOptionProperty() {
    if (fixedOption == null) {
      fixedOption = new ObjectPropertyBase<XRangeOption>() {
        @Override
        public Object getBean() {
          return XRangeField.this;
        }

        @Override
        public String getName() {
          return "fixedOption";
        }

        @Override
        public void set(XRangeOption newValue) {
          if (newValue == XRangeOption.BT || newValue == XRangeOption.NB)
            throw new InvalidParameterException("BT and NB are not allowed here");
          super.set(newValue);
        }
      };
    }
    return fixedOption;
  }

  /**
   * Get fixed option, default is null
   * 
   * @return fixed option
   */
  public final XRangeOption getFixedOption() {
    return fixedOptionProperty().get();
  }

  /**
   * Set fixed option, default is null
   * 
   * @param fixedOption
   *          the fixed option
   */
  public final void setFixedOption(XRangeOption fixedOption) {
    fixedOptionProperty().set(fixedOption);
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

  /**
   * <code>XRangeField</code> is not focus traversable in default to focus children at tab
   * key, but if requestFocus invoked and it isn't focus owner, the low field will be
   * focused
   */
  @Override
  public void requestFocus() {
    if (!isFocused()) {
      getProperties().put("FOCUS-LOW", Boolean.TRUE);
      super.requestFocus();
    }
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new XRangeFieldSkin<>(this);
  }

  @Override
  public String getUserAgentStylesheet() {
    return XRangeField.class.getResource("x-styles.css").toExternalForm();
  }
}
