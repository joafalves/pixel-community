package org.pixel.commons;

import java.util.concurrent.ConcurrentHashMap;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class ServiceProvider {
    private static final Logger log = LoggerFactory.getLogger(ServiceProvider.class);
    private static final ConcurrentHashMap<Class<?>, ServiceFactory<?>> serviceFactoryMap = new ConcurrentHashMap<>();

    /**
     * Register a service.
     * 
     * @param <T>         The service type
     * @param serviceType The service type
     * @param factory     The service factory
     */
    public static <T> void register(Class<T> serviceType, ServiceFactory<T> factory) {
        serviceFactoryMap.put(serviceType, factory);
    }

    /**
     * Get a service.
     * 
     * @param <T>         The service type
     * @param serviceType The service type
     * @return The service instance
     */
    public static <T> T create(Class<T> serviceType) {
        ServiceFactory<?> factory = serviceFactoryMap.get(serviceType);
        if (factory == null) {
            log.error("Service not registered: " + serviceType.getName());
            throw new RuntimeException("Service not registered: " + serviceType.getName());
        }

        return serviceType.cast(factory.create());
    }

    /**
     * Clear all registered services.
     */
    public static void clear() {
        serviceFactoryMap.clear();
    }
}
