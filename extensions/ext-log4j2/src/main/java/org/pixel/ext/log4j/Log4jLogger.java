package org.pixel.ext.log4j;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerContext;

public class Log4jLogger extends Logger {

    private final org.apache.logging.log4j.Logger logger;

    /**
     * Constructor.
     *
     * @param context Logger context.
     */
    public Log4jLogger(LoggerContext context) {
        super(context);
        this.logger = org.apache.logging.log4j.LogManager.getLogger(context.getIdentifier());
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
