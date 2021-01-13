/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.component;

import org.pixel.gui.common.UIContext;
import org.pixel.gui.layout.LayoutFactory;
import org.pixel.gui.layout.LayoutHandler;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.model.DisplayType;
import org.pixel.gui.style.properties.DisplayStyle;
import org.pixel.gui.style.properties.FlexStyle;

import java.util.ArrayList;
import java.util.List;

public abstract class UIContainer extends UIBoxComponent {

    private List<UIComponent> children;
    private DisplayStyle displayStyle;
    private FlexStyle flexStyle;
    private LayoutHandler layoutHandler;
    private boolean layoutDirty = false;

    private float xOffset;
    private float yOffset;

    /**
     * Constructor
     *
     * @param identifier
     */
    public UIContainer(String identifier) {
        super(identifier);
        xOffset = 0.f;
        yOffset = 0.f;
    }

    @Override
    public void refresh() {
        super.refresh();

        if (children != null) {
            for (UIComponent child : children) {
                child.refresh();
            }
        }
    }

    @Override
    public void updateStyle() {
        super.updateStyle();

        this.displayStyle = StyleUtils.getStyleProperty(this, DisplayStyle.class);
        this.flexStyle = StyleUtils.getStyleProperty(this, FlexStyle.class, displayStyle.getType().equals(DisplayType.FLEX));
        this.setupLayout();

        if (this.children != null) {
            this.children.forEach(component -> {
                if (component.getStyle() == null) {
                    component.setStyle(getStyle());
                }
                component.updateStyle();
            });
        }

        this.layoutDirty = true;
    }

    /**
     * Second (and last) update phase of the current frame
     *
     * @param ctx
     * @param delta
     */
    @Override
    public void lateUpdate(UIContext ctx, float delta) {
        super.lateUpdate(ctx, delta);

        if (layoutDirty) {
            computeLayout();
        }
    }

    /**
     * Force layout computation
     */
    private void computeLayout() {
        if (layoutDirty && layoutHandler != null && children != null) {
            layoutHandler.computeLayout();
            layoutDirty = false;
        }
    }

    /**
     * Setup layout handler
     */
    private void setupLayout() {
        if (displayStyle.getType() != DisplayType.FLEX || flexStyle == null) {
            return;
        }

        if (layoutHandler == null) {
            layoutHandler = LayoutFactory.createHandler(this);

            // add all existing child elements:
            if (children != null) {
                for (UIComponent child : children) {
                    layoutHandler.componentAdded(child);
                }
            }
        }

        layoutHandler.reload();
    }

    /**
     * @param components
     */
    public void addChild(UIComponent... components) {
        if (this.children == null) {
            // only create the children object when necessary
            this.children = new ArrayList<>();
        }

        for (UIComponent component : components) {
            this.children.add(component);
            component.setParent(this);

            if (getStyle() != null) {
                component.setStyle(getStyle());
            }

            if (layoutHandler != null) {
                layoutHandler.componentAdded(component);
            }
        }
    }

    /**
     * @param component
     * @return
     */
    public boolean removeChild(UIComponent component) {
        if (this.children != null) {
            boolean removed = this.children.remove(component);

            if (layoutHandler != null && removed) {
                layoutHandler.componentRemoved(component);
            }

            return removed;
        }

        return false;
    }

    /**
     * @return
     */
    public List<UIComponent> getChildren() {
        if (this.children == null) {
            return null;
        }

        return this.children;
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        super.dispose();

        if (layoutHandler != null) {
            layoutHandler.dispose();
        }
    }

    public float getXOffset() {
        return xOffset;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }
}
