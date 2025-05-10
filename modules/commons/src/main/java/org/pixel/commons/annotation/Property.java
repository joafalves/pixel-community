package org.pixel.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Property {
    /**
     * Determines if the property is mandatory.
     **/
    boolean mandatory() default false;

    /**
     * The default value of the property.
     */
    String defaultValue() default "";
}

