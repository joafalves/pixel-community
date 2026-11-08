package org.pixel.demo.concept.spaceshooter.ui;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.fx.FxDirector;
import org.pixel.demo.concept.spaceshooter.render.Palette;
import org.pixel.demo.concept.spaceshooter.wave.WaveDirector;
import org.pixel.demo.concept.spaceshooter.world.Enemy;
import org.pixel.demo.concept.spaceshooter.world.Player;
import org.pixel.demo.concept.spaceshooter.world.World;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.TextAlign;

/**
 * The in-game display: score, best and multiplier at the top (or the boss's health), hull, lives, bombs and weapon
 * power at the bottom, wave banners, and full-screen flashes. Texts are rebuilt in reused builders every frame.
 */
public final class Hud {

    private static final float W = GameConfig.WIDTH, H = GameConfig.HEIGHT;

    private final UiText text;
    private final StringBuilder line = new StringBuilder(32);
    private final Color scratch = new Color();
    private float bossTrail = 1f; // the boss's health a moment ago (the white part of the bar)
    private float lastTime;

    public Hud(UiText text) {
        this.text = text;
    }

    /**
     * @param best The session's best score (the current run's when higher).
     */
    public void draw(Canvas hud, World world, WaveDirector waves, FxDirector fx, int best, float time) {
        flashes(hud, world, fx, time);
        topBar(hud, world, Math.max(best, world.score.points()), time);
        bottom(hud, world.player, time);
        banner(hud, waves, time);
        lastTime = time;
    }

    private void topBar(Canvas hud, World world, int best, float time) {
        hud.rect(0, 0, W, GameConfig.HUD_HEIGHT).fill(Palette.PANEL);
        scratch.set(Palette.SHIELD);
        scratch.setAlpha(0.25f);
        hud.rect(0, GameConfig.HUD_HEIGHT - 1f, W, 1f).fill(scratch);

        float row = GameConfig.HUD_HEIGHT * 0.5f; // every item of the bar is centered on this line
        text.draw(hud, "SCORE", 6, row, 6, Palette.TEXT_DIM, TextAlign.LEFT);
        line.setLength(0);
        text.draw(hud, UiText.padded(line, world.score.points(), 7), 42, row, 8, Palette.TEXT, TextAlign.LEFT);
        text.draw(hud, "HI", W - 80, row, 6, Palette.TEXT_DIM, TextAlign.LEFT);
        line.setLength(0);
        text.draw(hud, UiText.padded(line, best, 7), W - 6, row, 8, Palette.GOLD, TextAlign.RIGHT);

        Enemy boss = world.boss();
        if (boss != null) {
            bossBar(hud, boss, time);
            return;
        }
        bossTrail = 1f;
        int multiplier = world.score.multiplier();
        if (world.score.chain() > 1) { // the chain: kills in a row, the multiplier and the time left
            line.setLength(0);
            line.append('x').append(multiplier);
            Color color = multiplier > 1 ? Palette.GOLD : Palette.TEXT;
            text.draw(hud, line, W * 0.5f - 22, row, 8, color, TextAlign.RIGHT);
            float barWidth = 50f;
            hud.rect(W * 0.5f - 16, row - 1.5f, barWidth, 3).fill(Palette.INK);
            hud.rect(W * 0.5f - 16, row - 1.5f, barWidth * world.score.chainRemaining(), 3).fill(color);
        }
    }

    private void bossBar(Canvas hud, Enemy boss, float time) {
        float x = W * 0.5f - 70f, width = 140f;
        // the name on the left of the bar, both centered on the bar's line
        float row = GameConfig.HUD_HEIGHT * 0.5f, labelWidth = text.width("BOSS", 6) + 5f;
        x += labelWidth * 0.5f;
        width -= labelWidth * 0.5f;
        text.draw(hud, "BOSS", x - 5f, row, 6, Palette.DANGER, TextAlign.RIGHT);
        hud.rect(x, row - 2f, width, 4).fill(Palette.INK).stroke(1f, Palette.TEXT_DIM);
        float health = boss.healthFraction();
        float dt = Math.max(0f, Math.min(0.1f, time - lastTime));
        bossTrail = Math.max(health, bossTrail - 0.25f * dt); // recent damage drains slowly, in white
        hud.rect(x, row - 2f, width * bossTrail, 4).fill(Palette.TEXT);
        hud.rect(x, row - 2f, width * health, 4).fill(Palette.DANGER);
        for (int third = 1; third < 3; third++) { // phase marks
            hud.rect(x + width * third / 3f, row - 3f, 1, 6).fill(Palette.TEXT_DIM);
        }
    }

    private void bottom(Canvas hud, Player p, float time) {
        float row = H - 8f; // every item of the bottom row is centered on this line
        // hull
        boolean critical = p.alive && p.hull == 1;
        for (int i = 0; i < GameConfig.PLAYER_HULL; i++) {
            Color color = i < p.hull ? (critical ? Palette.DANGER : Palette.HEAL) : Palette.INK;
            if (critical && i < p.hull && ((int) (time * 6f)) % 2 == 0) {
                color = Palette.TEXT;
            }
            hud.rect(6 + i * 9, row - 3f, 7, 6).fill(color).stroke(1f, Palette.INK);
        }
        // lives: small ships
        for (int i = 0; i < Math.min(p.lives, 6); i++) {
            float lx = 40 + i * 10, ly = row;
            hud.path().moveTo(lx - 3, ly - 3).lineTo(lx + 4, ly).lineTo(lx - 3, ly + 3).lineTo(lx - 1, ly).close()
                    .fill(Palette.SHIELD).stroke(1f, Palette.INK);
        }
        // weapon power
        text.draw(hud, "PWR", W - 78, row, 6, Palette.TEXT_DIM, TextAlign.RIGHT);
        for (int i = 0; i < GameConfig.MAX_POWER; i++) {
            hud.rect(W - 74 + i * 7, row - 3f, 5, 6).fill(i < p.power ? Palette.SHIELD : Palette.INK)
                    .stroke(1f, Palette.INK);
        }
        // bombs
        for (int i = 0; i < GameConfig.MAX_BOMBS; i++) {
            float bx = W - 6 - (GameConfig.MAX_BOMBS - 1 - i) * 7 - 3, by = row;
            if (i < p.bombs) {
                hud.circle(bx, by, 3f).fill(Palette.BOMB).stroke(1f, Palette.INK);
            } else {
                hud.circle(bx, by, 2f).fill(Palette.INK);
            }
        }
    }

    private void banner(Canvas hud, WaveDirector waves, float time) {
        if (waves.phase() == WaveDirector.Phase.BANNER) {
            float left = waves.phaseTimer(), shown = 2.2f - left;
            float alpha = Math.min(1f, Math.min(shown * 4f, left * 3f));
            float slide = (1f - Math.min(1f, shown * 3f)) * 30f;
            if (waves.isBossWave()) {
                boolean on = ((int) (time * 5f)) % 2 == 0;
                scratch.set(Palette.DANGER);
                scratch.setAlpha(0.18f * alpha);
                hud.rect(0, H * 0.5f - 22, W, 44).fill(scratch);
                text.draw(hud, "WARNING", W * 0.5f + slide, H * 0.5f - 7, 16, on ? Palette.DANGER : Palette.TEXT,
                        alpha, TextAlign.CENTER);
                text.draw(hud, "DREADNOUGHT APPROACHING", W * 0.5f - slide, H * 0.5f + 12, 6, Palette.TEXT, alpha,
                        TextAlign.CENTER);
            } else {
                line.setLength(0);
                line.append("WAVE ").append(waves.wave());
                text.draw(hud, line, W * 0.5f + slide, H * 0.5f - 7, 16, Palette.TEXT, alpha, TextAlign.CENTER);
                text.draw(hud, "GET READY", W * 0.5f - slide, H * 0.5f + 12, 6, Palette.SHIELD, alpha,
                        TextAlign.CENTER);
            }
        } else if (waves.phase() == WaveDirector.Phase.CLEARED && waves.wave() > 0) {
            float alpha = Math.min(1f, waves.phaseTimer() * 2f);
            text.draw(hud, waves.isBossWave() ? "DREADNOUGHT DOWN" : "WAVE CLEAR", W * 0.5f, H * 0.5f, 8,
                    Palette.GOLD, alpha, TextAlign.CENTER);
        }
    }

    private void flashes(Canvas hud, World world, FxDirector fx, float time) {
        if (fx.whiteFlash() > 0f) {
            scratch.set(1f, 0.97f, 0.9f, fx.whiteFlash() * 0.55f);
            hud.rect(0, 0, W, H).fill(scratch);
        }
        float red = fx.redFlash();
        Player p = world.player;
        if (p.alive && p.hull == 1) { // critical: a slow red pulse at the edges
            red = Math.max(red, 0.25f + 0.15f * (float) Math.sin(time * 5f));
        }
        if (red > 0f) {
            scratch.set(Palette.DANGER);
            scratch.setAlpha(red * 0.5f);
            Color edge = scratch;
            float border = 24f;
            hud.rect(0, 0, W, border).fillLinear(0, 0, edge, 0, border, Color.TRANSPARENT);
            hud.rect(0, H - border, W, border).fillLinear(0, H, edge, 0, H - border, Color.TRANSPARENT);
            hud.rect(0, 0, border, H).fillLinear(0, 0, edge, border, 0, Color.TRANSPARENT);
            hud.rect(W - border, 0, border, H).fillLinear(W, 0, edge, W - border, 0, Color.TRANSPARENT);
        }
    }
}
