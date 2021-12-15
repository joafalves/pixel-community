/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class LoggerFactory {

    //region Fields & Properties

    private static LoggerStrategy strategy = new ConsoleLoggerStrategy();

    //endregion

    //region Constructors

    private LoggerFactory() {
        // no instantiation allowed
    }

    //endregion

    //region Public Functions

    /**
     * Get a logger associated to given class reference.
     *
     * @param classRef Class to be used as context.
     * @return Logger instance.
     */
    public static Logger getLogger(Class<?> classRef) {
        return strategy.createLogger(classRef);
    }

    /**
     * Get a logger associated to given class reference.
     *
     * @param classRef Class to be used as context.
     * @param strategy Logger strategy.
     * @return Logger instance.
     */
    public static Logger getLogger(Class<?> classRef, LoggerStrategy strategy) {
        return strategy.createLogger(classRef);
    }

    /**
     * Get a logger associated to given context.
     *
     * @param context Logger context.
     * @return Logger instance.
     */
    public static Logger getLogger(String context) {
        return strategy.createLogger(context);
    }


    /**
     * Get a logger associated to given context.
     *
     * @param context Logger context.
     * @param strategy Logger strategy.
     * @return Logger instance.
     */
    public static Logger getLogger(String context, LoggerStrategy strategy) {
        return strategy.createLogger(context);
    }

    /**
     * Set factory logger strategy.
     *
     * @param strategy Logger strategy.
     */
    public static void setDefaultStrategy(LoggerStrategy strategy) {
        LoggerFactory.strategy = strategy;
    }

    //endregion
}
