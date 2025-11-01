package org.pixel.ext.weaver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.layout.BoxModel;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.ext.weaver.style.property.StyleProperty;
import org.pixel.ext.weaver.style.Styleable;

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
    @Setter(AccessLevel.PACKAGE)
    @Getter
    private WeaverContext context;
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
     * Gets the intrinsic width of the widget.
     *
     * @return Intrinsic width
     */
    public float getIntrinsicWidth(WeaverContext ctx) {
        return 0;
    }

    /**
     * Gets the intrinsic height of the widget.
     *
     * @return Intrinsic height
     */
    public float getIntrinsicHeight(WeaverContext ctx) {
        return 0;
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
        var styleEngine = ctx.getStyleEngine();
        var style = styleEngine.getComputedStyle(this);
        var overflow = style.get(StyleProperties.OVERFLOW);

        drawBackground(delta, ctx);

        // Apply clipping if overflow is set to hidden or scroll
        if (overflow.hasClipping()) {
            var box = getBoxModel().getContentBox(); // Clip to content box which excludes padding, border, margin
            ctx.getCanvas().save();
            ctx.getCanvas().clipRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
        }

        drawContent(delta, ctx);
        drawChildren(delta, ctx);

        // Restore canvas state if clipping was applied
        if (overflow.hasClipping()) {
            ctx.getCanvas().restore();
        }
    }

    protected abstract void drawContent(DeltaTime delta, WeaverContext ctx);

    protected void drawBackground(DeltaTime delta, WeaverContext ctx) {
        var styleEngine = ctx.getStyleEngine();
        var style = styleEngine.getComputedStyle(this);

        var bgColor = style.get(StyleProperties.BACKGROUND_COLOR);
        if (bgColor == null || bgColor.getAlpha() <= 0) {
            return; // No background to draw
        }

        var borderWidth = style.get(StyleProperties.BORDER_WIDTH);
        var borderTopLeftRadius = style.get(StyleProperties.BORDER_TOP_LEFT_RADIUS);
        var borderTopRightRadius = style.get(StyleProperties.BORDER_TOP_RIGHT_RADIUS);
        var borderBottomLeftRadius = style.get(StyleProperties.BORDER_BOTTOM_LEFT_RADIUS);
        var borderBottomRightRadius = style.get(StyleProperties.BORDER_BOTTOM_RIGHT_RADIUS);
        var borderColor = style.get(StyleProperties.BORDER_COLOR);

        var contentBox = getBoxModel().getBorderBox(); // Background and border are drawn in the border box
        var canvas = ctx.getCanvas();

        var op = canvas.rect(contentBox.getX(), contentBox.getY(), contentBox.getWidth(), contentBox.getHeight())
                .withFill(bgColor); // TODO: support background images, gradients, etc.

        if (borderWidth.value() > 0) {
            op.withStroke(borderWidth.value(), borderColor);
        }

        if (borderTopLeftRadius.value() > 0 || borderTopRightRadius.value() > 0 ||
            borderBottomLeftRadius.value() > 0 || borderBottomRightRadius.value() > 0) {
            op.withRoundedCorners(
                    borderTopLeftRadius.value(),
                    borderTopRightRadius.value(),
                    borderBottomRightRadius.value(),
                    borderBottomLeftRadius.value()
            );
        }

        op.apply();
    }

    protected void drawChildren(DeltaTime delta, WeaverContext ctx) {
        for (Widget child : children) {
            if (child.isEnabled()) {
                child.draw(delta, ctx);
            }
        }
    }

    /**
     * Adds a child widget.
     *
     * @param child The child widget to add
     */
    public void addChild(Widget child) {
        if (child != null && !children.contains(child)) {
            children.add(child);
            child.setParent(this);
            child.setContext(this.context); // Propagate context to child
            child.styleVersion++; // Increment style version on addition because of potential inheritance changes
            propagateContextToDescendants(child); // Ensure all descendants have context
            markLayoutDirty(); // Tree structure changed
        }
    }

    /**
     * Removes a child widget.
     *
     * @param child The child widget to remove
     */
    public void removeChild(Widget child) {
        if (child != null && children.remove(child)) {
            child.setParent(null);
            child.setContext(null); // Clear context
            child.styleVersion++; // Increment style version on removal because of potential inheritance changes
            clearContextFromDescendants(child); // Clear context from all descendants
            markLayoutDirty(); // Tree structure changed
        }
    }

    /**
     * Propagate context to all descendants recursively.
     */
    private void propagateContextToDescendants(Widget widget) {
        for (Widget child : widget.getChildren()) {
            child.setContext(this.context);
            propagateContextToDescendants(child);
        }
    }

    /**
     * Clear context from all descendants recursively.
     */
    private void clearContextFromDescendants(Widget widget) {
        for (Widget child : widget.getChildren()) {
            child.setContext(null);
            clearContextFromDescendants(child);
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
     * Mark this widget for layout recalculation.
     */
    protected void markLayoutDirty() {
        if (context != null) {
            context.getLayoutEngine().markDirty(this);
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
            markLayoutDirty();
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
            markLayoutDirty();
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
        markLayoutDirty();
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
            markLayoutDirty();
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
            markLayoutDirty();
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
            markLayoutDirty();
        }
    }
}
