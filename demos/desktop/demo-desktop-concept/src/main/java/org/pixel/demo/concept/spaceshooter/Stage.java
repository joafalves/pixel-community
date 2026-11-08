package org.pixel.demo.concept.spaceshooter;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.core.Camera2D;
import org.pixel.core.GameContext;
import org.pixel.core.ScreenSpace;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.demo.concept.spaceshooter.fx.FxDirector;
import org.pixel.demo.concept.spaceshooter.input.Controls;
import org.pixel.demo.concept.spaceshooter.render.Layers;
import org.pixel.demo.concept.spaceshooter.render.Palette;
import org.pixel.demo.concept.spaceshooter.render.Starfield;
import org.pixel.demo.concept.spaceshooter.render.WorldRenderer;
import org.pixel.demo.concept.spaceshooter.ui.Hud;
import org.pixel.demo.concept.spaceshooter.ui.Menus;
import org.pixel.demo.concept.spaceshooter.ui.UiText;
import org.pixel.demo.concept.spaceshooter.wave.WaveDirector;
import org.pixel.demo.concept.spaceshooter.world.World;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.pass.Effects;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.backend.RenderTarget;

/**
 * What every screen shares: the render pipeline, the world and its waves, the effects, the background and the UI.
 * <p>
 * Rendering: the backdrop, stars, world and light layers are drawn through the camera into a scene target, which
 * goes through the shockwave, bloom and vignette effects onto the screen; the HUD is drawn on top in screen space.
 */
public final class Stage implements Disposable {

    public final Assets assets;
    public final Camera2D camera;
    public final RenderPipeline pipeline;
    public final World world;
    public final WaveDirector waves;
    public final FxDirector fx;
    public final Starfield starfield;
    public final WorldRenderer worldRenderer;
    public final Hud hud;
    public final Menus menus;

    /** The session's best score (real runs only). */
    public int best;
    /** Debug: simulation speed, and collision circles. */
    public float gameSpeed = 1f;
    public boolean showHitboxes;

    private float accumulator, time;

    public Stage(GameContext context, Assets assets, long seed) {
        this.assets = assets;
        camera = new Camera2D(context);
        camera.setOrigin(0f); // (0, 0) is the top-left corner of the view

        world = new World(seed);
        waves = new WaveDirector(world);
        fx = new FxDirector(assets, world.random());
        world.setListener(fx);
        waves.setListener(fx);
        starfield = new Starfield(assets, world.random());
        worldRenderer = new WorldRenderer(assets);
        UiText text = new UiText(assets.font);
        hud = new Hud(text);
        menus = new Menus(text);

        RenderPipeline.Builder builder = RenderPipeline.builder(context.graphics().getRenderBackend());
        RenderTarget scene = builder.screenTarget(1f);
        scene.setClearColor(Palette.SPACE);
        pipeline = builder
                .pass(camera, scene, Layers.BACKDROP, Layers.STARS, Layers.WORLD, Layers.GLOW)
                .post(scene, fx.shockwave, Effects.bloom(0.78f, 0.7f).setSpread(1.2f), Effects.vignette(0.35f))
                .pass(new ScreenSpace(context), Layers.HUD)
                .build();
    }

    /** Start a run (or an attract-mode game) from a wave. */
    public void startRun(int wave) {
        world.reset();
        fx.clear();
        waves.start(wave);
        accumulator = 0f;
    }

    /**
     * Run the simulation for a frame, in fixed steps.
     *
     * @param dt Real seconds since the last frame.
     */
    public void simulate(float dt, Controls controls) {
        accumulator += Math.min(dt, 0.25f) * gameSpeed;
        int steps = 0;
        while (accumulator >= GameConfig.STEP && steps < GameConfig.MAX_STEPS_PER_FRAME) {
            float simulated = world.update(GameConfig.STEP, controls);
            waves.update(simulated);
            fx.update(simulated);
            starfield.update(simulated);
            time += simulated;
            accumulator -= GameConfig.STEP;
            steps++;
        }
        if (steps == GameConfig.MAX_STEPS_PER_FRAME) {
            accumulator = 0f; // too slow to keep up: slow down rather than spiral
        }
    }

    /**
     * Only the background moves (menus over a still world).
     */
    public void idle(float dt) {
        starfield.update(dt);
        time += dt;
    }

    /** Draw the background and the world (screens add the HUD and menus). */
    public void drawScene() {
        camera.setPosition(fx.shake.offsetX(), fx.shake.offsetY());
        starfield.draw(pipeline.canvas(Layers.BACKDROP), pipeline.canvas(Layers.STARS));
        worldRenderer.draw(world, fx, pipeline.canvas(Layers.WORLD), pipeline.canvas(Layers.GLOW), time,
                showHitboxes);
    }

    public Canvas hudCanvas() {
        return pipeline.canvas(Layers.HUD);
    }

    /** Seconds of simulated (and idle) time, for animations. */
    public float time() {
        return time;
    }

    @Override
    public void dispose() {
        pipeline.dispose(); // with its targets and the effects it was built with (the shockwave too)
        assets.dispose();
    }
}
