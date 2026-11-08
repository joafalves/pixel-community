package org.pixel.demo.concept.spaceshooter.wave;

import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.world.Enemy;
import org.pixel.demo.concept.spaceshooter.world.EnemyType;
import org.pixel.demo.concept.spaceshooter.world.Movement;
import org.pixel.demo.concept.spaceshooter.world.World;
import org.pixel.demo.concept.spaceshooter.world.WorldListener;

import java.util.Random;

/**
 * Runs the waves: a banner, then the wave's spawn schedule, then a short pause once every enemy is gone. Waves are
 * built from formations, harder as they go (more groups, faster darts that fire back, raiders); every fifth wave is
 * the boss. The schedule is a reused array of spawn events: nothing is allocated while playing.
 */
public final class WaveDirector {

    public enum Phase {
        /** The wave's banner is shown; nothing spawns yet. */
        BANNER,
        /** Enemies arrive on schedule; the wave ends when all have spawned and none is left. */
        ACTIVE,
        /** Short pause before the next wave. */
        CLEARED
    }

    public static final int BOSS_EVERY = 5;
    private static final float BANNER_SECONDS = 2.2f, CLEARED_SECONDS = 1.4f;
    private static final float SPAWN_X = GameConfig.WIDTH + 14f;

    private final World world;
    private final SpawnEvent[] events = new SpawnEvent[128];
    private WorldListener listener = new WorldListener() {
    };
    private int count, next, wave;
    private Phase phase = Phase.BANNER;
    private float timer, clock;

    public WaveDirector(World world) {
        this.world = world;
        for (int i = 0; i < events.length; i++) {
            events[i] = new SpawnEvent();
        }
    }

    public void setListener(WorldListener listener) {
        if (listener != null) {
            this.listener = listener;
        }
    }

    /** Start from the first wave (or {@code first}, e.g. to practice the boss). */
    public void start(int first) {
        begin(Math.max(1, first));
    }

    /**
     * @param dt Simulated seconds (follow the world's slow motion).
     */
    public void update(float dt) {
        switch (phase) {
            case BANNER -> {
                timer -= dt;
                if (timer <= 0f) {
                    phase = Phase.ACTIVE;
                    clock = 0f;
                }
            }
            case ACTIVE -> {
                clock += dt;
                while (next < count && events[next].time <= clock) {
                    spawn(events[next++]);
                }
                if (next >= count && world.enemyCount() == 0) {
                    phase = Phase.CLEARED;
                    timer = CLEARED_SECONDS;
                }
            }
            case CLEARED -> {
                timer -= dt;
                if (timer <= 0f) {
                    begin(wave + 1);
                }
            }
        }
    }

    /** Debug: go to a wave now (enemies on screen stay). */
    public void skipTo(int wave) {
        begin(Math.max(1, wave));
    }

    private void begin(int wave) {
        this.wave = wave;
        world.setLevel(wave);
        build(wave, world.random());
        next = 0;
        phase = Phase.BANNER;
        timer = BANNER_SECONDS;
        listener.waveStarted(wave, isBossWave());
    }

    // region schedule

    /**
     * Fill the spawn schedule of a wave.
     */
    void build(int wave, Random random) {
        count = 0;
        if (wave % BOSS_EVERY == 0) {
            SpawnEvent boss = add(0f, EnemyType.DREADNOUGHT, Movement.BOSS, GameConfig.WIDTH + 70f,
                    (GameConfig.HEIGHT + GameConfig.HUD_HEIGHT) * 0.5f, 0);
            boss.holdX = 318f;
            return;
        }
        int groups = Math.min(8, 3 + (wave + 1) / 2);
        float gap = Math.max(1.3f, 2.8f - 0.1f * wave);
        float time = 0f;
        Formation previous = null;
        for (int g = 0; g < groups; g++) {
            Formation formation;
            if (wave >= 3 && g % 3 == 2) {
                formation = wave >= 6 && random.nextBoolean() ? Formation.RAIDER_PAIR : Formation.RAIDER;
            } else if (wave == 2 && g == groups - 1) {
                formation = Formation.RAIDER; // the first gunship
            } else if (wave == 1) {
                formation = g % 2 == 0 ? Formation.LINE : Formation.SINE;
            } else {
                do {
                    formation = DART_FORMATIONS[random.nextInt(wave >= 3 ? DART_FORMATIONS.length
                            : DART_FORMATIONS.length - 1)];
                } while (formation == previous);
            }
            previous = formation;
            time = emit(formation, time, wave, random) + gap;
        }
    }

    private static final Formation[] DART_FORMATIONS = {
            Formation.LINE, Formation.SINE, Formation.VEE, Formation.SWOOP, Formation.DIVE // DIVE from wave 3
    };

    /**
     * Schedule a formation from {@code start}.
     *
     * @return When its last ship spawns.
     */
    private float emit(Formation formation, float start, int wave, Random random) {
        float speed = 105f * (1f + 0.035f * Math.min(wave, 20));
        float armedChance = wave >= 3 ? Math.min(0.55f, 0.1f * (wave - 2)) : 0f;
        int group = formation.rewarded ? world.newGroup() : 0;
        float top = GameConfig.HUD_HEIGHT + 24f, bottom = GameConfig.HEIGHT - 24f;
        float end = start;
        switch (formation) {
            case LINE -> {
                float y = between(random, top, bottom);
                for (int i = 0; i < 5; i++) {
                    end = start + i * 0.24f;
                    dart(end, SPAWN_X, y, Movement.STRAIGHT, speed, group, random, armedChance);
                }
            }
            case SINE -> {
                float y = between(random, top + 30f, bottom - 30f);
                float amplitude = between(random, 26f, 44f);
                for (int i = 0; i < 6; i++) {
                    end = start + i * 0.3f;
                    SpawnEvent e = dart(end, SPAWN_X, y, Movement.SINE, speed * 0.95f, group, random, armedChance);
                    e.amplitude = amplitude;
                    e.frequency = 2.4f;
                }
            }
            case VEE -> {
                float y = between(random, top + 32f, bottom - 32f);
                for (int i = -2; i <= 2; i++) {
                    dart(start, SPAWN_X + Math.abs(i) * 16f, y + i * 15f, Movement.STRAIGHT, speed * 0.9f, group,
                            random, armedChance);
                }
            }
            case SWOOP -> {
                boolean fromTop = random.nextBoolean();
                for (int i = 0; i < 5; i++) {
                    end = start + i * 0.3f;
                    SpawnEvent e = dart(end, GameConfig.WIDTH - 40f - i * 6f,
                            fromTop ? -14f : GameConfig.HEIGHT + 14f, Movement.SWOOP,
                            speed * 0.95f, group, random, armedChance);
                    e.amplitude = (fromTop ? 1f : -1f) * between(random, 130f, 150f);
                    e.frequency = 1.05f;
                }
            }
            case DIVE -> {
                for (int i = 0; i < 4; i++) {
                    end = start + i * 0.45f;
                    float y = i % 2 == 0 ? between(random, top, top + 40f) : between(random, bottom - 40f, bottom);
                    SpawnEvent e = dart(end, SPAWN_X, y, Movement.DIVE, speed * 0.9f, group, random, armedChance);
                    e.holdX = between(random, 250f, 310f);
                }
            }
            case RAIDER -> raider(start, between(random, top + 20f, bottom - 20f), between(random, 305f, 345f), random);
            case RAIDER_PAIR -> {
                raider(start, top + 30f, 335f, random);
                raider(start + 0.6f, bottom - 30f, 310f, random);
                end = start + 0.6f;
            }
            case BOSS -> {
            }
        }
        return end;
    }

    private SpawnEvent dart(float time, float x, float y, Movement movement, float speed, int group, Random random,
                            float armedChance) {
        SpawnEvent e = add(time, EnemyType.DART, movement, x, y, group);
        e.speed = speed;
        e.armed = random.nextFloat() < armedChance;
        e.fireDelay = between(random, 0.4f, 2.2f);
        return e;
    }

    private void raider(float time, float y, float holdX, Random random) {
        SpawnEvent e = add(time, EnemyType.RAIDER, Movement.HOLD, GameConfig.WIDTH + 30f, y, 0);
        e.holdX = holdX;
        e.amplitude = between(random, 18f, 38f);
        e.frequency = between(random, 0.7f, 1.1f);
        e.stay = between(random, 7f, 9.5f);
    }

    private SpawnEvent add(float time, EnemyType type, Movement movement, float x, float y, int group) {
        if (count == events.length) {
            throw new IllegalStateException("Wave schedule full.");
        }
        SpawnEvent e = events[count++];
        e.reset(time, type, movement, x, y, group);
        return e;
    }

    private void spawn(SpawnEvent s) {
        Enemy e = world.spawnEnemy(s.type, s.movement, s.x, s.y);
        if (e == null) {
            return; // pool full: skip (never happens with these waves)
        }
        e.speed = s.speed;
        e.amplitude = s.amplitude;
        e.frequency = s.frequency;
        e.holdX = s.holdX;
        e.stay = s.stay;
        e.armed = s.armed;
        e.fireTimer = s.fireDelay;
        world.join(e, s.group);
    }

    private static float between(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    // endregion

    // region queries

    public int wave() {
        return wave;
    }

    public Phase phase() {
        return phase;
    }

    /** Seconds left in the banner or the pause. */
    public float phaseTimer() {
        return timer;
    }

    public boolean isBossWave() {
        return wave % BOSS_EVERY == 0;
    }

    /** Spawns scheduled for the current wave (tests). */
    int scheduled() {
        return count;
    }

    SpawnEvent scheduledEvent(int index) {
        return events[index];
    }

    // endregion

    /** One scheduled spawn. */
    static final class SpawnEvent {
        float time;
        EnemyType type;
        Movement movement;
        float x, y;
        float speed, amplitude, frequency, holdX, stay, fireDelay;
        boolean armed;
        int group;

        void reset(float time, EnemyType type, Movement movement, float x, float y, int group) {
            this.time = time;
            this.type = type;
            this.movement = movement;
            this.x = x;
            this.y = y;
            this.group = group;
            speed = amplitude = frequency = holdX = stay = fireDelay = 0f;
            armed = false;
        }
    }
}
