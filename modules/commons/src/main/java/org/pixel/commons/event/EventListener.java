package org.pixel.commons.event;

public interface EventListener<T> {

    /**
     * Called when an event is fired.
     *
     * @param data The data of the event.
     */
    void onEvent(T data);

}
