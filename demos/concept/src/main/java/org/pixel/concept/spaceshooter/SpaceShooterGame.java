package org.pixel.concept.spaceshooter;

import org.pixel.concept.spaceshooter.component.PlayerInputComponent;
import org.pixel.concept.spaceshooter.object.PlayerShip;
import org.pixel.content.ContentManager;
import org.pixel.content.TexturePack;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;

public class SpaceShooterGame extends Game {

    private TexturePack texturePack;
    private ContentManager content;
    private SpriteBatch spriteBatch;

    private PlayerShip player;

    /**
     * Constructor
     *
     * @param settings
     */
    public SpaceShooterGame(GameSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = new SpriteBatch();
        content = new ContentManager();

        // content load
        texturePack = content.load("spaceshooter/spritemap.json", TexturePack.class);

        // instances
        player = new PlayerShip(spriteBatch, texturePack, texturePack.getFrame("player-ship"));
        player.getTransform().setRotation(MathHelper.PIo2);
        player.addComponent(new PlayerInputComponent());
    }

    @Override
    public void update(DeltaTime delta) {
        player.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        player.draw(delta);

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
    }

    public static void main(String[] args) {
        final int width = 480;
        final int height = 640;
        GameSettings settings = new GameSettings(width / 2, height / 2);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        Game game = new SpaceShooterGame(settings);
        game.start();
    }
}
