package org.pixel.commons.lifecycle;

public enum State {
    CREATED(0),
    INITIALIZING(1),
    INITIALIZED(2),
    LOADING(3),
    LOADED(4),
    UPDATING(5),
    DRAWING(6),
    DISPOSING(7),
    DISPOSED(8);

    private int value;

    State(int value) {
        this.value = value;
    }

    /**
     * Get the value of the state
     * @return The value of the state
     */
    public int getValue() {
        return value;
    }

    /**
     * Determines whether the state is initialized
     * @return True if the state is initialized, false otherwise
     */
    public boolean hasInitialized() {
        return value >= INITIALIZED.value;
    }

    /**
     * Determines whether the state is loaded
     * @return True if the state is loaded, false otherwise
     */
    public boolean hasLoaded() {
        return value >= LOADED.value;
    }

    /**
     * Determines whether the state is updated
     * @return True if the state is updated, false otherwise
     */
    public boolean isDisposed() {
        return value >= DISPOSING.value;
    }
}
