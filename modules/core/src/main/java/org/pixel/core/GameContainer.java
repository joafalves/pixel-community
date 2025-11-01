/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import org.pixel.blueprint.BlueprintAssembler;
import org.pixel.blueprint.BlueprintLoader;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.*;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.GraphicsDevice;

import java.util.Properties;

public abstract class GameContainer<S extends GraphicsDevice, Z extends GameSettings>
        implements Initializable, Loadable, Updatable, Drawable, Disposable {

    private static final Logger log = LoggerFactory.getLogger(GameContainer.class);

    protected S graphicsDevice;
    protected Z settings; // TODO: Make settings immutable and create necessary field properties here
    protected State state;

    private float elapsed;
    private int frameCount;
    private int instantFps;
    private float smoothFps;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public GameContainer(Z settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Game settings cannot be null.");
        }
        this.settings = settings;
        this.state = State.NEW;
    }

    /**
     * This function will initialize and run the game window.
     */
    public void start() {
        this.init();
    }

    /**
     * Initializes the window and rendering context.
     *
     * @return True if the window was initialized successfully.
     */
    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Game already initialized.");
            return false;
        }
        this.state = State.INITIALIZING;

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

        var blueprintPackages = settings.getBlueprintPackages();
        if (blueprintPackages != null && blueprintPackages.length > 0) {
            BlueprintLoader.load(blueprintPackages);
            BlueprintAssembler.assemble(this);
        }

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
    }

    public final void updateContainer(DeltaTime delta) {
        frameCount++;

        elapsed += delta.getElapsed();
        if (elapsed >= 1) {
            // FPS calculation:
            instantFps = (int) (frameCount / elapsed);
            smoothFps = smoothFps * 0.4f + instantFps * 0.6f;
            elapsed = 0;
            frameCount = 0;
        }
    }

    /**
     * Called when the window size changes.
     *
     * @param width  The new window size width.
     * @param height The new window size height.
     */
    public void onWindowSizeChange(int width, int height) {
        // empty by design (not abstract to make this optional)
    }

    /**
     * Clear the render window.
     */
    public void clear() {
        graphicsDevice.clear();
    }

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
     * Get the window viewport width.
     * @return The window viewport width.
     */
    public abstract int getViewportWidth();

    /**
     * Get the window viewport height.
     * @return The window viewport height.
     */
    public abstract int getViewportHeight();

    /**
     * Set the window background color.
     *
     * @param backgroundColor The window background color.
     */
    public void setBackgroundColor(Color backgroundColor) {
        if (this.graphicsDevice == null) {
            throw new IllegalStateException("Graphics device not initialized.");
        }

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

    /**
     * Get the current FPS (Instant)
     * @return The instant FPS
     */
    public int getFps() {
        return this.instantFps;
    }

    /**
     * Get the current FPS (Smoothed)
     * @return The smoothed FPS
     */
    public int getSmoothedFps() {
        return (int) this.smoothFps;
    }
}
