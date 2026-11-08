package org.pixel.commons.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.RequiredArgsConstructor;

public class EventManager {

    private static final EventManager defaultDispatcher = new EventManager();

    // Copy-on-write lists: publish() iterates a snapshot, so listeners may (un)subscribe while an event is being
    // dispatched, and subscribe/publish may happen on different threads.
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<EventListenerDispatcher>> listenerMap;

    /**
     * Constructor.
     */
    public EventManager() {
        this.listenerMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the default event dispatcher.
     *
     * @return The default event dispatcher.
     */
    public static EventManager getDefault() {
        return defaultDispatcher;
    }

    /**
     * Adds an unclassified event listener.
     *
     * @param eventName The event name.
     * @param listener  The listener.
     */
    public void subscribe(String eventName, EventListener<?> listener) {
        subscribeInternal(eventName, Object.class, listener);
    }

    /**
     * Adds an event listener.
     *
     * @param eventName The event name.
     * @param dataType  The data type.
     * @param listener  The listener.
     */
    public <T> void subscribe(String eventName, Class<T> dataType, EventListener<T> listener) {
        subscribeInternal(eventName, dataType, listener);
    }

    private void subscribeInternal(String eventName, Class<?> dataType, EventListener<?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        // addIfAbsent compares dispatchers by listener, so the same listener is never registered twice
        listenerMap.computeIfAbsent(eventName, k -> new CopyOnWriteArrayList<>())
                .addIfAbsent(new EventListenerDispatcher(dataType, listener));
    }

    /**
     * Unsubscribes all event listeners with the given event name key
     *
     * @param eventName The event name.
     */
    public void unsubscribe(String eventName) {
        listenerMap.remove(eventName);
    }

    /**
     * Unsubscribes an event listener.
     *
     * @param eventName The event name.
     * @param listener  The listener.
     * @return True if the listener was unsubscribed.
     */
    public boolean unsubscribe(String eventName, EventListener<?> listener) {
        CopyOnWriteArrayList<EventListenerDispatcher> listeners = listenerMap.get(eventName);
        return listeners != null && listeners.removeIf(l -> l.getListener() == listener);
    }

    /**
     * Publishes an event.
     *
     * @param eventName The event name.
     * @param eventData The event data.
     */
    public void publish(String eventName, Object eventData) {
        CopyOnWriteArrayList<EventListenerDispatcher> listeners = listenerMap.get(eventName);
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
                return listener == ((EventListenerDispatcher) obj).listener;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(listener);
        }
    }
}
