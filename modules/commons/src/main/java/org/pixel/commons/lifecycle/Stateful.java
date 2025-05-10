package org.pixel.commons.lifecycle;

public interface Stateful {
    /**
     * Get the current state of the object
     *
     * @return state
     */
    State getState();
}
