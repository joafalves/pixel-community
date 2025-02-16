package org.pixel.network.io;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Stateful;
import org.pixel.commons.lifecycle.Updatable;

public abstract class GameServer implements Initializable, Updatable, Disposable, Stateful {

    protected final GameServerSettings settings;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    protected GameServer(GameServerSettings settings) {
        this.settings = settings;
    }
}
