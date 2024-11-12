package org.pixel.blueprint;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class BlueprintRepository {

    private static final BlueprintRepository defaultRepository = new BlueprintRepository();

    private final Map<Class<?>, Object> components = new HashMap<>();
    private final Map<String, Object> namedComponents = new HashMap<>();
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final Map<String, Object> namedServices = new HashMap<>();

    /**
     * Get the default repository.
     *
     * @return The default repository.
     */
    public static BlueprintRepository getDefault() {
        return defaultRepository;
    }

    /**
     * Register a component instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     */
    public <T> void registerComponent(Class<T> clazz, T instance) {
        components.put(clazz, instance);
    }

    /**
     * Register a named component instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     * @param name     The name to register with.
     */
    public <T> void registerComponent(Class<?> clazz, T instance, String name) {
        namedComponents.put(name, instance);
        components.put(clazz, instance); // Optional: Keeps the type-based registration
    }

    /**
     * Register a service instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     */
    public <T> void registerService(Class<T> clazz, T instance) {
        services.put(clazz, instance);
    }

    /**
     * Register a named service instance.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     * @param name     The name to register with.
     */
    public <T> void registerService(Class<?> clazz, T instance, String name) {
        namedServices.put(name, instance);
        services.put(clazz, instance); // Optional: Keeps the type-based registration
    }

    /**
     * Get am instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> Optional<T> get(Class<T> clazz) {
        T instance = clazz.cast(services.getOrDefault(clazz, components.get(clazz)));
        return Optional.ofNullable(instance);
    }

    /**
     * Get a named instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> Optional<T> get(Class<T> clazz, String name) {
        T instance = clazz.cast(namedServices.getOrDefault(name, namedComponents.get(name)));
        return Optional.ofNullable(instance);
    }

    /**
     * (Unsafe) Get am instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> T uget(Class<T> clazz) {
        return clazz.cast(services.getOrDefault(clazz, components.get(clazz)));
    }

    /**
     * (Unsafe) Get a named instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> T uget(Class<T> clazz, String name) {
        return clazz.cast(namedServices.getOrDefault(name, namedComponents.get(name)));
    }

    /**
     * Get a component instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> Optional<T> getComponent(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(components.get(clazz)));
    }

    /**
     * Get a named component instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> Optional<T> getComponent(Class<T> clazz, String name) {
        return Optional.ofNullable(clazz.cast(namedComponents.get(name)));
    }

    /**
     * Get a component instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> T ugetComponent(Class<T> clazz) {
        return clazz.cast(components.get(clazz));
    }

    /**
     * Get a named component instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> T ugetComponent(Class<T> clazz, String name) {
        return clazz.cast(namedComponents.get(name));
    }

    /**
     * Get a service instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered service instance or an empty Optional if not found.
     */
    public <T> Optional<T> getService(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(services.get(clazz)));
    }

    /**
     * Get a named service instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered service instance or an empty Optional if not found.
     */
    public <T> Optional<T> getService(Class<T> clazz, String name) {
        return Optional.ofNullable(clazz.cast(namedServices.get(name)));
    }

    /**
     * Get a service instance by its class type.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered service instance or an empty Optional if not found.
     */
    public <T> T ugetService(Class<T> clazz) {
        return clazz.cast(services.get(clazz));
    }

    /**
     * Get a named service instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered service instance or an empty Optional if not found.
     */
    public <T> T ugetService(Class<T> clazz, String name) {
        return clazz.cast(namedServices.get(name));
    }

    /**
     * Deregister a component by its class type.
     *
     * @param clazz The class reference to deregister.
     */
    public void deregisterComponent(Class<?> clazz) {
        components.remove(clazz);
    }

    /**
     * Deregister a service by its class type.
     *
     * @param clazz The class reference to deregister.
     */
    public void deregisterService(Class<?> clazz) {
        services.remove(clazz);
    }

    /**
     * Returns a boolean indicating if the repository has any data loaded.
     * @return True if there is any data loaded into the repository.
     */
    public boolean hasData() {
        return (!components.isEmpty() || !services.isEmpty());
    }

    /**
     * Clear all registered components and services.
     */
    public void clear() {
        components.clear();
        namedComponents.clear();
        services.clear();
        namedServices.clear();
    }
}
