/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import org.pixel.audio.AudioPlayer;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.graphics.GraphicsDevice;

import java.util.Properties;

/**
 * Base class of the platform hosts that run a {@link GameApp} (desktop window, Android surface, ...). Owns the
 * platform services and implements the {@link GameContext} given to the game. Game code never extends this class.
 *
 * @param <S> The graphics device type.
 * @param <Z> The settings type.
 */
public abstract class GameContainer<S extends GraphicsDevice, Z extends GameSettings> implements GameContext {

    private static final Logger log = LoggerFactory.getLogger(GameContainer.class);

    protected final GameApp app;
    protected final Z settings;
    protected S graphicsDevice;
    protected ContentManager contentManager;
    protected AudioPlayer audioPlayer;
    protected State state;

    private boolean appLoaded;
    private float elapsed;
    private int frameCount;
    private int instantFps;
    private float smoothFps;

    /**
     * Constructor.
     *
     * @param app      The game to run.
     * @param settings The settings to use.
     */
    protected GameContainer(GameApp app, Z settings) {
        if (app == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Game settings cannot be null.");
        }
        this.app = app;
        this.settings = settings;
        this.state = State.NEW;
    }

    /**
     * Initialize the platform services and attach the game (its {@link GameApp#load()} is called by the host once
     * the platform is ready).
     *
     * @return True if everything was initialized.
     */
    protected boolean initContainer() {
        if (!initGraphicsDevice()) {
            log.error("Failed to initialize the graphics device.");
            return false;
        }
        if (!initAudio()) {
            log.error("Failed to initialize the audio device.");
            return false;
        }
        this.contentManager = createContentManager();
        this.audioPlayer = createAudioPlayer();
        app.attach(this);
        return true;
    }

    /**
     * Load the game (called by the host once the platform is ready).
     */
    protected final void loadApp() {
        appLoaded = true; // set first: a game that fails while loading still gets dispose() to clean up
        app.load();
    }

    /**
     * Run one frame of game logic (called by the host loop).
     */
    protected final void updateFrame(DeltaTime delta) {
        frameCount++;
        elapsed += delta.getElapsed();
        if (elapsed >= 1) {
            instantFps = (int) (frameCount / elapsed);
            smoothFps = smoothFps * 0.4f + instantFps * 0.6f;
            elapsed = 0;
            frameCount = 0;
        }
        app.update(delta);
    }

    /**
     * Draw one frame (called by the host loop).
     */
    protected final void drawFrame(DeltaTime delta) {
        if (settings.isAutoClear()) {
            graphicsDevice.clear();
        }
        app.draw(delta);
        graphicsDevice.endFrame();
    }

    /**
     * Forward a drawable-size change to the game (called by the platform).
     */
    public void onViewportChanged(int width, int height) {
        app.onViewportChanged(width, height);
    }

    /**
     * Dispose the game first (while every context is alive), then the shared services and the graphics device.
     * Platform hosts extend this to release their own resources afterwards.
     */
    protected void disposeContainer() {
        try {
            if (appLoaded) {
                app.dispose();
            }
        } finally {
            if (contentManager != null) {
                contentManager.dispose();
            }
            if (graphicsDevice != null) {
                graphicsDevice.dispose();
            }
        }
    }

    protected abstract boolean initGraphicsDevice();

    protected abstract boolean initAudio();

    protected abstract ContentManager createContentManager();

    protected abstract AudioPlayer createAudioPlayer();

    // region GameContext

    @Override
    public S graphics() {
        return graphicsDevice;
    }

    @Override
    public ContentManager content() {
        return contentManager;
    }

    @Override
    public AudioPlayer audio() {
        return audioPlayer;
    }

    @Override
    public Z settings() {
        return settings;
    }

    @Override
    public int getFps() {
        return instantFps;
    }

    @Override
    public int getSmoothedFps() {
        return (int) smoothFps;
    }

    // endregion

    /**
     * Set the clear color.
     *
     * @param backgroundColor The color.
     */
    public void setBackgroundColor(Color backgroundColor) {
        if (graphicsDevice == null) {
            throw new IllegalStateException("Graphics device not initialized.");
        }
        graphicsDevice.setClearColor(backgroundColor);
    }

    public Properties getClientProperties() {
        return settings.getClientProperties();
    }
}
