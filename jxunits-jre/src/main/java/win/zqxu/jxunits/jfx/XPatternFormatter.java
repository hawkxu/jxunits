package win.zqxu.jxunits.jfx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.BigIntegerStringConverter;
import javafx.util.converter.ByteStringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import javafx.util.converter.LongStringConverter;
import javafx.util.converter.NumberStringConverter;
import javafx.util.converter.ShortStringConverter;

/**
 * Use regular expression to restrict text input, do not support capturing group and OR(|)
 * operator, no restriction if the pattern is null or empty.
 * <p>
 * e.g. for input date, using pattern \\d{4}-\\d{2}-\\d{2}, or more restricted
 * [12][90]\\d{2}-[01]?\\d-[0-3]?\\d
 * </p>
 * 
 * @author zqxu
 *
 * @param <V>
 *          The type of the value
 */
public class XPatternFormatter<V> extends TextFormatter<V> {
  public static enum CharCase {
    UPPER, LOWER;
  }

  /**
   * default pattern for byte value
   */
  public static final String DEFAULT_BYTE_PATTERN = "[+-]?\\d{3}";
  /**
   * default pattern for short value
   */
  public static final String DEFAULT_SHORT_PATTERN = "[+-]?\\d{5}";
  /**
   * default pattern for integer value
   */
  public static final String DEFAULT_INTEGER_PATTERN = "[+-]?\\d{10}";
  /**
   * default pattern for unsigned integer value
   */
  public static final String DEFAULT_UNSIGNED_PATTERN = "\\d{10}";
  /**
   * default pattern for long value
   */
  public static final String DEFAULT_LONG_PATTERN = "[+-]?\\d{19}";
  /**
   * default pattern for big integer value
   */
  public static final String DEFAULT_BIGINT_PATTERN = "[+-]?\\d*";
  /**
   * default pattern for double value
   */
  public static final String DEFAULT_DOUBLE_PATTERN = "[+-]?\\d*\\.\\d*";
  /**
   * default pattern for big decimal value
   */
  public static final String DEFAULT_BIGDEC_PATTERN = "[+-]?\\d*\\.\\d*";
  /**
   * default date format
   */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  /**
   * default date pattern
   */
  public static final String DEFAULT_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
  /**
   * default time format
   */
  public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
  /**
   * default time pattern
   */
  public static final String DEFAULT_TIME_PATTERN = "\\d{2}:\\d{2}:\\d{2}";
  /**
   * default time stamp format
   */
  public static final String DEFAULT_DATETIME_FORMAT = DEFAULT_DATE_FORMAT + " "
      + DEFAULT_TIME_FORMAT;
  /**
   * default time stamp pattern
   */
  public static final String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " "
      + DEFAULT_TIME_PATTERN;

  /**
   * Create a new XPatternFormatter instance for string value using
   * <code>DefaultStringConverter</code> and pattern, if the pattern is null or empty,
   * then no input restrictions
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see DefaultStringConverter
   */
  public static XPatternFormatter<String> STRING(String pattern) {
    return new XPatternFormatter<>(new DefaultStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for string value using
   * <code>DefaultStringConverter</code> and pattern, convert input text to upper case. if
   * the pattern is null or empty, then no input restrictions
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see DefaultStringConverter
   */
  public static XPatternFormatter<String> UPPER(String pattern) {
    return new XPatternFormatter<>(new DefaultStringConverter(), pattern, CharCase.UPPER);
  }

  /**
   * Create a new XPatternFormatter instance for string value using
   * <code>DefaultStringConverter</code> and pattern, convert input text to lower case. if
   * the pattern is null or empty, then no input restrictions
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see DefaultStringConverter
   */
  public static XPatternFormatter<String> LOWER(String pattern) {
    return new XPatternFormatter<>(new DefaultStringConverter(), pattern, CharCase.LOWER);
  }

  /**
   * Create a XPatternFormatter instance for string value using
   * <code>XTrimStringConverter</code> with pattern.
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see XTrimStringConverter
   */
  public static XPatternFormatter<String> TRIM(String pattern) {
    return TRIM(pattern, null);
  }

  /**
   * Create a XPatternFormatter instance for string value using
   * <code>XTrimStringConverter</code> with pattern and char case.
   * 
   * @param pattern
   *          the regular expression pattern
   * @param charCase
   *          the specified char case
   * @return XPatternFormatter instance
   * @see XTrimStringConverter
   */
  public static XPatternFormatter<String> TRIM(String pattern, CharCase charCase) {
    return new XPatternFormatter<>(new XTrimStringConverter(), pattern, charCase);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default byte pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_BYTE_PATTERN
   */
  public static XPatternFormatter<Number> BYTE() {
    return NUMBER(DEFAULT_BYTE_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default short pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_SHORT_PATTERN
   */
  public static XPatternFormatter<Number> SHORT() {
    return NUMBER(DEFAULT_SHORT_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default integer pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_INTEGER_PATTERN
   */
  public static XPatternFormatter<Number> INTEGER() {
    return NUMBER(DEFAULT_INTEGER_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default unsigned pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_UNSIGNED_PATTERN
   */
  public static XPatternFormatter<Number> UNSIGNED() {
    return NUMBER(DEFAULT_UNSIGNED_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default long integer pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_LONG_PATTERN
   */
  public static XPatternFormatter<Number> LONG() {
    return NUMBER(DEFAULT_LONG_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default big integer pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_BIGINT_PATTERN
   */
  public static XPatternFormatter<Number> BIGINT() {
    return NUMBER(DEFAULT_BIGINT_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default double pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_DOUBLE_PATTERN
   */
  public static XPatternFormatter<Number> DOUBLE() {
    return NUMBER(DEFAULT_DOUBLE_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>NumberStringConverter</code> and default big decimal pattern
   * 
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   * @see #DEFAULT_BIGDEC_PATTERN
   */
  public static XPatternFormatter<Number> BIGDEC() {
    return NUMBER(DEFAULT_BIGDEC_PATTERN);
  }

  /**
   * Create XPatternFormatter instance for number using <code>NumberStringConverter</code>
   * with pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see NumberStringConverter
   */
  public static XPatternFormatter<Number> NUMBER(String pattern) {
    return new XPatternFormatter<>(new NumberStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for byte value using
   * <code>ByteStringConverter</code> and default byte pattern
   * 
   * @return XPatternFormatter instance
   * @see ByteStringConverter
   * @see #DEFAULT_BYTE_PATTERN
   */
  public static XPatternFormatter<Byte> Byte() {
    return Byte(DEFAULT_BYTE_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for byte value using
   * <code>ByteStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see ByteStringConverter
   */
  public static XPatternFormatter<Byte> Byte(String pattern) {
    return new XPatternFormatter<>(new ByteStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for short value using
   * <code>ShortStringConverter</code> and default short pattern
   * 
   * @return XPatternFormatter instance
   * @see ShortStringConverter
   * @see #DEFAULT_SHORT_PATTERN
   */
  public static XPatternFormatter<Short> Short() {
    return Short(DEFAULT_SHORT_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for short value using
   * <code>ShortStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see ShortStringConverter
   */
  public static XPatternFormatter<Short> Short(String pattern) {
    return new XPatternFormatter<>(new ShortStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for integer value using
   * <code>IntegerStringConverter</code> and default integer pattern
   * 
   * @return XPatternFormatter instance
   * @see IntegerStringConverter
   * @see #DEFAULT_INTEGER_PATTERN
   */
  public static XPatternFormatter<Integer> Integer() {
    return Integer(DEFAULT_INTEGER_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for unsigned integer value using
   * <code>IntegerStringConverter</code> and default unsigned pattern
   * 
   * @return XPatternFormatter instance
   * @see IntegerStringConverter
   * @see #DEFAULT_UNSIGNED_PATTERN
   */
  public static XPatternFormatter<Integer> Unsigned() {
    return Integer(DEFAULT_UNSIGNED_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for integer value using
   * <code>IntegerStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see IntegerStringConverter
   */
  public static XPatternFormatter<Integer> Integer(String pattern) {
    return new XPatternFormatter<>(new IntegerStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for long value using
   * <code>LongStringConverter</code> and default long integer pattern
   * 
   * @return XPatternFormatter instance
   * @see LongStringConverter
   * @see #DEFAULT_LONG_PATTERN
   */
  public static XPatternFormatter<Long> Long() {
    return Long(DEFAULT_LONG_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for long value using
   * <code>LongStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see LongStringConverter
   */
  public static XPatternFormatter<Long> Long(String pattern) {
    return new XPatternFormatter<>(new LongStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for big integer value using
   * <code>BigIntegerStringConverter</code> and default big integer pattern
   * 
   * @return XPatternFormatter instance
   * @see BigIntegerStringConverter
   * @see #DEFAULT_BIGINT_PATTERN
   */
  public static XPatternFormatter<BigInteger> BigInteger() {
    return BigInteger(DEFAULT_BIGINT_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for big integer value using
   * <code>BigIntegerStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see BigIntegerStringConverter
   */
  public static XPatternFormatter<BigInteger> BigInteger(String pattern) {
    return new XPatternFormatter<>(new BigIntegerStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for double value using
   * <code>DoubleStringConverter</code> and default double pattern
   * 
   * @return XPatternFormatter instance
   * @see DoubleStringConverter
   * @see #DEFAULT_DOUBLE_PATTERN
   */
  public static XPatternFormatter<Double> Double() {
    return Double(DEFAULT_DOUBLE_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for double value using
   * <code>DoubleStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see DoubleStringConverter
   */
  public static XPatternFormatter<Double> Double(String pattern) {
    return new XPatternFormatter<>(new DoubleStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for number using
   * <code>BigDecimalStringConverter</code> and default big decimal pattern
   * 
   * @return XPatternFormatter instance
   * @see BigDecimalStringConverter
   * @see #DEFAULT_BIGDEC_PATTERN
   */
  public static XPatternFormatter<BigDecimal> BigDecimal() {
    return BigDecimal(DEFAULT_BIGDEC_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for big decimal value using
   * <code>BigDecimalStringConverter</code> and specified pattern
   * 
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see BigDecimalStringConverter
   */
  public static XPatternFormatter<BigDecimal> BigDecimal(String pattern) {
    return new XPatternFormatter<>(new BigDecimalStringConverter(), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for date value using
   * <code>DateStringConverter</code> with default date format and pattern
   * 
   * @return XPatternFormatter instance
   * @see LocalDateStringConverter
   * @see #DEFAULT_DATE_FORMAT
   * @see #DEFAULT_DATE_PATTERN
   */
  public static XPatternFormatter<LocalDate> DATE() {
    return DATE(DEFAULT_DATE_FORMAT, DEFAULT_DATE_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for date value using
   * <code>DateStringConverter</code> with specify format and no pattern restriction,
   * usually for read-only field
   * 
   * @param format
   *          date format
   * @return XPatternFormatter instance
   * @see LocalDateStringConverter
   * @see #DEFAULT_DATE_FORMAT
   * @see #DEFAULT_DATE_PATTERN
   */
  public static XPatternFormatter<LocalDate> DATE(String format) {
    return DATE(format, null);
  }

  /**
   * Create a new XPatternFormatter instance for date value using
   * <code>DateStringConverter</code> with specified format and pattern
   * 
   * @param format
   *          the date format string
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see LocalDateStringConverter
   */
  public static XPatternFormatter<LocalDate> DATE(String format, String pattern) {
    return new XPatternFormatter<>(XDateTimeConverter.DATE(format), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for time value using
   * <code>TimeStringConverter</code> with default time format and pattern
   * 
   * @return XPatternFormatter instance
   * @see LocalTimeStringConverter
   * @see #DEFAULT_TIME_FORMAT
   * @see #DEFAULT_TIME_PATTERN
   */
  public static XPatternFormatter<LocalTime> TIME() {
    return TIME(DEFAULT_TIME_FORMAT, DEFAULT_TIME_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for time value using
   * <code>TimeStringConverter</code> with specify format and no pattern restriction,
   * usually for read-only field
   * 
   * @param format
   *          time format
   * @return XPatternFormatter instance
   * @see LocalTimeStringConverter
   * @see #DEFAULT_TIME_FORMAT
   * @see #DEFAULT_TIME_PATTERN
   */
  public static XPatternFormatter<LocalTime> TIME(String format) {
    return TIME(format, null);
  }

  /**
   * Create a new XPatternFormatter instance for time value using
   * <code>TimeStringConverter</code> with specified format and pattern
   * 
   * @param format
   *          the time format string
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see LocalTimeStringConverter
   */
  public static XPatternFormatter<LocalTime> TIME(String format, String pattern) {
    return new XPatternFormatter<>(XDateTimeConverter.TIME(format), pattern);
  }

  /**
   * Create a new XPatternFormatter instance for date time value using
   * <code>LocalDateTimeStringConverter</code> with default format and pattern
   * 
   * @return XPatternFormatter instance
   * @see LocalDateTimeStringConverter
   * @see #DEFAULT_DATETIME_FORMAT
   * @see #DEFAULT_DATETIME_PATTERN
   */
  public static XPatternFormatter<LocalDateTime> DATETIME() {
    return DATETIME(DEFAULT_DATETIME_FORMAT, DEFAULT_DATETIME_PATTERN);
  }

  /**
   * Create a new XPatternFormatter instance for date time value using
   * <code>LocalDateTimeStringConverter</code>with specify format and no pattern
   * restriction, usually for read-only field
   * 
   * @param format
   *          date time format
   * @return XPatternFormatter instance
   * @see LocalDateTimeStringConverter
   * @see #DEFAULT_DATETIME_FORMAT
   * @see #DEFAULT_DATETIME_PATTERN
   */
  public static XPatternFormatter<LocalDateTime> DATETIME(String format) {
    return DATETIME(format, null);
  }

  /**
   * Create a new XPatternFormatter instance for date time value using
   * <code>LocalDateTimeStringConverter</code> with specify format and pattern
   * 
   * @param format
   *          the date time format string
   * @param pattern
   *          the regular expression pattern
   * @return XPatternFormatter instance
   * @see LocalDateTimeStringConverter
   */
  public static XPatternFormatter<LocalDateTime> DATETIME(String format, String pattern) {
    return new XPatternFormatter<>(XDateTimeConverter.DATETIME(format), pattern);
  }

  private V defaultValue;

  /**
   * Constructor with value converter and pattern
   * 
   * @param valueConverter
   *          the value converter
   * @param pattern
   *          the regular expression pattern
   * @throws PatternSyntaxException
   *           If the pattern's syntax is invalid
   */
  public XPatternFormatter(StringConverter<V> valueConverter, String pattern) {
    this(valueConverter, pattern, null, null);
  }

  /**
   * Constructor with value converter and specified char case
   * 
   * @param valueConverter
   *          the value converter
   * @param charCase
   *          the specified char case
   */
  public XPatternFormatter(StringConverter<V> valueConverter, CharCase charCase) {
    this(valueConverter, null, charCase, null);
  }

  /**
   * Constructor with value converter, pattern and specified char case
   * 
   * @param valueConverter
   *          the value converter
   * @param pattern
   *          the regular expression pattern
   * @param charCase
   *          the specified char case
   * @throws PatternSyntaxException
   *           If the pattern's syntax is invalid
   */
  public XPatternFormatter(StringConverter<V> valueConverter, String pattern, CharCase charCase) {
    this(valueConverter, pattern, charCase, null);
  }

  /**
   * Constructor with value converter, pattern, specified char case and default value
   * 
   * @param valueConverter
   *          the value converter
   * @param pattern
   *          the regular expression pattern
   * @param charCase
   *          the specified char case
   * @param defaultValue
   *          the default value
   * @throws PatternSyntaxException
   *           If the pattern's syntax is invalid
   */
  public XPatternFormatter(StringConverter<V> valueConverter, String pattern,
      CharCase charCase, V defaultValue) throws PatternSyntaxException {
    super(valueConverter, defaultValue, new PatternFilter(pattern, charCase));
    this.defaultValue = defaultValue;
  }

  /**
   * get regular expression pattern applied to this formatter
   * 
   * @return regular expression pattern, may be null or empty
   */
  public String getPattern() {
    return ((PatternFilter) getFilter()).getPattern();
  }

  /**
   * get char case applied to this formatter
   * 
   * @return char case, may be null
   */
  public CharCase getCharCase() {
    return ((PatternFilter) getFilter()).getCharCase();
  }

  /**
   * get default value
   * 
   * @return default value
   */
  public V getDefaultValue() {
    return defaultValue;
  }

  /**
   * apply formatter to string, get formatted string
   * 
   * @param string
   *          the string to format
   * @return formatted string
   */
  public String apply(String string) {
    PatternFilter p = (PatternFilter) getFilter();
    return p.applyPatterns(p.applyCharcase(string));
  }

  /**
   * Clone this pattern formatter
   */
  @Override
  public XPatternFormatter<V> clone() {
    return new XPatternFormatter<>(getValueConverter(), getPattern(),
        getCharCase(), getDefaultValue());
  }

  private static class PatternFilter implements UnaryOperator<Change> {
    private static final String GROUP_NS = "capturing group is not supported";
    private static final String OR_OP_NS = "OR '|' operator is not supported";
    private Pattern[] patterns;
    private String pattern;
    private CharCase charCase;

    /**
     * 
     * @param pattern
     * @param charCase
     * @throws PatternSyntaxException
     *           If the pattern's syntax is invalid
     */
    public PatternFilter(String pattern, CharCase charCase) {
      this.charCase = charCase;
      analyzePattern(pattern);
    }

    protected String getPattern() {
      return pattern;
    }

    protected CharCase getCharCase() {
      return charCase;
    }

    private void analyzePattern(String pattern) {
      if (pattern == null) pattern = "";
      Pattern.compile(pattern); // check pattern
      this.pattern = pattern;
      if (pattern.isEmpty()) {
        patterns = new Pattern[0];
        return;
      }
      List<Pattern> patternList = new ArrayList<>();
      int start = 0, close = 0, steps;
      int bracket = 0, brackets = 0;
      boolean split = false;
      String flags = "";
      while (close < pattern.length()) {
        char c = pattern.charAt(close);
        steps = 1;
        switch (c) {
        case '(':
          if (brackets == 0) {
            if (pattern.charAt(close + 1) != '?')
              throw new PatternSyntaxException(GROUP_NS, pattern, close);
            split = !split;
          }
        case '[':
          split = !split;
        case '{':
          if (brackets == 0 || bracket == c) {
            bracket = c;
            brackets++;
          }
          break;
        case ')':
          if (bracket == '(') brackets--;
          break;
        case ']':
          if (bracket == '[') brackets--;
          break;
        case '}':
          if (bracket == '{') brackets--;
          break;
        case '|':
          if (brackets == 0)
            throw new PatternSyntaxException(OR_OP_NS, pattern, close);
        case '?':
        case '*':
        case '+':
          break;
        case '\\':
          String escape = pattern.substring(close + 1);
          steps = getEscapeLength(escape);
        default:
          if (brackets == 0) split = !split;
        }
        if (split) {
          split = false;
          if (close > start) {
            String s = pattern.substring(start, close);
            start = close;
            if (s.matches("\\(\\?[idmsuxU-].*"))
              flags += s;
            else {
              String f = flags;
              if (charCase != null) f += "(?i)";
              patternList.add(Pattern.compile(f + "^" + s));
            }
          }
        }
        close += steps;
      }
      pattern = pattern.substring(start);
      patternList.add(Pattern.compile("^" + pattern));
      patterns = patternList.toArray(new Pattern[0]);
    }

    private int getEscapeLength(String escape) {
      Pattern p = Pattern.compile("^(0[0-3][0-7]{2}|0[0-7]{1,2}"
          + "|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|c.|p\\{.*?\\})");
      Matcher m = p.matcher(escape);
      return m.find() ? m.group(1).length() + 1 : 2;
    }

    @Override
    public Change apply(Change change) {
      if (!change.isContentChange())
        return change;
      applyCharCase(change);
      return applyPatterns(change);
    }

    private void applyCharCase(Change change) {
      String text = change.getText();
      if (charCase == CharCase.UPPER)
        change.setText(text.toUpperCase());
      else if (charCase == CharCase.LOWER)
        change.setText(text.toLowerCase());
    }

    private Change applyPatterns(Change change) {
      if (patterns.length == 0) return change;
      String newText = change.getControlNewText();
      if (newText.isEmpty()) return change;
      String oldText = change.getControlText();
      if (newText.equals(oldText)) return change;
      int newLength = newText.length();
      int oldLength = oldText.length();
      String result = applyPatterns(newText);
      if (result.equals(newText)) return change;
      int distance = result.length() - newLength;
      int start = change.getRangeStart();
      int end = change.getRangeEnd();
      int changedLength = change.getText().length();
      int close = start + changedLength + distance;
      if (!result.equals(newText) && end != oldLength) {
        close = -1; // reject partial match at middle
      }
      String text = ""; // prevent update
      if (close >= start) {
        text = result.substring(start, close);
      } else if (change.isDeleted()) {
        // prevent delete
        text = oldText.substring(start, end);
      }
      change.setText(text);
      int anchor = change.getControlAnchor();
      int caret = change.getControlCaretPosition();
      if (change.isReplaced()) {
        if (anchor == caret)
          distance = 0; // deletion prevented
        else
          distance = text.length();
        anchor = Math.min(anchor, caret);
      } else if (change.isDeleted()) {
        distance = 0;
        anchor = Math.min(anchor, caret);
      } else {
        distance = end - start + text.length();
      }
      anchor += distance;
      change.selectRange(anchor, anchor);
      return change;
    }

    private String applyCharcase(String string) {
      if (charCase == CharCase.UPPER)
        string = string.toUpperCase();
      else if (charCase == CharCase.LOWER)
        string = string.toLowerCase();
      return string;
    }

    private String applyPatterns(String string) {
      if (patterns.length == 0) return string;
      StringBuilder result = new StringBuilder();
      StringBuilder parser = new StringBuilder();
      parser.append(string);
      for (Pattern p : patterns) {
        if (parser.length() == 0)
          break;
        Matcher m = p.matcher(parser);
        if (m.find()) {
          result.append(m.group());
          parser.delete(0, m.end());
        } else if (partialMatch(p, parser)) {
          result.append(parser);
          break; // partial match
        } else {
          char c = getSingleChar(p);
          if (c == 0) break; // partial match
          result.append(c);
        }
      }
      return result.toString();
    }

    private boolean partialMatch(Pattern pattern, StringBuilder text) {
      String o = pattern.toString();
      String p = o.replaceAll("\\{.*?(\\d+)\\}", "{1,$1}");
      if (p.equals(o)) return false;
      Matcher m = Pattern.compile(p).matcher(text);
      if (!m.find()) return false;
      String result = m.group();
      text.delete(0, text.length()).append(result);
      return true;
    }

    private char getSingleChar(Pattern pattern) {
      String s = pattern.toString();
      s = s.replaceFirst(".*?\\^", "");
      if (s.length() == 1)
        return s.charAt(0);
      return s.matches("^\\\\.$") ? s.charAt(1) : 0;
    }
  }
}
