package org.pixel.network.io;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Stateful;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.network.api.NetworkAuthenticator;

@Setter
@Getter
public abstract class GameServer implements Initializable, Updatable, Disposable, Stateful {

    protected final GameServerSettings settings;

    protected NetworkAuthenticator authenticator;

    /**
     * Constructor
     *
     * @param settings - The game server settings
     */
    protected GameServer(GameServerSettings settings) {
        this.settings = settings;
    }
}
