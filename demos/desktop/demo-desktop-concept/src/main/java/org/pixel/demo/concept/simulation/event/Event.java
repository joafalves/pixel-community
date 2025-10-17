package org.pixel.demo.concept.simulation.event;

import lombok.Getter;

@Getter
public abstract class Event<T> {

    private final T data;

    public Event(T data) {
        this.data = data;
    }

    public String getName() {
        return this.getClass().getPackageName();
    }
}
