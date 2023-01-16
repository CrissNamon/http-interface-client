package tech.hiddenproject.hic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tech.hiddenproject.hic.data.RequestContent;

/**
 * @author Danila Rassokhin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GET {

  String value();

  RequestContent contentType() default RequestContent.APPLICATION_JSON;

}
