/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.Color;
import org.pixel.graphics.GraphicsDevice;
import org.pixel.graphics.WindowManager;
import org.pixel.graphics.glfw.GLFWWindowManager;
import org.pixel.graphics.opengl.GLGraphicsDevice;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.math.SizeInt;

public abstract class GameWindow implements Initializable, Loadable, Updatable, Drawable, Disposable {

    private static final Logger log = LoggerFactory.getLogger(GameWindow.class);

    private final List<WindowEventListener> windowEventListeners;

    private final int[] audioAttributes = { 0 };

    private WindowManager windowManager;
    private GraphicsDevice graphicsDevice;
    private WindowSettings settings;
    private State state;

    private long audioDevice;
    private long audioContext;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public GameWindow(WindowSettings settings) {
        this.windowEventListeners = new ArrayList<>();
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
        // dispose audio context
        alcCloseDevice(audioDevice);
        alcDestroyContext(audioContext);

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
    private boolean initWindowManager() {
        // TODO: make configurable when/if more options are available
        this.windowManager = new GLFWWindowManager(this.settings);
        return this.windowManager.init();
    }

    /**
     * Initialize the graphics device.
     * 
     * @return True if the graphics device was initialized successfully.
     */
    private boolean initGraphicsDevice() {
        switch (this.settings.getGraphicsBackend()) {
            case OpenGL:
                this.graphicsDevice = new GLGraphicsDevice(this.windowManager, this.settings);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        return this.graphicsDevice.init();
    }

    /**
     * Initialize the audio context.
     * 
     * @return True if the audio context was initialized successfully.
     */
    private boolean initAudio() {
        try { // Attempt to initialize the audio device
            audioDevice = alcOpenDevice((ByteBuffer) null);
            if (audioDevice == NULL) {
                throw new IllegalStateException("Failed to open the default OpenAL device.");
            }

            audioContext = alcCreateContext(audioDevice, audioAttributes);
            if (audioContext == NULL) {
                throw new IllegalStateException("Failed to create OpenAL context.");
            }

            alcMakeContextCurrent(audioContext);

            ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
            AL.createCapabilities(alcCapabilities);

        } catch (Exception e) {
            log.error("Exception caught while initializing the audio device!", e);
            return false;
        }

        return true;
    }

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

            update(delta);
            draw(delta);

            Keyboard.clear();

            this.graphicsDevice.endFrame();
            this.windowManager.endFrame();

            if (!this.windowManager.isWindowFocused() && this.settings.isIdleThrottle()) {
                try {
                    Thread.sleep(100); // TODO: make idle period configurable

                } catch (InterruptedException e) {
                    log.error("Exception caught!", e);
                }
            }

            // TODO: frame cap mechanism (when v-sync is off)
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
     * Gets the debug state value.
     *
     * @return Returns 'true' if debug mode is enabled.
     */
    public boolean isDebugMode() {
        return settings.isDebugMode();
    }

    /**
     * Get the window dimensions.
     *
     * @return The window dimensions.
     */
    public WindowDimensions getWindowDimensions() {
        return this.windowManager.getWindowDimensions();
    }

    /**
     * Get the window width.
     *
     * @return The window width.
     */
    public int getWindowWidth() {
        return this.windowManager.getWindowDimensions().getWindowWidth();
    }

    /**
     * Get the window height.
     *
     * @return The window height.
     */
    public int getWindowHeight() {
        return this.windowManager.getWindowDimensions().getWindowHeight();
    }

    /**
     * Get the window frame width.
     *
     * @return The window frame width.
     */
    public int getWindowFrameWidth() {
        return this.windowManager.getWindowDimensions().getFrameWidth();
    }

    /**
     * Get the window frame height.
     *
     * @return The window frame height.
     */
    public int getWindowFrameHeight() {
        return this.windowManager.getWindowDimensions().getFrameHeight();
    }

    /**
     * Get the window virtual width.
     *
     * @return The window virtual width.
     */
    public int getVirtualWidth() {
        return this.windowManager.getWindowDimensions().getVirtualWidth();
    }

    /**
     * Get the window virtual height.
     *
     * @return The window virtual height.
     */
    public int getVirtualHeight() {
        return this.windowManager.getWindowDimensions().getVirtualHeight();
    }

    /**
     * Get the viewport width.
     *
     * @return The viewport width.
     */
    public int getViewportWidth() {
        return this.getWindowWidth();
    }

    /**
     * Get the viewport height.
     *
     * @return The viewport height.
     */
    public int getViewportHeight() {
        return this.getWindowHeight();
    }

    /**
     * Get the viewport dimensions.
     *
     * @return The viewport dimensions.
     */
    public SizeInt getViewportSize() {
        var windowDimensions = this.windowManager.getWindowDimensions();
        return new SizeInt(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight());
    }

    /**
     * Toggle fullscreen mode.
     */
    public void toggleFullscreen() {
        this.windowManager.setWindowMode(this.settings.getWindowMode() == WindowMode.WINDOWED ? WindowMode.FULLSCREEN
                : WindowMode.WINDOWED);

    }

    /**
     * Trigger window size change event.
     */
    private void triggerWindowSizeChangeEvent() {
        var windowDimensions = this.windowManager.getWindowDimensions();
        windowEventListeners.forEach(listener -> listener.windowSizeChanged(windowDimensions.getWindowWidth(),
                windowDimensions.getWindowHeight()));
    }

    /**
     * Trigger window size change event.
     */
    private void triggerWindowModeChangeEvent() {
        //windowEventListeners.forEach(listener -> listener.windowModeChanged(windowMode));
    }

    /**
     * Set cursor mode.
     *
     * @param mode The cursor mode.
     */
    public void setCursorMode(CursorMode mode) {
        this.windowManager.setCursorMode(mode);
    }

    /**
     * Set window title.
     *
     * @param title The window title.
     */
    public void setWindowTitle(String title) {
        this.windowManager.setWindowTitle(title);
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
     * Register a window event listener.
     *
     * @param listener The window event listener to remove.
     * @return True if the listener was successfully unregistered.
     */
    public boolean removeWindowEventListener(WindowEventListener listener) {
        return windowEventListeners.remove(listener);
    }

    /**
     * Register a window event listener.
     *
     * @param listener The window event listener to register.
     */
    public void addWindowEventListener(WindowEventListener listener) {
        this.windowEventListeners.add(listener);
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
     * Get the active graphics device.
     * 
     * @return The active graphics device.
     */
    public GraphicsDevice getGraphicsDevice() {
        return this.graphicsDevice;
    }

    /**
     * Get the active window manager.
     * 
     * @return The active window manager.
     */
    public WindowManager getWindowManager() {
        return windowManager;
    }
}
