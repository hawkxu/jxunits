package win.zqxu.jxunits.jre;

import java.util.Locale;
import java.util.ResourceBundle;

public class XResource {
  private static final String BUNDLE_NAME = "win.zqxu.jxunits.locale";

  /**
   * Get java-units resource string by the key for default locale
   * 
   * @param key
   *          the resource key
   * @return string by the key for default locale
   */
  public static String getString(String key) {
    return getString(key, Locale.getDefault());
  }

  /**
   * Get java-units resource string by the key and locale
   * 
   * @param key
   *          the resource key
   * @param locale
   *          the locale
   * @return resource string by the key and locale
   */
  public static String getString(String key, Locale locale) {
    return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
  }
}
