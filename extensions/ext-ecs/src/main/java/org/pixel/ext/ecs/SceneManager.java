package org.pixel.ext.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Updatable;

public class SceneManager implements Updatable, Drawable, Disposable {
    private BaseGameScene activeScene;

    /**
     * Constructor
     */
    public SceneManager() {
        this.activeScene = null;
    }

    /**
     * Constructor
     *
     * @param activeScene The initial active scene.
     */
    public SceneManager(BaseGameScene activeScene) {
        this.setActiveScene(activeScene);
    }

    /**
     * Set the active scene. If there is an active scene already, it will be disposed. The new scene will be loaded.
     *
     * @param scene The scene to set as active.
     */
    public void setActiveScene(BaseGameScene scene) {
        if (activeScene != null) {
            activeScene.dispose();
        }
        activeScene = scene;
        activeScene.load();
    }

    /**
     * Swap the active scene with another scene. The current active scene will NOT be disposed and the new scene will NOT be loaded.
     *
     * @param scene The scene to set as active.
     * @return The old active scene or null if no scene was active.
     */
    public BaseGameScene swapScene(BaseGameScene scene) {
        var oldScene = activeScene;
        activeScene = scene;
        return oldScene;
    }

    /**
     * Get the active scene.
     *
     * @return The active scene or null if no scene is active.
     */
    public BaseGameScene getActiveScene() {
        return activeScene;
    }

    /**
     * Detach the current active scene from the scene manager.
     *
     * @return The detached game scene.
     */
    public BaseGameScene dettachGameScene() {
        BaseGameScene scene = activeScene;
        activeScene = null;
        return scene;
    }

    @Override
    public void update(DeltaTime delta) {
        if (activeScene != null) {
            activeScene.update(delta);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        if (activeScene != null) {
            activeScene.draw(delta);
        }
    }

    @Override
    public void dispose() {
        if (activeScene != null) {
            activeScene.dispose();
        }
    }
}
