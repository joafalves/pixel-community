package org.pixel.ext.log4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class Log4j2LoggerTest {

    @Test
    public void testLoggerInstanceType() {
        LoggerFactory.setDefaultStrategy(new Log4j2LoggerStrategy());
        Assertions.assertInstanceOf(Log4j2Logger.class, LoggerFactory.getLogger(Log4j2LoggerTest.class));

        Logger logger = LoggerFactory.getLogger(Log4j2LoggerTest.class);
        logger.info("This is a test message with arguments {1} {0}.", "[A]", "[B]");
    }
}
