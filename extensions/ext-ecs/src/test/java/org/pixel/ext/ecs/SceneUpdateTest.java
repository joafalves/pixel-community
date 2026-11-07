package org.pixel.ext.ecs;

import org.junit.jupiter.api.Test;
import org.pixel.commons.DeltaTime;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SceneUpdateTest {

    /** Spawns a new game object into the scene the first time it updates (like an explosion on a collision). */
    private static final class Spawner extends GameComponent {
        private final GameScene scene;
        private final AtomicInteger spawnedUpdates;
        private boolean spawned;

        Spawner(GameScene scene, AtomicInteger spawnedUpdates) {
            this.scene = scene;
            this.spawnedUpdates = spawnedUpdates;
        }

        @Override
        public void update(DeltaTime delta) {
            if (!spawned) {
                spawned = true;
                GameObject spawn = new GameObject("spawn");
                spawn.addComponent(new GameComponent() {
                    @Override
                    public void update(DeltaTime delta) {
                        spawnedUpdates.incrementAndGet();
                    }
                });
                scene.addChild(spawn);
            }
        }
    }

    @Test
    void childrenAddedDuringUpdateDoNotBreakTheLoopAndAreUpdatedInTheSameFrame() {
        GameScene scene = new GameScene("scene", null, null);
        AtomicInteger spawnedUpdates = new AtomicInteger();
        GameObject spawner = new GameObject("spawner");
        spawner.addComponent(new Spawner(scene, spawnedUpdates));
        scene.addChild(spawner);

        assertDoesNotThrow(() -> scene.update(new DeltaTime()));
        assertEquals(2, scene.getChildren().size());
        assertEquals(1, spawnedUpdates.get());
    }

    @Test
    void disposedChildrenAreDetachedAfterTheUpdate() {
        GameScene scene = new GameScene("scene", null, null);
        GameObject keep = new GameObject("keep");
        GameObject gone = new GameObject("gone");
        scene.addChild(keep);
        scene.addChild(gone);
        gone.dispose();

        scene.update(new DeltaTime());

        assertEquals(1, scene.getChildren().size());
        assertSame(keep, scene.getChildren().get(0));
        assertNull(gone.getParent());
    }
}
