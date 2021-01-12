/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

import java.util.concurrent.ConcurrentHashMap;

public class LogManager {

    //region Fields & Properties

    private static final ConcurrentHashMap<String, LoggerContext> logContextMap = new ConcurrentHashMap<>();
    private static LogLevel defaultLevel = LogLevel.DEBUG;

    //endregion

    //region Constructors

    /**
     * Private constructor
     */
    private LogManager() {
        // disable new instances..
    }

    //endregion

    //region Public Functions

    /**
     * Creates or gets a logger context for a given class identifier
     *
     * @param classRef
     * @return
     */
    public static LoggerContext getContext(Class<?> classRef) {
        return getContext(classRef.getSimpleName());
    }

    /**
     * Creates or gets a logger context for a given identifier
     *
     * @param context
     * @return
     */
    public static LoggerContext getContext(String context) {
        LoggerContext loggerContext = logContextMap.get(context);
        if (loggerContext == null) {
            loggerContext = new LoggerContext(context);
            loggerContext.setLevel(defaultLevel);
            logContextMap.put(context, loggerContext);
        }

        return loggerContext;
    }

    /**
     * Sets the log level for all existing log contexts and assigns the new default log level
     *
     * @param level
     */
    public static void setLogLevel(LogLevel level) {
        logContextMap.forEach((s, loggerContext) -> loggerContext.setLevel(level));
        setDefaultLogLevel(level);
    }

    /**
     * Sets the default log level (applied when creating new loggers)
     *
     * @param level
     */
    public static void setDefaultLogLevel(LogLevel level) {
        defaultLevel = level;
    }

    /**
     * Gets the default log level. To
     *
     * @return
     */
    public static LogLevel getDefaultLogLevel() {
        return defaultLevel;
    }

    //endregion
}
