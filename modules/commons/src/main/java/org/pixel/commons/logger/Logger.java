/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Logger {
    //region Fields & Properties

    protected LoggerContext context;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param context Logger context.
     */
    public Logger(LoggerContext context) {
        this.context = context;
    }

    //endregion

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
    public boolean isTraceEnabled() {
        return this.context.getLevel().getValue() <= LogLevel.TRACE.getValue();
    }

    /**
     * Checks if logger has DEBUG enabled.
     *
     * @return True if enabled.
     */
    public boolean isDebugEnabled() {
        return this.context.getLevel().getValue() <= LogLevel.DEBUG.getValue();
    }

    /**
     * Checks if logger has INFO enabled.
     *
     * @return True if enabled.
     */
    public boolean isInfoEnabled() {
        return this.context.getLevel().getValue() <= LogLevel.INFO.getValue();
    }

    /**
     * Checks if logger has WARN enabled.
     *
     * @return True if enabled.
     */
    public boolean isWarnEnabled() {
        return this.context.getLevel().getValue() <= LogLevel.WARN.getValue();
    }

    /**
     * Checks if logger has ERROR enabled.
     *
     * @return True if enabled.
     */
    public boolean isErrorEnabled() {
        return this.context.getLevel().getValue() <= LogLevel.ERROR.getValue();
    }

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

    /**
     * Get logger context.
     *
     * @return Logger context.
     */
    public LoggerContext getContext() {
        return context;
    }

    /**
     * Set logger context.
     *
     * @param context Logger context.
     */
    public void setContext(LoggerContext context) {
        this.context = context;
    }

    //endregion

}
