/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Logger {

    //region Internal Functions

    /**
     * @param e Exception instance.
     * @return Exception message.
     */
    protected String toString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return sw.toString();
    }

    //endregion

    //region Public Functions

    /**
     * Checks if logger has TRACE enabled.
     *
     * @return True if enabled.
     */
    public abstract boolean isTraceEnabled();

    /**
     * Checks if logger has DEBUG enabled.
     *
     * @return True if enabled.
     */
    public abstract boolean isDebugEnabled();

    /**
     * Checks if logger has INFO enabled.
     *
     * @return True if enabled.
     */
    public abstract boolean isInfoEnabled();

    /**
     * Checks if logger has WARN enabled.
     *
     * @return True if enabled.
     */
    public abstract boolean isWarnEnabled();

    /**
     * Checks if logger has ERROR enabled.
     *
     * @return True if enabled.
     */
    public abstract boolean isErrorEnabled();

    /**
     * Log a TRACE level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    public abstract void trace(String message, Object... params);

    /**
     * Log a DEBUG level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    public abstract void debug(String message, Object... params);

    /**
     * Log an INFO level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    public abstract void info(String message, Object... params);

    /**
     * Log an WARN level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    public abstract void warn(String message, Object... params);

    /**
     * Log an ERROR level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    public abstract void error(String message, Object... params);

    //endregion

}
