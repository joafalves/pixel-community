/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {

    /**
     * Generate a MD5 hash from a string.
     *
     * @param input The input string to hash.
     * @return The MD5 hash of the input string.
     */
    public static String md5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(input));
            return String.format("%032x", new BigInteger(1, md5.digest()));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    //endregion
}
