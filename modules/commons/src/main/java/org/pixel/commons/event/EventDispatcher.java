package org.pixel.commons.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

public class EventDispatcher {

    private static final EventDispatcher defaultDispatcher = new EventDispatcher();

    private final ConcurrentHashMap<String, List<EventListenerDispatcher>> listenerMap;

    /**
     * Constructor.
     */
    public EventDispatcher() {
        this.listenerMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the default event dispatcher.
     *
     * @return The default event dispatcher.
     */
    public static EventDispatcher getDefault() {
        return defaultDispatcher;
    }

    /**
     * Adds an unclassified event listener.
     *
     * @param eventName The event name.
     * @param listener  The listener.
     */
    public void subscribe(String eventName, EventListener<Object> listener) {
        List<EventListenerDispatcher> listeners = listenerMap.computeIfAbsent(eventName, k -> new ArrayList<>());
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(new EventListenerDispatcher(Object.class, listener));
            }
        }
    }

    /**
     * Adds an event listener.
     *
     * @param eventName The event name.
     * @param dataType  The data type.
     * @param listener  The listener.
     */
    public <T> void subscribe(String eventName, Class<T> dataType, EventListener<T> listener) {
        List<EventListenerDispatcher> listeners = listenerMap.computeIfAbsent(eventName, k -> new ArrayList<>());
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(new EventListenerDispatcher(dataType, listener));
            }
        }
    }

    /**
     * Unsubscribes an event listener.
     *
     * @param eventName The event name.
     * @param listener  The listener.
     * @return True if the listener was unsubscribed.
     */
    public boolean unsubscribe(String eventName, EventListener listener) {
        List<EventListenerDispatcher> listeners = listenerMap.get(eventName);
        if (listeners != null) {
            synchronized (listeners) {
                return listeners.removeIf(l -> l.getListener() == listener);
            }
        }
        return false;
    }

    /**
     * Publishes an event.
     *
     * @param eventName The event name.
     * @param eventData The event data.
     */
    public void publish(String eventName, Object eventData) {
        List<EventListenerDispatcher> listeners = listenerMap.get(eventName);
        if (listeners != null) {
            for (EventListenerDispatcher listener : listeners) {
                listener.dispatch(eventData);
            }
        }
    }

    /**
     * Clears all event listeners.
     */
    public void clear() {
        listenerMap.clear();
    }

    @RequiredArgsConstructor
    private static class EventListenerDispatcher {

        private final Class<?> type;
        private final EventListener listener;

        public void dispatch(Object data) {
            if (data == null && type.isAssignableFrom(Object.class)
                    || data != null && type.isAssignableFrom(data.getClass())) {
                listener.onEvent(data);
            }
        }

        public EventListener getListener() {
            return listener;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EventListenerDispatcher) {
                return listener.equals(((EventListenerDispatcher) obj).listener);
            }

            return false;
        }
    }
}
