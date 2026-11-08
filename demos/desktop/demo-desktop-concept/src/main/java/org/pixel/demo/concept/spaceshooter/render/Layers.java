package org.pixel.demo.concept.spaceshooter.render;

import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.render.BlendMode;

/**
 * The game's layers, back to front. The first four are drawn through the camera into the scene target (then bloom,
 * shockwave and vignette); the HUD is drawn on top, crisp, in screen space.
 */
public final class Layers {

    /** Space tiles (plain images: the light sprite vertex format). */
    public static final Layer BACKDROP = Layer.ordered("backdrop");
    /** Nebulae and stars: light, added up. */
    public static final Layer STARS = Layer.ordered("stars").blend(BlendMode.ADDITIVE);
    /** Ships, pickups, shots, explosions and popups, by depth. */
    public static final Layer WORLD = Layer.sorted("world");
    /** Engine flames, shot glows, sparks, rings: light, added up. */
    public static final Layer GLOW = Layer.ordered("glow").blend(BlendMode.ADDITIVE);
    /** Score, gauges, banners and menus. */
    public static final Layer HUD = Layer.ordered("hud");

    // depths in WORLD (higher draws later)
    public static final float DEPTH_PICKUP = 1f, DEPTH_ENEMY = 2f, DEPTH_PLAYER = 3f, DEPTH_SHOT = 4f,
            DEPTH_EXPLOSION = 5f, DEPTH_POPUP = 6f;

    private Layers() {
    }
}
