package org.pixel.core;

import org.pixel.audio.AudioPlayer;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.core.dev.DevTools;
import org.pixel.graphics.GraphicsDevice;

/**
 * Base class of a game. Platform-free: the same class runs on every platform, hosted by that platform's launcher
 * (e.g. {@code DesktopLauncher.run(new MyGame(), settings)}).
 * <p>
 * Lifecycle, all called by the host on the render thread:
 * <ol>
 *   <li>{@link #load()}: once, after the platform is initialized. The {@link #context() context} and its
 *   accessors are available from here on (not in the constructor).</li>
 *   <li>{@link #update(DeltaTime)} and {@link #draw(DeltaTime)}: every frame.</li>
 *   <li>{@link #dispose()}: once, at the end, while the graphics and audio contexts are still alive. Release what
 *   the game created; the host then disposes the shared services ({@link #content()}, {@link #audio()}).</li>
 * </ol>
 */
public abstract class GameApp {

    private GameContext context;

    /**
     * Attach the game to its host. Called by the host before {@link #load()}.
     */
    final void attach(GameContext context) {
        if (this.context != null) {
            throw new IllegalStateException("Game is already attached to a host.");
        }
        this.context = context;
    }

    /**
     * @return The context provided by the host.
     * @throws IllegalStateException If called before the game is attached (e.g. from the constructor).
     */
    public final GameContext context() {
        if (context == null) {
            throw new IllegalStateException("The game context is available from load() on, not in the constructor.");
        }
        return context;
    }

    /**
     * Load content and set up the game. The context is available.
     */
    public void load() {
        // empty by design
    }

    /**
     * Update the game state.
     *
     * @param delta Time since the last frame.
     */
    public void update(DeltaTime delta) {
        // empty by design
    }

    /**
     * Draw the frame (the screen is cleared before when {@code settings().isAutoClear()}).
     *
     * @param delta Time since the last frame.
     */
    public void draw(DeltaTime delta) {
        // empty by design
    }

    /**
     * Release what the game created. Shared services are disposed by the host afterwards.
     */
    public void dispose() {
        // empty by design
    }

    /**
     * Called when the drawable surface changes size (window resize, display change, rotation).
     *
     * @param width  The new width in pixels.
     * @param height The new height in pixels.
     */
    public void onViewportChanged(int width, int height) {
        // empty by design
    }

    // region context shortcuts

    protected final GraphicsDevice graphics() {
        return context().graphics();
    }

    protected final ContentManager content() {
        return context().content();
    }

    protected final AudioPlayer audio() {
        return context().audio();
    }

    protected final GameWindow window() {
        return context().window();
    }

    protected final DevTools devTools() {
        return context().devTools();
    }

    protected final GameSettings settings() {
        return context().settings();
    }

    /**
     * Request the game to exit (see {@link GameContext#exit()}).
     */
    protected final void exit() {
        context().exit();
    }

    protected final int getVirtualWidth() {
        return context().settings().getVirtualWidth();
    }

    protected final int getVirtualHeight() {
        return context().settings().getVirtualHeight();
    }

    protected final int getFps() {
        return context().getFps();
    }

    protected final int getSmoothedFps() {
        return context().getSmoothedFps();
    }

    // endregion
}
