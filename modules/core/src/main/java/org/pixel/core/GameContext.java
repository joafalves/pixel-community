package org.pixel.core;

import org.pixel.audio.AudioPlayer;
import org.pixel.content.ContentManager;
import org.pixel.core.dev.DevTools;
import org.pixel.graphics.GraphicsDevice;

/**
 * Everything the platform provides to a running game. One context per game instance: there is no global registry,
 * so several games (or an editor and a game) can run in one process.
 * <p>
 * Shared services ({@link #content()}, {@link #audio()}) are owned by the game host and disposed by it; objects
 * created through them or through {@link #graphics()} (e.g. {@code graphics().createSpriteBatch()}) are owned by
 * the caller.
 */
public interface GameContext {

    /**
     * @return The graphics device (rendering backend, statistics, renderer factories).
     */
    GraphicsDevice graphics();

    /**
     * @return The game's content manager. Always the same instance, so its cache is shared by the whole game. For
     * assets with a shorter life (a level, a screen), use a child: {@code content().child("level-1")} shares what is
     * already loaded here and frees its own assets when it is unloaded or disposed.
     */
    ContentManager content();

    /**
     * @return The game's audio player.
     */
    AudioPlayer audio();

    /**
     * @return The window (or surface) the game draws to.
     */
    GameWindow window();

    /**
     * @return The game settings.
     */
    GameSettings settings();

    /**
     * @return The in-game dev tools (frame times, rendering statistics, assets, log, and the game's own watches and
     * switches). Always present; it shows only when available ({@code GameSettings.setDevToolsMode}).
     */
    DevTools devTools();

    /**
     * Request the game to exit: the current frame finishes, then the game is disposed.
     */
    void exit();

    /**
     * @return Frames rendered during the last second.
     */
    int getFps();

    /**
     * @return Smoothed frames per second.
     */
    int getSmoothedFps();
}
