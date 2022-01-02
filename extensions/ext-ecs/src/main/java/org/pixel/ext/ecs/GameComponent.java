package org.pixel.ext.ecs;

import java.io.Serializable;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.ext.ecs.lifecycle.Attachable;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;

public abstract class GameComponent implements Attachable, Updatable, SpriteDrawable, Disposable, Serializable {

    private String name;
    private GameObject gameObject;
    private boolean enabled;
    private boolean disposed;

    /**
     * Constructor.
     */
    public GameComponent() {
        this("");
    }

    /**
     * Constructor.
     *
     * @param name The name of this component.
     */
    public GameComponent(String name) {
        this.name = name;
        this.enabled = true;
        this.disposed = false;
    }

    @Override
    public void attached(GameObject parent, GameObject previousParent) {
        // intentionally left blank
    }

    @Override
    public void detached(GameObject previousParent) {
        // intentionally left blank
    }

    @Override
    public void update(DeltaTime delta) {
        // intentionally left blank
    }

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        // intentionally left blank
    }

    /**
     * Disables and disposes this game component instance. Notice that if it has a game object parent, it will
     * automatically detach this instance after the current cycle is executed.
     */
    @Override
    public void dispose() {
        this.enabled = false;
        this.disposed = true;
    }

    /**
     * Get the name of this component.
     *
     * @return The name of this component.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this component.
     *
     * @param name The name of this component.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the game object owner of this component.
     *
     * @return The game object owner of this component.
     */
    protected GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Get the Transform instance associated with the parent of this component.
     *
     * @return The Transform instance associated with the parent of this component.
     */
    protected Transform getTransform() {
        if (gameObject == null) {
            return null;
        }

        return gameObject.getTransform();
    }

    /**
     * Set the game object owner of this component.
     *
     * @param gameObject The game object owner of this component.
     */
    protected void setGameObject(GameObject gameObject) {
        var previousParent = this.gameObject;
        this.gameObject = gameObject;

        if (gameObject != null) {
            attached(gameObject, previousParent);
        } else {
            detached(previousParent);
        }
    }

    /**
     * Get the disposed flag of this game component.
     *
     * @return The disposed flag of this game component.
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Get the enabled flag of this game component.
     *
     * @return The enabled flag of this game component.
     */
    public boolean isEnabled() {
        return enabled && !isDisposed();
    }

    /**
     * Set the active state of this game component. If the game component is disabled, it will not be updated or drawn.
     *
     * @param enabled The active state of this game component.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
