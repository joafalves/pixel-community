package org.pixel.blueprint;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintRepository {

    private static final BlueprintRepository defaultRepository = new BlueprintRepository();

    // Instance storage (thread-safe)
    private final Map<Class<?>, Object> components = new ConcurrentHashMap<>();
    private final Map<String, Object> namedComponents = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private final Map<String, Object> namedServices = new ConcurrentHashMap<>();

    // Tag storage (thread-safe)
    private final Map<String, Set<Object>> taggedComponents = new ConcurrentHashMap<>();
    private final Map<String, Set<Object>> taggedServices = new ConcurrentHashMap<>();

    // Prototype factory storage (thread-safe)
    private final Map<String, Method> prototypeComponentFactories = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> prototypeServiceFactories = new ConcurrentHashMap<>();

    /**
     * Get the default repository.
     *
     * @return The default repository.
     */
    public static BlueprintRepository getDefault() {
        return defaultRepository;
    }

    // Getters for internal maps (used by BlueprintUtil and other internal classes)
    public Map<Class<?>, Object> getComponents() {
        return components;
    }

    public Map<String, Object> getNamedComponents() {
        return namedComponents;
    }

    public Map<Class<?>, Object> getServices() {
        return services;
    }

    public Map<String, Object> getNamedServices() {
        return namedServices;
    }

    public Map<String, Set<Object>> getTaggedComponents() {
        return taggedComponents;
    }

    public Map<String, Set<Object>> getTaggedServices() {
        return taggedServices;
    }

    public Map<String, Method> getPrototypeComponentFactories() {
        return prototypeComponentFactories;
    }

    public Map<String, Class<?>> getPrototypeServiceFactories() {
        return prototypeServiceFactories;
    }

    // region Registration

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
        components.put(clazz, instance);
    }

    /**
     * Register a named component instance with tags.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     * @param name     The name to register with.
     * @param tags     The tags to associate with this component.
     */
    public <T> void registerComponent(Class<?> clazz, T instance, String name, String[] tags) {
        namedComponents.put(name, instance);
        components.put(clazz, instance);
        for (String tag : tags) {
            taggedComponents.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet()).add(instance);
        }
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
        services.put(clazz, instance);
    }

    /**
     * Register a named service instance with tags.
     *
     * @param clazz    The class reference to register.
     * @param instance The instance to register.
     * @param name     The name to register with.
     * @param tags     The tags to associate with this service.
     */
    public <T> void registerService(Class<?> clazz, T instance, String name, String[] tags) {
        namedServices.put(name, instance);
        services.put(clazz, instance);
        for (String tag : tags) {
            taggedServices.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet()).add(instance);
        }
    }

    /**
     * Register a prototype component factory.
     *
     * @param name   The name to register with.
     * @param method The factory method to invoke for new instances.
     * @param tags   The tags to associate with this component.
     */
    public void registerPrototypeComponent(String name, Method method, String[] tags) {
        prototypeComponentFactories.put(name, method);
        for (String tag : tags) {
            taggedComponents.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet());
        }
    }

    /**
     * Register a prototype service factory.
     *
     * @param name  The name to register with.
     * @param clazz The class to instantiate for new instances.
     * @param tags  The tags to associate with this service.
     */
    public void registerPrototypeService(String name, Class<?> clazz, String[] tags) {
        prototypeServiceFactories.put(name, clazz);
        for (String tag : tags) {
            taggedServices.computeIfAbsent(tag, k -> ConcurrentHashMap.newKeySet());
        }
    }

    // endregion

    // region Retrieval by type

    /**
     * Get an instance by its class type.
     * Supports interface binding by searching for implementations.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or an empty Optional if not found.
     */
    public <T> Optional<T> get(Class<T> clazz) {
        // Direct type lookup
        T instance = clazz.cast(services.getOrDefault(clazz, components.get(clazz)));
        if (instance != null) {
            return Optional.of(instance);
        }

        // Interface binding: search for implementations in services
        for (Map.Entry<Class<?>, Object> entry : services.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return Optional.of(clazz.cast(entry.getValue()));
            }
        }

        // Interface binding: search for implementations in components
        for (Map.Entry<Class<?>, Object> entry : components.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return Optional.of(clazz.cast(entry.getValue()));
            }
        }

        return Optional.empty();
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
        // Check named instances
        Object namedInstance = namedServices.getOrDefault(name, namedComponents.get(name));
        if (namedInstance != null) {
            return Optional.of(clazz.cast(namedInstance));
        }

        // Check prototype factories
        if (prototypeServiceFactories.containsKey(name)) {
            return Optional.empty(); // Prototypes need to be created on demand via the assembler
        }
        if (prototypeComponentFactories.containsKey(name)) {
            return Optional.empty(); // Prototypes need to be created on demand via the assembler
        }

        return Optional.empty();
    }

    /**
     * (Unsafe) Get an instance by its class type.
     * Supports interface binding by searching for implementations.
     *
     * @param clazz The class reference to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or null if not found.
     */
    public <T> T uget(Class<T> clazz) {
        return get(clazz).orElse(null);
    }

    /**
     * (Unsafe) Get a named instance.
     *
     * @param clazz The class reference to validate type.
     * @param name  The name of the instance to retrieve.
     * @param <T>   The class type.
     * @return The registered instance or null if not found.
     */
    public <T> T uget(Class<T> clazz, String name) {
        return get(clazz, name).orElse(null);
    }

    // endregion

    // region Component retrieval

    public <T> Optional<T> getComponent(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(components.get(clazz)));
    }

    public <T> Optional<T> getComponent(Class<T> clazz, String name) {
        return Optional.ofNullable(clazz.cast(namedComponents.get(name)));
    }

    public <T> T ugetComponent(Class<T> clazz) {
        return clazz.cast(components.get(clazz));
    }

    public <T> T ugetComponent(Class<T> clazz, String name) {
        return clazz.cast(namedComponents.get(name));
    }

    // endregion

    // region Service retrieval

    public <T> Optional<T> getService(Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(services.get(clazz)));
    }

    public <T> Optional<T> getService(Class<T> clazz, String name) {
        return Optional.ofNullable(clazz.cast(namedServices.get(name)));
    }

    public <T> T ugetService(Class<T> clazz) {
        return clazz.cast(services.get(clazz));
    }

    public <T> T ugetService(Class<T> clazz, String name) {
        return clazz.cast(namedServices.get(name));
    }

    // endregion

    // region Tag-based retrieval

    /**
     * Get all instances tagged with the specified tag.
     *
     * @param clazz The type to filter by.
     * @param tag   The tag to search for.
     * @param <T>   The class type.
     * @return A list of matching instances.
     */
    public <T> List<T> getByTag(Class<T> clazz, String tag) {
        List<T> result = new ArrayList<>();
        Set<Object> tagged = new LinkedHashSet<>();
        tagged.addAll(taggedServices.getOrDefault(tag, Collections.emptySet()));
        tagged.addAll(taggedComponents.getOrDefault(tag, Collections.emptySet()));

        for (Object instance : tagged) {
            if (clazz.isInstance(instance)) {
                result.add(clazz.cast(instance));
            }
        }
        return result;
    }

    /**
     * Get all instances tagged with ANY of the specified tags.
     *
     * @param clazz The type to filter by.
     * @param tags  The tags to search for.
     * @param <T>   The class type.
     * @return A list of matching instances (union of all tags).
     */
    public <T> List<T> getByTags(Class<T> clazz, String... tags) {
        Set<T> result = new LinkedHashSet<>();
        for (String tag : tags) {
            result.addAll(getByTag(clazz, tag));
        }
        return new ArrayList<>(result);
    }

    /**
     * Get all component and service instances tagged with the specified tag.
     *
     * @param tag The tag to search for.
     * @return A list of matching instances.
     */
    public List<Object> getAllByTag(String tag) {
        Set<Object> result = new LinkedHashSet<>();
        result.addAll(taggedServices.getOrDefault(tag, Collections.emptySet()));
        result.addAll(taggedComponents.getOrDefault(tag, Collections.emptySet()));
        return new ArrayList<>(result);
    }

    /**
     * Get all component and service instances tagged with ANY of the specified tags.
     *
     * @param tags The tags to search for.
     * @return A list of matching instances (union of all tags).
     */
    public List<Object> getAllByTags(String... tags) {
        Set<Object> result = new LinkedHashSet<>();
        for (String tag : tags) {
            result.addAll(taggedServices.getOrDefault(tag, Collections.emptySet()));
            result.addAll(taggedComponents.getOrDefault(tag, Collections.emptySet()));
        }
        return new ArrayList<>(result);
    }

    // endregion

    // region Prototype factories

    public Optional<Method> getPrototypeComponentFactory(String name) {
        return Optional.ofNullable(prototypeComponentFactories.get(name));
    }

    public Optional<Class<?>> getPrototypeServiceFactory(String name) {
        return Optional.ofNullable(prototypeServiceFactories.get(name));
    }

    // endregion

    // region Deregistration

    /**
     * Deregister a component by its class type.
     *
     * @param clazz The class reference to deregister.
     */
    public void deregisterComponent(Class<?> clazz) {
        Object removed = components.remove(clazz);
        if (removed != null) {
            // Remove from tag mappings
            for (Set<Object> instances : taggedComponents.values()) {
                instances.remove(removed);
            }
        }
    }

    /**
     * Deregister a service by its class type.
     *
     * @param clazz The class reference to deregister.
     */
    public void deregisterService(Class<?> clazz) {
        Object removed = services.remove(clazz);
        if (removed != null) {
            // Remove from tag mappings
            for (Set<Object> instances : taggedServices.values()) {
                instances.remove(removed);
            }
        }
    }

    // endregion

    // region Utility

    /**
     * Returns a boolean indicating if the repository has any data loaded.
     *
     * @return True if there is any data loaded into the repository.
     */
    public boolean hasData() {
        return (!components.isEmpty() || !services.isEmpty()
                || !prototypeComponentFactories.isEmpty() || !prototypeServiceFactories.isEmpty());
    }

    /**
     * Clear all registered components and services.
     */
    public void clear() {
        components.clear();
        namedComponents.clear();
        services.clear();
        namedServices.clear();
        taggedComponents.clear();
        taggedServices.clear();
        prototypeComponentFactories.clear();
        prototypeServiceFactories.clear();
    }

    // endregion
}
