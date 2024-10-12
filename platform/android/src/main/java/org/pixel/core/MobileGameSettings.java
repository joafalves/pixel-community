package org.pixel.core;

public class MobileGameSettings extends GameSettings {
    /**
     * Constructor.
     *
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public MobileGameSettings(int virtualWidth, int virtualHeight) {
        super(virtualWidth, virtualHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle     The title of the game.
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public MobileGameSettings(String gameTitle, int virtualWidth, int virtualHeight) {
        super(gameTitle, virtualWidth, virtualHeight);
    }

}
