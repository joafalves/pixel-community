package org.pixel.commons;

import lombok.Getter;

public class GameContext {

    /**
     * Shared data storage.
     */
    @Getter
    private static final DataHashMap data = new DataHashMap();

}
