/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.core;

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
import pixel.commons.lifecycle.Disposable;
import pixel.commons.lifecycle.Drawable;
import pixel.commons.lifecycle.Loadable;
import pixel.commons.lifecycle.Updatable;
import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;
import pixel.commons.model.ImageData;
import pixel.commons.util.IOUtils;
import pixel.graphics.Color;
import pixel.input.keyboard.Keyboard;
import pixel.input.mouse.Mouse;
import pixel.math.Size;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Game implements Loadable, Updatable, Drawable, Disposable {

    private static final String DEFAULT_WINDOW_ICON_PATH_64 = "images/app-icon@64.png";
    private static final String DEFAULT_WINDOW_ICON_PATH_32 = "images/app-icon@32.png";
    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    private final List<GameWindowEventListener> gameWindowEventListeners;

    private final Properties clientProperties;
    private final WindowDimensions windowDimensions;
    private final String windowTitle;
    private final boolean windowResizable;
    private final boolean idleThrottling;
    private final boolean debugMode;
    private final int[] audioAttributes = {0};
    private final int multisampling;

    private long windowHnd;
    private long audioDevice;
    private long audioContext;
    private boolean initialized;
    private boolean windowFocused;
    private boolean vsyncEnabled;
    private WindowMode windowMode;
    private Color backgroundColor;
    private Callback debugLocalCallback;

    protected Camera2D gameCamera;

    /**
     * Constructor
     */
    public Game(GameSettings settings) {
        this.gameWindowEventListeners = new ArrayList<>();
        this.initialized = false;
        this.clientProperties = settings.getClientProperties();
        this.windowResizable = settings.isWindowResizable();
        this.multisampling = settings.getMultisampling();
        this.vsyncEnabled = settings.isVsyncEnabled();
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
     * This function will initialize and run the game window
     */
    public void start() {
        this.init();
        this.run();
    }

    /**
     * Initialization method
     */
    public void init() {
        if (initialized) {
            throw new RuntimeException("This game class has already been initialized");
        }

        // Setup an error callback. The default implementation
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
        gameCamera = new Camera2D(0, 0, windowDimensions.getVirtualWidth(), windowDimensions.getVirtualHeight());
        windowFocused = true;

        // Create the windowHnd
        long monitor = windowMode.equals(WindowMode.FULLSCREEN) ? glfwGetPrimaryMonitor() : NULL;
        windowHnd = glfwCreateWindow(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(),
                windowTitle, monitor, NULL);
        if (windowHnd == NULL) {
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
            glfwSetWindowMonitor(windowHnd, NULL,
                    (videoMode.width() - windowDimensions.getWindowWidth()) / 2,
                    (videoMode.height() - windowDimensions.getWindowHeight()) / 2,
                    windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);

        } else if (windowMode.equals(WindowMode.WINDOWED_BORDERLESS)) {
            videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHnd, NULL, (videoMode.width() - windowDimensions.getWindowWidth()) / 2,
                    (videoMode.height() - windowDimensions.getWindowHeight()) / 2,
                    windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHnd);

        // V-sync configurations
        glfwSwapInterval(vsyncEnabled ? GLFW_TRUE : GLFW_FALSE);

        // Make the windowHnd visible
        glfwShowWindow(windowHnd);

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
        glfwSetWindowSizeCallback(windowHnd, (window, width, height) -> {
            windowDimensions.setWindowWidth(width);
            windowDimensions.setWindowHeight(height);
            updateViewport();
            triggerWindowSizeChangeEvent();
        });

        glfwSetWindowFocusCallback(windowHnd, ((window, focused) -> {
            LOG.debug("Windows focus changed: %b", focused);
            windowFocused = focused;
        }));

        // setup audio main device
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
            LOG.error("Exception caught!", e);
        }

        // call implementation loading method
        updateViewport();
        setWindowIcon(DEFAULT_WINDOW_ICON_PATH_64, DEFAULT_WINDOW_ICON_PATH_32);
        load();

        initialized = true;
    }

    /**
     * Update Viewport
     */
    private void updateViewport() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(windowHnd, pWidth, pHeight);
            glViewport(0, 0, pWidth.get(0), pHeight.get(0));
            windowDimensions.setPixelRatio(pWidth.get(0) / (float) windowDimensions.getWindowWidth());
            windowDimensions.setFrameWidth(pWidth.get(0));
            windowDimensions.setFrameHeight(pHeight.get(0));
        }
    }

    /**
     * Set window icon
     *
     * @param filenames
     */
    public void setWindowIcon(String... filenames) {
        GLFWImage[] imageDataArray = new GLFWImage[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            ImageData imgData = IOUtils.loadImage(filenames[i]);
            if (imgData == null) {
                LOG.warn("Unable to set window icon, cannot load image from given file path '%s'", filenames[i]);
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

        glfwSetWindowIcon(windowHnd, buffer);
    }

    /**
     * Starts the game loop
     */
    public void run() {
        if (!initialized) {
            throw new RuntimeException("init() must be called before running the game..");
        }

        // start the main loop:
        gameLoop();

        // finalize
        dispose(); // call for graceful termination..

        glfwDestroyWindow(windowHnd);
        glfwTerminate();
    }

    private void gameLoop() {
        // pre-variable configurations
        double now;
        float dt;
        double lastTime = System.nanoTime();

        glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the windowHnd or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(windowHnd)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the screen

            // call the game user loop:
            now = System.nanoTime();
            dt = ((float) (now - lastTime)) / 1000000000.0f;
            // update call
            update(dt);
            // draw call
            draw(dt);
            // update timings:
            lastTime = now;
            // swap the color buffers
            glfwSwapBuffers(windowHnd);

            // Poll for windowHnd events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            if (!windowFocused && idleThrottling) {
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    LOG.error("Exception caught!", e);
                }
            }

            // TODO: frame cap mechanism (when v-sync is off)
            // the possibility to run the game in low-resource usage should be available when the windows isn't focused

            // TODO: study the feasibility/impact of implementing a multi-threaded mechanism on the game loop
            // more info: https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/glfw/Threads.java
        }
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
     * Close game window
     */
    public void close() {
        // set the closing flag so at the start of the next game loop the game closes
        glfwSetWindowShouldClose(windowHnd, true);
    }

    /**
     * Centers game window
     */
    public void centerWindow() {
        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the windowHnd size passed to glfwCreateWindow
            glfwGetWindowSize(windowHnd, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the windowHnd
            glfwSetWindowPos(
                    windowHnd,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );

            // Set Input handlers:
            glfwSetKeyCallback(windowHnd, new Keyboard.KeyboardInputHandler());
            glfwSetCursorPosCallback(windowHnd, new Mouse.CursorPositionHandler());
            glfwSetMouseButtonCallback(windowHnd, new Mouse.MouseButtonHandler());
            //glfwSetCursor(windowHnd, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
        }
    }

    /**
     * @return
     */
    public WindowDimensions getWindowDimensions() {
        return windowDimensions;
    }

    /**
     * @return
     */
    public int getWindowWidth() {
        return this.windowDimensions.getWindowWidth();
    }

    /**
     * @return
     */
    public int getWindowHeight() {
        return this.windowDimensions.getWindowHeight();
    }

    /**
     * @return
     */
    public int getWindowFrameWidth() {
        return this.windowDimensions.getFrameWidth();
    }

    /**
     * @return
     */
    public int getWindowFrameHeight() {
        return this.windowDimensions.getFrameHeight();
    }

    /**
     * Get the window width
     *
     * @return
     */
    public int getVirtualWidth() {
        return this.windowDimensions.getVirtualWidth();
    }

    /**
     * Get the window height
     *
     * @return
     */
    public int getVirtualHeight() {
        return this.windowDimensions.getVirtualHeight();
    }

    /**
     * @return
     */
    public int getViewportWidth() {
        return this.getWindowWidth();
    }

    /**
     * @return
     */
    public int getViewportHeight() {
        return this.getWindowHeight();
    }

    /**
     * @return
     */
    public Size getViewportSize() {
        return new Size(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight());
    }

    /**
     * Get active monitor size
     *
     * @return
     */
    public Size getPrimaryMonitorSize() {
        GLFWVidMode video = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (video == null) {
            return null;
        }

        return new Size(video.width(), video.height());
    }

    /**
     * Toggle fullscreen
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

        glfwSetWindowMonitor(windowHnd, monitor, 0, 0,
                windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight(), GLFW_DONT_CARE);

        if (windowMode.equals(WindowMode.WINDOWED)) {
            centerWindow();
        }

        triggerWindowSizeChangeEvent();
        triggerWindowModeChangeEvent();
    }

    /**
     * Trigger window size change event
     */
    private void triggerWindowSizeChangeEvent() {
        gameWindowEventListeners.forEach(listener ->
                listener.gameWindowSizeChanged(windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight()));
    }

    /**
     * Trigger window size change event
     */
    private void triggerWindowModeChangeEvent() {
        gameWindowEventListeners.forEach(listener ->
                listener.gameWindowModeChanged(windowMode));
    }

    /**
     * Set cursor mode
     *
     * @param mode
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

        glfwSetInputMode(windowHnd, GLFW_CURSOR, glfwMode);
    }

    /**
     * Set window title
     *
     * @param title
     */
    public void setWindowTitle(String title) {
        glfwSetWindowTitle(windowHnd, title);
    }

    /**
     * @param backgroundColor
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;

        if (initialized) {
            glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0.0f);
        }
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * Get user properties
     */
    public Properties getClientProperties() {
        return clientProperties;
    }

    /**
     * Get default game camera
     *
     * @return
     */
    public Camera2D getGameCamera() {
        return gameCamera;
    }

    /**
     * @param listener
     * @return
     */
    public boolean removeWindowEventListener(GameWindowEventListener listener) {
        return gameWindowEventListeners.remove(listener);
    }

    /**
     * @param listener
     */
    public void addWindowEventListener(GameWindowEventListener listener) {
        this.gameWindowEventListeners.add(listener);
    }

    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    public void setVsyncEnabled(boolean vsyncEnabled) {
        if (vsyncEnabled == this.vsyncEnabled) return;
        this.vsyncEnabled = vsyncEnabled;
        glfwSwapInterval(vsyncEnabled ? GLFW_TRUE : GLFW_FALSE);
    }

    public boolean isWindowFocused() {
        return windowFocused;
    }
}
