package org.pixel.demo.concept.spaceshooter;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventDispatcher;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.component.PlayerBoundaryComponent;
import org.pixel.demo.concept.spaceshooter.component.PlayerInputComponent;
import org.pixel.demo.concept.spaceshooter.content.BackgroundTexture;
import org.pixel.demo.concept.spaceshooter.entity.BackgroundSprite;
import org.pixel.demo.concept.spaceshooter.entity.PlayerSprite;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.component.ConstantVelocityComponent;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class SpaceShooterGame extends PixelWindow {

    public static final EventDispatcher EVENT = new EventDispatcher();

    private Camera2D gameCamera;
    private ContentManager content;

    private GameScene gameScene;

    public SpaceShooterGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        content = new ContentManager();
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);

        // content load
        var texturePack = content.loadTexturePack("spaceshooter/spritemap.json");

        // game scene
        gameScene = new GameScene("MainScene", gameCamera);

        // instances
        var playerSprite = new PlayerSprite(texturePack.getFrame("player-ship"));
        playerSprite.getTransform().setPosition(getVirtualWidth() / 2.f, getVirtualHeight() / 2.f);
        playerSprite.getTransform().setRotation(MathHelper.PIo2);
        playerSprite.addComponent(new PlayerInputComponent());
        playerSprite.addComponent(new ConstantVelocityComponent(new Vector2(0, 40.f)));
        playerSprite.addComponent(
                new PlayerBoundaryComponent(
                        new Boundary(gameCamera.getPositionX(), gameCamera.getPositionY(),
                                gameCamera.getWidth(), gameCamera.getHeight())));

        var bgTex = new BackgroundTexture();
        bgTex.setData(texturePack.getTexture(), texturePack.getFrames("bg-01", "bg-02", "bg-03", "bg-04"),
                20, 15);

        var backgroundSprite = new BackgroundSprite("backgroundTexture", bgTex,
                new Rectangle(0, 0, getVirtualWidth(), getVirtualHeight()),
                new Rectangle(0, 0, getVirtualWidth(), getVirtualHeight()));
        backgroundSprite.getTransform().setScale(getVirtualWidth(), getVirtualHeight());

        // add entities to scene:
        gameScene.addChild(backgroundSprite);
        gameScene.addChild(playerSprite);
    }

    @Override
    public void update(DeltaTime delta) {
        gameScene.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        gameScene.draw(delta);
    }

    @Override
    public void dispose() {
        content.dispose();
        gameScene.dispose();
    }

    public static void main(String[] args) {
        final int width = 800;
        final int height = 600;
        WindowSettings settings = new WindowSettings(width / 2, height / 2);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        PixelWindow window = new SpaceShooterGame(settings);
        window.start();
    }
}
