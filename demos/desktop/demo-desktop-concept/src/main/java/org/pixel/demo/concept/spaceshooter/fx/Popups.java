package org.pixel.demo.concept.spaceshooter.fx;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;
import org.pixel.graphics.text.TextBaseline;

/**
 * Floating texts (points, pickups), rising and fading. Their text is built in reused builders.
 */
public final class Popups {

    private static final float LIFE = 1.1f, RISE = 18f;

    private final Popup[] popups;
    private final Color scratch = new Color(), outline = new Color();

    public Popups(int capacity) {
        popups = new Popup[capacity];
        for (int i = 0; i < capacity; i++) {
            popups[i] = new Popup();
        }
    }

    /**
     * Start a popup; append its text to the returned builder (the oldest popup is reused when all are in use).
     */
    public StringBuilder add(float x, float y, float size, Color color) {
        Popup slot = popups[0];
        for (Popup p : popups) {
            if (!p.active) {
                slot = p;
                break;
            }
            if (p.age > slot.age) {
                slot = p;
            }
        }
        slot.active = true;
        slot.x = x;
        slot.y = y;
        slot.age = 0f;
        slot.size = size;
        slot.color.set(color);
        slot.text.setLength(0);
        return slot.text;
    }

    public void update(float dt) {
        for (Popup p : popups) {
            if (p.active) {
                p.age += dt;
                p.active = p.age < LIFE;
            }
        }
    }

    public void draw(Canvas canvas, SdfFont font, float depth) {
        for (Popup p : popups) {
            if (!p.active) {
                continue;
            }
            float t = p.age / LIFE;
            float alpha = t < 0.6f ? 1f : 1f - (t - 0.6f) / 0.4f;
            float pop = t < 0.08f ? 0.7f + t / 0.08f * 0.3f : 1f; // grows in quickly
            scratch.set(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha);
            outline.set(0.05f, 0.03f, 0.08f, alpha * alpha); // the outline goes first, or the fading text looks dark
            float y = p.y - RISE * (1f - (1f - t) * (1f - t));
            float half = font.measure(p.text, p.size) * 0.5f + 3f; // keep it on screen
            float x = Math.max(half, Math.min(GameConfig.WIDTH - half, p.x));
            y = Math.max(GameConfig.HUD_HEIGHT + p.size, y);
            canvas.text(font, p.text, Math.round(x), Math.round(y)).size(p.size * pop).align(TextAlign.CENTER)
                    .baseline(TextBaseline.MIDDLE).fill(scratch).stroke(1.5f, outline).depth(depth);
        }
    }

    public void clear() {
        for (Popup p : popups) {
            p.active = false;
        }
    }

    private static final class Popup {
        final StringBuilder text = new StringBuilder(24);
        final Color color = new Color();
        boolean active;
        float x, y, age, size;
    }
}
