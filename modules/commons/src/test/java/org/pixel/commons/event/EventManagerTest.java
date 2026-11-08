package org.pixel.commons.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {

    @Test
    void sameListenerIsOnlyRegisteredOnce() {
        EventManager manager = new EventManager();
        AtomicInteger calls = new AtomicInteger();
        EventListener<Object> listener = data -> calls.incrementAndGet();

        manager.subscribe("evt", listener);
        manager.subscribe("evt", listener);
        manager.subscribe("evt", Object.class, listener);
        manager.publish("evt", "data");

        assertEquals(1, calls.get());
    }

    @Test
    void listenerCanUnsubscribeWhileBeingDispatched() {
        EventManager manager = new EventManager();
        List<String> received = new ArrayList<>();
        EventListener<Object>[] holder = new EventListener[1];
        holder[0] = data -> {
            received.add("first");
            manager.unsubscribe("evt", holder[0]);
        };
        manager.subscribe("evt", holder[0]);
        manager.subscribe("evt", data -> received.add("second"));

        assertDoesNotThrow(() -> manager.publish("evt", null));
        manager.publish("evt", null);

        assertEquals(List.of("first", "second", "second"), received);
    }

    @Test
    void typedListenerOnlyReceivesMatchingData() {
        EventManager manager = new EventManager();
        List<Object> received = new ArrayList<>();
        manager.subscribe("evt", String.class, received::add);

        manager.publish("evt", 42);
        manager.publish("evt", "hello");

        assertEquals(List.of("hello"), received);
    }
}
