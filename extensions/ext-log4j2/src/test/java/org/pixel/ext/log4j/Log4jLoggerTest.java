package org.pixel.ext.log4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.commons.logger.LoggerFactory;

public class Log4jLoggerTest {

    @Test
    public void testLoggerInstanceType() {
        LoggerFactory.setDefaultStrategy(new Log4jLoggerStrategy());
        Assertions.assertInstanceOf(Log4jLogger.class, LoggerFactory.getLogger(Log4jLoggerTest.class));
    }
}
