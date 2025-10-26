package org.pixel.ext.weaver.widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.layout.BoxModel;
import org.pixel.ext.weaver.style.property.StyleProperty;
import org.pixel.ext.weaver.style.Styleable;
import org.pixel.math.Vector2;

import java.util.*;

public abstract class Widget implements Styleable {

    private final Map<String, Object> inlineStyleMap = new HashMap<>();
    private final Set<String> pseudoClasses = new HashSet<>();
    private final Set<String> classes = new HashSet<>();

    @Getter
    private final List<Widget> children = new ArrayList<>();

    @Getter
    private final BoxModel boxModel = new BoxModel();

    @Setter
    @Getter
    private boolean enabled = true;
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private Widget parent;
    @Setter
    private String id;

    private int styleVersion = 0;

    @Override
    public String getStyleId() {
        return id;
    }

    @Override
    public Set<String> getClasses() {
        return classes;
    }

    @Override
    public Set<String> getPseudoClasses() {
        return pseudoClasses;
    }

    @Override
    public int getStyleVersion() {
        return styleVersion;
    }

    @Override
    public Styleable getStyleableParent() {
        return parent;
    }

    /*@Override
    @SuppressWarnings("unchecked")
    public List<Styleable> getStyleableChildren() {
        return (List<Styleable>) (List<?>) children;
    }*/

    @Override
    public Map<String, Object> getInlineStyleMap() {
        return inlineStyleMap;
    }

    /**
     * Updates the widget.
     *
     * @param delta Time since last update
     * @param ctx   Weaver context
     */
    public void update(DeltaTime delta, WeaverContext ctx) {
        for (Widget child : children) {
            if (child.isEnabled()) {
                child.update(delta, ctx);
            }
        }
    }

    /**
     * Draws the widget.
     *
     * @param delta Time since last draw
     * @param ctx   Weaver context
     */
    public void draw(DeltaTime delta, WeaverContext ctx) {
        for (Widget child : children) {
            if (child.isEnabled()) {
                child.draw(delta, ctx);
            }
        }
    }

    /**
     * Adds a child widget.
     * @param child The child widget to add
     */
    public void addChild(Widget child) {
        if (child != null && !children.contains(child)) {
            children.add(child);
            child.setParent(this);
            child.styleVersion++; // Increment style version on addition because of potential inheritance changes
        }
    }

    /**
     * Removes a child widget.
     * @param child The child widget to remove
     */
    public void removeChild(Widget child) {
        if (child != null && children.remove(child)) {
            child.setParent(null);
            child.styleVersion++; // Increment style version on removal because of potential inheritance changes
        }
    }

    /**
     * Detaches this widget from its parent.
     */
    public void detach() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    /**
     * Removes all child widgets of a specific type.
     *
     * @param type The style type of the child widgets to remove
     */
    public void removeChildByType(String type) {
        Iterator<Widget> iterator = children.iterator();
        while (iterator.hasNext()) {
            Widget child = iterator.next();
            if (child.getStyleType().equals(type)) {
                iterator.remove();
                child.setParent(null);
                child.styleVersion++; // Increment style version on removal because of potential inheritance changes
            }
        }
    }

    /**
     * Adds a style class to the widget.
     *
     * @param className The style class to add
     */
    public void addClass(String className) {
        if (this.classes.add(className)) {
            this.styleVersion++;
        }
    }

    /**
     * Removes a style class from the widget.
     *
     * @param className The style class to remove
     */
    public void removeClass(String className) {
        if (this.classes.remove(className)) {
            this.styleVersion++;
        }
    }

    /**
     * Sets an inline style property for the widget.
     *
     * @param property The style property to set
     * @param value    The value of the style property
     * @param <T>      The type of the style property
     */
    public <T> void setInlineStyle(StyleProperty<T> property, T value) {
        this.inlineStyleMap.put(property.name(), value);
        this.styleVersion++;
    }

    /**
     * Gets an inline style property value for the widget.
     *
     * @param property The style property to get
     * @param <T>      The type of the style property
     * @return The value of the style property, or null if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T getInlineStyle(StyleProperty<T> property) {
        return (T) this.inlineStyleMap.get(property.name());
    }

    /**
     * Removes an inline style property from the widget.
     *
     * @param property The style property to remove
     * @param <T>      The type of the style property
     */
    public <T> void removeInlineStyle(StyleProperty<T> property) {
        if (this.inlineStyleMap.remove(property.name()) != null) {
            this.styleVersion++;
        }
    }

    /**
     * Adds a pseudo-class to the widget.
     *
     * @param pseudoClass The pseudo-class to add
     */
    public void addPseudoClass(String pseudoClass) {
        if (this.pseudoClasses.add(pseudoClass)) {
            this.styleVersion++;
        }
    }

    /**
     * Removes a pseudo-class from the widget.
     *
     * @param pseudoClass The pseudo-class to remove
     */
    public void removePseudoClass(String pseudoClass) {
        if (this.pseudoClasses.remove(pseudoClass)) {
            this.styleVersion++;
        }
    }
}
