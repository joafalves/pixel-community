package org.pixel.commons.factory;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for factory instances used to create platform-specific implementations.
 *
 * <p>This is separate from {@link org.pixel.commons.service.ServiceProvider} and is designed
 * for types that require constructor parameters or are not singletons.
 *
 * <p>Factories are registered at platform initialization (e.g., in {@code Game.initServices()})
 * and retrieved through static {@code create()} methods on abstract classes.
 *
 * <p>Example usage:
 * <pre>
 * // Platform initialization
 * FactoryProvider.register(CanvasFactory.class, new GLCanvasFactory());
 *
 * // User code (via static method on Canvas)
 * Canvas canvas = Canvas.create(800, 600);
 * // Internally calls: FactoryProvider.get(CanvasFactory.class).create(800, 600)
 * </pre>
 */
public class FactoryProvider {

    private static final Logger log = LoggerFactory.getLogger(FactoryProvider.class);
    private static final ConcurrentHashMap<Class<?>, Factory> factoryMap = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private FactoryProvider() {
        // private constructor
    }

    /**
     * Register a factory instance.
     *
     * @param <T>         The factory type
     * @param factoryType The factory type class
     * @param factory     The factory instance
     */
    public static <T extends Factory> void register(Class<T> factoryType, T factory) {
        log.info("Registering factory: {0} ({1}).", factoryType.getSimpleName(), factory.getClass().getSimpleName());
        factoryMap.put(factoryType, factory);
    }

    /**
     * Get a registered factory by type.
     *
     * @param <T>         The factory type
     * @param factoryType The factory type class
     * @return The factory instance
     * @throws RuntimeException if the factory is not registered
     */
    @SuppressWarnings("unchecked")
    public static <T extends Factory> T get(Class<T> factoryType) {
        Factory factory = factoryMap.get(factoryType);
        if (factory == null) {
            log.error("Factory not registered: {0}.", factoryType.getName());
            throw new RuntimeException("Factory not registered: " + factoryType.getName());
        }
        return (T) factory;
    }

    /**
     * Unregister a factory by type.
     *
     * @param <T>         The factory type
     * @param factoryType The factory type class
     * @return The factory instance if it was registered, null otherwise
     */
    public static <T extends Factory> Factory unregister(Class<T> factoryType) {
        log.info("Unregistering factory: {0}.", factoryType.getSimpleName());
        Factory factory = factoryMap.remove(factoryType);
        if (factory == null) {
            log.warn("Factory not registered: {0}.", factoryType.getName());
        }
        return factory;
    }

    /**
     * Clear all registered factories.
     */
    public static void unregisterAll() {
        factoryMap.clear();
    }

    /**
     * Check if a factory is registered.
     *
     * @param factoryType The factory type class
     * @return true if registered, false otherwise
     */
    public static boolean isRegistered(Class<? extends Factory> factoryType) {
        return factoryMap.containsKey(factoryType);
    }
}
