package org.pixel.concept.spaceshooter;

import org.pixel.commons.DeltaTime;
import org.pixel.concept.spaceshooter.component.PlayerInputComponent;
import org.pixel.concept.spaceshooter.content.BackgroundTexture;
import org.pixel.concept.spaceshooter.game.BackgroundSprite;
import org.pixel.concept.spaceshooter.game.PlayerSprite;
import org.pixel.content.ContentManager;
import org.pixel.content.TexturePack;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;

public class SpaceShooterGame extends PixelWindow {

    private Camera2D gameCamera;
    private TexturePack texturePack;
    private ContentManager content;
    private SpriteBatch spriteBatch;

    private PlayerSprite player;
    private BackgroundSprite bg;

    /**
     * Constructor
     *
     * @param settings
     */
    public SpaceShooterGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = new SpriteBatch();
        content = new ContentManager();
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);

        // content load
        texturePack = content.load("spaceshooter/spritemap.json", TexturePack.class);

        // instances
        player = new PlayerSprite(spriteBatch, texturePack, texturePack.getFrame("player-ship"));
        player.getTransform().setPosition(getVirtualWidth() / 2.f, getVirtualHeight() / 2.f);
        player.getTransform().setRotation(MathHelper.PIo2);
        player.addComponent(new PlayerInputComponent());

        BackgroundTexture bgTex = new BackgroundTexture();
        bgTex.setData(texturePack.getTexture(), texturePack.getFrames("bg-01", "bg-02", "bg-03", "bg-04"),
                15, 20);
        bg = new BackgroundSprite(bgTex, new Rectangle(0, 0, getVirtualWidth(), getVirtualHeight()));
    }

    @Override
    public void update(DeltaTime delta) {
        player.update(delta);
        bg.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        bg.draw(spriteBatch);
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
        WindowSettings settings = new WindowSettings(width / 2, height / 2);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        PixelWindow window = new SpaceShooterGame(settings);
        window.start();
    }
}
