/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public enum LogLevel {
    //region Fields & Properties

    OFF((byte) 0b1),    // 1
    TRACE((byte) 0b10), // 2
    DEBUG((byte) 0b11), // 3
    INFO((byte) 0b100), // 4
    WARN((byte) 0b101), // 5
    ERROR((byte) 0b110);// 6

    private final byte value;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param value The value of the log level.
     */
    LogLevel(byte value) {
        this.value = value;
    }

    //endregion

    //region Public Functions

    /**
     * Get the value of the log level.
     *
     * @return The value of the log level.
     */
    public byte getValue() {
        return this.value;
    }

    //endregion
}
