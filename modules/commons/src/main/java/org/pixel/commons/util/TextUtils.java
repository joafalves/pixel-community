/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import java.nio.ByteBuffer;

public class TextUtils {

    /**
     * Convert bytebuffer to String.
     *
     * @param buffer The buffer to convert.
     * @return The buffer represented as a String.
     */
    public static String convertBufferToString(ByteBuffer buffer) {
        byte[] array = new byte[buffer.limit()];
        buffer.get(array, 0, buffer.limit());
        return new String(array);
    }

}
