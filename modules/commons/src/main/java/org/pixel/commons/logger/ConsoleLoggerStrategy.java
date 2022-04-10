/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class ConsoleLoggerStrategy implements LoggerStrategy {

    /**
     * Creates a logger object.
     *
     * @param classRef The class reference.
     * @return Logger instance.
     */
    @Override
    public Logger createLogger(Class<?> classRef) {
        return new ConsoleLogger(classRef.getSimpleName());
    }

    /**
     * Creates a logger object.
     *
     * @param context Logger context.
     * @return Logger instance.
     */
    @Override
    public Logger createLogger(String context) {
        return new ConsoleLogger(context);
    }

}
