/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.util;

import java.util.concurrent.atomic.AtomicLong;

public class HashUtils {

    //region private properties

    private static final String UID_PREFIX = "px-";
    private static final AtomicLong uid = new AtomicLong(0);

    //endregion

    //region public static methods

    /**
     * Generate a unique identification value
     *
     * @return
     */
    public static String generateUID() {
        return UID_PREFIX + uid.incrementAndGet();
    }

    /**
     * Generate a unique identification value
     *
     * @return
     */
    public static long generateNumericUID() {
        return uid.incrementAndGet();
    }

    //endregion
}
