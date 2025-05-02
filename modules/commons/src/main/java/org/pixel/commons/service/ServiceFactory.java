package org.pixel.commons.service;

public interface ServiceFactory<T> {
    /**
     * no-settings lookup
     */
    default T get() {
        throw new UnsupportedOperationException("no-arg get() not supported");
    }
}
