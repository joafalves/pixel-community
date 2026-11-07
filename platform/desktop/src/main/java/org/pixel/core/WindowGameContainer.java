package org.pixel.core;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.GraphicsDevice;

import java.util.concurrent.locks.LockSupport;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Host base for windowed platforms: owns the window manager and the game loop.
 *
 * @param <T> The window manager type.
 * @param <S> The graphics device type.
 * @param <Z> The settings type.
 */
public abstract class WindowGameContainer<T extends WindowManager, S extends GraphicsDevice, Z extends WindowSettings>
        extends GameContainer<S, Z> {

    private static final Logger log = LoggerFactory.getLogger(WindowGameContainer.class);

    protected T windowManager;

    private double targetFpsTimeRef;

    protected WindowGameContainer(GameApp app, Z settings) {
        super(app, settings);
    }

    /**
     * Initialize the window manager.
     *
     * @return True if the window manager was initialized successfully.
     */
    protected abstract boolean initWindowManager();

    /**
     * Release the platform's audio context (after the content manager released its sounds).
     */
    protected abstract void disposePlatformAudio();

    /**
     * Initialize the platform, load the game, run the loop until the window closes, then dispose everything (also
     * when the game throws).
     */
    public void start() {
        if (state != State.NEW) {
            throw new IllegalStateException("A game host can only be started once.");
        }
        state = State.INITIALIZING;
        try {
            if (!initWindowManager()) {
                log.error("Failed to initialize the window manager.");
                return;
            }
            if (!initContainer()) {
                return;
            }
            state = State.INITIALIZED;
            loadApp();
            gameLoop();
        } finally {
            dispose();
        }
    }

    @Override
    public void exit() {
        if (windowManager != null) {
            windowManager.requestClose();
        }
    }

    /**
     * Release the game, then the shared services and graphics device (GL context still alive), then audio, then the
     * window. Runs once.
     */
    private void dispose() {
        if (state.isDisposed()) {
            return;
        }
        state = State.DISPOSING;
        try {
            disposeContainer(); // null-safe: only disposes what was created
        } finally {
            disposePlatformAudio();
            if (windowManager != null) {
                windowManager.dispose();
            }
            state = State.DISPOSED;
        }
    }

    /**
     * Set vsync mode.
     *
     * @param vsyncEnabled The vsync mode.
     */
    public void setVsyncEnabled(boolean vsyncEnabled) {
        this.windowManager.setVSync(vsyncEnabled);
    }

    /**
     * The game loop.
     */
    private void gameLoop() {
        var delta = new DeltaTime();
        while (this.windowManager.isWindowActive()) {
            delta.tick();

            // FPS Limiting Logic (VSYNC cannot be enabled)
            if (settings.getTargetFps() > 0 && !settings.isVsync()) {
                double currentTime = glfwGetTime();
                double targetFrameDuration = 1.0 / settings.getTargetFps(); // e.g., 16.67ms for 60 FPS

                // Calculate the next frame's target time. If we have no reference yet (first frame) or we fell
                // behind by more than a frame (hitch, debugger, window drag), re-anchor to "now" instead of running
                // uncapped until the reference catches up with real time.
                if (targetFpsTimeRef <= 0 || currentTime - targetFpsTimeRef > targetFrameDuration) {
                    targetFpsTimeRef = currentTime;
                }
                double nextFrameTime = targetFpsTimeRef + targetFrameDuration;

                // Sleep if there's still time until the next frame
                double sleepTime = nextFrameTime - currentTime;
                if (sleepTime > 0) {
                    LockSupport.parkNanos((long) (sleepTime * 1_000_000_000));
                }

                targetFpsTimeRef = nextFrameTime;
            }

            this.windowManager.beginFrame();
            updateFrame(delta);
            if (!this.windowManager.isWindowActive()) {
                break; // update code may have requested exit
            }
            drawFrame(delta);
            this.windowManager.endFrame();
        }
    }
}
