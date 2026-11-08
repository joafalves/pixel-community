package org.pixel.demo.android;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.GameApp;
import org.pixel.ext.tween.Tween;
import org.pixel.ext.tween.TweenEasingMode;
import org.pixel.ext.tween.TweenLoopMode;
import org.pixel.ext.tween.TweenSequencer;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.pass.Effects;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.canvas.LineJoin;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

/**
 * Android demo on the URP v2 renderer (the same GL backend as desktop): Canvas shapes, gradients, a soft shadow and
 * a path drawn into a screen-sized render target and post-processed (vignette), then tweened sprites on top
 * through the sprite batch. The origin is at the center of the screen.
 */
public class AndroidDemoGame extends GameApp {

    private static final Color PANEL = new Color(0.12f, 0.14f, 0.20f, 0.92f);
    private static final Color GOLD = new Color(0.98f, 0.78f, 0.30f, 1f);
    private static final Color ORANGE = new Color(0.95f, 0.42f, 0.21f, 1f);
    private static final Color TEAL = new Color(0.20f, 0.80f, 0.75f, 1f);
    private static final Color PINK = new Color(0.93f, 0.35f, 0.55f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.45f);

    private SpriteBatch spriteBatch;
    private static final Layer MAIN = Layer.ordered("main");

    private RenderPipeline pipeline;
    private Camera2D camera;
    private Texture spriteTexture;
    private Vector2 spritePositionA;
    private Vector2 spritePositionB;
    private TweenSequencer sequencer;
    private float time;

    @Override
    public void load() {
        spriteBatch = graphics().createSpriteBatch();
        camera = new Camera2D(context());
        camera.setOrigin(0.5f);
        // the canvas goes through a screen-sized target and a vignette (render targets + post effects on GLES)
        RenderPipeline.Builder builder = RenderPipeline.builder(graphics().getRenderBackend());
        RenderTarget scene = builder.screenTarget(1f);
        scene.setClearColor(settings().getBackgroundColor()); // opaque: the vignette covers the whole screen
        pipeline = builder.pass(camera, scene, MAIN).post(scene, Effects.vignette(0.7f)).build();

        spriteTexture = content().loadTexture("images/earth-48x48.png");

        spritePositionA = new Vector2(200, 200);
        Tween tweenA = new Tween(spritePositionA.getX(), spritePositionA.getY())
                .target(spritePositionA)
                .to(200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.SIN);

        spritePositionB = new Vector2(-200, 200);
        Tween tweenB = new Tween(spritePositionB.getX(), spritePositionB.getY())
                .target(spritePositionB)
                .to(-200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.ELASTIC);

        sequencer = new TweenSequencer(tweenA, tweenB);
        sequencer.onComplete(sequencer::restart);
    }

    @Override
    public void update(DeltaTime delta) {
        sequencer.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = pipeline.canvas(MAIN);

        // a panel with a soft shadow, a gradient bar, a stroked circle and a rotating star
        c.roundRect(-420, -620, 840, 380, 36).fill(PANEL).shadow(0, 12, 30, SHADOW).stroke(3, TEAL);
        c.roundRect(-360, -560, 720, 60, 30).fillLinear(-360, 0, GOLD, 360, 0, ORANGE);
        c.circle(-220, -360, 70).fill(PINK).stroke(8, Color.WHITE);
        c.save();
        c.translate(180, -360);
        c.rotate(time);
        star(c, 95, 40, 5).fillLinear(-95, 0, TEAL, 95, 0, GOLD).stroke(5, Color.WHITE).join(LineJoin.ROUND);
        c.restore();
        pipeline.render();

        spriteBatch.begin(camera.getViewMatrix());
        spriteBatch.draw(spriteTexture, spritePositionA, Color.WHITE, Vector2.HALF, 4f);
        spriteBatch.draw(spriteTexture, spritePositionB, Color.WHITE, Vector2.HALF, 4f);
        spriteBatch.end();
    }

    private static Canvas.Path star(Canvas c, float outer, float inner, int points) {
        Canvas.Path path = c.path();
        for (int i = 0; i < points * 2; i++) {
            double a = -Math.PI / 2 + i * Math.PI / points;
            float r = i % 2 == 0 ? outer : inner;
            float x = (float) Math.cos(a) * r, y = (float) Math.sin(a) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        return path.close();
    }

    @Override
    public void onViewportChanged(int width, int height) {
        // For the sake of the demo, the virtual resolution follows the screen.
        settings().setVirtualHeight(height);
        settings().setVirtualWidth(width);
        camera.setWidth(width);
        camera.setHeight(height);
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        spriteBatch.dispose(); // the content manager belongs to the host, which disposes it
    }
}
