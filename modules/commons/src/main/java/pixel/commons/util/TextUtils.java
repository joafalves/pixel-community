/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.util;

import java.nio.ByteBuffer;

public class TextUtils {

    /**
     * Convert bytebuffer to string
     *
     * @param buffer
     * @return
     */
    public static String convertToString(ByteBuffer buffer) {
        byte[] array = new byte[buffer.limit()];
        buffer.get(array, 0, buffer.limit());
        return new String(array);
    }

}
