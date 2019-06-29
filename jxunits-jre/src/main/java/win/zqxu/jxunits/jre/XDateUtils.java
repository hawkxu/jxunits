package win.zqxu.jxunits.jre;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

/**
 * Date utilities
 * 
 * @author zqxu
 */
public class XDateUtils {
  private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
  private static final LocalDate DEFAULT_DATE = LocalDate.of(1970, 1, 1);

  /**
   * convert java.util.Date value to LocalDate
   * 
   * @param value
   *          the date value
   * @return converted LocalDate value
   */
  public static LocalDate toLocalDate(Date value) {
    return value == null ? null : toZoned(value).toLocalDate();
  }

  /**
   * convert java.util.Date value to LocalDate
   * 
   * @param value
   *          the date value
   * @return converted LocalTime value
   */
  public static LocalTime toLocalTime(Date value) {
    return value == null ? null : toZoned(value).toLocalTime();
  }

  /**
   * convert java.util.Date value to LocalDateTime
   * 
   * @param value
   *          the date value
   * @return converted LocalDateTime value
   */
  public static LocalDateTime toLocalDateTime(Date value) {
    return value == null ? null : toZoned(value).toLocalDateTime();
  }

  private static ZonedDateTime toZoned(Date value) {
    return Instant.ofEpochMilli(value.getTime()).atZone(DEFAULT_ZONE);
  }

  /**
   * convert java time value to java.util.Date, for LocalTime at 1970-01-01
   * 
   * @param value
   *          the java time value
   * @return converted java.util.Date value
   */
  public static Date toDate(Temporal value) {
    return value == null ? null : Date.from(toZoned(value).toInstant());
  }

  private static ZonedDateTime toZoned(Temporal value) {
    if (value instanceof LocalDate)
      return ((LocalDate) value).atStartOfDay(DEFAULT_ZONE);
    if (value instanceof LocalTime)
      return ((LocalTime) value).atDate(DEFAULT_DATE).atZone(DEFAULT_ZONE);
    if (value instanceof LocalDateTime)
      return ((LocalDateTime) value).atZone(DEFAULT_ZONE);
    return ZonedDateTime.from(value);
  }

  /**
   * Get current date time, millisecond was set to zero
   * 
   * @return current date time, millisecond was set to zero
   */
  public static Date now() {
    return reset(new Date(), Calendar.MILLISECOND);
  }

  /**
   * Get current date, time was set to zero
   * 
   * @return current date, time was set to zero
   */
  public static Date today() {
    return date(new Date());
  }

  /**
   * Get a new date value, time was set to zero
   * 
   * @param value
   *          the given date value
   * @return a new date value, time was set to zero
   */
  public static Date date(Date value) {
    return reset(value, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
        Calendar.SECOND, Calendar.MILLISECOND);
  }

  /**
   * Get current time, date was set to 1970-01-01
   * 
   * @return current time
   */
  public static Date time() {
    return time(new Date());
  }

  /**
   * Get a new date value, date was set to 1970-01-01
   * 
   * @param value
   *          the given date value
   * @return a new date value, date was set to 1970-01-01
   */
  public static Date time(Date value) {
    return reset(value, Calendar.YEAR, Calendar.MONTH, Calendar.DATE);
  }

  /**
   * Get current time, date was set to 1970-01-01 and millisecond was set to zero
   * 
   * @return current time, date was set to 1970-01-01 and millisecond was set to zero
   */
  public static Date current() {
    return current(new Date());
  }

  /**
   * Get a new date value, date was set to 1970-01-01 and millisecond was set to zero
   * 
   * @param value
   *          the given date value
   * @return a new date value, date was set to 1970-01-01 and millisecond was set to zero
   */
  public static Date current(Date value) {
    return reset(value, Calendar.YEAR, Calendar.MONTH,
        Calendar.DATE, Calendar.MILLISECOND);
  }

  private static Date reset(Date value, int... fields) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(value);
    for (int field : fields) {
      switch (field) {
      case Calendar.YEAR:
        calendar.set(field, 1970);
        break;
      case Calendar.DATE:
        calendar.set(field, 1);
        break;
      default:
        calendar.set(field, 0);
      }
    }
    return calendar.getTime();
  }

  /**
   * Determine whether date of the value isn't 1970-01-01
   * 
   * @param value
   *          the given date value
   * @return true if date of the value isn't 1970-01-01
   */
  public static boolean hasDate(Date value) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(value);
    return calendar.get(Calendar.YEAR) != 1970
        || calendar.get(Calendar.MONTH) != 0
        || calendar.get(Calendar.DATE) != 1;
  }

  /**
   * Determine whether time of the value isn't zero
   * 
   * @param value
   *          the given date value
   * @return true if time of the value isn't zero
   */
  public static boolean hasTime(Date value) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(value);
    return calendar.get(Calendar.HOUR_OF_DAY) != 0
        || calendar.get(Calendar.MINUTE) != 0
        || calendar.get(Calendar.SECOND) != 0
        || calendar.get(Calendar.MILLISECOND) != 0;
  }
}
