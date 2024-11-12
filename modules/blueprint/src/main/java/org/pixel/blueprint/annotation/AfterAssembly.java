package org.pixel.blueprint.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AfterAssembly {
    boolean async() default false;
}
