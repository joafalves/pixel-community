package org.pixel.demo.concept.spaceshooter;

import org.pixel.core.DesktopLauncher;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventManager;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.FpsCounter;
import org.pixel.demo.concept.commons.component.PlayerBoundaryComponent;
import org.pixel.demo.concept.spaceshooter.component.CollisionHandlingComponent;
import org.pixel.demo.concept.spaceshooter.component.PlayerInputComponent;
import org.pixel.demo.concept.spaceshooter.content.GLBackgroundTexture;
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
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import static org.pixel.demo.concept.spaceshooter.SpaceShooterEvents.COLLISION;

@Deprecated()
public class  SpaceShooterGame extends GameApp {

    private FpsCounter fpsCounter;
    private Camera2D gameCamera;

    private GameScene gameScene;
    private Texture explosionTexture;
    private GLBackgroundTexture backgroundTexture;

    private EventManager eventManager;
    private SpriteBatch spriteBatch;

    @Override
    public void load() {
        fpsCounter = new FpsCounter(context());
        spriteBatch = graphics().createSpriteBatch();
        var content = content();
        eventManager = new EventManager(); // scene-wide event bus (was injected by blueprints)
        gameCamera = new Camera2D(context());
        gameCamera.setOrigin(0);

        // content load
        var texturePack = content.loadTexturePack("spaceshooter/spritemap.json");
        var engineFireTexture = content.loadTexture("spaceshooter/engine_fire_multiphase.png");
        explosionTexture = content.loadTexture("spaceshooter/explosion.png");

        // game scene
        gameScene = new GameScene("MainScene", gameCamera, spriteBatch);
        gameScene.getData().put(eventManager);

        // instances
        backgroundTexture = new GLBackgroundTexture();
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
        playerSprite.getData().put(SpaceShooterAttribute.BULLET_CONTAINER, bulletContainer);
        playerSprite.getData().put(SpaceShooterAttribute.BULLET1_FRAME, texturePack.getFrame("bullet1"));
        playerSprite.getTransform().setPosition(getVirtualWidth() / 2.f, getVirtualHeight() / 2.f);
        playerSprite.getTransform().setRotation(-MathHelper.PIo2);
        playerSprite.addComponent(new PlayerInputComponent(eventManager));
        playerSprite.addComponent(new ConstantVelocityComponent(new Vector2(0, 55.f)));
        playerSprite.addComponent(
                new PlayerBoundaryComponent(new Boundary(0, 0, gameCamera.getWidth(), gameCamera.getHeight())));
        gameScene.addChild(playerSprite);
        playerSprite.listen(eventManager);

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
        collisionHandler.addComponent(new CollisionHandlingComponent(eventManager));

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
        gameScene.dispose();
        spriteBatch.dispose();
        super.dispose();
    }

    private void bindEvents() {
        eventManager.subscribe(COLLISION, CollisionData.class, (data) -> {
            var explosionSprite = new Sprite("explosion", explosionTexture);
            explosionSprite.setPivot(0.5f, 0.5f); // centered on the hit (sprites default to a top-left pivot)
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
        final int windowWidth = 1000;
        final int windowHeight = 800;
        final int virtualWidth = windowWidth / 2;
        final int virtualHeight = windowHeight / 2;
        var settings = new WindowSettings(virtualWidth, virtualHeight);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDevMode(false);
        settings.setWindowWidth(windowWidth);
        settings.setWindowHeight(windowHeight);

        DesktopLauncher.run(new SpaceShooterGame(), settings);
    }
}
