/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

public interface ContentImporter<T> {

    /**
     * Processes raw data and returns a content object.
     *
     * @param ctx The context of the raw content to process.
     * @return The processed content item.
     */
    T process(ImportContext ctx);
}
