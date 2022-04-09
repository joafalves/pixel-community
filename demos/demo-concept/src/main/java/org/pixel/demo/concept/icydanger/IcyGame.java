package org.pixel.demo.concept.icydanger;

import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.PlayerIndex;
import org.pixel.demo.concept.commons.TitleFpsCounter;
import org.pixel.demo.concept.commons.component.PlayerBoundaryComponent;
import org.pixel.demo.concept.icydanger.component.FlashAnimationComponent;
import org.pixel.demo.concept.icydanger.component.PlayerMovementComponent;
import org.pixel.demo.concept.icydanger.component.PlayerThrowComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.Text;
import org.pixel.ext.ecs.Text.Alignment;
import org.pixel.ext.ldtk.LdtkGameIntLayer;
import org.pixel.ext.ldtk.LdtkGameLevel;
import org.pixel.ext.ldtk.LdtkGameWorld;
import org.pixel.ext.ldtk.importer.LdtkGameWorldImporter;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class IcyGame extends PixelWindow {

    private ContentManager contentManager;
    private Camera2D gameCamera;
    private Camera2D uiCamera;
    private SpriteBatch spriteBatch;
    private GameScene gameScene;

    private LdtkGameLevel level;
    private IcyPointsBar pointsBar;
    private Text playerOnePoints;
    private Text playerTwoPoints;

    private TitleFpsCounter fpsCounter;
    private List<Rectangle> staticCollisionList;

    public IcyGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        fpsCounter = new TitleFpsCounter(this);
        contentManager = new ContentManager();
        contentManager.addContentImporter(new LdtkGameWorldImporter());
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);
        uiCamera = new Camera2D(0, 0, getViewportWidth(), getViewportHeight());
        uiCamera.setOrigin(0);
        spriteBatch = new SpriteBatch();
        gameScene = new GameScene("MainScene", gameCamera, spriteBatch);

        LdtkGameWorld tilemap = contentManager.load("icydanger.ldtk", LdtkGameWorld.class);
        level = tilemap.getLevel("Level_0");
        setBackgroundColor(level.getBackgroundColor());

        initializeCollisions();

        Texture gameTexture = contentManager.loadTexture("tileset/tileset.png");
        Font gameFont = contentManager.loadFont("font/8-bit-pusab.ttf");
        gameFont.setOversampling(2);
        gameFont.setFontSize(64);

        var playerOne = new Sprite("PlayerOne", gameTexture, new Rectangle(0, 32, 32, 32));
        playerOne.setSortingDepth(5);
        playerOne.getAttributeMap().put("playerIndex", 1);
        playerOne.getAttributeMap().put("staticCollisions", staticCollisionList);
        playerOne.getTransform().setPosition(50, 128);
        playerOne.addComponent(new PlayerMovementComponent(PlayerIndex.P1));
        playerOne.addComponent(new PlayerThrowComponent(PlayerIndex.P1));
        playerOne.addComponent(new PlayerBoundaryComponent(new Boundary(10, 10, 80, getVirtualHeight() - 20)));
        playerOne.addComponent(new FlashAnimationComponent());
        gameScene.addChild(playerOne);

        var playerTwo = new Sprite("PlayerTwo", gameTexture, new Rectangle(32, 32, 32, 32));
        playerTwo.setSortingDepth(5);
        playerTwo.getAttributeMap().put("playerIndex", 2);
        playerTwo.getAttributeMap().put("staticCollisions", staticCollisionList);
        playerTwo.getTransform().setPosition(200, 128);
        playerTwo.getTransform().setScaleX(-1); // flip horizontally
        playerTwo.addComponent(new PlayerMovementComponent(PlayerIndex.P2));
        playerTwo.addComponent(new PlayerThrowComponent(PlayerIndex.P2));
        playerTwo.addComponent(new PlayerBoundaryComponent(new Boundary(166, 10, 80, getVirtualHeight() - 20)));
        playerTwo.addComponent(new FlashAnimationComponent());
        gameScene.addChild(playerTwo);

        playerOne.getAttributeMap().put("enemy", playerTwo);
        playerTwo.getAttributeMap().put("enemy", playerOne);

        playerOnePoints = new Text("PlayerOnePoints", gameFont, "0", Color.WHITE, Alignment.CENTER);
        playerOnePoints.setPivot(0.5f, 0f);
        playerOnePoints.getTransform().setPosition(150, 10);

        playerTwoPoints = new Text("PlayerTwoPoints", gameFont, "0", Color.WHITE, Alignment.CENTER);
        playerTwoPoints.setPivot(0.5f, 0f);
        playerTwoPoints.getTransform().setPosition(getViewportWidth() - 150, 10);

        playerOne.getAttributeMap().put("points", playerOnePoints);
        playerTwo.getAttributeMap().put("points", playerTwoPoints);

        pointsBar = new IcyPointsBar(gameTexture,
                new Rectangle(17, 65, 1, 1),
                new Rectangle(19, 65, 1, 1),
                getViewportWidth(), getViewportHeight());

        playerOne.getAttributeMap().put("pointsBar", pointsBar);
        playerTwo.getAttributeMap().put("pointsBar", pointsBar);
    }

    @Override
    public void update(DeltaTime delta) {
        // game
        gameScene.update(delta);

        // GUI
        playerOnePoints.update(delta);
        playerTwoPoints.update(delta);
        pointsBar.update(delta);

        // other
        fpsCounter.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        // draw game level:
        spriteBatch.begin(gameCamera.getViewMatrix());
        level.draw(delta, spriteBatch);
        spriteBatch.end();

        // draw the game scene entities:
        gameScene.draw(delta);

        // draw GUI
        spriteBatch.begin(uiCamera.getViewMatrix());
        pointsBar.draw(delta, spriteBatch);
        playerOnePoints.draw(delta, spriteBatch);
        playerTwoPoints.draw(delta, spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        contentManager.dispose();
    }

    private void initializeCollisions() {
        // create rectangle collision data based of tile data (LDTK)
        staticCollisionList = new ArrayList<>();
        level.getLayers().forEach(layer -> {
            if (layer.getIdentifier().equalsIgnoreCase("collision") && layer instanceof LdtkGameIntLayer) {
                LdtkGameIntLayer intLayer = (LdtkGameIntLayer) layer;
                for (LdtkGameIntLayer.LayerCoordinate layerCoordinate : intLayer.getLayerCoordinateList()) {
                    var rect = Rectangle.builder()
                            .x(layerCoordinate.getX() * intLayer.getGridWidth() + level.getWorldPosition().getX())
                            .y(layerCoordinate.getY() * intLayer.getGridHeight() + level.getWorldPosition().getY())
                            .width(intLayer.getGridWidth())
                            .height(intLayer.getGridHeight())
                            .build();
                    rect.shrink(1f);
                    staticCollisionList.add(rect);
                }
            }
        });
    }

    public static void main(String[] args) {
        final int width = 800;
        final int height = 800;
        WindowSettings settings = new WindowSettings(256, 256);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        PixelWindow window = new IcyGame(settings);
        window.start();
    }
}
