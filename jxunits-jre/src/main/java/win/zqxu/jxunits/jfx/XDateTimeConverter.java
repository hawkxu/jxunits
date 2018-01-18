package win.zqxu.jxunits.jfx;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import javafx.util.StringConverter;

/**
 * A local date time converter support any valid date time pattern, use defaultDate and
 * defautTime to fill in converted value if the string does not contains those parts.
 * 
 * @author zqxu
 */
public class XDateTimeConverter<T extends Temporal> extends StringConverter<T> {
  private static final LocalDate DEFAULT_DATE = LocalDate.of(1900, 1, 1);
  private static final LocalTime DEFAULT_TIME = LocalTime.of(0, 0);
  private Class<T> type;
  private DateTimeFormatter formatter;
  private DateTimeFormatter parser;
  private LocalDate defaultDate;
  private LocalTime defaultTime;

  /**
   * Create converter using special format string to format and parse value
   * 
   * @param format
   *          any valid date time format
   * @return converter
   */
  public static final XDateTimeConverter<LocalDate> DATE(String format) {
    return new XDateTimeConverter<>(LocalDate.class, DateTimeFormatter.ofPattern(format), null);
  }

  /**
   * Create converter using special formatter to format and parse value
   * 
   * @param formatter
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalDate> DATE(DateTimeFormatter formatter) {
    return new XDateTimeConverter<>(LocalDate.class, formatter, null);
  }

  /**
   * Create converter using special formatter to format and parser to parse value
   * 
   * @param formatter
   *          date time formatter
   * @param parser
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalDate> DATE(DateTimeFormatter formatter,
      DateTimeFormatter parser) {
    return new XDateTimeConverter<>(LocalDate.class, formatter, null);
  }

  /**
   * Create converter using special format string to format and parse value
   * 
   * @param format
   *          any valid date time format
   * @return converter
   */
  public static final XDateTimeConverter<LocalTime> TIME(String format) {
    return new XDateTimeConverter<>(LocalTime.class, DateTimeFormatter.ofPattern(format), null);
  }

  /**
   * Create converter using special formatter to format and parse value
   * 
   * @param formatter
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalTime> TIME(DateTimeFormatter formatter) {
    return new XDateTimeConverter<>(LocalTime.class, formatter, null);
  }

  /**
   * Create converter using special formatter to format and parser to parse value
   * 
   * @param formatter
   *          date time formatter
   * @param parser
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalTime> TIME(DateTimeFormatter formatter,
      DateTimeFormatter parser) {
    return new XDateTimeConverter<>(LocalTime.class, formatter, null);
  }

  /**
   * Create converter using special format string to format and parse value
   * 
   * @param format
   *          any valid date time format
   * @return converter
   */
  public static final XDateTimeConverter<LocalDateTime> DATETIME(String format) {
    return new XDateTimeConverter<>(LocalDateTime.class, DateTimeFormatter.ofPattern(format), null);
  }

  /**
   * Create converter using special formatter to format and parse value
   * 
   * @param formatter
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalDateTime> DATETIME(DateTimeFormatter formatter) {
    return new XDateTimeConverter<>(LocalDateTime.class, formatter, null);
  }

  /**
   * Create converter using special formatter to format and parser to parse value
   * 
   * @param formatter
   *          date time formatter
   * @param parser
   *          date time formatter
   * @return converter
   */
  public static final XDateTimeConverter<LocalDateTime> DATETIME(DateTimeFormatter formatter,
      DateTimeFormatter parser) {
    return new XDateTimeConverter<>(LocalDateTime.class, formatter, null);
  }

  private XDateTimeConverter(Class<T> type, DateTimeFormatter formatter, DateTimeFormatter paser) {
    this.type = type;
    this.formatter = formatter;
    this.parser = paser;
    if (this.parser == null)
      this.parser = formatter;
    defaultDate = DEFAULT_DATE;
    defaultTime = DEFAULT_TIME;
  }

  /**
   * default date to fill converted value if the string does not contains date part.
   * default is 1900-01-01
   * 
   * @return default date
   */
  public LocalDate getDefaultDate() {
    return defaultDate;
  }

  /**
   * default date to fill converted value if the string does not contains date part.
   * default is 1900-01-01
   * 
   * @param defaultDate
   *          default date
   */
  public void setDefaultDate(LocalDate defaultDate) {
    this.defaultDate = defaultDate;
  }

  /**
   * default time to fill converted value if the string does not contains time part.
   * default is 00:00:00
   * 
   * @return default time
   */
  public LocalTime getDefaultTime() {
    return defaultTime;
  }

  /**
   * default time to fill converted value if the string does not contains time part.
   * default is 00:00:00
   * 
   * @param defaultTime
   *          default time
   */
  public void setDefaultTime(LocalTime defaultTime) {
    this.defaultTime = defaultTime;
  }

  @Override
  public String toString(T value) {
    return value == null ? "" : formatter.format(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T fromString(String text) {
    if (text == null || text.trim().isEmpty()) return null;
    TemporalAccessor parsed = parser.parse(text.trim());
    if (type == LocalDate.class)
      return (T) LocalDate.from(parsed);
    else if (type == LocalTime.class)
      return (T) LocalTime.from(parsed);
    return (T) localDateTime(parsed);
  }

  private LocalDateTime localDateTime(TemporalAccessor parsed) {
    LocalDate date = getDefaultDate();
    LocalTime time = getDefaultTime();
    try {
      date = LocalDate.from(parsed);
    } catch (Exception ex) {
    }
    try {
      time = LocalTime.from(parsed);
    } catch (Exception ex) {
    }
    return date.atTime(time);
  }
}
