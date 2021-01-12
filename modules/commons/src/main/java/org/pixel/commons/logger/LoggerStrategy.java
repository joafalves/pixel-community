/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public interface LoggerStrategy {

    /**
     * Creates a logger object
     *
     * @return
     */
    Logger createLogger(Class<?> classRef);

    /**
     * Creates a logger object
     *
     * @param context
     * @return
     */
    Logger createLogger(String context);
}
