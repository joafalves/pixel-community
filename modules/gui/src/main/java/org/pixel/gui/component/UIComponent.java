/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.component;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.gui.common.UIContext;
import org.pixel.gui.model.ComponentState;
import org.pixel.gui.style.Style;
import org.pixel.gui.style.StyleFactory;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.model.BoxSizingType;
import org.pixel.gui.style.model.DisplayType;
import org.pixel.gui.style.model.DisplayUnit;
import org.pixel.gui.style.properties.*;
import org.pixel.math.Rectangle;

import java.util.List;

import static org.pixel.gui.style.StyleUtils.*;

public abstract class UIComponent implements Disposable {

    private String name;
    private String identifier;
    private String styleName;
    private String customStyle;

    private Style style;
    private ComponentState state;
    private List<StyleProperty> styleProperties;
    private List<StyleProperty> directProperties;

    protected SizeStyle sizeStyle;
    protected MarginStyle marginStyle;
    protected PositionStyle positionStyle;
    protected OriginStyle originStyle;
    protected DisplayStyle displayStyle;
    protected BoxSizingStyle boxSizingStyle;
    protected BorderStyle borderStyle;

    private boolean propertiesChanged;

    protected UIComponent parent;
    protected Rectangle bounds;
    protected Float x = null;
    protected Float y = null;
    protected Float width = null;
    protected Float height = null;
    protected UIContext context;

    /**
     * @param identifier
     */
    public UIComponent(String identifier) {
        this.identifier = identifier;
        this.name = identifier;
        this.propertiesChanged = true;
        this.bounds = new Rectangle();
        this.state = ComponentState.ACTIVE;
    }

    /**
     * Get the computed style path (parent back-tracing)
     *
     * @return
     */
    public String getComputedStylePath() {
        var self = identifier + state.toPathString() + (styleName != null ?
                STYLE_CLASS_SEPARATOR + String.join(STYLE_CLASS_SEPARATOR, styleName.split(STYLE_PATH_SEPARATOR)) : "");

        if (parent == null) {
            return self;

        } else {
            return parent.getComputedStylePath() + STYLE_PATH_SEPARATOR + self;
        }
    }

    /**
     * @param max
     * @param unit
     * @return
     */
    protected Float getComputedUnit(float max, DisplayUnit unit) {
        return getComputedUnit(max, 0, unit);
    }

    /**
     * @param max
     * @param origin
     * @param unit
     * @return
     */
    protected Float getComputedUnit(float max, float origin, DisplayUnit unit) {
        if (unit.getType().equals(DisplayUnit.Type.PIXELS) || unit.getType().equals(DisplayUnit.Type.VALUE)) {
            return origin + unit.getValue() * (max > origin ? 1 : -1);

        } else if (unit.getType().equals(DisplayUnit.Type.PERCENTAGE)) {
            return max * (unit.getValue() / 100.f);

        } else if (unit.getType().equals(DisplayUnit.Type.AUTO)) {
            return 0.f; // can't infer any org.pixel.other value at this point
        }

        return null;
    }

    /**
     * @return
     */
    protected float getParentWidth() {
        return parent == null ? context.getViewportWidth() : parent.bounds.getWidth();
    }

    /**
     * @return
     */
    protected float getParentHeight() {
        return parent == null ? context.getViewportHeight() : parent.bounds.getHeight();
    }

    /**
     * @return
     */
    protected float getComputedPositionX() {
        if (x != null) {
            return x;
        }

        if (positionStyle.getRight() != null) {
            return getComputedUnit(0, getParentWidth(), positionStyle.getRight());
        }

        return getComputedUnit(getParentWidth(), positionStyle.getLeft());
    }

    /**
     * @return
     */
    protected float getComputedPositionY() {
        if (y != null) {
            return y;
        }

        if (positionStyle.getBottom() != null) {
            return getComputedUnit(0, getParentHeight(), positionStyle.getBottom());
        }

        return getComputedUnit(getParentHeight(), positionStyle.getTop());
    }

    /**
     * Get the computed bounds (parent back-tracing)
     *
     * @return
     */
    public void updateBounds() {
        float parentWidth = getParentWidth();
        float parentHeight = getParentHeight();

        Float bX = getComputedPositionX();
        Float bY = getComputedPositionY();
        Float bWidth = width != null ? width : getComputedUnit(parentWidth, sizeStyle.getWidth());
        Float bHeight = height != null ? height : getComputedUnit(parentHeight, sizeStyle.getHeight());

        if (marginStyle != null) {
            bX += marginStyle.getLeft();
            bY += marginStyle.getTop();
        }

        if (originStyle.getX().getValue() > 0.0f || originStyle.getY().getValue() > 0.0f) {
            bX -= bWidth * (originStyle.getX().getValue() / 100.f);
            bY -= bHeight * (originStyle.getY().getValue() / 100.f);
        }

        // apply padding:
        if (parent != null) {
            PaddingStyle padding = StyleUtils.getStyleProperty(parent, PaddingStyle.class);

            bX += padding.getLeft();
            bWidth -= padding.getRight() - padding.getLeft();
            bY += padding.getTop();
            bHeight -= padding.getBottom() - padding.getTop();

            if (parent instanceof UIContainer) {
                UIContainer pContainer = (UIContainer) parent;
                bX += pContainer.getXOffset();
                bY += pContainer.getYOffset();
            }
        }

        if (boxSizingStyle.getType().equals(BoxSizingType.BORDER_BOX)) {
            bX += borderStyle.getWidth();
            bY += borderStyle.getWidth();
            bWidth -= borderStyle.getWidth() * 2.f;
            bHeight -= borderStyle.getWidth() * 2.f;
        }

        if (bounds.getX() != bX || bounds.getY() != bY || bounds.getWidth() != bWidth || bounds.getHeight() != bHeight) {
            bounds.set(bX, bY, bWidth, bHeight);
        }
    }

    /**
     * @param style
     */
    public void setStyle(Style style) {
        if (style == null) {
            return;
        }

        this.style = style;
        this.updateStyle();
    }

    /**
     *
     */
    public void updateStyle() {
        if (style == null) {
            return;
        }

        this.styleProperties = StyleUtils.getComputedProperties(this, style);
        this.sizeStyle = getStyleProperty(this, SizeStyle.class);
        this.marginStyle = getStyleProperty(this, MarginStyle.class);
        this.positionStyle = getStyleProperty(this, PositionStyle.class);
        this.originStyle = getStyleProperty(this, OriginStyle.class);
        this.displayStyle = getStyleProperty(this, DisplayStyle.class);
        this.boxSizingStyle = getStyleProperty(this, BoxSizingStyle.class);
        this.borderStyle = StyleUtils.getStyleProperty(this, BorderStyle.class);
        this.propertiesChanged = true;
    }

    /**
     * @param ctx
     * @param delta
     */
    public void update(UIContext ctx, float delta) {
        if (this.style != ctx.getStyle() && ctx.getStyle() != null) {
            this.setStyle(ctx.getStyle());
        }
        if (this.propertiesChanged) {
            this.refresh();
            this.propertiesChanged = false;
        }
    }

    /**
     * Second (and last) update phase of the current frame
     *
     * @param ctx
     * @param delta
     */
    public void lateUpdate(UIContext ctx, float delta) {
        // intentionally empty
    }

    /**
     * Called when properties are changed
     */
    public void refresh() {
        if (this.style == null) {
            return;
        }

        updateBounds();
    }

    /**
     * @param ctx
     * @param delta
     */
    public void draw(UIContext ctx, float delta) {

    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isHidden() {
        if (displayStyle == null) {
            return false;
        }

        return displayStyle.getType() != DisplayType.NONE;
    }

    public UIComponent getParent() {
        return parent;
    }

    public void setParent(UIComponent parent) {
        this.parent = parent;
        this.propertiesChanged = true;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
        this.propertiesChanged = true;
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.propertiesChanged = true;
    }

    /**
     * @param x
     */
    public void setX(float x) {
        this.x = x;
        this.propertiesChanged = true;
    }

    /**
     * @param y
     */
    public void setY(float y) {
        this.y = y;
        this.propertiesChanged = true;
    }

    /**
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.propertiesChanged = true;
    }

    /**
     * @return
     */
    public float getVirtualWidth() {
        if (width != null) {
            return width;
        }

        // TODO: check this!
        if (sizeStyle.getWidth().getType().equals(DisplayUnit.Type.PERCENTAGE)) {
            return bounds.getWidth();

        } else {
            return sizeStyle.getWidth().getValue();
        }
    }

    public float getVirtualHeight() {
        if (height != null) {
            return height;
        }

        // TODO: check this!
        if (sizeStyle.getHeight().getType().equals(DisplayUnit.Type.PERCENTAGE)) {
            return bounds.getHeight();

        } else {
            return sizeStyle.getHeight().getValue();
        }
    }

    /**
     * @return
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return
     */
    public float getHeight() {
        return height;
    }

    /**
     * @param height
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * @return
     */
    public float getX() {
        return x;
    }

    /**
     * @return
     */
    public float getY() {
        return y;
    }

    public List<StyleProperty> getStyleProperties() {
        return styleProperties;
    }

    public String getCustomStyle() {
        return customStyle;
    }

    public void setCustomStyle(String customStyle) {
        this.customStyle = customStyle;
        this.directProperties = StyleFactory.getProperties(customStyle);

        if (this.styleProperties != null && this.style != null) {
            // style already exists, must recalculate:
            this.styleProperties = StyleUtils.getComputedProperties(this, style);

        } else if (this.styleProperties == null) {
            this.styleProperties = directProperties; // no org.pixel.other properties, use this as the main source
        }

        this.propertiesChanged = true;
    }

    public List<StyleProperty> getDirectProperties() {
        return directProperties;
    }

    public UIContext getContext() {
        return context;
    }

    public void setContext(UIContext context) {
        this.context = context;
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {

    }

    public Style getStyle() {
        return style;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isEnabled() {
        return state != ComponentState.DISABLED;
    }

    public void setState(ComponentState state) {
        if (state == this.state) {
            return;
        }

        this.state = state;
        this.updateStyle();
    }

    public ComponentState getState() {
        return state;
    }
}
