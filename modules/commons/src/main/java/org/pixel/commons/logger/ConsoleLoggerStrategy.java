/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class ConsoleLoggerStrategy implements LoggerStrategy {

    /**
     * Creates a logger object
     *
     * @return
     */
    @Override
    public Logger createLogger(Class<?> classRef) {
        return new ConsoleLogger(LogManager.getContext(classRef));
    }

    /**
     * Creates a logger object
     *
     * @param context
     * @return
     */
    @Override
    public Logger createLogger(String context) {
        return new ConsoleLogger(LogManager.getContext(context));
    }

}
