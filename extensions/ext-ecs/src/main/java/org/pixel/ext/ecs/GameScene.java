package org.pixel.ext.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.graphics.render.SpriteBatch;

public class GameScene extends BaseGameScene {

    protected SpriteBatch spriteBatch;
    protected Camera2D gameCamera;

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
    public void draw(DeltaTime delta) {
        drawChildren(delta, spriteBatch, gameCamera);
    }

    @Override
    public void dispose() {
        super.dispose();
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
