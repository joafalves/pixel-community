package org.pixel.commons.service;

public interface ServiceFactoryWithSettings<T, S> extends ServiceFactory<T> {
    /**
     * settings lookup
     */
    default T get(S settings) {
        throw new UnsupportedOperationException("settings get(...) not supported");
    }
}
