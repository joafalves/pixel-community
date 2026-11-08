package org.pixel.demo.concept.spaceshooter.screen;

import org.pixel.demo.concept.spaceshooter.Stage;
import org.pixel.demo.concept.spaceshooter.input.Autopilot;
import org.pixel.demo.concept.spaceshooter.input.Controls;

/**
 * A run: playing, paused, then game over. Starts with a warp-in of the stars.
 */
public final class PlayScreen implements Screen {

    private enum State {PLAYING, PAUSED, OVER}

    private static final float WARP_SECONDS = 1.3f, WARP_SPEED = 9f;

    private final Stage stage;
    private final int firstWave;
    private final Controls idle = new Controls();
    private Autopilot autopilot; // play-testing: the autopilot flies instead of the player
    private State state = State.PLAYING;
    private float stateTime;
    private boolean record;
    private int lives;

    public PlayScreen(Stage stage, int firstWave) {
        this.stage = stage;
        this.firstWave = firstWave;
    }

    /** Let the autopilot play (play-testing, capture runs). */
    public PlayScreen autopilot() {
        autopilot = new Autopilot();
        return this;
    }

    /** Debug: start with this many lives (0 = the default). */
    public PlayScreen lives(int lives) {
        this.lives = lives;
        return this;
    }

    @Override
    public void enter() {
        stage.startRun(firstWave);
        if (lives > 0) {
            stage.world.player.lives = lives;
        }
        setState(State.PLAYING);
    }

    @Override
    public Screen update(float dt, Controls controls) {
        stateTime += dt;
        switch (state) {
            case PLAYING -> {
                if (controls.pause) {
                    setState(State.PAUSED);
                    return this;
                }
                if (autopilot != null) {
                    autopilot.steer(stage.world, controls);
                }
                stage.starfield.setWarp(1f + (WARP_SPEED - 1f) * Math.max(0f, 1f - stateTime / WARP_SECONDS));
                stage.simulate(dt, controls);
                if (stage.world.isOver()) {
                    int score = stage.world.score.points();
                    record = score > stage.best;
                    stage.best = Math.max(stage.best, score);
                    setState(State.OVER);
                }
            }
            case PAUSED -> {
                stage.idle(dt * 0.15f);
                if (controls.pause || controls.confirm) {
                    setState(State.PLAYING);
                    stateTime = WARP_SECONDS; // no warp again
                } else if (controls.back) {
                    return new TitleScreen(stage, firstWave);
                }
            }
            case OVER -> {
                idle.clear();
                stage.simulate(dt, idle); // the enemies fly on
                if ((controls.confirm || autopilot != null && stateTime > 4f) && stateTime > 1.2f) {
                    return new TitleScreen(stage, firstWave);
                }
            }
        }
        return this;
    }

    private void setState(State state) {
        this.state = state;
        stateTime = 0f;
    }

    @Override
    public void draw() {
        stage.drawScene();
        var hud = stage.hudCanvas();
        stage.hud.draw(hud, stage.world, stage.waves, stage.fx, stage.best, stage.time());
        switch (state) {
            case PAUSED -> stage.menus.pause(hud, stage.time());
            case OVER -> stage.menus.gameOver(hud, stage.world.score.points(), stage.best, stage.waves.wave(),
                    record, stateTime, stage.time());
            default -> {
            }
        }
    }
}
