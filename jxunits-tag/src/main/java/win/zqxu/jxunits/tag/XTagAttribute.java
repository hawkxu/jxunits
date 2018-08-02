package win.zqxu.jxunits.tag;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used on setter method, to tell the builder some attribute settings.
 * 
 * @author zqxu
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface XTagAttribute {
  /**
   * Whether the attribute is required, default is false
   * 
   * @return true if the attribute is required, otherwise false
   */
  boolean required() default false;

  /**
   * Whether the attribute will be ignored, default is false
   * 
   * @return true for ignore the attribute, otherwise false
   */
  boolean ignored() default false;
}
