/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author João Filipe Alves
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentImporterInfo {
    Class type();

    String[] extension();
}
