package org.pixel.demo.concept.spaceshooter.ui;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.render.Palette;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.TextAlign;

/**
 * The full-screen overlays: title (over the attract-mode game), pause and game over.
 */
public final class Menus {

    private static final float W = GameConfig.WIDTH, H = GameConfig.HEIGHT;

    private final UiText text;
    private final StringBuilder line = new StringBuilder(32);
    private final Color scratch = new Color();

    public Menus(UiText text) {
        this.text = text;
    }

    /**
     * @param appear 0 to 1 as the title fades in.
     */
    public void title(Canvas hud, int best, float time, float appear) {
        scratch.set(Palette.SPACE);
        scratch.setAlpha(0.55f * appear);
        hud.rect(0, 0, W, H).fill(scratch);

        float y = 50f - (1f - appear) * 12f; // vertical middles from here on
        Color glow = scratch;
        glow.set(Palette.SHIELD);
        glow.setAlpha(0.6f * appear);
        text.draw(hud, "SPACE", W * 0.5f, y, 24, Palette.TEXT, appear, TextAlign.CENTER).shadow(0, 0, 8, glow);
        text.draw(hud, "SHOOTER", W * 0.5f, y + 31, 24, Palette.FIRE, appear, TextAlign.CENTER).shadow(0, 0, 8, glow);
        text.draw(hud, "A PIXEL ENGINE DEMO", W * 0.5f, y + 57, 6, Palette.TEXT_DIM, appear, TextAlign.CENTER);

        if (((int) (time * 2.2f)) % 2 == 0) {
            text.draw(hud, "PRESS SPACE TO START", W * 0.5f, 142, 8, Palette.GOLD, appear, TextAlign.CENTER);
        }
        float rows = 168f;
        text.draw(hud, "MOVE  ARROWS / WASD", W * 0.5f, rows, 6, Palette.TEXT_DIM, appear, TextAlign.CENTER);
        text.draw(hud, "FIRE  SPACE / Z      BOMB  X / SHIFT", W * 0.5f, rows + 11, 6, Palette.TEXT_DIM, appear,
                TextAlign.CENTER);
        text.draw(hud, "PAUSE  ESC      DEV TOOLS  F1", W * 0.5f, rows + 22, 6, Palette.TEXT_DIM, appear,
                TextAlign.CENTER);
        if (best > 0) {
            line.setLength(0);
            line.append("HI ");
            UiText.padded(line, best, 7);
            text.draw(hud, line, W * 0.5f, H - 10, 6, Palette.GOLD, appear, TextAlign.CENTER);
        }
    }

    public void pause(Canvas hud, float time) {
        scratch.set(Palette.SPACE);
        scratch.setAlpha(0.7f);
        hud.rect(0, 0, W, H).fill(scratch);
        panel(hud, W * 0.5f - 90, 64, 180, 90);
        text.draw(hud, "PAUSED", W * 0.5f, 88, 16, Palette.TEXT, TextAlign.CENTER);
        text.draw(hud, "ESC   RESUME", W * 0.5f, 116, 6, Palette.SHIELD, TextAlign.CENTER);
        text.draw(hud, "Q   QUIT TO TITLE", W * 0.5f, 132, 6, Palette.TEXT_DIM, TextAlign.CENTER);
    }

    /**
     * @param shown Seconds since the game-over screen appeared.
     */
    public void gameOver(Canvas hud, int score, int best, int wave, boolean record, float shown, float time) {
        float appear = Math.min(1f, shown * 2f);
        scratch.set(Palette.SPACE);
        scratch.setAlpha(0.72f * appear);
        hud.rect(0, 0, W, H).fill(scratch);
        panel(hud, W * 0.5f - 110, 46, 220, 132);
        text.draw(hud, "GAME OVER", W * 0.5f, 68, 16, Palette.DANGER, appear, TextAlign.CENTER);

        line.setLength(0);
        line.append("SCORE  ");
        UiText.padded(line, score, 7);
        text.draw(hud, line, W * 0.5f, 96, 8, Palette.TEXT, appear, TextAlign.CENTER);
        line.setLength(0);
        line.append("BEST   ");
        UiText.padded(line, best, 7);
        text.draw(hud, line, W * 0.5f, 110, 8, Palette.GOLD, appear, TextAlign.CENTER);
        line.setLength(0);
        line.append("REACHED WAVE ").append(wave);
        text.draw(hud, line, W * 0.5f, 125, 6, Palette.TEXT_DIM, appear, TextAlign.CENTER);
        if (record && ((int) (time * 4f)) % 2 == 0) {
            text.draw(hud, "NEW RECORD!", W * 0.5f, 141, 8, Palette.HEAL, appear, TextAlign.CENTER);
        }
        if (shown > 1.2f && ((int) (time * 2.2f)) % 2 == 0) {
            text.draw(hud, "PRESS SPACE", W * 0.5f, 163, 8, Palette.SHIELD, TextAlign.CENTER);
        }
    }

    private void panel(Canvas hud, float x, float y, float w, float h) {
        hud.roundRect(x, y, w, h, 6).fill(Palette.PANEL).stroke(1f, Palette.TEXT_DIM)
                .shadow(0, 4, 12, Palette.INK);
    }
}
