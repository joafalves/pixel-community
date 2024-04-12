package org.pixel.demo.android;

import android.content.Context;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.MobileGameSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class AndroidDemoGame extends Game {

    private Camera2D camera;
    private ContentManager contentManager;
    private SpriteBatch spriteBatch;

    private Texture sprite;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     * @param context The android context.
     */
    public AndroidDemoGame(MobileGameSettings settings, Context context) {
        super(settings, context);
    }

    @Override
    public void load() {
        contentManager = ServiceProvider.create(ContentManager.class);
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        camera = new Camera2D(this);

        sprite = contentManager.loadTexture("images/earth-48x48.png");
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(camera.getViewMatrix());
        spriteBatch.draw(sprite, Vector2.ZERO, Color.WHITE, Vector2.HALF, 4);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        contentManager.dispose();
        spriteBatch.dispose();
    }
}
