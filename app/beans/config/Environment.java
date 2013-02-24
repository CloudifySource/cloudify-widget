package beans.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: guym
 * Date: 1/23/13
 * Time: 2:34 PM
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.FIELD, ElementType.METHOD, ElementType.TYPE} )
public @interface Environment {
    String key();
}
