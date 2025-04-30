/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TextHelper {

    /**
     * Generate a base64 representation of a string.
     *
     * @param input The input string to encode.
     * @return The base64 representation of the input string.
     */
    public static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a base64 string.
     *
     * @param input The base64 string to decode.
     * @return The decoded string.
     */
    public static String decodeBase64(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    }

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
