package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.urp.shader.GrayscaleShader;
import org.pixel.graphics.render.Renderable;
import org.pixel.graphics.render.SpriteRenderable;
import org.pixel.graphics.render.RenderPipeline;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class UrpDemo extends DemoGame {

    private static final int SPRITE_COUNT = 2048;

    private ContentManager content;
    private RenderPipeline renderPipeline;
    private Camera2D camera;

    private SpriteRenderable specialSprite;
    private Shader shaderEffect;

    private final Timer debugTimer = new Timer(1000);

    public UrpDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ServiceProvider.get(ContentManager.class);
        camera = new Camera2D(this);

        renderPipeline = new RenderPipeline();

        Texture starTexture = content.loadTexture("images/earth-48x48.png");
        Texture earthTexture = content.loadTexture("images/earth-48x48.png");

        for (int i = 0; i < SPRITE_COUNT; i++) {
            var starRenderable = new SpriteRenderable()
                    .setTexture(starTexture)
                    .setPosition(new Vector2(MathHelper.random(0, getVirtualWidth()), MathHelper.random(0, getVirtualHeight())))
                    .setColor(Color.random())
                    .setAnchor(0.5f, 0.5f)
                    .setScale(0.7f, 0.7f);
            renderPipeline.submit(starRenderable);
        }

        shaderEffect = new GrayscaleShader();
        specialSprite = new SpriteRenderable()
                .setTexture(earthTexture)
                .setPosition(new Vector2(getVirtualWidth() / 2f, getVirtualHeight() / 2f))
                .setAnchor(Vector2.HALF)
                .setScale(1, 1)
                .setShader(shaderEffect);

        renderPipeline.submit(specialSprite);

        renderPipeline.setCullingEnabled(true);
        
        // For demo purposes, set the view frustum to a portion of the screen:
        float margin = 0.2f;
        float viewWidth = getVirtualWidth() * (1.0f - margin * 2);
        float viewHeight = getVirtualHeight();
        float viewX = getVirtualWidth() * margin;
        float viewY = 0;
        
        renderPipeline.setFrustum(viewX, viewY, viewWidth, viewHeight);
    }

    @Override
    public void update(DeltaTime delta) {
        // Animate the Earth sprite with pulsing glow
        float pulse = (float) Math.sin(delta.getTotalElapsed()) * 10;
        float intensity = (float) (Math.sin(delta.getTotalElapsed() * 0.8f) + 1) / 2f;
        specialSprite.setScale(pulse, pulse);
        specialSprite.setRotation(delta.getTotalElapsed());
        specialSprite.setUniform("u_intensity", intensity);

        for (Renderable renderable : renderPipeline.getQueue()) {
            if (renderable.canBatch() & renderable instanceof SpriteRenderable) { // let's modify batch items only:
                var sprite = (SpriteRenderable) renderable;
                sprite.setRotation(specialSprite.getRotation() + 10 * delta.getTotalElapsed());
            }
        }

        if (debugTimer.elapsed()) {
            log.info("FPS: {0} - Smoothed FPS: {1}.", getFps(), getSmoothedFps());
        }

        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        // Render the pipeline without clearing it
        renderPipeline.render(camera.getViewMatrix());
    }

    @Override
    public void dispose() {
        super.dispose();
        renderPipeline.dispose();
        shaderEffect.dispose();
        content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setMultisampling(4);
        settings.setVsync(false);
        settings.setTitle("Unified Render Pipeline Demo");

        var game = new UrpDemo(settings);
        game.start();
    }
}
