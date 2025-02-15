package org.pixel.network;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Stateful;
import org.pixel.commons.lifecycle.Updatable;

public abstract class GameClient implements Initializable, Updatable, Disposable, Stateful {

    protected final GameClientSettings settings;

    /**
     * Constructor
     *
     * @param settings - The game client settings
     */
    protected GameClient(GameClientSettings settings) {
        this.settings = settings;
    }
}