package org.pixel.ext.log4j;

import org.pixel.commons.logger.Logger;

public class Log4j2Logger extends Logger {

    private final org.apache.logging.log4j.Logger logger;

    /**
     * Constructor.
     *
     * @param classRef The class reference.
     */
    public Log4j2Logger(Class<?> classRef) {
        this.logger = org.apache.logging.log4j.LogManager.getLogger(classRef);
    }

    /**
     * Constructor.
     *
     * @param context The logger context.
     */
    public Log4j2Logger(String context) {
        this.logger = org.apache.logging.log4j.LogManager.getLogger(context);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override
    public void trace(String message, Object... params) {
        this.logger.trace(message, params);
    }

    @Override
    public void debug(String message, Object... params) {
        this.logger.debug(message, params);
    }

    @Override
    public void info(String message, Object... params) {
        this.logger.info(message, params);
    }

    @Override
    public void warn(String message, Object... params) {
        this.logger.warn(message, params);
    }

    @Override
    public void error(String message, Object... params) {
        this.logger.error(message, params);
    }
}
