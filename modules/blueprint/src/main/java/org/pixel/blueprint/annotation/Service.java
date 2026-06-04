package org.pixel.blueprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default ""; // Name of the instance to inject
    String[] tags() default {}; // Tags for filtering
}
