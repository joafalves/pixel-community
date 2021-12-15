/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public interface LoggerStrategy {

    /**
     * Creates a logger object based on the given class.
     *
     * @return Logger instance.
     */
    Logger createLogger(Class<?> classRef);

    /**
     * Creates a logger object based on the given context.
     *
     * @param context Logger context.
     * @return Logger instance.
     */
    Logger createLogger(String context);
}
