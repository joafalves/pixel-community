package org.pixel.network;

import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.network.command.HelloCommand;
import org.pixel.network.command.MessageCommand;

public class DataSerializer {

    private static final Logger log = LoggerFactory.getLogger(DataSerializer.class);

    private static final Fury fury;
    private static final Fury unsafeFury;

    static {
        fury = Fury.builder().withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                .build();

        // register built-in cmd classes:
        whitelist(HelloCommand.class);
        whitelist(MessageCommand.class);

        unsafeFury = Fury.builder().withLanguage(Language.JAVA)
                .requireClassRegistration(false)
                .build();
    }

    private DataSerializer() {
        // private constructor to prevent instantiation
    }

    private static void whitelist(Class<?> clazz) {
        fury.register(clazz);
    }

    public static byte[] serialize(Object object) {
        try {
            return fury.serialize(object);
        } catch (Exception e) {
            log.error("Error serializing object: {0}.", object.getClass().getName(), e);
        }
        return null;
    }

    public static byte[] unsafeSerialize(Object object) {
        try {
            return unsafeFury.serialize(object);
        } catch (Exception e) {
            log.error("Error serializing object: {0}.", object.getClass().getName(), e);
        }
        return null;
    }

    public static Object deserialize(byte[] data) {
        try {
            return fury.deserialize(data);
        } catch (Exception e) {
            log.error("Error deserializing object.", e);
        }
        return null;
    }

    public static Object unsafeDeserialize(byte[] data) {
        try {
            return unsafeFury.deserialize(data);
        } catch (Exception e) {
            log.error("Error deserializing object.", e);
        }
        return null;
    }

}
