package org.pixel.ext.log4j;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerStrategy;

public class Log4j2LoggerStrategy implements LoggerStrategy  {

    @Override
    public Logger createLogger(Class<?> classRef) {
        return new Log4j2Logger(classRef);
    }

    @Override
    public Logger createLogger(String context) {
        return new Log4j2Logger(context);
    }
}
