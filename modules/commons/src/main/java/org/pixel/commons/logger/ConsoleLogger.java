/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class ConsoleLogger extends Logger {

    //region Properties

    private static LogLevel logLevel = LogLevel.DEBUG;

    private final String context;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param context Logger context.
     */
    public ConsoleLogger(String context) {
        this.context = context;
    }

    //endregion

    //region Private Functions

    /**
     * Prints given message to System Out.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    private void printToConsole(LogLevel level, String message, Object... params) {
        // TODO: allow customizable formats
        try {
            StringBuilder output = new StringBuilder(
                    level + "|" + context + "|" + formatMessage(message, params));

            for (Object param : params) {
                if (param instanceof Exception) {
                    output.append(System.lineSeparator()).append(toString((Exception) param));
                }
            }

            System.out.println(output);

        } catch (Exception e) {
            System.out.println("System Error: " + toString(e));
        }
    }

    /**
     * Formats the given message with the given parameters.
     *
     * @param message The message to format.
     * @param params  The parameters to format.
     */
    private String formatMessage(String message, Object... params) {
        int paramIndex = 0;
        while (message.contains("{}")) {
            message = message.replaceFirst("\\{}",
                    paramIndex < params.length ? params[paramIndex++].toString() : "");
        }

        return message;
    }

    //endregion

    //region Public Functions

    @Override
    public boolean isTraceEnabled() {
        return logLevel.getValue() <= LogLevel.TRACE.getValue();
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevel.getValue() <= LogLevel.DEBUG.getValue();
    }

    @Override
    public boolean isInfoEnabled() {
        return logLevel.getValue() <= LogLevel.INFO.getValue();
    }

    @Override
    public boolean isWarnEnabled() {
        return logLevel.getValue() <= LogLevel.WARN.getValue();
    }

    @Override
    public boolean isErrorEnabled() {
        return logLevel.getValue() <= LogLevel.ERROR.getValue();
    }

    /**
     * Get the current log level.
     *
     * @return The current log level.
     */
    public static LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Set the log level.
     *
     * @param logLevel The log level to set.
     */
    public static void setLogLevel(LogLevel logLevel) {
        ConsoleLogger.logLevel = logLevel;
    }

    /**
     * Log a TRACE level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void trace(String message, Object... params) {
        if (this.isTraceEnabled()) {
            this.printToConsole(LogLevel.TRACE, message, params);
        }
    }

    /**
     * Log a DEBUG level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void debug(String message, Object... params) {
        if (this.isDebugEnabled()) {
            this.printToConsole(LogLevel.DEBUG, message, params);
        }
    }

    /**
     * Log an INFO level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void info(String message, Object... params) {
        if (this.isInfoEnabled()) {
            this.printToConsole(LogLevel.INFO, message, params);
        }
    }

    /**
     * Log a WARN level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void warn(String message, Object... params) {
        if (this.isWarnEnabled()) {
            this.printToConsole(LogLevel.WARN, message, params);
        }
    }

    /**
     * Log an ERROR level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void error(String message, Object... params) {
        if (this.isErrorEnabled()) {
            this.printToConsole(LogLevel.ERROR, message, params);
        }
    }

    //endregion
}
