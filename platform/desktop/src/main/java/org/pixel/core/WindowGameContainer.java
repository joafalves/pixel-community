package org.pixel.core;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.*;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.GraphicsDevice;

import java.util.concurrent.locks.LockSupport;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public abstract class WindowGameContainer<T extends WindowManager, S extends GraphicsDevice, Z extends GameSettings>
        extends GameContainer<S, Z>
        implements Initializable, Loadable, Updatable, Drawable, Disposable {

    private static final Logger log = LoggerFactory.getLogger(GameContainer.class);

    protected T windowManager;

    private double targetFpsTimeRef;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public WindowGameContainer(Z settings) {
        super(settings);
    }

    /**
     * Initialize the window manager.
     *
     * @return True if the window manager was initialized successfully.
     */
    protected abstract boolean initWindowManager();

    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Game already initialized.");
            return false;
        }
        this.state = State.INITIALIZING;

        // Initialize main components
        if (!initWindowManager()) {
            log.error("Failed to initialize the window manager.");
            return false;
        }

        if (super.init()) {
            // Load window procedure
            load();
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        super.start();

        if (this.state.hasInitialized()) {
            this.gameLoop();
            this.dispose();
        }
    }

    @Override
    public void dispose() {
        windowManager.dispose();
        super.dispose();
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
     * Get the active window manager.
     *
     * @return The active window manager.
     */
    public T getWindowManager() {
        return windowManager;
    }

    /**
     * The render loop.
     */
    protected void gameLoop() {
        var delta = new DeltaTime();
        while (this.windowManager.isWindowActive()) {
            delta.tick();

            // FPS Limiting Logic (VSYNC cannot be enabled)
            if (settings.getTargetFps() > 0 && !settings.isVsync()) {
                double currentTime = glfwGetTime();
                double targetFrameDuration = 1.0 / settings.getTargetFps(); // e.g., 16.67ms for 60 FPS

                // Calculate the next frame's target time
                double nextFrameTime = targetFpsTimeRef + targetFrameDuration;

                // Sleep if there's still time until the next frame
                double sleepTime = nextFrameTime - currentTime;
                if (sleepTime > 0) {
                    LockSupport.parkNanos((long) (sleepTime * 1_000_000_000));
                }

                // Update lastFrameTime for the next frame
                targetFpsTimeRef = nextFrameTime;
            }

            this.windowManager.beginFrame();

            // call container update
            updateContainer(delta);

            // call game update
            update(delta);

            if (!this.windowManager.isWindowActive()) {
                break; // update code might kill the window...
            }

            // call game draw
            if (this.settings.isAutoClear()) {
                this.clear();
            }
            draw(delta);

            this.windowManager.endFrame();

            // TODO: study the feasibility/impact of implementing a multi-threaded mechanism
            // on the game loop, more info:
            // https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/glfw/Threads.java
        }
    }
}
