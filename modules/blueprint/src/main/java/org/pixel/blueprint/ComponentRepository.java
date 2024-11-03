package org.pixel.blueprint;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ComponentRepository {
    private final Map<Class<?>, Object> components = new HashMap<>();
    private final Map<String, Object> namedComponents = new HashMap<>();

    /**
     * Register a game instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     */
    public <T> void register(Class<T> clazz, T instance) {
        components.put(clazz, instance);
    }

    /**
     * Register a named instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     * @param name     The name to register with.
     */
    public <T> void register(Class<?> clazz, T instance, String name) {
        namedComponents.put(name, instance);
        components.put(clazz, instance); // Optional: Keeps the type-based registration
    }

    /**
     * Get a game instance based on the given class.
     * Use this if you are not running this command every-frame.
     *
     * @param clazz The class reference to register.
     * @param <T>   The class type.
     * @return The registered instance or null if it doesn't exist.
     */
    public <T> Optional<T> get(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(components.get(clazz)));
    }

    /**
     * Get a named instance based on the given class and name.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or empty Optional if it doesn't exist.
     */
    public <T> Optional<T> get(Class<T> clazz, String name) {
        return Optional.ofNullable(clazz.cast(namedComponents.get(name)));
    }

    /**
     * (Unsafe) Get a game instance based on the given class.
     * Use this if you are running this command every-frame.
     *
     * @param clazz The class reference to register.
     * @param <T>   The class type.
     * @return The registered instance or null if it doesn't exist.
     */
    public <T> T uget(Class<T> clazz) {
        return clazz.cast(components.get(clazz));
    }

    /**
     * (Unsafe) Get a named instance based on the given class and name. Use this if you are running this command every-frame.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or null if it doesn't exist.
     */
    public <T> T uget(Class<T> clazz, String name) {
        return clazz.cast(namedComponents.get(name));
    }

    /**
     * Deregister a game instance based on the given class.
     *
     * @param clazz The class reference to deregister.
     */
    public void deregister(Class<?> clazz) {
        components.remove(clazz);
    }

    /**
     * Clear all registered instances.
     */
    public void clear() {
        components.clear();
    }

}

