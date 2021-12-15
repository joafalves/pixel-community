/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import java.nio.ByteBuffer;

public class TextUtils {

    /**
     * Convert bytebuffer to string.
     *
     * @param buffer Input buffer.
     * @return String.
     */
    public static String convertToString(ByteBuffer buffer) {
        byte[] array = new byte[buffer.limit()];
        buffer.get(array, 0, buffer.limit());
        return new String(array);
    }

}
