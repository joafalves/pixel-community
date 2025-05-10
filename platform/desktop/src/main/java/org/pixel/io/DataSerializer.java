package org.pixel.io;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.*;

@Deprecated
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
        try (FileOutputStream fileOutputStream = new FileOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(instance);
            return true;

        } catch (IOException e) {
            log.error("Failed to write object to file: {0}.", path, e);
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
        try (FileInputStream fileInputStream = new FileInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Object deserializedInstance = objectInputStream.readObject();

            if (type.isInstance(deserializedInstance)) {
                return type.cast(deserializedInstance);
            } else {
                log.warn("Deserialized object is not of the expected type: {0}.", type.getName());
            }
        } catch (ClassNotFoundException e) {
            log.error("Failed to deserialize object. Class not found.", e);
        } catch (IOException e) {
            log.error("I/O error occurred while reading the file: {0}.", path, e);
        }
        return null;
    }
}
