package org.pixel.graphics.glfw;

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
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.pixel.commons.data.ImageData;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.core.CursorMode;
import org.pixel.core.WindowDimensions;
import org.pixel.core.WindowMode;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.GraphicsBackend;
import org.pixel.graphics.WindowManager;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.mouse.Mouse;

public class GLFWWindowManager implements WindowManager {

    private static final Logger log = LoggerFactory.getLogger(GLFWWindowManager.class);
    private static final String DEFAULT_WINDOW_ICON_PATH_64 = "engine/images/app-icon@64.png";
    private static final String DEFAULT_WINDOW_ICON_PATH_32 = "engine/images/app-icon@32.png";
    private static final int OPENGL_VERSION_MAJOR = 3;
    private static final int OPENGL_VERSION_MINOR = 3;

    private final WindowSettings windowSettings;

    private State state;
    private WindowDimensions windowDimensions;
    private long windowHandle;
    private boolean isWindowFocused;

    public GLFWWindowManager(WindowSettings windowSettings) {
        this.windowSettings = windowSettings;
        this.state = State.CREATED;
    }

    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Window Manager already initialized.");
            return false;
        }

        log.debug("Initializing GLFW window manager...");

        // Pre-calculate window dimensions
        this.windowDimensions = WindowDimensions.builder()
                .windowWidth(this.windowSettings.getWindowWidth())
                .windowHeight(this.windowSettings.getWindowHeight())
                .virtualWidth(this.windowSettings.getVirtualWidth())
                .virtualHeight(this.windowSettings.getVirtualHeight())
                .pixelRatio(1f)
                .frameWidth(this.windowSettings.getWindowWidth())
                .frameHeight(this.windowSettings.getWindowHeight())
                .build();

        // Initialize GLFW & setup render window:
        this.initGLFW();
        this.windowHandle = this.createWindow();
        this.centerWindow();
        this.updateWindowMode();

        // TODO: does it make sense to expose this functionality?
        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        // V-SYNC setup
        glfwSwapInterval(this.windowSettings.isVsync() ? GLFW_TRUE : GLFW_FALSE);

        // Make the window visible & set default icon
        glfwShowWindow(windowHandle);
        this.isWindowFocused = true;

        // Setup window callbacks
        this.initWindowCallbacks();

        this.state = State.INITIALIZED;
        this.setWindowIcon(DEFAULT_WINDOW_ICON_PATH_64, DEFAULT_WINDOW_ICON_PATH_32);

        return true;
    }

    @Override
    public synchronized void dispose() {
        if (this.state.isDisposed()) {
            // Window manager is already disposed, nothing to do here
            return;
        }

        this.state = State.DISPOSING;

        glfwSetWindowShouldClose(windowHandle, true);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();

        this.state = State.DISPOSED;
    }

    @Override
    public void beginFrame() {
        // nothing to do here
    }

    @Override
    public void endFrame() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }

    @Override
    public long getWindowHandle() {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to get window handle, window manager is not initialized.");
            return -1;
        }

        return this.windowHandle;
    }

    @Override
    public void setWindowDimensions(int width, int height) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set window dimensions, window manager is not initialized.");
            return;
        }

        glfwSetWindowSize(windowHandle, width, height);  
        
        // Update window dimensions
        this.windowDimensions.setWindowWidth(width);
        this.windowDimensions.setWindowHeight(height);
        this.windowDimensions.setPixelRatio(width / (float) windowDimensions.getWindowWidth());
        this.windowDimensions.setFrameWidth(width);
        this.windowDimensions.setFrameHeight(height);
    }

    @Override
    public void setWindowMode(WindowMode mode) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set window mode, window manager is not initialized.");
            return;
        }

        this.windowSettings.setWindowMode(mode);
        this.updateWindowMode();        
    }

    @Override
    public void setCursorMode(CursorMode mode) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set cursor mode, window manager is not initialized.");
            return;
        }

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

    @Override
    public void setVSync(boolean enabled) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set V-SYNC, window manager is not initialized.");
            return;
        }

        glfwSwapInterval(enabled ? GLFW_TRUE : GLFW_FALSE);
    }

    @Override
    public void setWindowTitle(String title) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set window title, window manager is not initialized.");
            return;
        }

        glfwSetWindowTitle(windowHandle, title);
    }

    @Override
    public void setWindowIcon(String... iconPaths) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set window icon, window manager is not initialized.");
            return;
        }

        GLFWImage[] imageDataArray = new GLFWImage[iconPaths.length];
        for (int i = 0; i < iconPaths.length; i++) {
            ImageData imgData = IOUtils.loadImage(iconPaths[i]);
            if (imgData == null) {
                log.warn("Unable to set window icon, cannot load image from given file path '{}'.", iconPaths[i]);
                return;
            }

            GLFWImage glfwImage = GLFWImage.malloc();
            glfwImage.set(imgData.getWidth(), imgData.getHeight(), imgData.getData());
            imageDataArray[i] = glfwImage;
        }

        GLFWImage.Buffer buffer = GLFWImage.malloc(iconPaths.length);
        for (int i = 0; i < imageDataArray.length; i++) {
            buffer.put(i, imageDataArray[i]);
        }

        glfwSetWindowIcon(windowHandle, buffer);
    }

    @Override
    public WindowDimensions getWindowDimensions() {
        return this.windowDimensions;
    }

    @Override
    public boolean isWindowFocused() {
        return this.isWindowFocused;
    }

    @Override
    public boolean isWindowActive() {
        if (this.state.isDisposed()) {
            // Window manager is disposed, no need to check for window activity
            return false;
        }

        return !glfwWindowShouldClose(windowHandle) ;
    }

    private void initGLFW() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            // CRITICAL ERROR: Unable to initialize GLFW, application will be terminated.
            log.error("Unable to initialize GLFW.");
            throw new RuntimeException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current windowHnd hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the windowHnd will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, this.windowSettings.isWindowResizable() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, this.windowSettings.getMultisampling());

        if (this.windowSettings.getGraphicsBackend() == GraphicsBackend.OpenGL) {
            // OpenGL specific settings
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL_VERSION_MAJOR);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL_VERSION_MINOR);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GL_TRUE);

            if (this.windowSettings.isDebugMode()) {
                glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            }
        }
    }

    private long createWindow() {
        long monitorHandle = this.windowSettings.getWindowMode() == WindowMode.FULLSCREEN ? glfwGetPrimaryMonitor() : 0;
        long windowHandle = glfwCreateWindow(
                this.windowSettings.getWindowWidth(), this.windowSettings.getWindowHeight(),
                this.windowSettings.getWindowTitle(), monitorHandle, 0);
        if (windowHandle == 0) {
            // CRITICAL ERROR: Failed to create the GLFW window, application will be
            // terminated.
            log.error("Failed to create the GLFW window.");
            throw new RuntimeException("Failed to create the GLFW window");
        }

        return windowHandle;
    }

    private void centerWindow() {
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
                    (videoMode.height() - pHeight.get(0)) / 2);
        }
    }

    private void updateWindowMode() {
        if (this.windowSettings.getWindowMode() != WindowMode.WINDOWED) {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        }

        GLFWVidMode videoMode;
        if (this.windowSettings.getWindowMode().equals(WindowMode.WINDOWED)) {
            // Get the resolution of the primary monitor
            videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHandle, NULL,
                    (videoMode.width() - this.windowSettings.getWindowWidth()) / 2,
                    (videoMode.height() - this.windowSettings.getWindowHeight()) / 2,
                    this.windowSettings.getWindowWidth(), this.windowSettings.getWindowHeight(), GLFW_DONT_CARE);

        } else if (this.windowSettings.getWindowMode().equals(WindowMode.WINDOWED_BORDERLESS)) {
            videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHandle, NULL, (videoMode.width() - this.windowSettings.getWindowWidth()) / 2,
                    (videoMode.height() - this.windowSettings.getWindowHeight()) / 2,
                    this.windowSettings.getWindowWidth(), this.windowSettings.getWindowHeight(), GLFW_DONT_CARE);
        }
    }

    private void initWindowCallbacks() {
        // Input handlers:
        glfwSetKeyCallback(windowHandle, new Keyboard.KeyboardInputHandler());
        glfwSetCharCallback(windowHandle, new Keyboard.KeyboardCharacterHandler());
        glfwSetCursorPosCallback(windowHandle, new Mouse.CursorPositionHandler());
        glfwSetMouseButtonCallback(windowHandle, new Mouse.MouseButtonHandler());

        // Window resize callback:
        glfwSetWindowSizeCallback(windowHandle, (window, width, height) -> {
            this.windowSettings.setWindowWidth(width);
            this.windowSettings.setWindowHeight(height);
            this.windowDimensions.setPixelRatio(width / (float) windowDimensions.getWindowWidth());
            this.windowDimensions.setFrameWidth(width);
            this.windowDimensions.setFrameHeight(height);
            // TODO: trigger event!
        });

        // Window focus callback:
        glfwSetWindowFocusCallback(windowHandle, ((window, focused) -> {
            log.debug("Render window focus changed: '{}'.", focused);
            this.isWindowFocused = focused;
        }));

        glfwSetWindowCloseCallback(windowHandle, (window) -> {
            log.debug("Close render window requested by user.");
            dispose();
        });
    }
}
