package org.pixel.arkade;

import org.pixel.commons.PropertyLoader;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.ext.log4j.Log4j2LoggerStrategy;

import java.util.Properties;

public class Application {

    private static final String SERVER_PROPERTIES_FILENAME = "server.properties";

    static {
        LoggerFactory.setDefaultStrategy(new Log4j2LoggerStrategy());
    }

    public static void main(String[] args) {
        final var log = LoggerFactory.getLogger(Application.class);

        // load server.properties form the resources:
        final var properties = new Properties();
        try (var inputStream = Application.class.getClassLoader().getResourceAsStream(SERVER_PROPERTIES_FILENAME)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                log.error("Could not find 'server.properties' file, terminating application...");
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Error loading server.properties file: {0}", e.getMessage(), e);
            System.exit(1);
        }

        // load properties into settings:
        ArkadeSettings settings = null;
        try {
            settings = PropertyLoader.load(ArkadeSettings.class, properties);
        } catch (Exception e) {
            log.error("Error loading server.properties file: {0}", e.getMessage(), e);
            System.exit(1);
        }

        // create and run the Arkade application:
        var arkade = new Arkade(settings);
        try {
            log.info("Initializing application...");
            arkade.init();
            log.debug("Application initialized.");

            log.info("Running application...");
            arkade.run();
            log.info("Application exited.");

        } catch (Exception e) {
            log.error("Error running application!", e);
        } finally {
            log.info("Disposing application...");
            arkade.dispose();
            log.info("Application finished.");
        }
    }
}
