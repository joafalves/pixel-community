package org.pixel.demo.learning.physics;

import java.util.concurrent.ThreadLocalRandom;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.NvgRenderEngine;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.physics.Body;
import org.pixel.physics.BodyType;
import org.pixel.physics.World;
import org.pixel.physics.shape.CircleShape;

public class PhysicsDemo extends DemoGame {

    private RenderEngine2D re;
    private World world;
    private Camera2D gameCamera;
    private SpriteBatch spriteBatch;
    private ContentManager content;
    private Texture earthTexture;

    /**
     * Constructor
     *
     * @param settings
     */
    public PhysicsDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);

        content = new ContentManager();
        spriteBatch = new SpriteBatch();
        earthTexture = content.load("images/earth-48x48.png", Texture.class);

        re = new NvgRenderEngine(this.getViewportWidth(), this.getViewportHeight());
        world = new World();
        for (int i = 0; i < 100; i++) {
            var size = ThreadLocalRandom.current().nextInt(15, 30);
            var circleShape = new CircleShape(size);
            var entity = new Body(new Vector2(
                    ThreadLocalRandom.current().nextInt(0, getViewportWidth()),
                    ThreadLocalRandom.current().nextInt(0, getViewportHeight())));
            entity.setMass(size);
            entity.setShape(circleShape);
            entity.setAttachment(Color.random(0.25f));

            world.addBody(entity);
        }

        var circleShape = new CircleShape(100);
        var entity = new Body(new Vector2(getViewportWidth() / 2f, getViewportHeight() / 1.25f));
        entity.setMass(200);
        entity.setType(BodyType.STATIC);
        entity.setShape(circleShape);
        entity.setAttachment(Color.random(0.25f));

        world.addBody(entity);
    }

    @Override
    public void update(DeltaTime delta) {
        world.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());
        world.getBodies().forEach(body -> {
            if (body.getShape() instanceof CircleShape) {
                spriteBatch.draw(earthTexture,
                        new Rectangle(body.getPosition(), ((CircleShape) body.getShape()).getRadius() * 2),
                        Color.WHITE,
                        Vector2.ZERO,
                        body.getOrientation());
            }
        });
        spriteBatch.end();

        re.begin();
        re.scale(gameCamera.getZoom(), gameCamera.getZoom());
        re.translate(gameCamera.getPosition().getX() * -1, gameCamera.getPosition().getY() * -1);
        world.getBodies().forEach(body -> {
            re.beginPath();

            if (body.getShape() instanceof CircleShape) {
                re.circle(body.getPosition(), ((CircleShape) body.getShape()).getRadius());
            }

            re.fillColor((Color) body.getAttachment());
            re.fill();
            re.endPath();
        });
        re.end();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(800, 600);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);

        PixelWindow window = new PhysicsDemo(settings);
        window.start();
    }
}
