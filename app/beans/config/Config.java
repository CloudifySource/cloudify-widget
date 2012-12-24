package beans.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 2:04 PM
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.FIELD, ElementType.METHOD, ElementType.TYPE} )
public @interface Config {
    String playKey() default "";
    boolean ignoreNullValues() default true;// in case we have a primitive and we are trying to set it a NULL value
}
