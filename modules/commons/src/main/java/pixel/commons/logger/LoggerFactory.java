/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.logger;

public class LoggerFactory {

    //region Fields & Properties

    private static LoggerStrategy strategy = new ConsoleLoggerStrategy();

    //endregion

    //region Constructors

    private LoggerFactory() {
        // no instantiation allowed
    }

    //endregion

    //region Private Functions


    //endregion

    //region Public Functions

    /**
     * Get a logger associated to given class reference
     *
     * @param classRef
     * @return
     */
    public static Logger getLogger(Class<?> classRef) {
        return strategy.createLogger(classRef);
    }

    /**
     * Get a logger associated to given class reference
     *
     * @param classRef
     * @param strategy
     * @return
     */
    public static Logger getLogger(Class<?> classRef, LoggerStrategy strategy) {
        return strategy.createLogger(classRef);
    }

    /**
     * Get a logger associated to given context
     *
     * @param context
     * @return
     */
    public static Logger getLogger(String context) {
        return strategy.createLogger(context);
    }


    /**
     * Get a logger associated to given context
     *
     * @param context
     * @param strategy
     * @return
     */
    public static Logger getLogger(String context, LoggerStrategy strategy) {
        return strategy.createLogger(context);
    }

    /**
     * Set factory logger strategy
     *
     * @param strategy
     */
    public static void setDefaultStrategy(LoggerStrategy strategy) {
        LoggerFactory.strategy = strategy;
    }

    //endregion
}
