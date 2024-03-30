package org.pixel.desktop.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class DataSerializer {

    private static final Logger log = LoggerFactory.getLogger(DataSerializer.class);

    /**
     * Serialize an instance to the given path.
     *
     * @param instance The instance to serialize.
     * @param path     The output path.
     * @return True if executed successfully or false otherwise.
     */
    public static boolean write(Object instance, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(instance);
            objectOutputStream.flush();
            objectOutputStream.close();
            return true;

        } catch (IOException e) {
            log.error("Exception caught!", e);
        }
        return false;
    }

    /**
     * Deserialize a file into an instance of the given class type.
     *
     * @param path The input path.
     * @param type The class type of the expected instance.
     * @param <T>  The type of the class to deserialize into.
     * @return An instance of the given class type or null if unable to deserialize.
     */
    public static <T> T read(String path, Class<T> type) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object deserializedInstance = objectInputStream.readObject();
            T instance = (T) deserializedInstance;
            objectInputStream.close();
            return instance;

        } catch (ClassNotFoundException | IOException e) {
            log.error("Exception caught!", e);
        }
        return null;
    }
}
