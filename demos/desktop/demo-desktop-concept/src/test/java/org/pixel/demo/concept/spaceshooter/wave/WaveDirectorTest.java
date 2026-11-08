package org.pixel.demo.concept.spaceshooter.wave;

import org.junit.jupiter.api.Test;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.input.Controls;
import org.pixel.demo.concept.spaceshooter.world.EnemyType;
import org.pixel.demo.concept.spaceshooter.world.Movement;
import org.pixel.demo.concept.spaceshooter.world.World;
import org.pixel.demo.concept.spaceshooter.world.WorldListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WaveDirectorTest {

    private final World world = new World(7L);
    private final WaveDirector waves = new WaveDirector(world);

    @Test
    void schedulesAreInTimeOrderAndGrowWithTheWaves() {
        int previous = 0;
        for (int wave = 1; wave <= 12; wave++) {
            if (wave % WaveDirector.BOSS_EVERY == 0) {
                continue;
            }
            waves.build(wave, new Random(wave));
            for (int i = 1; i < waves.scheduled(); i++) {
                assertTrue(waves.scheduledEvent(i).time >= waves.scheduledEvent(i - 1).time, "wave " + wave);
            }
            assertTrue(waves.scheduled() >= previous - 6, "wave " + wave + " isn't much smaller than the last");
            previous = waves.scheduled();
        }
    }

    @Test
    void theFirstWaveTeachesWithDartsOnlyAndEveryFifthIsTheBoss() {
        waves.build(1, new Random(1));
        Set<Movement> movements = EnumSet.noneOf(Movement.class);
        for (int i = 0; i < waves.scheduled(); i++) {
            assertEquals(EnemyType.DART, waves.scheduledEvent(i).type);
            assertFalse(waves.scheduledEvent(i).armed, "nobody fires back in wave 1");
            movements.add(waves.scheduledEvent(i).movement);
        }
        assertEquals(EnumSet.of(Movement.STRAIGHT, Movement.SINE), movements);

        waves.build(2, new Random(2));
        assertEquals(EnemyType.RAIDER, waves.scheduledEvent(waves.scheduled() - 1).type, "the first gunship");

        waves.build(10, new Random(10));
        assertEquals(1, waves.scheduled());
        assertEquals(EnemyType.DREADNOUGHT, waves.scheduledEvent(0).type);
    }

    @Test
    void aWaveEndsWhenItsEnemiesAreGoneThenTheNextStarts() {
        List<Integer> started = new ArrayList<>();
        waves.setListener(new WorldListener() {
            @Override
            public void waveStarted(int wave, boolean boss) {
                started.add(wave);
            }
        });
        world.invincible = true;
        waves.start(1);
        Controls controls = new Controls();
        controls.fire = true;
        for (int i = 0; i < 120 * 90 && started.size() < 3; i++) {
            world.player.y = 40f + 150f * (0.5f + 0.5f * (float) Math.sin(i * 0.01)); // sweep the screen
            waves.update(world.update(GameConfig.STEP, controls));
        }
        assertEquals(List.of(1, 2, 3), started);
        assertEquals(3, world.level());
    }
}
