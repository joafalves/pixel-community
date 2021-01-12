/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.nio.ByteBuffer;

public class FontData {

    private ByteBuffer source;

    public FontData(ByteBuffer source) {
        this.source = source;
    }

    public ByteBuffer getSource() {
        return source;
    }
}
