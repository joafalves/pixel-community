package org.pixel.ext.ecs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;

public class GameObject extends GameObjectContainer implements Updatable, SpriteDrawable {

    private final Transform transform;
    private boolean enabled;
    private List<GameComponent> components;

    /**
     * Constructor.
     *
     * @param name The name of the game object.
     */
    public GameObject(String name) {
        super(name);
        this.transform = new Transform();
        this.transform.setGameObject(this);
        this.enabled = true;
    }

    @Override
    public void update(DeltaTime delta) {
        if (components != null && !components.isEmpty()) {
            for (Iterator<GameComponent> iterator = components.iterator(); iterator.hasNext();) {
                var component = iterator.next();
                if (component.isEnabled()) {
                    component.update(delta);
                }
                if (component.isDisposed()) {
                    component.setGameObject(null);
                    iterator.remove();
                }
                if (isDisposed()) {
                    // any of the attached components can call the "dispose()", that's why this
                    // validation exists...
                    return;
                }
            }
        }

        var children = getChildren();
        if (children != null && !children.isEmpty()) {
            for (Iterator<GameObject> iterator = children.iterator(); iterator.hasNext();) {
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
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        if (components != null) {
            for (GameComponent component : components) {
                if (component.isEnabled()) {
                    component.draw(delta, spriteBatch);
                }
            }
        }

        var children = getChildren();
        if (children != null) {
            for (GameObject child : children) {
                if (child.isEnabled()) {
                    child.draw(delta, spriteBatch);
                }
            }
        }
    }

    /**
     * Add a component to the game object.
     *
     * @param component The component to add.
     */
    public void addComponent(GameComponent component) {
        if (components == null) {
            components = new ArrayList<>();
        }

        components.add(component);
        component.setGameObject(this);
    }

    /**
     * Get the components of this game object.
     *
     * @return The components of this game object.
     */
    public List<GameComponent> getComponents() {
        return this.components;
    }

    /**
     * Get a component by name.
     *
     * @param name The name of the component (case-sensitive).
     * @return The component with the given name or null if not found.
     */
    public GameComponent getComponent(String name) {
        if (components != null) {
            for (GameComponent component : components) {
                if (name.equals(component.getName())) {
                    return component;
                }
            }
        }

        return null;
    }

    /**
     * Get all component with given name.
     *
     * @param name The name of the component (case-sensitive).
     * @return The components with the given name or an empty list if not found.
     */
    public List<GameComponent> getComponents(String name) {
        List<GameComponent> result = new ArrayList<>();
        if (components != null) {
            for (GameComponent component : components) {
                if (name.equals(component.getName())) {
                    result.add(component);
                }
            }
        }

        return result;
    }

    /**
     * Get all components by name (recursive).
     *
     * @param name The name of the component (case-sensitive).
     * @return The components with the given type or an empty list if not found.
     */
    public List<GameComponent> getAllComponents(String name) {
        var children = getChildren();
        List<GameComponent> result = getComponents(name);
        if (children != null) {
            for (GameObject child : children) {
                var childResult = child.getAllComponents(name);
                if (childResult != null) {
                    result.addAll(childResult);
                }
            }
        }

        return result;
    }

    /**
     * Get a component by type (first occurrence).
     *
     * @param type The class type of the component.
     * @param <T>  The class type (assignable from).
     * @return The component with the given type or null if not found.
     */
    public <T> T getComponent(Class<T> type) {
        if (components != null) {
            for (GameComponent component : components) {
                if (type.isAssignableFrom(component.getClass())) {
                    return (T) component;
                }
            }
        }

        return null;
    }

    /**
     * Get all component with given type.
     *
     * @param type The class type of the component.
     * @param <T>  The class type (assignable from).
     * @return The components with the given type or an empty list if not found.
     */
    public <T> List<T> getComponents(Class<T> type) {
        List<T> result = new ArrayList<>();
        if (components != null) {
            for (GameComponent component : components) {
                if (type.isAssignableFrom(component.getClass())) {
                    result.add((T) component);
                }
            }
        }

        return result;
    }

    /**
     * Get all components by type (recursive).
     *
     * @param type The class type of the component.
     * @param <T>  The class type (assignable from).
     * @return The components with the given type or an empty list if not found.
     */
    public <T> List<T> getAllComponents(Class<T> type) {
        var children = getChildren();
        List<T> result = getComponents(type);
        if (children != null) {
            for (GameObject child : children) {
                var childResult = child.getAllComponents(type);
                if (childResult != null) {
                    result.addAll(childResult);
                }
            }
        }

        return result;
    }

    /**
     * Get the transform of this game object.
     *
     * @return The transform of this game object.
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Get the active state of this game object.
     *
     * @return The active state of this game object.
     */
    public boolean isEnabled() {
        return enabled && !isDisposed();
    }

    /**
     * Set the active state of this game object. If the game object is disabled, it
     * will not be updated or drawn
     * (neither its children).
     *
     * @param enabled The active state of this game object.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Propagate to children that the transform has changed.
     */
    protected void propagateTransformChanged() {
        this.transform.setDirty();
        var children = getChildren();
        if (children != null && !children.isEmpty()) {
            for (GameObject child : children) {
                child.propagateTransformChanged();
            }
        }
    }
}
