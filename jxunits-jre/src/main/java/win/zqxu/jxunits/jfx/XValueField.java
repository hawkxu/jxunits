package win.zqxu.jxunits.jfx;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import win.zqxu.jxunits.jre.XObjectUtils;

/**
 * A text field support any value through formatter and provider, manual input has been
 * blocked as default, must set formatter to enable manual input.
 * <p>
 * Please notice:
 * </p>
 * <ul>
 * <li>do not change formatter through textFormatterProperty</li>
 * </ul>
 * 
 * @author zqxu
 */
public class XValueField<T> extends TextField {
  private ObjectProperty<TextFormatter<T>> formatter =
      new SimpleObjectProperty<>(this, "formatter");
  private ObjectProperty<XValueProvider<T>> provider =
      new SimpleObjectProperty<>(this, "provider");
  private ObjectProperty<T> value = new ObjectPropertyBase<T>() {
    @Override
    public Object getBean() {
      return XValueField.this;
    }

    @Override
    public String getName() {
      return "value";
    }

    protected void invalidated() {
      handleValueChanged(get());
    }
  };
  private BooleanProperty valueClearAllowed =
      new SimpleBooleanProperty(this, "valueClearAllowed");

  public XValueField() {
    this(null, null);
  }

  public XValueField(TextFormatter<T> formatter) {
    this(formatter, null);
  }

  public XValueField(XValueProvider<T> provider) {
    this(null, provider);
  }

  public XValueField(TextFormatter<T> formatter, XValueProvider<T> provider) {
    getStyleClass().add("x-value-field");
    installListeners();
    setFormatter(formatter);
    setProvider(provider);
    setValueClearAllowed(true);
    textFormatterProperty().bind(formatterProperty());
  }

  /**
   * formatter property
   * 
   * @return formatter property
   */
  public final ObjectProperty<TextFormatter<T>> formatterProperty() {
    return formatter;
  }

  /**
   * get formatter of this field
   * 
   * @return formatter
   */
  public final TextFormatter<T> getFormatter() {
    return formatterProperty().get();
  }

  /**
   * set formatter of this field
   * 
   * @param formatter
   *          formatter
   */
  public final void setFormatter(TextFormatter<T> formatter) {
    formatterProperty().set(formatter);
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
   * get provider of this field
   * 
   * @return provider
   */
  public final XValueProvider<T> getProvider() {
    return providerProperty().get();
  }

  /**
   * set provider of this field
   * 
   * @param provider
   *          provider
   */
  public final void setProvider(XValueProvider<T> provider) {
    providerProperty().set(provider);
  }

  /**
   * value property
   * 
   * @return value property
   */
  public final ObjectProperty<T> valueProperty() {
    return value;
  }

  /**
   * get value of this field
   * 
   * @return value
   */
  public final T getValue() {
    return valueProperty().get();
  }

  /**
   * set value of this field
   * 
   * @param value
   *          value
   */
  public final void setValue(T value) {
    valueProperty().set(value);
  }

  /**
   * When formatter not set and the field is editable (usually a provider set), whether
   * allow clear value to null by delete/backspace key or not.
   * 
   * @return valueClearAllowed property
   */
  public final BooleanProperty valueClearAllowedProperty() {
    return valueClearAllowed;
  }

  /**
   * When formatter not set and the field is editable (usually a provider set), whether
   * allow clear value to null by delete/backspace key or not.
   * 
   * @return true or false
   */
  public final boolean isValueClearAllowed() {
    return valueClearAllowed.get();
  }

  /**
   * When formatter not set and the field is editable (usually a provider set), whether
   * allow clear value to null by delete/backspace key or not.
   * 
   * @param valueClearAllowed
   *          true or false
   */
  public final void setValueClearAllowed(boolean valueClearAllowed) {
    this.valueClearAllowed.set(valueClearAllowed);
  }

  /**
   * Determine whether current value is empty
   * 
   * @return true if current value is empty
   * @see XObjectUtils#isEmpty(Object)
   */
  public boolean isEmpty() {
    return XObjectUtils.isEmpty(getValue());
  }

  private void handleValueChanged(T value) {
    if (getFormatter() != null) {
      getFormatter().setValue(value);
      return;
    }
    String text = Objects.toString(value, "");
    if (!Objects.equals(text, getText())) {
      setText(text);
      positionCaret(getText().length());
    }
  }

  private void installListeners() {
    addEventFilter(ActionEvent.ACTION, event -> commitValue());
    addEventFilter(KeyEvent.ANY, event -> filterKeyEvent(event));
  }

  private void filterKeyEvent(KeyEvent event) {
    if (getFormatter() == null && isEditable()) {
      KeyCode code = event.getCode();
      if (code != KeyCode.TAB) event.consume();
      boolean clear = code == KeyCode.DELETE
          || code == KeyCode.BACK_SPACE;
      if (clear && isValueClearAllowed()) {
        String type = event.getEventType().getName();
        if (type.equals("KEY_RELEASED")) setValue(null);
      }
    }
  }

  @Override
  public void cut() {
    if (getFormatter() != null) super.cut();
  }

  @Override
  public void paste() {
    if (getFormatter() != null) super.paste();
  }

  /**
   * Convert string to value
   * 
   * @param text
   *          the string
   * @return converted value
   * @throws IllegalStateException
   *           if formatter not set
   */
  public T convertToValue(String text) {
    TextFormatter<T> f = getFormatter();
    if (f == null)
      throw new IllegalStateException("formatter not set");
    return f.getValueConverter().fromString(text);
  }

  /**
   * convert value to string through formatter, returns value.toString if formatter not
   * set
   * 
   * @param value
   *          the value
   * @return value string
   */
  public String convertToString(T value) {
    TextFormatter<T> f = getFormatter();
    if (f == null) return Objects.toString(value, "");
    return f.getValueConverter().toString(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Skin<?> createDefaultSkin() {
    return new XValueFieldSkin<T>(this);
  }

  @Override
  public String getUserAgentStylesheet() {
    return XValueField.class.getResource("x-styles.css").toExternalForm();
  }
}
