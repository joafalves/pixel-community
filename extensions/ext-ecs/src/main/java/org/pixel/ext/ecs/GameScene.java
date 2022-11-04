package org.pixel.ext.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.core.Camera2D;
import org.pixel.graphics.render.SpriteBatch;

public class GameScene extends GameObjectContainer implements Loadable, Updatable, Drawable {

    private SpriteBatch spriteBatch;
    private Camera2D gameCamera;

    /**
     * Constructor.
     *
     * @param name       The name of the game scene.
     * @param gameCamera The camera to use for this game scene.
     */
    public GameScene(String name, Camera2D gameCamera) {
        this(name, gameCamera, new SpriteBatch());
    }

    /**
     * Constructor.
     *
     * @param name        The name of the game scene.
     * @param gameCamera  The camera to use for this game scene.
     * @param spriteBatch The sprite batch to use for this game scene.
     */
    public GameScene(String name, Camera2D gameCamera, SpriteBatch spriteBatch) {
        super(name);
        this.gameCamera = gameCamera;
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void load() {
        // intentionally empty
    }

    @Override
    public void update(DeltaTime delta) {
        var children = getChildren();
        // do not replace with for-each (can result in concurrent modification exception)
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).isEnabled()) {
                children.get(i).update(delta);
            }
        }
        getChildren().removeIf(GameObjectContainer::isDisposed);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());
        for (GameObject child : getChildren()) {
            if (child.isEnabled()) {
                child.draw(delta, spriteBatch);
            }
        }
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteBatch.dispose();
    }

    /**
     * Get the game camera associated with this game scene.
     *
     * @return The game camera.
     */
    public Camera2D getGameCamera() {
        return gameCamera;
    }

    /**
     * Set the game camera associated with this game scene.
     *
     * @param gameCamera The game camera.
     */
    public void setGameCamera(Camera2D gameCamera) {
        this.gameCamera = gameCamera;
    }

    /**
     * Get sprite batch associated with this game scene.
     *
     * @return The sprite batch.
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    /**
     * Set the sprite batch associated with this game scene.
     *
     * @param spriteBatch The sprite batch.
     */
    public void setSpriteBatch(SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

}
