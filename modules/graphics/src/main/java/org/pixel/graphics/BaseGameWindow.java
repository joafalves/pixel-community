/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics;

import java.util.Properties;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public abstract class BaseGameWindow<T extends WindowManager, S extends GraphicsDevice, Z extends BaseGameWindowSettings>
        implements Initializable, Loadable, Updatable, Drawable, Disposable {

    private static final Logger log = LoggerFactory.getLogger(BaseGameWindow.class);

    protected T windowManager;
    protected S graphicsDevice;
    protected Z settings;

    private State state;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public BaseGameWindow(Z settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Game settings cannot be null.");
        }
        this.settings = settings;
        this.state = State.CREATED;
    }

    /**
     * This function will initialize and run the game window.
     */
    public void start() {
        this.init();
        this.run();
    }

    /**
     * Initializes the window and rendering context.
     *
     * @return True if the window was initialized successfully.
     */
    @Override
    public boolean init() {
        // Initialize main components
        if (!initWindowManager()) {
            log.error("Failed to initialize the window manager.");
            return false;
        }
        if (!initGraphicsDevice()) {
            log.error("Failed to initialize the graphics device.");
            return false;
        }
        if (!initAudio()) {
            log.error("Failed to initialize the audio device.");
            return false;
        }
        if (!initServices()) {
            log.error("Failed to initialize the services.");
            return false;
        }

        // At this point, the window, audio and rendering engines are initialized. Now
        // it's time to
        // load the game window (user-space) and allow the game loop to start.

        // Load window procedure
        load();

        this.state = State.INITIALIZED;
        return true;
    }

    @Override
    public void load() {
        // empty by design (not abstract to make this optional)
    }

    @Override
    public void update(DeltaTime delta) {
        // empty by design (not abstract to make this optional)
    }

    @Override
    public void draw(DeltaTime delta) {
        // empty by design (not abstract to make this optional)
    }

    @Override
    public void dispose() {
        // dispose graphics device and window manager
        graphicsDevice.dispose();
        windowManager.dispose();
    }

    /**
     * Clear the render window.
     */
    public void clear() {
        graphicsDevice.clear();
    }

    /**
     * Initialize the window manager.
     * 
     * @return True if the window manager was initialized successfully.
     */
    protected abstract boolean initWindowManager();

    /**
     * Initialize the graphics device.
     * 
     * @return True if the graphics device was initialized successfully.
     */
    protected abstract boolean initGraphicsDevice();

    /**
     * Initialize the audio context.
     * 
     * @return True if the audio context was initialized successfully.
     */
    protected abstract boolean initAudio();

    /**
     * Initialize the services.
     * 
     * @return True if the services were initialized successfully.
     */
    protected abstract boolean initServices();

    /**
     * Starts the game loop. To terminate the loop, call {@link #close()} or close
     * the window.
     */
    public void run() {
        if (!this.state.hasInitialized()) {
            throw new RuntimeException("init() must be called before running the game..");
        }

        renderLoop();

        // Dispose of the window
        dispose();
    }

    /**
     * The render loop.
     */
    private void renderLoop() {
        var delta = new DeltaTime();
        while (this.windowManager.isWindowActive()) {
            delta.tick();

            this.windowManager.beginFrame();
            this.graphicsDevice.beginFrame();

            // call game update
            update(delta);

            // call game draw
            if (this.settings.isAutoWindowClear()) {
                this.clear();
            }
            draw(delta);

            this.graphicsDevice.endFrame();
            this.windowManager.endFrame();

            if (!this.windowManager.isWindowFocused() && this.settings.isIdleThrottle()) {
                try {
                    Thread.sleep(100); // TODO: make idle period configurable

                } catch (InterruptedException e) {
                    log.error("Exception caught!", e);
                }
            }

            // TODO: study the feasibility/impact of implementing a multi-threaded mechanism
            // on the game loop, more info:
            // https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/glfw/Threads.java
        }
    }

    /**
     * Flag the rendering loop to terminate.
     */
    public void close() {
        this.graphicsDevice.dispose();
        this.windowManager.dispose();
    }

    /**
     * Get the window virtual width.
     *
     * @return The window virtual width.
     */
    public int getVirtualWidth() {
        return this.settings.getVirtualWidth();
    }

    /**
     * Get the window virtual height.
     *
     * @return The window virtual height.
     */
    public int getVirtualHeight() {
        return this.settings.getVirtualHeight();
    }

    /**
     * Set the window background color.
     *
     * @param backgroundColor The window background color.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.graphicsDevice.setClearColor(backgroundColor);
    }

    /**
     * Get client properties.
     *
     * @return The client properties.
     */
    public Properties getClientProperties() {
        return this.settings.getClientProperties();
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
     * Get the active graphics device.
     * 
     * @return The active graphics device.
     */
    public S getGraphicsDevice() {
        return this.graphicsDevice;
    }

    /**
     * Get the game settings.
     * 
     * @return The game settings.
     */
    public Z getSettings() {
        return settings;
    }
}
