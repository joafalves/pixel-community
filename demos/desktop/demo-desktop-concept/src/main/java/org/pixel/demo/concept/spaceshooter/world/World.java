package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.input.Controls;

import java.util.Random;

/**
 * The simulation of one run: the player, enemies, shots and pickups, collisions, scoring, lives and bombs. It never
 * draws, never reads devices (it gets {@link Controls}) and allocates nothing once created, so it runs the same in
 * the game, in the attract mode and in tests. What happens is reported to a {@link WorldListener}.
 * <p>
 * Enemies are spawned by the wave director ({@code wave.WaveDirector}) through {@link #spawnEnemy}.
 */
public final class World {

    public static final int MAX_ENEMIES = 64, MAX_PICKUPS = 16, MAX_GROUPS = 32;
    private static final WorldListener NOBODY = new WorldListener() {
    };

    public final Player player = new Player();
    public final ShotPool playerShots = new ShotPool(320);
    public final ShotPool enemyShots = new ShotPool(512);
    public final Score score = new Score();

    private final Enemy[] enemies = new Enemy[MAX_ENEMIES];
    private final Pickup[] pickups = new Pickup[MAX_PICKUPS];
    private final int[] groupSize = new int[MAX_GROUPS], groupAlive = new int[MAX_GROUPS];
    private final boolean[] groupBroken = new boolean[MAX_GROUPS];
    private final Random random;
    private WorldListener listener = NOBODY;

    private int enemyCount, nextGroup = 1, level = 1;
    private float time, slowMotion, slowMotionScale = 1f, gameOverTimer = -1f;
    private boolean over;
    /** Debug: nothing hurts the player. */
    public boolean invincible;

    public World(long seed) {
        random = new Random(seed);
        for (int i = 0; i < MAX_ENEMIES; i++) {
            enemies[i] = new Enemy();
        }
        for (int i = 0; i < MAX_PICKUPS; i++) {
            pickups[i] = new Pickup();
        }
        reset();
    }

    public void setListener(WorldListener listener) {
        this.listener = listener != null ? listener : NOBODY;
    }

    /** Start a new run. */
    public void reset() {
        for (Enemy e : enemies) {
            e.active = false;
        }
        for (Pickup p : pickups) {
            p.active = false;
        }
        playerShots.clear();
        enemyShots.clear();
        enemyCount = 0;
        nextGroup = 1;
        level = 1;
        time = 0f;
        slowMotion = 0f;
        gameOverTimer = -1f;
        over = false;
        score.reset();
        player.reset();
        listener.playerSpawned(player);
    }

    // region simulation

    /**
     * Advance the world by one step.
     *
     * @param dt       Seconds (a fixed step).
     * @param controls What the player asks for (one-frame presses are consumed).
     * @return The simulated seconds: less than {@code dt} in slow motion (the waves follow this time).
     */
    public float update(float dt, Controls controls) {
        if (slowMotion > 0f) {
            slowMotion -= dt;
        }
        float scaled = slowMotion > 0f ? dt * slowMotionScale : dt;
        time += scaled;

        updatePlayer(scaled, controls);
        controls.consumePresses();
        for (Enemy e : enemies) {
            if (e.active) {
                EnemyAi.update(e, this, scaled);
            }
        }
        playerShots.update(scaled);
        enemyShots.update(scaled);
        updatePickups(scaled);
        collide();
        score.update(scaled);
        if (score.takeExtraLife() && player.lives > 0) {
            player.lives = Math.min(9, player.lives + 1);
            listener.extraLife(player);
        }
        if (gameOverTimer > 0f) {
            gameOverTimer -= dt;
            if (gameOverTimer <= 0f) {
                over = true;
                listener.gameOver(score.points());
            }
        }
        return scaled;
    }

    private void updatePlayer(float dt, Controls c) {
        Player p = player;
        p.sinceShot += dt;
        p.sinceHit += dt;
        if (!p.alive) {
            if (p.lives > 0) {
                p.respawnTimer -= dt;
                if (p.respawnTimer <= 0f) {
                    p.spawn();
                    listener.playerSpawned(p);
                }
            }
            return;
        }
        p.invulnerable = Math.max(0f, p.invulnerable - dt);
        if (p.entering) { // fly in from the left
            p.x += 150f * dt;
            p.vx = 150f;
            p.vy = 0f;
            if (p.x >= 60f) {
                p.entering = false;
            }
            return;
        }

        float mx = c.moveX, my = c.moveY;
        float length = (float) Math.sqrt(mx * mx + my * my);
        if (length > 1f) { // diagonals aren't faster
            mx /= length;
            my /= length;
        }
        float response = Math.min(1f, GameConfig.PLAYER_RESPONSE * dt);
        p.vx += (mx * GameConfig.PLAYER_SPEED - p.vx) * response;
        p.vy += (my * GameConfig.PLAYER_SPEED - p.vy) * response;
        p.x = EnemyAi.clamp(p.x + p.vx * dt, 12f, GameConfig.WIDTH - 28f);
        p.y = EnemyAi.clamp(p.y + p.vy * dt, GameConfig.HUD_HEIGHT + 12f, GameConfig.HEIGHT - 12f);

        p.fireCooldown -= dt;
        if (c.fire && p.fireCooldown <= 0f) {
            fireVolley(p);
            p.fireCooldown += p.power >= 4 ? 0.085f : 0.11f;
            p.fireCooldown = Math.max(p.fireCooldown, 0f);
        } else if (!c.fire) {
            p.fireCooldown = Math.max(p.fireCooldown, 0f);
        }
        if (c.bomb && p.bombs > 0) {
            bomb();
        }
    }

    private void fireVolley(Player p) {
        float s = GameConfig.SHOT_SPEED;
        playerShots.spawn(p.x + 10f, p.y - 6f, s, 0f, 3f, 1f);
        playerShots.spawn(p.x + 10f, p.y + 6f, s, 0f, 3f, 1f);
        if (p.power >= 2) {
            playerShots.spawn(p.x + 15f, p.y, s * 1.1f, 0f, 3f, 1f);
        }
        if (p.power >= 3) {
            angled(p.x + 6f, p.y - 8f, -0.11f);
            angled(p.x + 6f, p.y + 8f, 0.11f);
        }
        if (p.power >= 4) {
            angled(p.x + 2f, p.y - 9f, -0.24f);
            angled(p.x + 2f, p.y + 9f, 0.24f);
        }
        p.sinceShot = 0f;
        listener.shotFired(p.x + 14f, p.y, true);
    }

    private void angled(float x, float y, float angle) {
        float s = GameConfig.SHOT_SPEED;
        playerShots.spawn(x, y, s * (float) Math.cos(angle), s * (float) Math.sin(angle), 3f, 1f);
    }

    private void bomb() {
        Player p = player;
        p.bombs--;
        p.invulnerable = Math.max(p.invulnerable, GameConfig.BOMB_INVULNERABILITY);
        listener.bomb(p.x, p.y);
        cancelEnemyShots(10);
        for (Enemy e : enemies) {
            if (e.isSolid()) {
                damage(e, e.type == EnemyType.DREADNOUGHT ? 45f : 16f, e.x, e.y);
            }
        }
    }

    private void updatePickups(float dt) {
        Player p = player;
        for (Pickup pickup : pickups) {
            if (!pickup.active) {
                continue;
            }
            pickup.age += dt;
            float dx = p.x - pickup.x, dy = p.y - pickup.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (p.alive && distance < GameConfig.PICKUP_MAGNET && distance > 0.01f) { // pulled in when close
                float pull = 190f * (1f - distance / GameConfig.PICKUP_MAGNET) + 40f;
                pickup.x += dx / distance * pull * dt;
                pickup.y += dy / distance * pull * dt;
            } else {
                pickup.x += pickup.vx * dt;
                pickup.y += pickup.vy * dt + (float) Math.sin(pickup.age * 3f) * 10f * dt;
                pickup.vy *= (float) Math.pow(0.2, dt); // the drop's push fades
            }
            pickup.y = EnemyAi.clamp(pickup.y, GameConfig.HUD_HEIGHT + 6f, GameConfig.HEIGHT - 6f);
            if (p.alive && !p.entering && distance < GameConfig.PICKUP_REACH) {
                collect(pickup);
            } else if (pickup.x < -16f) {
                pickup.active = false;
            }
        }
    }

    private void collect(Pickup pickup) {
        pickup.active = false;
        Player p = player;
        int points = 0;
        switch (pickup.type) {
            case POWER -> {
                if (p.power < GameConfig.MAX_POWER) {
                    p.power++;
                } else {
                    points = score.add(pickup.type.pointsWhenFull);
                }
            }
            case BOMB -> {
                if (p.bombs < GameConfig.MAX_BOMBS) {
                    p.bombs++;
                } else {
                    points = score.add(pickup.type.pointsWhenFull);
                }
            }
            case REPAIR -> {
                if (p.hull < GameConfig.PLAYER_HULL) {
                    p.hull++;
                } else {
                    points = score.add(pickup.type.pointsWhenFull);
                }
            }
        }
        listener.pickupCollected(pickup.type, pickup.x, pickup.y, points);
    }

    private void collide() {
        Player p = player;
        boolean vulnerable = p.isVulnerable() && !invincible;
        for (Enemy e : enemies) {
            if (!e.isSolid()) {
                continue;
            }
            for (int i = 0, n = playerShots.size(); i < n && e.isSolid(); i++) {
                Shot s = playerShots.get(i);
                if (s.active && near(s.x, s.y, e.x, e.y, s.radius + e.type.radius)) {
                    playerShots.remove(s);
                    damage(e, s.damage, s.x, s.y);
                }
            }
            if (vulnerable && e.isSolid() && near(p.x, p.y, e.x, e.y, GameConfig.PLAYER_BODY * 0.6f + e.type.radius)) {
                hitPlayer();
                vulnerable = p.isVulnerable();
                damage(e, e.type == EnemyType.DREADNOUGHT ? 0f : 6f, e.x, e.y); // rammed darts break
            }
        }
        if (!vulnerable) {
            return;
        }
        for (int i = 0, n = enemyShots.size(); i < n; i++) {
            Shot s = enemyShots.get(i);
            if (s.active && near(s.x, s.y, p.x, p.y, s.radius + GameConfig.PLAYER_HITBOX)) {
                enemyShots.remove(s);
                hitPlayer();
                if (!p.isVulnerable()) {
                    return;
                }
            }
        }
    }

    private static boolean near(float x1, float y1, float x2, float y2, float distance) {
        float dx = x1 - x2, dy = y1 - y2;
        return dx * dx + dy * dy < distance * distance;
    }

    private void damage(Enemy e, float amount, float hitX, float hitY) {
        if (amount <= 0f) {
            return;
        }
        e.hitPoints -= amount;
        if (e.flash <= 0f) { // restarts only once faded: constant fire flickers instead of staying white
            e.flash = 1f;
        }
        if (e.hitPoints > 0f) {
            listener.enemyHit(e, hitX, hitY);
            return;
        }
        if (e.type == EnemyType.DREADNOUGHT) { // breaks apart for a while, in slow motion
            e.dying = true;
            e.stateTime = 0f; // EnemyAi counts the breakup from here
            e.fireTimer = 0f;
            cancelEnemyShots(25);
            slowDown(0.35f, 1.4f);
            return;
        }
        int points = score.kill(e.type.points);
        listener.enemyDestroyed(e, points, score.multiplier());
        if (e.type == EnemyType.RAIDER && random.nextFloat() < 0.4f) {
            drop(chooseDrop(), e.x, e.y);
        }
        remove(e, false);
    }

    private void hitPlayer() {
        Player p = player;
        p.hull--;
        p.sinceHit = 0f;
        score.breakChain();
        listener.playerHit(p);
        if (p.hull > 0) {
            p.invulnerable = GameConfig.HIT_INVULNERABILITY;
            return;
        }
        p.alive = false;
        p.lives--;
        p.power = Math.max(1, p.power - 1);
        p.respawnTimer = GameConfig.RESPAWN_DELAY;
        cancelEnemyShots(0); // a moment to breathe
        slowDown(0.4f, 0.9f);
        listener.playerDestroyed(p);
        if (p.lives <= 0) {
            gameOverTimer = GameConfig.GAME_OVER_DELAY;
        }
    }

    private void cancelEnemyShots(int pointsEach) {
        for (int i = 0, n = enemyShots.size(); i < n; i++) {
            Shot s = enemyShots.get(i);
            if (s.active) {
                listener.shotCancelled(s.x, s.y);
                if (pointsEach > 0) {
                    score.add(pointsEach);
                }
                enemyShots.remove(s);
            }
        }
    }

    private void slowDown(float scale, float seconds) {
        slowMotionScale = scale;
        slowMotion = seconds;
    }

    // endregion

    // region enemies (called by the wave director and the enemy AI)

    /**
     * Spawn an enemy with its default tuning for the current level. The caller sets the movement parameters it needs
     * ({@link Enemy#speed}, {@link Enemy#amplitude}...).
     *
     * @return The enemy, or {@code null} when the pool is full.
     */
    public Enemy spawnEnemy(EnemyType type, Movement movement, float x, float y) {
        for (Enemy e : enemies) {
            if (!e.active) {
                float health = switch (type) {
                    case DART -> 1f;
                    case RAIDER -> 1f + 0.08f * (level - 1);
                    case DREADNOUGHT -> 1f + 0.35f * ((level - 1) / 5);
                };
                e.init(type, movement, x, y, health);
                enemyCount++;
                return e;
            }
        }
        return null;
    }

    /** A new formation id, for {@link #join}. */
    public int newGroup() {
        int group = nextGroup;
        nextGroup = nextGroup + 1 < MAX_GROUPS ? nextGroup + 1 : 1;
        groupSize[group] = 0;
        groupAlive[group] = 0;
        groupBroken[group] = false;
        return group;
    }

    /** Make an enemy part of a formation: destroying all of a formation drops a capsule. */
    public void join(Enemy e, int group) {
        if (e != null && group > 0) {
            e.group = group;
            groupSize[group]++;
            groupAlive[group]++;
        }
    }

    /** An enemy left the screen alive. */
    void escape(Enemy e) {
        remove(e, true);
    }

    private void remove(Enemy e, boolean escaped) {
        e.active = false;
        enemyCount--;
        int g = e.group;
        if (g <= 0) {
            return;
        }
        groupAlive[g]--;
        if (escaped) {
            groupBroken[g] = true;
        } else if (groupAlive[g] == 0 && !groupBroken[g] && groupSize[g] >= 4) {
            int points = score.add(250 * groupSize[g]);
            listener.formationCleared(e.x, e.y, points);
            if (random.nextFloat() < 0.35f) {
                drop(player.power < GameConfig.MAX_POWER ? PickupType.POWER : PickupType.BOMB, e.x, e.y);
            }
        }
    }

    void fireEnemyShot(float x, float y, float angle, float speed) {
        enemyShots.spawn(x, y, speed * (float) Math.cos(angle), speed * (float) Math.sin(angle), 3f, 1f);
    }

    /**
     * Fire at the player (straight left when there is no player): {@code count} shots spread {@code spread} radians
     * apart, centered on the aim; 0 means a single shot.
     */
    void fireAimed(float x, float y, float speed, int count, float spread) {
        float aim = player.alive ? (float) Math.atan2(player.y - y, player.x - x) : (float) Math.PI;
        if (count <= 1) {
            fireEnemyShot(x, y, aim, speed);
        } else {
            float first = aim - spread * (count - 1) * 0.5f;
            for (int i = 0; i < count; i++) {
                fireEnemyShot(x, y, first + spread * i, speed);
            }
        }
        listener.shotFired(x, y, false);
    }

    void bossPhaseChanged(Enemy boss) {
        listener.bossPhase(boss, boss.bossPhase);
    }

    void bossBlast(Enemy boss) {
        listener.bossBlast(boss, boss.x + (random.nextFloat() - 0.5f) * 70f, boss.y + (random.nextFloat() - 0.5f) * 56f);
    }

    void finishBoss(Enemy boss) {
        int points = score.kill(boss.type.points);
        listener.bossDefeated(boss, points);
        drop(PickupType.POWER, boss.x - 10f, boss.y - 14f);
        drop(PickupType.REPAIR, boss.x, boss.y + 14f);
        drop(PickupType.BOMB, boss.x + 10f, boss.y);
        remove(boss, false);
    }

    void summonDarts(Enemy boss) {
        for (int side = -1; side <= 1; side += 2) {
            Enemy dart = spawnEnemy(EnemyType.DART, Movement.DIVE, boss.x - 20f, boss.y + side * 22f);
            if (dart != null) {
                dart.speed = 90f;
                dart.holdX = boss.x - 60f;
            }
        }
    }

    private void drop(PickupType type, float x, float y) {
        for (Pickup p : pickups) {
            if (!p.active) {
                p.active = true;
                p.type = type;
                p.x = Math.min(x, GameConfig.WIDTH - 12f);
                p.y = y;
                p.vx = -26f;
                p.vy = (random.nextFloat() - 0.5f) * 40f;
                p.age = 0f;
                return;
            }
        }
    }

    private PickupType chooseDrop() {
        if (player.power < GameConfig.MAX_POWER && random.nextFloat() < 0.55f) {
            return PickupType.POWER;
        }
        if (player.hull < GameConfig.PLAYER_HULL && random.nextFloat() < 0.6f) {
            return PickupType.REPAIR;
        }
        return PickupType.BOMB;
    }

    // endregion

    // region queries

    public Enemy enemy(int index) {
        return enemies[index];
    }

    public Pickup pickup(int index) {
        return pickups[index];
    }

    public int enemyCount() {
        return enemyCount;
    }

    /** The boss on screen, or {@code null}. */
    public Enemy boss() {
        for (Enemy e : enemies) {
            if (e.active && e.type == EnemyType.DREADNOUGHT) {
                return e;
            }
        }
        return null;
    }

    public Random random() {
        return random;
    }

    /** The difficulty level (the wave number). */
    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    /** Simulated seconds since the run started. */
    public float time() {
        return time;
    }

    /** Whether the run ended (no lives left, after the last explosion). */
    public boolean isOver() {
        return over;
    }

    /** Whether the game is slowed down (a boss or the player exploding). */
    public boolean isSlowMotion() {
        return slowMotion > 0f;
    }

    // endregion
}
