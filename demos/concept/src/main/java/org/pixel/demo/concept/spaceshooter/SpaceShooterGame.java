package org.pixel.demo.concept.spaceshooter;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventDispatcher;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.TitleFpsCounter;
import org.pixel.demo.concept.commons.component.PlayerBoundaryComponent;
import org.pixel.demo.concept.spaceshooter.component.CollisionHandlingComponent;
import org.pixel.demo.concept.spaceshooter.component.PlayerInputComponent;
import org.pixel.demo.concept.spaceshooter.content.BackgroundTexture;
import org.pixel.demo.concept.spaceshooter.entity.BackgroundSprite;
import org.pixel.demo.concept.spaceshooter.entity.EnemyContainer;
import org.pixel.demo.concept.spaceshooter.entity.PlayerSprite;
import org.pixel.demo.concept.spaceshooter.model.CollisionData;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.AutoDisposeComponent;
import org.pixel.ext.ecs.component.ConstantVelocityComponent;
import org.pixel.ext.ecs.component.SpriteAnimationComponent;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class SpaceShooterGame extends PixelWindow {

    public static final EventDispatcher $ = new EventDispatcher();

    private TitleFpsCounter fpsCounter;
    private Camera2D gameCamera;
    private ContentManager content;

    private GameScene gameScene;
    private Texture explosionTexture;
    private BackgroundTexture backgroundTexture;

    public SpaceShooterGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        fpsCounter = new TitleFpsCounter(this);
        content = new ContentManager();
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);

        // content load
        var texturePack = content.loadTexturePack("spaceshooter/spritemap.json");
        var engineFireTexture = content.loadTexture("spaceshooter/engine_fire_multiphase.png");
        explosionTexture = content.loadTexture("spaceshooter/explosion.png");

        // game scene
        gameScene = new GameScene("MainScene", gameCamera);

        // instances
        backgroundTexture = new BackgroundTexture();
        backgroundTexture.setData(texturePack.getTexture(), texturePack.getFrames("bg-01", "bg-02", "bg-03", "bg-04"),
                20, 15);
        var backgroundSprite = new BackgroundSprite("backgroundTexture", backgroundTexture,
                new Rectangle(0, 0, getVirtualWidth(), getVirtualHeight()),
                new Rectangle(0, 0, getVirtualWidth(), getVirtualHeight()));
        backgroundSprite.getTransform().setScale(getVirtualWidth(), getVirtualHeight());
        gameScene.addChild(backgroundSprite);

        var bulletContainer = new GameObject("BulletContainer");
        gameScene.addChild(bulletContainer);

        var playerSprite = new PlayerSprite(texturePack.getFrame("player-ship"));
        playerSprite.getAttributeMap().put(SpaceShooterAttribute.BULLET_CONTAINER, bulletContainer);
        playerSprite.getAttributeMap().put(SpaceShooterAttribute.BULLET1_FRAME, texturePack.getFrame("bullet1"));
        playerSprite.getTransform().setPosition(getVirtualWidth() / 2.f, getVirtualHeight() / 2.f);
        playerSprite.getTransform().setRotation(-MathHelper.PIo2);
        playerSprite.addComponent(new PlayerInputComponent());
        playerSprite.addComponent(new ConstantVelocityComponent(new Vector2(0, 55.f)));
        playerSprite.addComponent(
                new PlayerBoundaryComponent(new Boundary(0, 0, gameCamera.getWidth(), gameCamera.getHeight())));
        gameScene.addChild(playerSprite);

        var engineFireSprite = new Sprite("EngineFire", engineFireTexture);
        engineFireSprite.getTransform().setRotation(MathHelper.PIo2);
        engineFireSprite.getTransform().setScale(1.4f);
        engineFireSprite.getTransform().setPosition(24, 0);
        playerSprite.addChild(engineFireSprite);

        var engineFireAnimationComponent = new SpriteAnimationComponent(11, 11, 20);
        engineFireAnimationComponent.setPreFrame(4);
        engineFireAnimationComponent.setStartFrame(30);
        engineFireAnimationComponent.setEndFrame(119);
        engineFireSprite.addComponent(engineFireAnimationComponent);
        engineFireAnimationComponent.play(true);

        var enemyContainer = new EnemyContainer("EnemyContainer", new Rectangle(0, -100, getVirtualWidth(), 50),
                texturePack.getFrame("enemy-ship1"), texturePack.getFrame("enemy-ship2"));
        gameScene.addChild(enemyContainer);

        var miscContainer = new GameObject("MiscContainer");
        gameScene.addChild(miscContainer);

        var collisionHandler = new GameObject("CollisionHandler");
        gameScene.addChild(collisionHandler);
        collisionHandler.addComponent(new CollisionHandlingComponent());

        bindEvents();
    }

    @Override
    public void update(DeltaTime delta) {
        gameScene.update(delta);
        fpsCounter.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        gameScene.draw(delta);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        content.dispose();
        gameScene.dispose();
        super.dispose();
    }

    private void bindEvents() {
        $.subscribe("collision", CollisionData.class, (data) -> {
            var explosionSprite = new Sprite("explosion", explosionTexture);
            explosionSprite.getTransform().setPosition(data.getPosition());
            var animationComponent = new SpriteAnimationComponent(9, 9, 5);
            explosionSprite.addComponent(animationComponent);
            explosionSprite.addComponent(new AutoDisposeComponent(5));

            animationComponent.setStartFrame(12);
            animationComponent.play(false);

            gameScene.addChild(explosionSprite);
        });
    }

    public static void main(String[] args) {
        final int width = 800;
        final int height = 600;
        WindowSettings settings = new WindowSettings(width / 2, height / 2);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDebugMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        PixelWindow window = new SpaceShooterGame(settings);
        window.start();
    }
}
