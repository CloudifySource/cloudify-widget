package data.validation;

import play.data.validation.Constraints;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * User: guym
 * Date: 1/23/13
 * Time: 8:44 AM
 */
public class GsConstraints {

    /**
      * Defines a url constraint for a string field.
      */
     @Target({FIELD})
     @Retention(RUNTIME)
     @Constraint(validatedBy = UrlValidator.class)
     @play.data.Form.Display(name="constraint.email", attributes={})
     public static @interface Url {
         String message() default UrlValidator.message;
         Class<?>[] groups() default {};
         Class<? extends Payload>[] payload() default {};
     }

     /**
      * Validator for <code>@Url</code> fields.
      */
     public static class UrlValidator extends Constraints.Validator<String> implements ConstraintValidator<Url, String> {

         final static public String message = "error.url";
         org.apache.commons.validator.UrlValidator urlValidator = new org.apache.commons.validator.UrlValidator();

         public UrlValidator() {}

         public void initialize(Url constraintAnnotation) {
         }

         public boolean isValid(String object) {
             if(object == null || object.length() == 0 || object.startsWith("http://localhost")) {
                 return true;
             }

             return urlValidator.isValid( object );
         }

     }

     /**
      * Constructs a 'email' validator.
      */
     public static Constraints.Validator<String> url() {
         return new UrlValidator();
     }

}
