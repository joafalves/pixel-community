/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

public interface ContentImporter<T> {

    /**
     * Imports and transforms the content from the given path to the given type.
     *
     * @param ctx The context of the content.
     * @return The transformed content.
     */
    T process(ImportContext ctx);
}
