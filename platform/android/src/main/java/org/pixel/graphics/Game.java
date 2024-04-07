package org.pixel.graphics;

import android.content.Context;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.opengl.GLESGraphicsDevice;

import java.util.Objects;

public abstract class Game
        extends GameContainer<GLESGraphicsDevice, MobileGameSettings> {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    private final Context androidContext;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public Game(MobileGameSettings settings, Context context) {
        super(settings);
        this.androidContext = context;
    }

    @Override
    protected boolean initGraphicsDevice() {
        if (Objects.requireNonNull(this.settings.getGraphicsBackend()) == GraphicsBackend.OpenGL) {
            this.graphicsDevice = new GLESGraphicsDevice(this, this.androidContext);

        } else {
            throw new UnsupportedOperationException(
                    "Unsupported graphics backend: " + this.settings.getGraphicsBackend());
        }

        return this.graphicsDevice.init();
    }

    @Override
    protected boolean initAudio() {
        return true;
    }

    @Override
    protected boolean initServices() {
        return true;
    }

}
