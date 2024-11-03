package org.pixel.commons;

import org.pixel.commons.annotations.Auto;

import java.lang.reflect.Field;
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

    /**
     * Automatically injects fields annotated with @Auto in the provided instance.
     * Returns true if all @Auto fields are successfully injected, or if there are no @Auto fields.
     *
     * @param instance The instance to bootstrap.
     * @return true if all @Auto fields are assigned, false if any field remains unassigned.
     */
    public static boolean bootstrap(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        boolean allInjected = true;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Auto.class)) {
                field.setAccessible(true);
                try {
                    if (field.get(instance) == null) { // Only inject if field is null
                        Object dependency = uget(field.getType());
                        if (dependency != null) {
                            field.set(instance, dependency);
                        } else {
                            allInjected = false; // Mark as false if the dependency is missing
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject instance for field: " + field.getName(), e);
                }
            }
        }

        return allInjected;
    }
}
