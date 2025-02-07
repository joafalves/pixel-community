package org.pixel.ext.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.core.Camera;
import org.pixel.graphics.render.SpriteBatch;

import java.util.Iterator;

public abstract class BaseGameScene extends GameObjectContainer implements Loadable, Updatable, Drawable {

    /**
     * Constructor.
     *
     * @param name The name of the game scene.
     */
    public BaseGameScene(String name) {
        super(name);
    }

    @Override
    public void update(DeltaTime delta) {
        var children = getChildren();
        if (children != null && !children.isEmpty()) {
            for (Iterator<GameObject> iterator = children.iterator(); iterator.hasNext(); ) {
                var child = iterator.next();
                if (child.isEnabled()) {
                    child.update(delta);
                }
                if (child.isDisposed()) {
                    child.setParent(null);
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void setParent(GameObjectContainer parent) {
        throw new RuntimeException("Scenes MUST be top-level instances!");
    }

    /**
     * Draws the children associated to the game scene.
     */
    protected void drawChildren(DeltaTime delta, SpriteBatch spriteBatch, Camera gameCamera) {
        if (gameCamera == null || spriteBatch == null) {
            return;
        }

        spriteBatch.begin(gameCamera.getViewMatrix());
        for (GameObject child : getChildren()) {
            if (child.isEnabled()) {
                child.draw(delta, spriteBatch);
            }
        }
        spriteBatch.end();
    }
}
