/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

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
    protected Z settings;
    protected State state;

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
        this.state = State.CREATED;
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

    /**
     * Called when the viewport changes.
     *
     * @param width  The new viewport width.
     * @param height The new viewport height.
     */
    public void onViewportChanged(int width, int height) {
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
}
