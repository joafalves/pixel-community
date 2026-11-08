package org.pixel.demo.concept.spaceshooter.render;

import org.pixel.commons.Color;

/**
 * The game's colors, picked from the sprite art. Shared: never change them (copy one to get a mutable color).
 */
public final class Palette {

    public static final Color SPACE = new Color(0.047f, 0.043f, 0.071f, 1f);
    public static final Color NEBULA_VIOLET = new Color(0.36f, 0.16f, 0.52f, 1f);
    public static final Color NEBULA_TEAL = new Color(0.08f, 0.32f, 0.42f, 1f);

    public static final Color PLAYER_SHOT = new Color(0.55f, 0.92f, 1f, 1f);
    public static final Color ENEMY_SHOT = new Color(1f, 0.55f, 0.18f, 1f);
    public static final Color ENEMY_SHOT_CORE = new Color(1f, 0.93f, 0.75f, 1f);
    public static final Color FIRE = new Color(1f, 0.62f, 0.2f, 1f);
    public static final Color HOT = new Color(1f, 0.9f, 0.55f, 1f);
    public static final Color SHIELD = new Color(0.45f, 0.85f, 1f, 1f);
    public static final Color GOLD = new Color(1f, 0.82f, 0.3f, 1f);
    public static final Color DANGER = new Color(1f, 0.25f, 0.22f, 1f);
    public static final Color HEAL = new Color(0.45f, 1f, 0.55f, 1f);
    public static final Color BOMB = new Color(1f, 0.5f, 0.25f, 1f);

    public static final Color TEXT = new Color(0.93f, 0.92f, 0.98f, 1f);
    public static final Color TEXT_DIM = new Color(0.55f, 0.53f, 0.66f, 1f);
    public static final Color INK = new Color(0.04f, 0.03f, 0.07f, 1f);
    public static final Color PANEL = new Color(0.07f, 0.06f, 0.12f, 0.82f);

    private Palette() {
    }

    /** The color of a pickup type, by its letter. */
    public static Color pickup(org.pixel.demo.concept.spaceshooter.world.PickupType type) {
        return switch (type) {
            case POWER -> SHIELD;
            case BOMB -> BOMB;
            case REPAIR -> HEAL;
        };
    }
}
