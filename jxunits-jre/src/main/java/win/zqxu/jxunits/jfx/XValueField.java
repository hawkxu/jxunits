package win.zqxu.jxunits.jfx;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
  private ObjectProperty<TextFormatter<T>> formatter = new SimpleObjectProperty<>();
  private ObjectProperty<XValueProvider<T>> provider = new SimpleObjectProperty<>();
  private ObjectProperty<T> value = new SimpleObjectProperty<>();
  private ChangeListener<T> formatterValueHandler;

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
   * Determine whether current value is empty
   * 
   * @return true if current value is empty
   * @see XObjectUtils#isEmpty(Object)
   */
  public boolean isEmpty() {
    return XObjectUtils.isEmpty(getValue());
  }

  private void installListeners() {
    formatterProperty().addListener((v, o, n) -> handleFormatterChanged(o, n));
    addEventFilter(KeyEvent.ANY, event -> filterKeyEvent(event));
    valueProperty().addListener((v, o, n) -> handleValueChanged(n));
  }

  private void handleFormatterChanged(TextFormatter<T> o, TextFormatter<T> n) {
    ChangeListener<T> handler = getFormatterValueHandler();
    if (o != null) {
      o.valueProperty().removeListener(handler);
    }
    if (n != null) {
      n.setValue(getValue());
      n.valueProperty().addListener(handler);
    }
  }

  private ChangeListener<T> getFormatterValueHandler() {
    if (formatterValueHandler == null) {
      formatterValueHandler = (v, o, n) -> {
        if (!value.isBound()) setValue(n);
      };
    }
    return formatterValueHandler;
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

  private void filterKeyEvent(KeyEvent event) {
    if (getFormatter() == null && isEditable()) {
      String eventName = event.getEventType().getName();
      KeyCode code = event.getCode();
      boolean clear = code == KeyCode.DELETE || code == KeyCode.BACK_SPACE;
      if (clear || eventName.equals("KEY_TYPED")) event.consume();
      if (clear && eventName.equals("KEY_RELEASED")) setValue(null);
    }
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
