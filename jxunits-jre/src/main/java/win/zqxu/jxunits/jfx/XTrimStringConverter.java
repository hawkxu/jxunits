package win.zqxu.jxunits.jfx;

import javafx.util.StringConverter;

/**
 * Converter to convert string to value (another string) with trim automatically, the
 * value (String) will not be trim while convert to string
 * 
 * @author zqxu
 */
public class XTrimStringConverter extends StringConverter<String> {

  /**
   * Convert string value to string, no null will be return
   */
  @Override
  public String toString(String value) {
    return value == null ? "" : value;
  }

  /**
   * Convert text to string value, the text will be trim automatically, no null will be
   * return
   */
  @Override
  public String fromString(String text) {
    return text == null ? "" : text.trim();
  }
}
