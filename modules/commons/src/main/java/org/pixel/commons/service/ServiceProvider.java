package org.pixel.commons.service;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(ServiceProvider.class);
    private static final ConcurrentHashMap<Class<?>, ServiceFactory<?>> serviceFactoryMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Class<?>> settingsTypes = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private ServiceProvider() {
        // private constructor
    }

    /**
     * Register a service registrar.
     *
     * @param registrar The service registrar
     */
    public static void register(ServiceRegistrar registrar) {
        log.info("Registering services from {0}.", registrar.getClass().getSimpleName());

        if (registrar.getServices() == null) {
            log.warn("Registrar has no services to register.");
            return;
        }

        for (var registration : registrar.getServices()) {
            registerService((ServiceRegistry<?>) registration);
        }
    }

    /**
     * Register a service.
     *
     * @param <T>         The service type
     * @param serviceType The service type
     * @param factory     The service factory
     */
    public static <T> void register(Class<T> serviceType, ServiceFactory<T> factory) {
        log.info("Registering service: {0} ({1}).", serviceType.getSimpleName(), factory.getClass().getSimpleName());
        serviceFactoryMap.put(serviceType, factory);
        settingsTypes.remove(serviceType); // remove any existing settings type (if any)
    }

    /**
     * Register a service with settings.
     *
     * @param <T>          The service type
     * @param <S>          The settings type
     * @param serviceType  The service type
     * @param settingsType The settings type
     * @param factory      The service factory
     */
    public static <T, S> void register(Class<T> serviceType, Class<S> settingsType, ServiceFactory<T> factory) {
        log.info("Registering service: {0} ({1}) with settings: {2}.",
                serviceType.getSimpleName(), factory.getClass().getSimpleName(), settingsType.getSimpleName());
        serviceFactoryMap.put(serviceType, factory);
        settingsTypes.put(serviceType, settingsType);
    }

    /**
     * Get a service by type.
     *
     * @param <T>         The service type
     * @param serviceType The service type
     * @return The service instance if available
     */
    public static <T> T get(Class<T> serviceType) {
        if (settingsTypes.containsKey(serviceType)) {
            log.error("Service requires settings: {0}.", serviceType.getName());
            throw new RuntimeException("Service not available without settings: " + serviceType.getName());
        }

        ServiceFactory<?> factory = serviceFactoryMap.get(serviceType);
        if (factory == null) {
            log.error("Service not registered: {0}.", serviceType.getName());
            throw new RuntimeException("Service not registered: " + serviceType.getName());
        }
        return serviceType.cast(factory.get());
    }

    /**
     * Get a service by type with settings.
     *
     * @param serviceType The service type
     * @param settings    The settings
     * @param <T>         The service type
     * @param <S>         The settings type
     * @return The service instance if available
     */
    @SuppressWarnings("unchecked")
    public static <T, S> T get(Class<T> serviceType, S settings) {
        Class<?> expectedType = settingsTypes.get(serviceType);
        if (expectedType == null) {
            log.error("Service does not accept settings: {0}.", serviceType.getName());
            throw new RuntimeException("Service not registered with settings: " + serviceType.getName());
        }
        if (!expectedType.isInstance(settings)) {
            throw new IllegalArgumentException(
                    "Settings for " + serviceType.getSimpleName() +
                            " must be of type " + expectedType.getSimpleName() +
                            ", but got " + settings.getClass().getSimpleName()
            );
        }

        ServiceFactoryWithSettings<T, S> factory = (ServiceFactoryWithSettings<T, S>) serviceFactoryMap.get(serviceType);
        if (factory == null) {
            log.error("Service not registered: {0}.", serviceType.getName());
            throw new RuntimeException("Service not registered: " + serviceType.getName());
        }
        return serviceType.cast(factory.get(settings));
    }

    /**
     * Remove a service by type.
     *
     * @param <T>         The service type
     * @param serviceType The service type
     * @return The service factory if available
     */
    public static <T> ServiceFactory<?> unregister(Class<T> serviceType) {
        log.info("Unregistering service: {0}.", serviceType.getSimpleName());
        ServiceFactory<?> factory = serviceFactoryMap.remove(serviceType);
        if (factory == null) {
            log.warn("Service not registered: {0}.", serviceType.getName());
            return null;
        }
        settingsTypes.remove(serviceType); // remove any existing settings type (if any)
        return factory;
    }

    /**
     * Unregister a service registrar.
     *
     * @param registrar The service registrar
     */
    public static void unregister(ServiceRegistrar registrar) {
        log.info("Unregistering services from {0}.", registrar.getClass().getSimpleName());

        if (registrar.getServices() == null) {
            log.warn("Registrar has no services to unregister.");
            return;
        }

        for (var registration : registrar.getServices()) {
            unregister((Class<?>) registration.getServiceType());
        }
    }

    /**
     * Clear all registered services.
     */
    public static void unregisterAll() {
        serviceFactoryMap.clear();
        settingsTypes.clear();
    }

    /**
     * Register a service registration.
     *
     * @param registration The service registration
     * @param <T>          The service type
     */
    private static <T> void registerService(ServiceRegistry<T> registration) {
        if (registration.getSettingsType() != null) {
            register(registration.getServiceType(), registration.getSettingsType(), registration.getServiceFactory());
        } else {
            register(registration.getServiceType(), registration.getServiceFactory());
        }
    }
}
