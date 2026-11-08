/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Logger {

    private static final CopyOnWriteArrayList<LogListener> LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Receive every message logged from now on (at or above each logger's level), in addition to its normal output.
     */
    public static void addListener(LogListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        LISTENERS.addIfAbsent(listener);
    }

    public static void removeListener(LogListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * @return Whether anyone listens (so loggers only format for them when needed).
     */
    protected static boolean hasListeners() {
        return !LISTENERS.isEmpty();
    }

    /**
     * Pass a logged message to the listeners (implementations call this for every message they output). A failing
     * listener never breaks logging.
     */
    protected static void notifyListeners(LogLevel level, String context, String message) {
        for (LogListener listener : LISTENERS) {
            try {
                listener.onLog(level, context, message);
            } catch (RuntimeException e) {
                // a broken listener must not break the caller's logging
            }
        }
    }

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
