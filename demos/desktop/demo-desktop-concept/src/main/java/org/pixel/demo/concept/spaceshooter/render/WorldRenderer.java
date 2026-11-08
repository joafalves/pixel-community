package org.pixel.demo.concept.spaceshooter.render;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.demo.concept.spaceshooter.assets.Sprites;
import org.pixel.demo.concept.spaceshooter.fx.FxDirector;
import org.pixel.demo.concept.spaceshooter.world.Enemy;
import org.pixel.demo.concept.spaceshooter.world.Pickup;
import org.pixel.demo.concept.spaceshooter.world.Player;
import org.pixel.demo.concept.spaceshooter.world.Shot;
import org.pixel.demo.concept.spaceshooter.world.ShotPool;
import org.pixel.demo.concept.spaceshooter.world.World;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.TextAlign;
import org.pixel.graphics.text.TextBaseline;

/**
 * Draws the world: ships with their engine flames and hit flashes, shots, pickups, and the effects. Ships go to the
 * sorted WORLD layer; light (flames, glows, sparks) to the additive GLOW layer.
 */
public final class WorldRenderer {

    private static final float HALF_PI = (float) (Math.PI / 2);

    private final Assets assets;
    private final Color scratch = new Color(), scratch2 = new Color(), clear = new Color(0f, 0f, 0f, 0f);

    public WorldRenderer(Assets assets) {
        this.assets = assets;
    }

    /**
     * @param time     Seconds, for animations.
     * @param hitboxes Debug: draw collision circles.
     */
    public void draw(World world, FxDirector fx, Canvas canvas, Canvas glow, float time, boolean hitboxes) {
        for (int i = 0; i < World.MAX_PICKUPS; i++) {
            Pickup p = world.pickup(i);
            if (p.active) {
                drawPickup(p, canvas, glow, time);
            }
        }
        for (int i = 0; i < World.MAX_ENEMIES; i++) {
            Enemy e = world.enemy(i);
            if (e.active) {
                drawEnemy(e, canvas, glow, time);
            }
        }
        drawPlayer(world.player, canvas, glow, time);
        drawPlayerShots(world.playerShots, canvas, glow);
        drawEnemyShots(world.enemyShots, canvas, glow, time);

        fx.explosions.draw(canvas, Layers.DEPTH_EXPLOSION);
        fx.particles.draw(glow);
        fx.popups.draw(canvas, assets.font, Layers.DEPTH_POPUP);

        if (hitboxes) {
            drawHitboxes(world, canvas);
        }
    }

    private void drawPlayer(Player p, Canvas canvas, Canvas glow, float time) {
        if (!p.alive) {
            return;
        }
        boolean blinking = p.invulnerable > 0f && !p.entering;
        if (blinking && ((int) (time * 16f)) % 2 == 1) {
            return; // flicker while invulnerable
        }
        // engine flame (the art points down: turned a quarter clockwise, it points left), stronger when pushing
        float thrust = clamp(0.75f + p.vx / GameConfig.PLAYER_SPEED * 0.35f, 0.35f, 1.1f);
        scratch.set(1f, 1f, 1f, thrust);
        glow.save();
        glow.translate(Math.round(p.x) - 24f, Math.round(p.y));
        glow.rotate(HALF_PI);
        assets.flameLoop.draw(glow, assets.flameLoop.frameAt(time, 30f, true), 0f, 0f, 1f, false).tint(scratch);
        glow.restore();
        glow.circle(p.x - 16f, p.y, 7f).fillRadial(p.x - 16f, p.y, 7f, Palette.SHIELD, clear);

        float flash = Math.max(0f, 1f - p.sinceHit * 6f);
        Sprites.draw(canvas, assets.sprites, assets.player, p.x, p.y, false)
                .material(assets.hitFlash, flash).depth(Layers.DEPTH_PLAYER);

        if (p.invulnerable > 0f && !p.entering) { // shield bubble
            float pulse = 0.35f + 0.15f * (float) Math.sin(time * 12f);
            scratch.set(Palette.SHIELD);
            scratch.setAlpha(pulse * Math.min(1f, p.invulnerable * 2f));
            glow.circle(p.x, p.y, 19f).noFill().stroke(1.5f, scratch);
        }
    }

    private void drawEnemy(Enemy e, Canvas canvas, Canvas glow, float time) {
        float x = e.x, y = e.y;
        float flash = e.flash * e.flash; // a sharp blink
        if (e.dying) { // a boss breaking apart: shakes and flickers
            x += (float) Math.sin(time * 71f) * 2f;
            y += (float) Math.cos(time * 53f) * 2f;
            flash = ((int) (time * 20f)) % 2 == 0 ? 0.7f : 0.1f;
        }
        switch (e.type) {
            case DART -> {
                glow.circle(x + 9f, y, 6f).fillRadial(x + 9f, y, 6f, Palette.FIRE, clear);
                Sprites.draw(canvas, assets.sprites, assets.dart, x, y, true)
                        .material(assets.hitFlash, flash).depth(Layers.DEPTH_ENEMY);
            }
            case RAIDER -> {
                engine(glow, x + 25f, y, 1, time + e.age);
                Sprites.draw(canvas, assets.sprites, assets.raider, x, y, true)
                        .material(assets.hitFlash, flash).depth(Layers.DEPTH_ENEMY);
            }
            case DREADNOUGHT -> {
                if (!e.dying) { // two engine glows at the stern
                    float flicker = 0.8f + 0.2f * (float) Math.sin(time * 31f);
                    for (int side = -1; side <= 1; side += 2) {
                        float gx = x + 42f, gy = y + side * 9f, r = 12f * flicker;
                        glow.circle(gx, gy, r).fillRadial(gx, gy, r, Palette.FIRE, clear);
                    }
                }
                var r = assets.raider;
                Sprites.draw(canvas, assets.sprites, r.getX(), r.getY(), r.getWidth(), r.getHeight(), x, y, 3f, true)
                        .material(assets.hitFlash, flash * 0.5f).depth(Layers.DEPTH_ENEMY); // big: a softer flash
                if (e.bossPhase >= 3 && !e.dying) { // overheating core
                    float pulse = 0.5f + 0.5f * (float) Math.sin(time * 10f);
                    scratch.set(Palette.DANGER);
                    scratch.setAlpha(0.35f + 0.35f * pulse);
                    glow.circle(x - 6f, y, 16f).fillRadial(x - 6f, y, 16f, scratch, clear);
                }
            }
        }
    }

    /** An enemy engine flame pointing right, centered {@code scale * 10} px behind (x, y)'s nozzle. */
    private void engine(Canvas glow, float x, float y, int scale, float time) {
        glow.save();
        glow.translate(Math.round(x), Math.round(y));
        glow.rotate(-HALF_PI);
        assets.flameSteady.draw(glow, assets.flameSteady.frameAt(time, 24f, true), 0f, 0f, scale, false);
        glow.restore();
    }

    private void drawPlayerShots(ShotPool shots, Canvas canvas, Canvas glow) {
        var region = assets.playerShot;
        for (int i = 0, n = shots.size(); i < n; i++) {
            Shot s = shots.get(i);
            if (!s.active) {
                continue;
            }
            float trail = Math.min(14f, s.age * 600f);
            float speed = (float) Math.sqrt(s.vx * s.vx + s.vy * s.vy);
            float tx = s.x - s.vx / speed * trail, ty = s.y - s.vy / speed * trail;
            scratch.set(Palette.PLAYER_SHOT);
            scratch.setAlpha(0.55f);
            glow.line(tx, ty, s.x, s.y).width(2f).color(scratch);
            if (s.vy == 0f) {
                Sprites.draw(canvas, assets.sprites, region, s.x, s.y, false).depth(Layers.DEPTH_SHOT);
            } else {
                canvas.save();
                canvas.translate(s.x, s.y);
                canvas.rotate((float) Math.atan2(s.vy, s.vx));
                canvas.image(assets.sprites, -3f, -2f).source(region).depth(Layers.DEPTH_SHOT);
                canvas.restore();
            }
        }
    }

    private void drawEnemyShots(ShotPool shots, Canvas canvas, Canvas glow, float time) {
        for (int i = 0, n = shots.size(); i < n; i++) {
            Shot s = shots.get(i);
            if (!s.active) {
                continue;
            }
            float pulse = 0.8f + 0.2f * (float) Math.sin(time * 18f + i);
            scratch.set(Palette.ENEMY_SHOT);
            scratch.setAlpha(0.55f * pulse);
            glow.circle(s.x, s.y, 7f).fillRadial(s.x, s.y, 7f, scratch, clear);
            canvas.circle(s.x, s.y, 2.6f).fill(Palette.ENEMY_SHOT_CORE).stroke(1.2f, Palette.ENEMY_SHOT)
                    .depth(Layers.DEPTH_SHOT);
        }
    }

    private void drawPickup(Pickup p, Canvas canvas, Canvas glow, float time) {
        Color color = Palette.pickup(p.type);
        float bob = (float) Math.sin(p.age * 4f) * 1.5f;
        float x = p.x, y = p.y + bob;
        float pulse = 0.6f + 0.4f * (float) Math.sin(p.age * 6f);
        scratch.set(color);
        scratch.setAlpha(0.45f * pulse);
        glow.circle(x, y, 15f).fillRadial(x, y, 15f, scratch, clear);
        scratch2.set(color.getRed() * 0.45f, color.getGreen() * 0.45f, color.getBlue() * 0.45f, 1f);
        canvas.circle(x, y, 7f).fillRadial(x - 2f, y - 2f, 9f, color, scratch2).stroke(1.2f, Palette.TEXT)
                .depth(Layers.DEPTH_PICKUP);
        canvas.text(assets.font, p.type.letter, Math.round(x), Math.round(y) + 0.5f).size(8f)
                .align(TextAlign.CENTER).baseline(TextBaseline.MIDDLE).fill(Palette.INK).depth(Layers.DEPTH_PICKUP);
    }

    private void drawHitboxes(World world, Canvas canvas) {
        for (int i = 0; i < World.MAX_ENEMIES; i++) {
            Enemy e = world.enemy(i);
            if (e.active) {
                canvas.circle(e.x, e.y, e.type.radius).noFill().stroke(1f, e.isSolid() ? Palette.DANGER
                        : Palette.TEXT_DIM).depth(10f);
            }
        }
        Player p = world.player;
        if (p.alive) {
            canvas.circle(p.x, p.y, GameConfig.PLAYER_HITBOX).noFill().stroke(1f, Palette.HEAL).depth(10f);
        }
        for (int i = 0, n = world.enemyShots.size(); i < n; i++) {
            Shot s = world.enemyShots.get(i);
            if (s.active) {
                canvas.circle(s.x, s.y, s.radius).noFill().stroke(0.75f, Palette.GOLD).depth(10f);
            }
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
