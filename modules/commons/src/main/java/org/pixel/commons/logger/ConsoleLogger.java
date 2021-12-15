/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class ConsoleLogger extends Logger {

    //region Constructors

    /**
     * Constructor.
     *
     * @param context Logger context.
     */
    public ConsoleLogger(LoggerContext context) {
        super(context);
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
                    level + "|" + context.getIdentifier() + "|" + String.format(message, params));

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

    //endregion

    //region Public Functions

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
