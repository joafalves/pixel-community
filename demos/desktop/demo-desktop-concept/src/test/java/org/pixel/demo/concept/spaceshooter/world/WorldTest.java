package org.pixel.demo.concept.spaceshooter.world;

import org.junit.jupiter.api.Test;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.input.Controls;
import org.pixel.demo.concept.spaceshooter.wave.WaveDirector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The simulation without a window: the world is plain Java, driven by {@link Controls}.
 */
class WorldTest {

    private final World world = new World(42L);
    private final Controls controls = new Controls();
    private final List<String> events = new ArrayList<>();

    WorldTest() {
        world.setListener(new WorldListener() {
            @Override
            public void enemyDestroyed(Enemy enemy, int points, int multiplier) {
                events.add("destroyed " + enemy.type + " " + points);
            }

            @Override
            public void playerHit(Player player) {
                events.add("hit");
            }

            @Override
            public void playerDestroyed(Player player) {
                events.add("lost a life");
            }

            @Override
            public void bossDefeated(Enemy boss, int points) {
                events.add("boss defeated");
            }

            @Override
            public void gameOver(int score) {
                events.add("game over");
            }
        });
    }

    private void run(float seconds) {
        for (int i = 0, n = Math.round(seconds / GameConfig.STEP); i < n; i++) {
            world.update(GameConfig.STEP, controls);
        }
    }

    private void flyIn() {
        run(1.5f); // the ship flies in from the left
        assertFalse(world.player.entering);
        world.player.invulnerable = 0f;
    }

    @Test
    void theShipFliesInThenFollowsTheControls() {
        assertTrue(world.player.entering);
        flyIn();
        float x = world.player.x;
        controls.moveX = 1f;
        run(0.5f);
        assertTrue(world.player.x > x + 50f, "moved right: " + world.player.x);
        controls.moveX = 0f;
        controls.moveY = -1f;
        run(3f);
        assertEquals(GameConfig.HUD_HEIGHT + 12f, world.player.y, 0.01f, "stops at the top of the playfield");
    }

    @Test
    void shotsDestroyEnemiesOnlyOnceTheyAreOnScreen() {
        flyIn();
        Enemy dart = world.spawnEnemy(EnemyType.DART, Movement.STRAIGHT, GameConfig.WIDTH + 14f, world.player.y);
        dart.speed = 60f;
        controls.fire = true;
        run(0.3f);
        assertTrue(dart.active, "still arriving: can't be hit yet");
        run(1.5f);
        assertFalse(dart.active);
        assertEquals("destroyed DART 100", events.get(0));
        assertEquals(100, world.score.points());
    }

    @Test
    void enemyShotsCostHullThenALifeThenTheGame() {
        flyIn();
        Player p = world.player;
        for (int life = GameConfig.START_LIVES; life > 0; life--) {
            for (int hull = GameConfig.PLAYER_HULL; hull > 0; hull--) {
                p.invulnerable = 0f;
                world.enemyShots.spawn(p.x, p.y, 0f, 0f, 3f, 1f);
                run(GameConfig.STEP);
            }
            assertFalse(p.alive);
            assertEquals(life - 1, p.lives);
            if (life > 1) {
                run(GameConfig.RESPAWN_DELAY + 1.5f); // respawns and flies in again
                assertTrue(p.alive);
                assertEquals(GameConfig.PLAYER_HULL, p.hull);
            }
        }
        assertEquals(0, p.lives);
        assertFalse(p.alive);
        assertTrue(events.contains("lost a life"));
        run(GameConfig.GAME_OVER_DELAY + 0.1f);
        assertTrue(world.isOver());
        assertEquals("game over", events.get(events.size() - 1));
    }

    @Test
    void theBossCanBeDamagedOnceInPlaceAndBreaksApart() {
        flyIn();
        world.invincible = true;
        WaveDirector waves = new WaveDirector(world);
        waves.start(WaveDirector.BOSS_EVERY);
        for (int i = 0; i < 120 * 12 && world.boss() == null; i++) {
            float dt = world.update(GameConfig.STEP, controls);
            waves.update(dt);
        }
        Enemy boss = world.boss();
        assertNotNull(boss);
        assertFalse(boss.isSolid(), "entering");
        for (int i = 0; i < 120 * 8 && boss.state == 0; i++) {
            waves.update(world.update(GameConfig.STEP, controls));
        }
        assertEquals(1, boss.state, "in place at x=" + boss.x);
        assertTrue(boss.isSolid());

        controls.fire = true;
        float before = boss.hitPoints;
        for (int i = 0; i < 120 * 3; i++) {
            world.player.y = boss.y; // line up
            waves.update(world.update(GameConfig.STEP, controls));
        }
        assertTrue(boss.hitPoints < before, "damaged: " + boss.hitPoints + " of " + before);

        world.player.power = GameConfig.MAX_POWER;
        for (int i = 0; i < 120 * 90 && world.boss() != null; i++) {
            if (world.boss() != null) {
                world.player.y = world.boss().y;
            }
            waves.update(world.update(GameConfig.STEP, controls));
        }
        assertNull(world.boss());
        assertTrue(events.contains("boss defeated"), events.toString());
    }

    @Test
    void destroyingAWholeFormationPaysABonus() {
        flyIn();
        int group = world.newGroup();
        for (int i = 0; i < 4; i++) {
            Enemy dart = world.spawnEnemy(EnemyType.DART, Movement.STRAIGHT, 300f + i * 10f, world.player.y);
            world.join(dart, group);
        }
        controls.fire = true;
        run(1.5f);
        assertEquals(0, world.enemyCount());
        assertTrue(world.score.points() >= 4 * 100 + 1000, "bonus included: " + world.score.points());
    }

    @Test
    void nothingIsAllocatedWhilePlaying() {
        flyIn();
        world.invincible = true;
        WaveDirector waves = new WaveDirector(world);
        waves.start(3);
        controls.fire = true;
        for (int i = 0; i < 120 * 5; i++) { // warm up
            waves.update(world.update(GameConfig.STEP, controls));
        }
        var threads = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        long least = Long.MAX_VALUE;
        for (int window = 0; window < 5; window++) {
            long before = threads.getCurrentThreadAllocatedBytes();
            for (int i = 0; i < 120; i++) {
                world.player.y = 60f + 40f * (float) Math.sin(i * 0.05);
                waves.update(world.update(GameConfig.STEP, controls));
            }
            least = Math.min(least, threads.getCurrentThreadAllocatedBytes() - before);
        }
        assertEquals(0L, least, "bytes allocated per simulated second");
    }
}
