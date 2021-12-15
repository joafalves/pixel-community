/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.nio.ByteBuffer;

public class FontData {

    private final ByteBuffer source;

    /**
     * Constructor.
     *
     * @param source Source data.
     */
    public FontData(ByteBuffer source) {
        this.source = source;
    }

    /**
     * Get the source data.
     *
     * @return Source data.
     */
    public ByteBuffer getSource() {
        return source;
    }
}
