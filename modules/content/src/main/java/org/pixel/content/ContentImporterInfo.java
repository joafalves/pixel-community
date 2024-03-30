/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ContentImporterInfo {

    /**
     * @return The output class type.
     */
    Class<?> type();

    /**
     * @return The associated file extensions.
     */
    String[] extension();
}
