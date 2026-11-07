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
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL11C.GL_PACK_ALIGNMENT;
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
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.render.DrawListSpriteBatch;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.opengl.render.GLRenderBackend;

public class GLGraphicsDevice implements GraphicsDevice {

    private static final Logger log = LoggerFactory.getLogger(GLGraphicsDevice.class);

    private final DesktopWindowManager windowManager;
    private final WindowSettings windowSettings;

    private final RenderStats stats = new RenderStats();
    private GLRenderBackend renderBackend;

    private State state;
    private Callback debugLocalCallback;

    /**
     * Constructor
     */
    public GLGraphicsDevice(DesktopWindowManager windowManager, WindowSettings windowSettings) {
        this.windowManager = windowManager;
        this.windowSettings = windowSettings;
        this.state = State.NEW;
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
        // NOTE: on resize / DPI change the viewport is updated by the window manager's framebuffer-size callback.

        // Debug-specific initialization
        if (this.windowSettings.isGlfwDebugMode()) {
            this.debugLocalCallback = GLUtil.setupDebugMessageCallback(); // must be called after "createCapabilities()"
            Configuration.DISABLE_CHECKS.set(false);
        } else {
            Configuration.DISABLE_CHECKS.set(true);
        }

        // URP v2 render backend (needs the GL context created above)
        this.renderBackend = new GLRenderBackend(stats);

        // Set base clear color:
        setClearColor(windowSettings.getBackgroundColor());

        // Show version information
        log.debug("OpenGL Vendor: {0}.", glGetString(GL_VENDOR));
        log.debug("OpenGL Renderer: {0}.", glGetString(GL_RENDERER));
        log.debug("OpenGL Version: {0}.", glGetString(GL_VERSION));
        log.debug("LWJGL Version: {0}.", org.lwjgl.Version.getVersion());

        this.state = State.INITIALIZED;
        return true;
    }

    /**
     * Get the URP v2 render backend of this device (all GPU work of draw-list based renderers goes through it).
     *
     * @return The render backend.
     */
    public GLRenderBackend getRenderBackend() {
        if (renderBackend == null) {
            throw new IllegalStateException("Graphics device is not initialized.");
        }
        return renderBackend;
    }

    @Override
    public SpriteBatch createSpriteBatch() {
        DrawListSpriteBatch spriteBatch = new DrawListSpriteBatch(getRenderBackend());
        spriteBatch.init();
        return spriteBatch;
    }

    @Override
    public RenderStats getStats() {
        return stats;
    }

    @Override
    public void endFrame() {
        stats.endFrame();
    }

    @Override
    public void dispose() {
        if (this.renderBackend != null) {
            this.renderBackend.dispose();
            this.renderBackend = null;
        }
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
        // NOTE: (sx, sy) is in framebuffer pixels with the OpenGL origin (bottom-left); rows are returned bottom-up.
        ByteBuffer imageBuffer = memAlloc(width * height * 4);
        try {
            glPixelStorei(GL_PACK_ALIGNMENT, 1);
            glReadPixels(sx, sy, width, height, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);

            byte[] data = new byte[width * height * 4];
            imageBuffer.get(0, data); // direct buffers have no backing array
            if (opaque) {
                for (int i = 3; i < data.length; i += 4) {
                    data[i] = (byte) 255;
                }
            }
            return data;
        } finally {
            memFree(imageBuffer);
        }
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
