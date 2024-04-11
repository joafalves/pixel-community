package org.pixel.graphics.opengl;

import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_RENDERER;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_VENDOR;
import static org.lwjgl.opengl.GL11C.GL_VERSION;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGetString;
import static org.lwjgl.opengl.GL11C.glReadPixels;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.DesktopWindowManager;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.GraphicsDevice;

public class GLGraphicsDevice implements GraphicsDevice {

    private static final Logger log = LoggerFactory.getLogger(GLGraphicsDevice.class);

    private final DesktopWindowManager windowManager;
    private final WindowSettings windowSettings;

    private State state;
    private Callback debugLocalCallback;

    /**
     * Constructor
     */
    public GLGraphicsDevice(DesktopWindowManager windowManager, WindowSettings windowSettings) {
        this.windowManager = windowManager;
        this.windowSettings = windowSettings;
        this.state = State.CREATED;
    }

    @Override
    public synchronized boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Graphics device already initialized.");
            return false;
        }

        log.debug("Initializing OpenGL graphics device...");

        // Initialize GL & set window callbacks
        this.initGLCapabilities();

        // Update viewport dimensions
        var windowDimensions = this.windowManager.getWindowDimensions();
        glViewport(0, 0, windowDimensions.getWindowWidth(), windowDimensions.getWindowHeight());
        // TODO: call glViewport when window is resized (sync with window manager)

        // Debug-specific initialization
        if (this.windowSettings.isDebugMode()) {
            this.debugLocalCallback = GLUtil.setupDebugMessageCallback(); // must be called after "createCapabilities()"
            Configuration.DISABLE_CHECKS.set(false);
        } else {
            Configuration.DISABLE_CHECKS.set(true);
        }

        // Set base clear color:
        setClearColor(windowSettings.getBackgroundColor());

        // Show version information
        log.debug("OpenGL Vendor: '{}'.", glGetString(GL_VENDOR));
        log.debug("OpenGL Renderer: '{}'.", glGetString(GL_RENDERER));
        log.debug("OpenGL Version: '{}'.", glGetString(GL_VERSION));
        log.debug("LWJGL Version: '{}'.", org.lwjgl.Version.getVersion());

        this.state = State.INITIALIZED;
        return true;
    }

    @Override
    public void dispose() {
        if (this.debugLocalCallback != null) {
            this.debugLocalCallback.free();
        }

        this.state = State.DISPOSED;
    }

    @Override
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void setClearColor(Color color) {
        glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    @Override
    public byte[] readPixels(int sx, int sy, int width, int height, boolean opaque) {
        final int stride = width * 4;

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

        var data = imageBuffer.array();
        memFree(imageBuffer);

        return data;
    }

    private void initGLCapabilities() {
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context,
        // or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities
        // instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        if (this.windowSettings.getMultisampling() > 0) {
            glEnable(GL_MULTISAMPLE);

        } else {
            glDisable(GL_MULTISAMPLE);
        }
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
}
