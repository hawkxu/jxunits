package win.zqxu.jxunits.jfx;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

public class XRangeItem<T extends Comparable<? super T>> {
  private ObjectProperty<XRangeSign> sign = new SimpleObjectProperty<>();
  private ObjectProperty<XRangeOption> option = new SimpleObjectProperty<>();
  private ObjectProperty<T> low = new SimpleObjectProperty<>();
  private ObjectProperty<T> high = new SimpleObjectProperty<>();

  public XRangeItem() {
    this(XRangeSign.I, null, null, null);
  }

  public XRangeItem(T low) {
    this(XRangeSign.I, null, low, null);
  }

  public XRangeItem(XRangeOption option, T low) {
    this(XRangeSign.I, option, low, null);
  }

  public XRangeItem(T low, T high) {
    this(XRangeSign.I, null, low, high);
  }

  public XRangeItem(XRangeOption option, T low, T high) {
    this(XRangeSign.I, option, low, high);
  }

  public XRangeItem(XRangeSign sign, XRangeOption option, T low, T high) {
    setSign(sign);
    setOption(option);
    setLow(low);
    setHigh(high);
    updateOption(low, true);
    installListeners();
  }

  private void installListeners() {
    option.addListener((v, o, n) -> handleOptionChanged(n));
    low.addListener((v, o, n) -> handleLowChanged(o, n));
    high.addListener((v, o, n) -> handleHighChanged(o, n));
  }

  public final ObjectProperty<XRangeSign> signProperty() {
    return sign;
  }

  public XRangeSign getSign() {
    return signProperty().get();
  }

  public void setSign(XRangeSign sign) {
    signProperty().set(sign);
  }

  public final ObjectProperty<XRangeOption> optionProperty() {
    return option;
  }

  public XRangeOption getOption() {
    return optionProperty().get();
  }

  public void setOption(XRangeOption option) {
    optionProperty().set(option);
  }

  public final ObjectProperty<T> lowProperty() {
    return low;
  }

  public T getLow() {
    return lowProperty().get();
  }

  public void setLow(T low) {
    lowProperty().set(low);
  }

  public final ObjectProperty<T> highProperty() {
    return high;
  }

  public T getHigh() {
    return highProperty().get();
  }

  public void setHigh(T high) {
    highProperty().set(high);
  }

  private void handleOptionChanged(XRangeOption option) {
    if (option != null && getSign() == null)
      setSign(XRangeSign.I);
  }

  private void handleLowChanged(T oldValue, T newValue) {
    if (!isEmpty(oldValue) || !isEmpty(newValue))
      updateOption(oldValue, false);
  }

  private void handleHighChanged(T oldValue, T newValue) {
    if (isEmpty(oldValue) && isEmpty(newValue))
      return;
    if (isEmpty(oldValue) || isEmpty(newValue))
      updateOption(null, false);
  }

  private void updateOption(T oldLow, boolean init) {
    T lowValue = getLow(), highValue = getHigh();
    XRangeOption option = getOption();
    if (!isEmpty(highValue)) {
      option = option == XRangeOption.NB ? option : XRangeOption.BT;
    } else if (isEmpty(lowValue)) {
      if (!init) option = null;
    } else if (containsWildcard(lowValue)) {
      if (!containsWildcard(oldLow)) option = XRangeOption.CP;
    } else if (option == null
        || option == XRangeOption.CP
        || option == XRangeOption.NP
        || option == XRangeOption.BT
        || option == XRangeOption.NB) {
      option = XRangeOption.EQ;
    }
    setOption(option);
  }

  /**
   * Determine whether current range is empty, the range considered as empty if sign or
   * option is null
   * 
   * @return true if empty and otherwise false
   */
  public boolean isEmpty() {
    return getSign() == null || getOption() == null;
  }

  /**
   * Determine whether the value match this range, always return false for empty range
   * 
   * @param value
   *          the value
   * @return true or false
   */
  public boolean match(T value) {
    if (isEmpty()) return false;
    switch (getOption()) {
    case EQ:
      return equals(value, getLow());
    case NE:
      return !equals(value, getLow());
    case LT:
      return lessThan(value, getLow());
    case LE:
      return lessThanOrEquals(value, getLow());
    case GT:
      return !lessThanOrEquals(value, getLow());
    case GE:
      return !lessThan(value, getLow());
    case CP:
      return matchWildcards(value, getLow());
    case NP:
      return !matchWildcards(value, getLow());
    case BT:
      return between(value, getLow(), getHigh());
    case NB:
      return !between(value, getLow(), getHigh());
    default:
      throw new RuntimeException(); // impossible
    }
  }

  private boolean equals(T value, T low) {
    return (isEmpty(value) && isEmpty(low))
        || (value != null && low != null && value.compareTo(low) == 0);
  }

  private boolean lessThan(T value, T low) {
    return isEmpty(value) || (value != null && low != null && value.compareTo(low) < 0);
  }

  private boolean lessThanOrEquals(T value, T low) {
    return isEmpty(value) || (value != null && low != null && value.compareTo(low) <= 0);
  }

  private boolean matchWildcards(T value, T lowValue) {
    String text = Objects.toString(value, "");
    String pattern = Objects.toString(lowValue, "")
        .replace("?", ".").replace("*", ".*");
    return text.toString().matches(pattern);
  }

  private boolean between(T value, T low, T high) {
    return !lessThan(value, low) && lessThanOrEquals(value, high);
  }

  /**
   * Clone this range
   */
  @Override
  public XRangeItem<T> clone() {
    return new XRangeItem<>(getSign(), getOption(), getLow(), getHigh());
  }

  /**
   * Get option image for range
   * 
   * @param range
   *          the range
   * @return option image
   */
  static Image getOptionImage(XRangeItem<?> range) {
    XRangeSign sign = range == null ? null : range.getSign();
    XRangeOption option = range == null ? null : range.getOption();
    return getOptionImage(sign, option);
  }

  /**
   * Get option image for sign and option
   * 
   * @param sign
   *          the sign
   * @param option
   *          the option
   * @return option image
   */
  static Image getOptionImage(XRangeSign sign, XRangeOption option) {
    if (option == null) return XImageLoader.get("OPTION_NAN.png");
    sign = sign == null ? XRangeSign.I : sign;
    return XImageLoader.get("OPTION_" + sign + option + ".png");
  }

  /**
   * Determine whether the value if empty, the value considered as empty if null or an
   * empty string
   * 
   * @param value
   *          the value to check
   * @return true or false
   */
  public static boolean isEmpty(Object value) {
    return value == null || (value instanceof String && ((String) value).isEmpty());
  }

  /**
   * Determine whether the range is empty, the range considered as empty if null or
   * range.isEmtpy()
   * 
   * @param range
   *          the range
   * @return true if empty and otherwise false
   */
  public static boolean isEmpty(XRangeItem<?> range) {
    return range == null || range.isEmpty();
  }

  /**
   * Determine whether the value contains wild-card (the value is string and contains * or
   * ?)
   * 
   * @param value
   *          the value
   * @return true if the value contains wild-card
   */
  public static boolean containsWildcard(Object value) {
    return value instanceof String && ((String) value).matches(".*(\\*|\\?).*");
  }

  /**
   * Determine whether the value match ranges, always returns true for empty ranges
   * 
   * @param <T>
   *          the value type
   * @param value
   *          the value
   * @param ranges
   *          the ranges
   * @return true if the value match the range
   */
  public static <T extends Comparable<? super T>> boolean match(
      T value, XRangeItem<T>[] ranges) {
    return match(value, Arrays.asList(ranges));
  }

  /**
   * Determine whether the value match ranges, always returns true for empty ranges
   * 
   * @param <T>
   *          the value type
   * @param value
   *          the value
   * @param ranges
   *          the ranges
   * @return true if the value match the range
   */
  public static <T extends Comparable<? super T>> boolean match(
      T value, List<XRangeItem<T>> ranges) {
    if (ranges.isEmpty()) return true;
    boolean match = false;
    for (XRangeItem<T> range : ranges) {
      if (isEmpty(range)) continue;
      if (range.getSign() == XRangeSign.E) {
        if (range.match(value)) return false;
      } else {
        match = match || range.match(value);
      }
    }
    return match;
  }
}
