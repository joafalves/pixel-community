/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import static org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.data.ImageData;
import org.pixel.commons.util.IOUtils;
import org.pixel.graphics.Color;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.mouse.Mouse;
import org.pixel.math.IntSize;

public abstract class PixelWindow implements Loadable, Updatable, Drawable, Disposable {

    private static final String DEFAULT_WINDOW_ICON_PATH_64 = "engine/images/app-icon@64.png";
    private static final String DEFAULT_WINDOW_ICON_PATH_32 = "engine/images/app-icon@32.png";

    private final Logger log = LoggerFactory.getLogger(PixelWindow.class);
    private final List<WindowEventListener> windowEventListeners;

    private final Properties clientProperties;
    private final WindowDimensions windowDimensions;
    private final String windowTitle;
    private final boolean windowResizable;
    private final boolean idleThrottling;
    private final boolean debugMode;
    private final int[] audioAttributes = {0};
    private final int multisampling;

    private WindowMode windowMode;
    private Color backgroundColor;
    private Callback debugLocalCallback;
    private long windowHandle;
    private long audioDevice;
    private long audioContext;
    private boolean initialized;
    private boolean windowFocused;
    private boolean vsyncEnabled;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public PixelWindow(WindowSettings settings) {
        this.windowEventListeners = new ArrayList<>();
        this.initialized = false;
        this.clientProperties = settings.getClientProperties();
        this.windowResizable = settings.isWindowResizable();
        this.multisampling = settings.getMultisampling();
        this.vsyncEnabled = settings.isVsync();
        this.backgroundColor = settings.getBackgroundColor();
        this.debugMode = settings.isDebugMode();
        this.windowTitle = settings.getWindowTitle();
        this.windowMode = settings.getWindowMode();
        this.idleThrottling = settings.isIdleThrottle();
        this.windowDimensions = WindowDimensions.builder()
                .windowWidth(settings.getWindowWidth())
                .windowHeight(settings.getWindowHeight())
                .virtualWidth(settings.getVirtualWidth())
                .virtualHeight(settings.getVirtualHeight())
                .build();
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
     */
    public void init() {
        if (initialized) {
            throw new RuntimeException("This game class has already been initialized");
        }

        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current windowHnd hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the windowHnd will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, windowResizable ? GLFW_TRUE : GLFW_FALSE); // the windowHnd will be resizable
        glfwWindowHint(GLFW_SAMPLES, multisampling);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GL_TRUE);

        if (windowMode != WindowMode.WINDOWED) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        }

        // DEBUG CONTEXT
        if (debugMode) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }

        // Initial values
        windowFocused = true;

        // Create the windowHnd
        long monitor = windowMode.equals(WindowMode.FULLSCREEN) ? glfwGetPrimaryMonitor() : NULL;
        windowHandle = glfwCreateWindow(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(),
                windowTitle, monitor, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW windowHnd");
        }

        // center window in the user screen
        centerWindow();

        // window mode
        GLFWVidMode videoMode;
        if (windowMode.equals(WindowMode.WINDOWED)) {
            // Get the resolution of the primary monitor
            videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHandle, NULL,
                    (videoMode.width() - windowDimensions.getWindowWidth()) / 2,
                    (videoMode.height() - windowDimensions.getWindowHeight()) / 2,
                    windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);

        } else if (windowMode.equals(WindowMode.WINDOWED_BORDERLESS)) {
            videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHandle, NULL, (videoMode.width() - windowDimensions.getWindowWidth()) / 2,
                    (videoMode.height() - windowDimensions.getWindowHeight()) / 2,
                    windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        // V-sync configurations
        glfwSwapInterval(vsyncEnabled ? GLFW_TRUE : GLFW_FALSE);

        // Make the windowHnd visible
        glfwShowWindow(windowHandle);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        if (debugMode) {
            debugLocalCallback = GLUtil.setupDebugMessageCallback(); // must be called after "createCapabilities()"
        }

        // OpenGL general setup:
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_MULTISAMPLE); // should be enabled by default but let's be safe
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Configuration.DISABLE_CHECKS.set(!debugMode);

        // set resize callback:
        glfwSetWindowSizeCallback(windowHandle, (window, width, height) -> {
            windowDimensions.setWindowWidth(width);
            windowDimensions.setWindowHeight(height);
            updateViewport();
            triggerWindowSizeChangeEvent();
        });

        glfwSetWindowFocusCallback(windowHandle, ((window, focused) -> {
            log.debug("Windows focus changed: {}.", focused);
            windowFocused = focused;
        }));

        // setup org.pixel.audio main device
        try {
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
            log.error("Exception caught!", e);
        }

        // call implementation loading method
        updateViewport();
        setWindowIcon(DEFAULT_WINDOW_ICON_PATH_64, DEFAULT_WINDOW_ICON_PATH_32);
        load();

        initialized = true;
    }

    /**
     * Update Viewport.
     */
    private void updateViewport() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(windowHandle, pWidth, pHeight);
            glViewport(0, 0, pWidth.get(0), pHeight.get(0));
            windowDimensions.setPixelRatio(pWidth.get(0) / (float) windowDimensions.getWindowWidth());
            windowDimensions.setFrameWidth(pWidth.get(0));
            windowDimensions.setFrameHeight(pHeight.get(0));
        }
    }

    /**
     * Set multiple window icons from common image extensions (for example: png).
     *
     * @param filenames The filenames
     */
    public void setWindowIcon(String... filenames) {
        GLFWImage[] imageDataArray = new GLFWImage[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            ImageData imgData = IOUtils.loadImage(filenames[i]);
            if (imgData == null) {
                log.warn("Unable to set window icon, cannot load image from given file path '{}'.", filenames[i]);
                return;
            }

            GLFWImage glfwImage = GLFWImage.malloc();
            glfwImage.set(imgData.getWidth(), imgData.getHeight(), imgData.getData());
            imageDataArray[i] = glfwImage;
        }

        GLFWImage.Buffer buffer = GLFWImage.malloc(filenames.length);
        for (int i = 0; i < imageDataArray.length; i++) {
            buffer.put(i, imageDataArray[i]);
        }

        glfwSetWindowIcon(windowHandle, buffer);
    }

    /**
     * Starts the game loop. To terminate the loop, call {@link #close()} or close the window.
     */
    public void run() {
        if (!initialized) {
            throw new RuntimeException("init() must be called before running the game..");
        }

        // start the main loop:
        renderLoop();

        // finalize
        dispose(); // call for graceful termination..

        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }

    /**
     * The render loop.
     */
    private void renderLoop() {
        // pre-variable configurations
        DeltaTime delta = new DeltaTime();

        glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the windowHnd or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the screen

            // call the game user loop:
            delta.tick();

            // update call
            update(delta);

            // draw call
            draw(delta);

            // swap the color buffers
            glfwSwapBuffers(windowHandle);

            // Poll for windowHnd events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            if (!windowFocused && idleThrottling) {
                try {
                    Thread.sleep(100); // TODO: make idle period configurable

                } catch (InterruptedException e) {
                    log.error("Exception caught!", e);
                }
            }

            // TODO: frame cap mechanism (when v-sync is off)
            // the possibility to run the game in low-resource usage should be available when the windows isn't focused

            // TODO: study the feasibility/impact of implementing a multi-threaded mechanism on the game loop
            // more info: https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/glfw/Threads.java
        }
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
        alcCloseDevice(audioDevice);
        alcDestroyContext(audioContext);

        if (debugLocalCallback != null) {
            debugLocalCallback.free();
        }
    }

    /**
     * Flag the rendering loop to terminate.
     */
    public void close() {
        // set the closing flag so at the start of the next game loop the game closes
        glfwSetWindowShouldClose(windowHandle, true);
    }

    /**
     * Centers game window on the primary screen.
     */
    public void centerWindow() {
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the windowHnd size passed to glfwCreateWindow
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null) {
                log.warn("Failure to center window (unable to get resolution from the primary monitor).");
                return;
            }

            // Center the windowHnd
            glfwSetWindowPos(
                    windowHandle,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );

            // Set Input handlers:
            glfwSetKeyCallback(windowHandle, new Keyboard.KeyboardInputHandler());
            glfwSetCursorPosCallback(windowHandle, new Mouse.CursorPositionHandler());
            glfwSetMouseButtonCallback(windowHandle, new Mouse.MouseButtonHandler());
            //glfwSetCursor(windowHnd, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
        }
    }

    /**
     * Get the window dimensions.
     *
     * @return The window dimensions.
     */
    public WindowDimensions getWindowDimensions() {
        return windowDimensions;
    }

    /**
     * Get the window width.
     *
     * @return The window width.
     */
    public int getWindowWidth() {
        return this.windowDimensions.getWindowWidth();
    }

    /**
     * Get the window height.
     *
     * @return The window height.
     */
    public int getWindowHeight() {
        return this.windowDimensions.getWindowHeight();
    }

    /**
     * Get the window frame width.
     *
     * @return The window frame width.
     */
    public int getWindowFrameWidth() {
        return this.windowDimensions.getFrameWidth();
    }

    /**
     * Get the window frame height.
     *
     * @return The window frame height.
     */
    public int getWindowFrameHeight() {
        return this.windowDimensions.getFrameHeight();
    }

    /**
     * Get the window virtual width.
     *
     * @return The window virtual width.
     */
    public int getVirtualWidth() {
        return this.windowDimensions.getVirtualWidth();
    }

    /**
     * Get the window virtual height.
     *
     * @return The window virtual height.
     */
    public int getVirtualHeight() {
        return this.windowDimensions.getVirtualHeight();
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
    public IntSize getViewportSize() {
        return new IntSize(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight());
    }

    /**
     * Get primary screen size.
     *
     * @return The primary screen size.
     */
    public IntSize getPrimaryMonitorSize() {
        GLFWVidMode video = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (video == null) {
            return null;
        }

        return new IntSize(video.width(), video.height());
    }

    /**
     * Toggle fullscreen mode.
     */
    public void toggleFullscreen() {
        long monitor = NULL;
        if (windowMode.equals(WindowMode.WINDOWED) ||
                windowMode.equals(WindowMode.WINDOWED_BORDERLESS)) {
            windowMode = WindowMode.FULLSCREEN;
            monitor = glfwGetPrimaryMonitor();

            GLFWVidMode video = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (video != null) {
                windowDimensions.setWindowWidth(video.width());
                windowDimensions.setWindowHeight(video.height());
            }

        } else if (windowMode.equals(WindowMode.FULLSCREEN)) {
            windowMode = WindowMode.WINDOWED;
            windowDimensions.setWindowWidth(windowDimensions.getVirtualWidth());
            windowDimensions.setWindowHeight(windowDimensions.getVirtualHeight());
        }

        glfwSetWindowMonitor(windowHandle, monitor, 0, 0,
                windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);

        if (windowMode.equals(WindowMode.WINDOWED)) {
            centerWindow();
        }

        triggerWindowSizeChangeEvent();
        triggerWindowModeChangeEvent();
    }

    /**
     * Trigger window size change event.
     */
    private void triggerWindowSizeChangeEvent() {
        windowEventListeners.forEach(listener ->
                listener.windowSizeChanged(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight()));
    }

    /**
     * Trigger window size change event.
     */
    private void triggerWindowModeChangeEvent() {
        windowEventListeners.forEach(listener ->
                listener.windowModeChanged(windowMode));
    }

    /**
     * Set cursor mode.
     *
     * @param mode The cursor mode.
     */
    public void setCursorMode(CursorMode mode) {
        int glfwMode;
        switch (mode) {
            case DISABLED:
                glfwMode = GLFW_CURSOR_DISABLED;
                break;
            case HIDDEN:
                glfwMode = GLFW_CURSOR_HIDDEN;
                break;
            default:
                glfwMode = GLFW_CURSOR_NORMAL;
        }

        glfwSetInputMode(windowHandle, GLFW_CURSOR, glfwMode);
    }

    /**
     * Set window title.
     *
     * @param title The window title.
     */
    public void setWindowTitle(String title) {
        glfwSetWindowTitle(windowHandle, title);
    }

    /**
     * Set the window background color.
     *
     * @param backgroundColor The window background color.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;

        if (initialized) {
            glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.0f);
        }
    }

    /**
     * Get the window background color.
     *
     * @return The window background color.
     */
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * Get client properties.
     *
     * @return The client properties.
     */
    public Properties getClientProperties() {
        return clientProperties;
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
     * Get vsync mode.
     *
     * @return The vsync mode.
     */
    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    /**
     * Set vsync mode.
     *
     * @param vsyncEnabled The vsync mode.
     */
    public void setVsyncEnabled(boolean vsyncEnabled) {
        if (vsyncEnabled == this.vsyncEnabled) {
            return;
        }
        this.vsyncEnabled = vsyncEnabled;
        glfwSwapInterval(vsyncEnabled ? GLFW_TRUE : GLFW_FALSE);
    }

    /**
     * Get the window focus state.
     *
     * @return The window focus state.
     */
    public boolean isWindowFocused() {
        return windowFocused;
    }

    /**
     * Get the window native handle.
     *
     * @return The window native handle.
     */
    public long getWindowHandle() {
        return windowHandle;
    }
}
