package org.pixel.demo.concept.spaceshooter.screen;

import org.pixel.demo.concept.spaceshooter.Stage;
import org.pixel.demo.concept.spaceshooter.input.Autopilot;
import org.pixel.demo.concept.spaceshooter.input.Controls;

/**
 * The title, over an attract-mode game flown by the autopilot (as arcade machines do). Confirm starts a run.
 */
public final class TitleScreen implements Screen {

    private final Stage stage;
    private final Autopilot pilot = new Autopilot();
    private final Controls attract = new Controls();
    private final int firstWave;
    private float shown;

    /**
     * @param firstWave The wave runs start from (1, or more to practice).
     */
    public TitleScreen(Stage stage, int firstWave) {
        this.stage = stage;
        this.firstWave = firstWave;
    }

    @Override
    public void enter() {
        shown = 0f;
        stage.starfield.setWarp(1f);
        stage.startRun(1);
    }

    @Override
    public Screen update(float dt, Controls controls) {
        shown += dt;
        pilot.steer(stage.world, attract);
        stage.simulate(dt, attract);
        if (stage.world.isOver()) {
            stage.startRun(1); // the attract game starts over
        }
        if (controls.confirm && shown > 0.3f) {
            return new PlayScreen(stage, firstWave);
        }
        return this;
    }

    @Override
    public void draw() {
        stage.drawScene();
        stage.menus.title(stage.hudCanvas(), stage.best, stage.time(), Math.min(1f, shown * 1.5f));
    }
}
