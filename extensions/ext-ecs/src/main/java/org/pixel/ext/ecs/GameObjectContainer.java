package org.pixel.ext.ecs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.pixel.commons.AttributeMap;
import org.pixel.commons.lifecycle.Disposable;

public abstract class GameObjectContainer implements Disposable, Serializable {

    private final AttributeMap attributeMap;

    private String name;
    private List<GameObject> children;
    private GameObjectContainer parent;

    private boolean disposed;

    /**
     * Constructor.
     *
     * @param name The name of the container.
     */
    public GameObjectContainer(String name) {
        this.attributeMap = new AttributeMap();
        this.name = name;
        this.parent = null;
        this.children = null;
        this.disposed = false;
    }

    /**
     * Disposes this game object instance and all of its children. Notice that if it has a parent, it will automatically
     * detach this instance after the current cycle is executed.
     */
    @Override
    public void dispose() {
        if (children != null && !children.isEmpty()) {
            for (GameObject child : children) {
                child.dispose();
            }
        }
        this.disposed = true;
    }

    /**
     * Initializes the children list.
     */
    protected void initializeChildrenList() {
        if (children == null) {
            children = new ArrayList<>();
        }
    }

    /**
     * Add a child GameObject to this instance.
     *
     * @param child The child to add.
     */
    public void addChild(GameObject child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
    }

    /**
     * Get a direct child by name.
     *
     * @param name The name of the child (case-sensitive).
     * @return The child with the given name or null if not found.
     */
    public GameObject getChild(String name) {
        if (children != null) {
            for (var child : children) {
                if (name.equals(child.getName())) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Get all direct children by name
     *
     * @param name The name of the child (case-sensitive).
     * @return The children with the given name if not found.
     */
    public List<GameObject> getChildren(String name) {
        List<GameObject> result = new ArrayList<>();
        if (children != null) {
            for (var child : children) {
                if (child.getName().equals(name)) {
                    result.add(child);
                }
            }
        }

        return result;
    }

    /**
     * Get all children by name (recursive).
     *
     * @param name The name of the child (case-sensitive).
     * @return The children with the given name or an empty list if not found.
     */
    public List<GameObject> getAllChildren(String name) {
        List<GameObject> result = new ArrayList<>();
        if (children != null) {
            for (var child : children) {
                if (name.equals(child.getName())) {
                    result.add(child);
                }

                var childResult = child.getAllChildren(name);
                if (childResult != null) {
                    result.addAll(childResult);
                }
            }
        }

        return result;
    }

    /**
     * Get all children by type (recursive).
     *
     * @param type The class type of the child.
     * @param <T>  The class type (assignable from).
     * @return The children with the given type or an empty list if not found.
     */
    public <T> List<T> getAllChildren(Class<T> type) {
        List<T> result = new ArrayList<>();
        if (children != null) {
            for (var child : children) {
                if (type.isAssignableFrom(child.getClass())) {
                    result.add((T) child);
                }

                var childResult = child.getAllChildren(type);
                if (childResult != null) {
                    result.addAll(childResult);
                }
            }
        }

        return result;
    }

    /**
     * Get a direct child by type.
     *
     * @param type The class type of the child.
     * @param <T>  The class type (assignable from).
     * @return The child with the given type or null if not found.
     */
    public <T> T getChild(Class<T> type) {
        if (children != null) {
            for (var child : children) {
                if (type.isAssignableFrom(child.getClass())) {
                    return (T) child;
                }
            }
        }

        return null;
    }

    /**
     * Get all direct children by type
     *
     * @param type The class type of the game component.
     * @param <T>  The class type (assignable from).
     * @return The children with the given type or an empty list if not found.
     */
    public <T> List<T> getChildren(Class<T> type) {
        List<T> result = new ArrayList<>();
        if (children != null) {
            for (var child : children) {
                if (type.isAssignableFrom(child.getClass())) {
                    result.add((T) child);
                }
            }
        }

        return result;
    }

    /**
     * Remove a child from this game object.
     *
     * @param child The child to remove.
     * @return True if the child was removed, false otherwise.
     */
    public boolean removeChild(GameObjectContainer child) {
        if (children != null) {
            boolean removed = children.remove(child);
            if (removed && child.getParent() == this) {
                child.setParent(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the children of this game object.
     *
     * @return The children of this game object.
     */
    public List<GameObject> getChildren() {
        return this.children;
    }


    /**
     * Get the parent of this game object.
     *
     * @return The parent of this game object.
     */
    public GameObjectContainer getParent() {
        return parent;
    }

    /**
     * Set the parent of this game object.
     *
     * @param parent The parent of this game object.
     */
    protected void setParent(GameObjectContainer parent) {
        this.parent = parent;
    }

    /**
     * Detaches itself from its parent (if any).
     *
     * @return The parent of this game object or null if there was no parent.
     */
    public GameObjectContainer detach() {
        if (parent == null) {
            return null;
        }

        var parent = this.parent;
        if (parent.removeChild(this)) {
            return parent;
        }

        return null;
    }

    /**
     * Get the name of this game object.
     *
     * @return The name of this game object.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this game object.
     *
     * @param name The name of this game object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the attribute map of this game object.
     *
     * @return The attribute map of this game object.
     */
    public AttributeMap getAttributeMap() {
        return attributeMap;
    }

    /**
     * Get the disposed flag of this game object.
     *
     * @return The disposed flag of this game object.
     */
    public boolean isDisposed() {
        return disposed;
    }
}
