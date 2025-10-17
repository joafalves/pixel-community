package org.pixel.core;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.pixel.audio.ALAudioPlayerFactory;
import org.pixel.audio.AudioPlayer;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.GLContentManagerFactory;
import org.pixel.graphics.glfw.GLFWWindowManager;
import org.pixel.graphics.opengl.GLGraphicsDevice;
import org.pixel.graphics.render.DirectRenderer;
import org.pixel.graphics.render.SdfTextRenderer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.canvas.text.GLSdfTextRendererServiceFactory;
import org.pixel.graphics.render.opengl.GLDirectRendererServiceFactory;
import org.pixel.graphics.render.opengl.GLSpriteBatchServiceFactory;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.ALC10.*;

public abstract class Game extends WindowGameContainer<DesktopWindowManager, GLGraphicsDevice, WindowSettings> {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    private final int[] audioAttributes = {0};
    private long audioDevice;
    private long audioContext;

    /**
     * Constructor
     *
     * @param settings The game settings.
     */
    public Game(WindowSettings settings) {
        super(settings);
    }

    @Override
    protected boolean initWindowManager() {
        this.windowManager = new GLFWWindowManager(this);
        return this.windowManager.init();
    }

    @Override
    protected boolean initGraphicsDevice() {
        log.debug("Initializing {0} graphics device.", this.settings.getGraphicsBackend());

        switch (this.settings.getGraphicsBackend()) {
            case OpenGL:
                this.graphicsDevice = new GLGraphicsDevice(this.windowManager, this.settings);
                break;
            case Vulkan:
                throw new UnsupportedOperationException("Vulkan is not supported yet.");
            default:
                throw new UnsupportedOperationException(
                        "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        return this.graphicsDevice.init();
    }

    @Override
    protected boolean initAudio() {
        log.debug("Initializing audio device.");

        try { // Attempt to initialize the audio device
            audioDevice = alcOpenDevice((ByteBuffer) null);
            if (audioDevice == 0L) {
                throw new IllegalStateException("Failed to open the default OpenAL device.");
            }

            audioContext = alcCreateContext(audioDevice, audioAttributes);
            if (audioContext == 0L) {
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

    @Override
    protected boolean initServices() {
        log.debug("Initializing services.");

        switch (this.settings.getGraphicsBackend()) {
            case OpenGL:
                ServiceProvider.register(SpriteBatch.class, new GLSpriteBatchServiceFactory());
                ServiceProvider.register(DirectRenderer.class, new GLDirectRendererServiceFactory());
                ServiceProvider.register(SdfTextRenderer.class, new GLSdfTextRendererServiceFactory());
                ServiceProvider.register(ContentManager.class, new GLContentManagerFactory());
                ServiceProvider.register(AudioPlayer.class, new ALAudioPlayerFactory());
                break;
            case Vulkan:
                throw new UnsupportedOperationException("Vulkan is not supported yet.");
            default:
                throw new UnsupportedOperationException(
                        "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        return true;
    }

    @Override
    public void dispose() {
        // dispose audio context
        alcCloseDevice(audioDevice);
        alcDestroyContext(audioContext);

        super.dispose();
    }

    /**
     * Get the window virtual width.
     *
     * @return The window virtual width.
     */
    @Override
    public int getViewportWidth() {
        return this.graphicsDevice.getViewportWidth();
    }

    /**
     * Get the window virtual height.
     *
     * @return The window virtual height.
     */
    @Override
    public int getViewportHeight() {
        return this.graphicsDevice.getViewportHeight();
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        syncViewportSize(); // By default, sync viewport size with window size on size change
        super.onWindowSizeChange(width, height);
    }

    /**
     * Sync viewport size with the window size.
     */
    public void syncViewportSize() {
        if (graphicsDevice != null) {
            int width = this.windowManager.getWindowWidth();
            int height = this.windowManager.getWindowHeight();
            graphicsDevice.updateViewport(width, height);
        }
    }

    /**
     * Toggle fullscreen mode.
     */
    public void toggleFullscreen() {
        this.windowManager.setWindowMode(
                settings.getWindowMode() == WindowMode.WINDOWED ? WindowMode.FULLSCREEN : WindowMode.WINDOWED);
    }

    /**
     * Set cursor mode.
     *
     * @param mode The cursor mode.
     */
    public void setCursorMode(WindowCursorMode mode) {
        this.windowManager.setWindowCursorMode(mode);
    }

    /**
     * Set window title.
     *
     * @param title The window title.
     */
    public void setWindowTitle(String title) {
        this.windowManager.setWindowTitle(title);
    }
}
