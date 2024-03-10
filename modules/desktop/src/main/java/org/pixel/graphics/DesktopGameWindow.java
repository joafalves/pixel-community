package org.pixel.graphics;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.GameWindow;
import org.pixel.graphics.glfw.GLFWWindowManager;
import org.pixel.graphics.opengl.GLGraphicsDevice;

public class DesktopGameWindow extends GameWindow<DesktopWindowManager, GLGraphicsDevice, DesktopGameSettings> {

    private static final Logger log = LoggerFactory.getLogger(DesktopGameWindow.class);

    private final int[] audioAttributes = { 0 };
    private long audioDevice;
    private long audioContext;

    /**
     * Constructor
     *
     * @param settings
     */
    public DesktopGameWindow(DesktopGameSettings settings) {
        super(settings);
    }

    @Override
    protected boolean initWindowManager() {
        // TODO: make configurable when/if more options are available
        this.windowManager = new GLFWWindowManager(this.settings);
        return this.windowManager.init();
    }

    @Override
    protected boolean initGraphicsDevice() {
        switch (this.settings.getGraphicsBackend()) {
            case OpenGL:
                this.graphicsDevice = new GLGraphicsDevice((DesktopWindowManager) this.windowManager, this.settings);
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
    public void dispose() {
        // dispose audio context
        alcCloseDevice(audioDevice);
        alcDestroyContext(audioContext);

        super.dispose();
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
