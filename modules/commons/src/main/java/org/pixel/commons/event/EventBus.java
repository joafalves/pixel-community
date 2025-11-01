package org.pixel.commons.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    /**
     * Subscribes a handler to a specific event type.
     *
     * @param eventType The class of the event to listen for.
     * @param handler   The lambda or method reference to execute.
     * @param <T>       The event type.
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * Unsubscribes a handler from a specific event type.
     *
     * @param eventType The class of the event.
     * @param handler   The handler to remove.
     * @param <T>       The event type.
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<?>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }

    /**
     * Publishes an event to all registered subscribers.
     *
     * @param event The event object to publish.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publish(Object event) {
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            for (Consumer handler : handlers) {
                handler.accept(event);
            }
        }
    }
}
