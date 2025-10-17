package org.pixel.graphics.glfw;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.pixel.commons.data.ImageData;
import org.pixel.commons.data.Pair;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.*;
import org.pixel.io.FileUtils;
import org.pixel.graphics.GraphicsBackend;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.mouse.Mouse;

public class GLFWWindowManager extends DesktopWindowManager {

    private static final Logger log = LoggerFactory.getLogger(GLFWWindowManager.class);
    private static final String DEFAULT_WINDOW_ICON_PATH_64 = "engine/images/app-icon@64.png";
    private static final String DEFAULT_WINDOW_ICON_PATH_32 = "engine/images/app-icon@32.png";
    private static final int OPENGL_VERSION_MAJOR = 3;
    private static final int OPENGL_VERSION_MINOR = 3;

    private final WindowGameContainer<?, ?, ?> game;
    private final WindowSettings windowSettings;

    private State state;
    private WindowDimensions windowDimensions;
    private long windowHandle;
    private long monitorHandle;
    private boolean isWindowFocused;

    public GLFWWindowManager(WindowGameContainer<?, ?, ?> game) {
        this.game = game;
        this.windowSettings = (WindowSettings) game.getSettings();
        this.state = State.NEW;
    }

    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Window Manager already initialized.");
            return false;
        }

        log.debug("Initializing GLFW window manager...");

        if (this.windowSettings.isHighPriorityProcess()) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        }

        // Pre-calculate window dimensions
        this.windowDimensions = WindowDimensions.builder()
                .windowWidth(this.windowSettings.getWindowWidth())
                .windowHeight(this.windowSettings.getWindowHeight())
                .viewportWidth(this.windowSettings.getViewportWidth())
                .viewportHeight(this.windowSettings.getViewportHeight())
                .pixelRatio(1f)
                .build();

        // Initialize GLFW & setup render window:
        this.initGLFW();
        this.windowHandle = this.createWindow();
        this.updateWindowMode();
        this.centerWindow();

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

        glfwFreeCallbacks(windowHandle);
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
        // Clear single-frame mapped keys
        Keyboard.clear();

        // NOTE: The following code, MUST be at the end of the render cycle:
        // Swap buffers and poll events
        glfwSwapBuffers(windowHandle);

        if (!isWindowFocused() && windowSettings.isIdleThrottle()) {
            // Wait for direct events if the window is not focused to reduce unnecessary CPU usage
            glfwWaitEventsTimeout(.5); // Argument is in seconds
        } else {
            glfwPollEvents();
        }
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
    public void setWindowSize(int width, int height) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set window dimensions, window manager is not initialized.");
            return;
        }

        glfwSetWindowSize(windowHandle, width, height);

        // Update window dimensions
        this.windowDimensions.setWindowWidth(width);
        this.windowDimensions.setWindowHeight(height);
        this.windowDimensions.setPixelRatio(width / (float) windowDimensions.getWindowWidth());

        this.game.onWindowSizeChange(width, height);
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
    public void setWindowCursorMode(WindowCursorMode mode) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set cursor mode, window manager is not initialized.");
            return;
        }

        int glfwMode = switch (mode) {
            case DISABLED -> GLFW_CURSOR_DISABLED;
            case HIDDEN -> GLFW_CURSOR_HIDDEN;
            default -> GLFW_CURSOR_NORMAL;
        };

        glfwSetInputMode(windowHandle, GLFW_CURSOR, glfwMode);
    }

    @Override
    public void setWindowCursorType(WindowCursorType type) {
        if (!this.state.hasInitialized()) {
            log.warn("Unable to set cursor type, window manager is not initialized.");
            return;
        }

        int glfwType = switch (type) {
            case CROSSHAIR -> GLFW_CROSSHAIR_CURSOR;
            case HAND -> GLFW_HAND_CURSOR;
            case IBEAM -> GLFW_IBEAM_CURSOR;
            case HRESIZE -> GLFW_HRESIZE_CURSOR;
            case VRESIZE -> GLFW_VRESIZE_CURSOR;
            case POINTER -> GLFW_POINTING_HAND_CURSOR;
            case RESIZE_NWSE -> GLFW_RESIZE_NWSE_CURSOR;
            case RESIZE_NESW -> GLFW_RESIZE_NESW_CURSOR;
            case RESIZE_ALL -> GLFW_RESIZE_ALL_CURSOR;
            case NOT_ALLOWED -> GLFW_NOT_ALLOWED_CURSOR;
            default -> GLFW_ARROW_CURSOR;
        };

        glfwSetCursor(windowHandle, glfwCreateStandardCursor(glfwType));
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
            ImageData imgData = FileUtils.loadImage(iconPaths[i]);
            if (imgData == null) {
                log.warn("Unable to set window icon, cannot load image from given file path {0}.", iconPaths[i]);
                return;
            }

            GLFWImage glfwImage = GLFWImage.malloc();
            glfwImage.set(imgData.width(), imgData.height(), imgData.data());
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

        return !glfwWindowShouldClose(windowHandle);
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

        // Assign the active monitor:
        // TODO: make this configurable
        this.monitorHandle = glfwGetPrimaryMonitor();

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current windowHnd hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the windowHnd will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, this.windowSettings.isWindowResizable() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, this.windowSettings.getMultisampling());
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, this.windowSettings.isWindowHighDpi() ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, this.windowSettings.isWindowHighDpi() ? GLFW_TRUE : GLFW_FALSE);

        if (this.windowSettings.getGraphicsBackend() == GraphicsBackend.OpenGL) {
            // OpenGL specific settings
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, OPENGL_VERSION_MAJOR);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, OPENGL_VERSION_MINOR);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

            if (this.windowSettings.isGlfwDebugMode()) {
                glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            }
        }
    }

    private long createWindow() {
        // Note: For some reason, if you define the monitorHandle on glfwCreateWindow, it assumes that the window is
        // fullscreen. So, we need to set it to NULL if the window is not fullscreen.
        long windowCreateMonitorHandle =
                this.windowSettings.getWindowMode().equals(WindowMode.FULLSCREEN) ? monitorHandle : NULL;
        long windowHandle = glfwCreateWindow(
                this.windowSettings.getWindowWidth(), this.windowSettings.getWindowHeight(),
                this.windowSettings.getTitle(), windowCreateMonitorHandle, 0);
        if (windowHandle == 0) {
            // CRITICAL ERROR: Failed to create the GLFW window, application will be
            // terminated.
            log.error("Failed to create the GLFW window.");
            throw new RuntimeException("Failed to create the GLFW window");
        }

        return windowHandle;
    }

    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            // Get monitor work area (usable space excluding taskbars/docks)
            IntBuffer xPos = stack.mallocInt(1);
            IntBuffer yPos = stack.mallocInt(1);
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetMonitorWorkarea(monitorHandle, xPos, yPos, width, height);

            // Get window size in screen coordinates
            IntBuffer winWidth = stack.mallocInt(1);
            IntBuffer winHeight = stack.mallocInt(1);
            glfwGetWindowSize(windowHandle, winWidth, winHeight);

            // Calculate centered position
            int centerX = xPos.get(0) + (width.get(0) - winWidth.get(0)) / 2;
            int centerY = yPos.get(0) + (height.get(0) - winHeight.get(0)) / 2;

            glfwSetWindowPos(windowHandle, centerX, centerY);
        }
    }

    private Pair<Float, Float> getMonitorContentScale() {
        // Note: On Linux this function may return unexpected values for fractional scaling as it depends on the
        // window manager and compositor. For example, a fractional scaling of 125% might return 2.0f.
        try (MemoryStack stack = stackPush()) {
            FloatBuffer xScale = stack.mallocFloat(1);
            FloatBuffer yScale = stack.mallocFloat(1);
            glfwGetWindowContentScale(windowHandle, xScale, yScale);
            return new Pair<>(xScale.get(), yScale.get());
        }
    }

    private void updateWindowMode() {
        if (this.windowSettings.getWindowMode() == WindowMode.WINDOWED) {
            glfwWindowHint(GLFW_DECORATED, windowSettings.isWindowDecorated() ? GLFW_TRUE : GLFW_FALSE);
        } else {
            glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        }

        GLFWVidMode videoMode;
        if (this.windowSettings.getWindowMode().equals(WindowMode.WINDOWED)) {
            // Get the resolution of the monitor
            videoMode = glfwGetVideoMode(monitorHandle);
            // Set to windowed mode and center on the user screen:
            assert videoMode != null;
            glfwSetWindowMonitor(windowHandle, NULL,
                    (videoMode.width() - this.windowSettings.getWindowWidth()) / 2,
                    (videoMode.height() - this.windowSettings.getWindowHeight()) / 2,
                    this.windowSettings.getWindowWidth(), this.windowSettings.getWindowHeight(), GLFW_DONT_CARE);

        } else if (this.windowSettings.getWindowMode().equals(WindowMode.WINDOWED_BORDERLESS)) {
            videoMode = glfwGetVideoMode(monitorHandle);
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
            try (MemoryStack stack = stackPush()) {
                IntBuffer fbWidth = stack.mallocInt(1);
                IntBuffer fbHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(window, fbWidth, fbHeight);
                int actualWidth = fbWidth.get(0);
                int actualHeight = fbHeight.get(0);

                setWindowSize(actualWidth, actualHeight);
            }
        });

        // Window focus callback:
        glfwSetWindowFocusCallback(windowHandle, ((window, focused) -> {
            log.debug("Render window focus changed: {0}.", focused);
            this.isWindowFocused = focused;
        }));

        glfwSetWindowCloseCallback(windowHandle, (window) -> {
            log.debug("Close render window requested by user.");
            dispose();
        });
    }
}
