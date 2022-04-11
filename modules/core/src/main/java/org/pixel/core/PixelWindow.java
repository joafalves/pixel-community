/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

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
import org.pixel.commons.data.ImageData;
import org.pixel.commons.lifecycle.*;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.graphics.Color;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.mouse.Mouse;
import org.pixel.math.SizeInt;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public abstract class PixelWindow implements Initializable, Loadable, Updatable, Drawable, Disposable {

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
    private boolean autoWindowClear;

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
        this.autoWindowClear = settings.isAutoWindowClear();
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
     *
     * @return True if the window was initialized successfully.
     */
    @Override
    public boolean init() {
        if (initialized) {
            throw new RuntimeException("This game class has already been initialized");
        }

        initializeGlfw();

        windowFocused = true;

        // Create the windowHnd
        long monitor = windowMode.equals(WindowMode.FULLSCREEN) ? glfwGetPrimaryMonitor() : NULL;
        windowHandle = glfwCreateWindow(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(),
                windowTitle, monitor, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW windowHnd");
        }
        centerWindow();

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

        glfwMakeContextCurrent(windowHandle); // set current window context
        glfwSwapInterval(vsyncEnabled ? GLFW_TRUE : GLFW_FALSE);
        glfwShowWindow(windowHandle);

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities
        // instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();
        if (debugMode) {
            debugLocalCallback = GLUtil.setupDebugMessageCallback(); // must be called after "createCapabilities()"
        }

        // OpenGL general setup:
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_MULTISAMPLE); // Should be enabled by default but let's be safe
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Configuration.DISABLE_CHECKS.set(!debugMode);

        log.debug("OpenGL Vendor: '{}'.", glGetString(GL_VENDOR));
        log.debug("OpenGL Renderer: '{}'.", glGetString(GL_RENDERER));
        log.debug("OpenGL Version: '{}'.", glGetString(GL_VERSION));

        initializeWindowCallbacks();
        initializeAudio();

        updateViewport();
        setWindowIcon(DEFAULT_WINDOW_ICON_PATH_64, DEFAULT_WINDOW_ICON_PATH_32);

        load();

        return initialized = true;
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
     * Clear the render window.
     */
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the screen
    }

    /**
     * Initialize GLFW.
     */
    private void initializeGlfw() {
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

        if (debugMode) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }
    }

    /**
     * Set the native window callbacks.
     */
    private void initializeWindowCallbacks() {
        // Input handlers:
        glfwSetKeyCallback(windowHandle, new Keyboard.KeyboardInputHandler());
        glfwSetCharCallback(windowHandle, new Keyboard.KeyboardCharacterHandler());
        glfwSetCursorPosCallback(windowHandle, new Mouse.CursorPositionHandler());
        glfwSetMouseButtonCallback(windowHandle, new Mouse.MouseButtonHandler());

        // Window resize callback:
        glfwSetWindowSizeCallback(windowHandle, (window, width, height) -> {
            windowDimensions.setWindowWidth(width);
            windowDimensions.setWindowHeight(height);
            updateViewport();
            triggerWindowSizeChangeEvent();
        });

        // Window focus callback:
        glfwSetWindowFocusCallback(windowHandle, ((window, focused) -> {
            log.debug("Windows focus changed: '{}'.", focused);
            windowFocused = focused;
        }));
    }

    /**
     * Initialize the audio context.
     */
    private void initializeAudio() {
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
        }
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

        renderLoop();

        dispose();

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

        while (!glfwWindowShouldClose(windowHandle)) {
            if (autoWindowClear) {
                clear();
            }

            delta.tick();

            update(delta);
            draw(delta);

            Keyboard.clear();

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();

            if (!windowFocused && idleThrottling) {
                try {
                    Thread.sleep(100); // TODO: make idle period configurable

                } catch (InterruptedException e) {
                    log.error("Exception caught!", e);
                }
            }

            // TODO: frame cap mechanism (when v-sync is off)
            // TODO: study the feasibility/impact of implementing a multi-threaded mechanism on the game loop
            // more info: https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/glfw/Threads.java
        }
    }

    /**
     * Takes a screenshot of the current frame (PNG format).
     *
     * @param filename The output filename.
     * @param opaque   Determine if the image should be opaque (transparency) or not.
     */
    public void screenshot(String filename, boolean opaque) {
        int width = getViewportWidth();
        int height = getViewportHeight();
        int stride = width * 4;

        ByteBuffer imageBuffer = memAlloc(width * height * 4);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);

        if (opaque) {
            for (int y = 0, x; y < height; y++) {
                int row = y * stride;
                for (x = 0; x < width; x++) {
                    imageBuffer.put(row + x * 4 + 3, (byte) 255);
                }
            }
        }

        // flip the image data:
        int i = 0, j = height - 1, k;
        while (i < j) {
            int ri = i * stride;
            int rj = j * stride;
            for (k = 0; k < width * 4; k++) {
                byte t = imageBuffer.get(ri + k);
                imageBuffer.put(ri + k, imageBuffer.get(rj + k));
                imageBuffer.put(rj + k, t);
            }
            i++;
            j--;
        }

        stbi_write_png(filename, width, height, 4, imageBuffer, width * 4);
        memFree(imageBuffer);
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
    public SizeInt getViewportSize() {
        return new SizeInt(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight());
    }

    /**
     * Get primary screen size.
     *
     * @return The primary screen size.
     */
    public SizeInt getPrimaryMonitorSize() {
        GLFWVidMode video = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (video == null) {
            return null;
        }

        return new SizeInt(video.width(), video.height());
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

    /**
     * Get if the render window is automatically cleared before the rendering phase.
     *
     * @return True if the render window is automatically cleared before the rendering phase.
     */
    public boolean isAutoWindowClear() {
        return autoWindowClear;
    }

    /**
     * Set if the render window is automatically cleared before the rendering phase.
     *
     * @param autoWindowClear The automatic clear state.
     */
    public void setAutoWindowClear(boolean autoWindowClear) {
        this.autoWindowClear = autoWindowClear;
    }
}
