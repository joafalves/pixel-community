package org.pixel.demo.concept.spaceshooter;

/**
 * Fixed numbers of the game: the virtual screen and the tuning of the player. Everything else (enemies, waves) keeps
 * its tuning next to its code.
 */
public final class GameConfig {

    /** The virtual screen, in pixel-art pixels (the window shows it at an integer scale). */
    public static final int WIDTH = 400, HEIGHT = 225;
    /** Window pixels per virtual pixel. */
    public static final int WINDOW_SCALE = 3;

    /** The simulation runs at a fixed step, whatever the frame rate. */
    public static final float STEP = 1f / 120f;
    /** At most this many steps per frame (after a hitch the game slows down instead of spiralling). */
    public static final int MAX_STEPS_PER_FRAME = 8;

    /** The band at the top kept for the score and the boss bar. */
    public static final float HUD_HEIGHT = 16f;

    // player
    public static final float PLAYER_SPEED = 140f;
    public static final float PLAYER_RESPONSE = 16f; // how fast the ship reaches the stick's speed (1/s)
    public static final float PLAYER_HITBOX = 4f; // small, as in the genre: only the cockpit counts
    public static final float PLAYER_BODY = 11f; // ramming enemies
    public static final float PICKUP_REACH = 14f, PICKUP_MAGNET = 52f;
    public static final int PLAYER_HULL = 3, START_LIVES = 3, START_BOMBS = 2, MAX_BOMBS = 5, MAX_POWER = 4;
    public static final float HIT_INVULNERABILITY = 1.8f, SPAWN_INVULNERABILITY = 2.6f, BOMB_INVULNERABILITY = 1.2f;
    public static final float RESPAWN_DELAY = 1.6f, GAME_OVER_DELAY = 2.2f;
    public static final float SHOT_SPEED = 440f;

    // scoring
    public static final float CHAIN_WINDOW = 1.8f; // kills closer than this keep the chain going
    public static final int CHAIN_PER_MULTIPLIER = 5, MAX_MULTIPLIER = 8;
    public static final int EXTRA_LIFE_EVERY = 30_000;

    private GameConfig() {
    }
}
