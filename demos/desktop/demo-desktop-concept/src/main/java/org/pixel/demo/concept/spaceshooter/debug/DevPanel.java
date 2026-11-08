package org.pixel.demo.concept.spaceshooter.debug;

import org.pixel.core.dev.DevTools;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.Stage;
import org.pixel.demo.concept.spaceshooter.wave.WaveDirector;
import org.pixel.demo.concept.spaceshooter.world.Player;

/**
 * The game's page in the engine's dev tools (F1): live counts, cheats and tuning, and the render pipeline's
 * statistics.
 */
public final class DevPanel {

    private final Stage stage;
    private final DevTools.Toggle invincible, hitboxes;
    private final DevTools.Slider speed;

    public DevPanel(DevTools tools, Stage stage) {
        this.stage = stage;
        tools.track(stage.pipeline, "space shooter");
        tools.watch("wave", () -> stage.waves.wave(), 0)
                .watch("enemies", () -> stage.world.enemyCount(), 0)
                .watch("player shots", () -> stage.world.playerShots.activeCount(), 0)
                .watch("enemy shots", () -> stage.world.enemyShots.activeCount(), 0)
                .watch("particles", () -> stage.fx.particles.count(), 0)
                .watch("explosions", () -> stage.fx.explosions.count(), 0)
                .watch("multiplier", () -> stage.world.score.multiplier(), 0);
        invincible = tools.addToggle("invincible", false);
        hitboxes = tools.addToggle("hitboxes", false);
        speed = tools.addSlider("game speed", 0.25f, 2f, 1f);
        tools.addAction("next wave", () -> stage.waves.skipTo(stage.waves.wave() + 1));
        tools.addAction("boss wave", () -> {
            int wave = stage.waves.wave();
            stage.waves.skipTo((wave / WaveDirector.BOSS_EVERY + 1) * WaveDirector.BOSS_EVERY);
        });
        tools.addAction("max power", () -> stage.world.player.power = GameConfig.MAX_POWER);
        tools.addAction("+1 life", () -> {
            Player p = stage.world.player;
            p.lives = Math.min(9, p.lives + 1);
        });
    }

    /** Apply the switches (every frame). */
    public void apply() {
        stage.world.invincible = invincible.isOn();
        stage.showHitboxes = hitboxes.isOn();
        stage.gameSpeed = speed.get();
    }
}
