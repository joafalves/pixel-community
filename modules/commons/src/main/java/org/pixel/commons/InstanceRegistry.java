package org.pixel.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstanceRegistry {
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Register a game instance.
     *
     * @param clazz   The class reference to register.
     * @param instance The instance to register.
     */
    public static <T> void register(Class<T> clazz, T instance) {
        instances.put(clazz, instance);
    }

    /**
     * Get a game instance based on the given class.
     * Use this if you are not running this command every-frame.
     *
     * @param clazz The class reference to register.
     * @param <T>   The class type.
     * @return The registered instance or null if it doesn't exist.
     */
    public static <T> Optional<T> get(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(instances.get(clazz)));
    }

    /**
     * (Unsafe) Get a game instance based on the given class.
     * Use this if you are running this command every-frame.
     *
     * @param clazz The class reference to register.
     * @param <T>   The class type.
     * @return The registered instance or null if it doesn't exist.
     */
    public static <T> T uget(Class<T> clazz) {
        return clazz.cast(instances.get(clazz));
    }

    /**
     * Deregister a game instance based on the given class.
     *
     * @param clazz The class reference to deregister.
     */
    public static void deregister(Class<?> clazz) {
        instances.remove(clazz);
    }

    /**
     * Clear all registered instances.
     */
    public static void clear() {
        instances.clear();
    }
}
