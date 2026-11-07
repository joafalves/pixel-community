package org.pixel.core;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.pixel.audio.ALAudioPlayer;
import org.pixel.audio.AudioPlayer;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.DesktopResourceLoader;
import org.pixel.content.importer.ALVorbisAudioImporter;
import org.pixel.content.importer.ByteBufferImporter;
import org.pixel.content.importer.GLFontImporter;
import org.pixel.content.importer.GLTextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.graphics.glfw.GLFWWindowManager;
import org.pixel.graphics.opengl.GLGraphicsDevice;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.ALC10.*;

/**
 * Desktop host (GLFW window, OpenGL, OpenAL) running a {@link GameApp}. Created by {@link DesktopLauncher}.
 */
public final class DesktopGameHost extends WindowGameContainer<DesktopWindowManager, GLGraphicsDevice, WindowSettings> {

    private static final Logger log = LoggerFactory.getLogger(DesktopGameHost.class);

    private final int[] audioAttributes = {0};
    private long audioDevice;
    private long audioContext;

    DesktopGameHost(GameApp app, WindowSettings settings) {
        super(app, settings);
    }

    @Override
    public DesktopWindowManager window() {
        return windowManager;
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
    protected ContentManager createContentManager() {
        return new ContentManager(new DesktopResourceLoader(),
                // desktop specific importers
                new GLTextureImporter(),
                new GLFontImporter(),
                new ALVorbisAudioImporter(),
                // common importers
                new TextImporter(),
                new TexturePackImporter(),
                new ByteBufferImporter()
        );
    }

    @Override
    protected AudioPlayer createAudioPlayer() {
        return new ALAudioPlayer();
    }

    @Override
    protected void disposePlatformAudio() {
        // the context must be released before its device is closed
        if (audioContext != 0L) {
            alcMakeContextCurrent(0L);
            alcDestroyContext(audioContext);
            audioContext = 0L;
        }
        if (audioDevice != 0L) {
            alcCloseDevice(audioDevice);
            audioDevice = 0L;
        }
    }
}
