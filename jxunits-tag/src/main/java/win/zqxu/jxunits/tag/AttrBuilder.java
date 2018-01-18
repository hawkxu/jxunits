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
public @interface AttrBuilder {
  /** Whether the attribute is required */
  boolean required() default false;

  /** Whether the attribute is ignored */
  boolean ignored() default false;
}
