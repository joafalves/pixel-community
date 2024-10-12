package org.pixel.core;

import org.pixel.commons.GameContext;
import org.pixel.commons.ServiceProvider;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.GLES30ContentManagerFactory;
import org.pixel.graphics.GraphicsBackend;
import org.pixel.graphics.opengl.GLES30GraphicsDevice;
import org.pixel.graphics.render.GLES30SpriteBatchServiceFactory;
import org.pixel.graphics.render.SpriteBatch;

import java.util.Objects;

public abstract class Game
        extends GameContainer<GLES30GraphicsDevice, MobileGameSettings> {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    private final android.content.Context androidContext;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public Game(MobileGameSettings settings, android.content.Context context) {
        super(settings);
        this.androidContext = context;
    }

    @Override
    protected boolean initGraphicsDevice() {
        log.debug ("Initializing '{}' graphics device.", this.settings.getGraphicsBackend());

        if (Objects.requireNonNull(this.settings.getGraphicsBackend()) == GraphicsBackend.OpenGL) {
            this.graphicsDevice = new GLES30GraphicsDevice(this, this.androidContext);

        } else {
            throw new UnsupportedOperationException(
                    "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        return this.graphicsDevice.init();
    }

    @Override
    protected boolean initAudio() {
        log.debug("Initializing audio device.");

        return true;
    }

    @Override
    protected boolean initServices() {
        log.debug("Initializing services.");

        switch (this.settings.getGraphicsBackend()) {
            case OpenGL:
                ServiceProvider.register(SpriteBatch.class, new GLES30SpriteBatchServiceFactory());
                ServiceProvider.register(ContentManager.class, new GLES30ContentManagerFactory());
                break;
            case Vulkan:
                throw new UnsupportedOperationException("Vulkan is not supported yet.");
            default:
                throw new UnsupportedOperationException(
                        "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        // Register game context data:
        GameContext.getData().put(MobileTags.ACTIVE_CONTEXT, this.androidContext);

        return true;
    }
}
