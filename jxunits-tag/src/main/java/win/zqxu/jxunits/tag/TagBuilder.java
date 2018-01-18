package win.zqxu.jxunits.tag;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used on tag class, to tell the builder some tag settings. the builder should
 * scan this annotation in the tag class and its ancestor classes
 * 
 * @author zqxu
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface TagBuilder {
  ContentType content() default ContentType.empty;
}
