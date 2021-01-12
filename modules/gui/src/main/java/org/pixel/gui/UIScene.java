/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui;

import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.gui.common.UIContext;
import org.pixel.gui.component.UIComponent;
import org.pixel.gui.component.UIContainer;
import org.pixel.gui.model.ComponentState;
import org.pixel.input.mouse.Mouse;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class UIScene implements Updatable, Drawable {

    private static final Logger LOG = LoggerFactory.getLogger(UIScene.class);

    private UIContext context;
    private final List<UIComponent> components;
    private final Rectangle intersectionRect;

    /**
     * Constructor
     */
    protected UIScene() {
        this.components = new ArrayList<>();
        this.intersectionRect = new Rectangle();
    }

    /**
     * Update function
     *
     * @param delta
     */
    @Override
    public void update(float delta) {
        if (components.size() > 0) {
            updateRecursive(components, delta, false);
            updateRecursive(components, delta, true);
        }
    }

    /**
     * @param components
     * @param delta
     */
    private void updateRecursive(List<UIComponent> components, float delta, boolean lateUpdate) {
        if (components == null || components.size() == 0) {
            return;
        }

        for (UIComponent child : components) {
            if (!child.isEnabled()) {
                continue;
            }

            // update current child
            if (lateUpdate) {
                child.lateUpdate(context, delta);

            } else {
                child.update(context, delta);
            }

            // update the child children
            if (child instanceof UIContainer) {
                updateRecursive(((UIContainer) child).getChildren(), delta, lateUpdate);
            }
        }
    }

    /**
     * Draw function
     *
     * @param delta
     */
    @Override
    public void draw(float delta) {
        if (components.size() > 0) {
            context.getRenderEngine().begin();
            drawRecursive(components, delta, 0, 0);
            context.getRenderEngine().end();
        }
    }

    /**
     * @param components
     * @param delta
     */
    private void drawRecursive(List<UIComponent> components, float delta, float tx, float ty) {
        if (components == null || components.size() == 0) {
            return;
        }

        RenderEngine2D re = context.getRenderEngine();
        for (UIComponent child : components) {
            // is the child visible?
            if (!isVisible(child)) {
                continue;
            }

            // push rendering engine transformation stack
            if (child instanceof UIContainer) {
                re.push();
            }

            // handle user interaction (this is being executed here because it only makes sense to compute when the
            // component is visible).
            handleUserInteraction(child, tx, ty);

            // draw the child
            //if (child.isDirty()) {
            child.draw(context, delta);
            // }

            Rectangle bounds = child.getBounds();

            if (child instanceof UIContainer) {
                // only container elements can have children, so we optimize the scissor to only be applied when the
                // ui component is a container:
                if (child.getParent() == null) {
                    re.scissor(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

                } else {
                    Rectangle pBounds = child.getParent().getBounds();
                    intersectionRect.set(bounds);
                    intersectionRect.intersection(0, 0, pBounds.getWidth(), pBounds.getHeight());
                    re.scissor(intersectionRect.getX(), intersectionRect.getY(), intersectionRect.getWidth(),
                            intersectionRect.getHeight());
                }

                // translate the whole rendering engine (current stack) to the given child bounds:
                re.translate(bounds.getX(), bounds.getY());

                // draw component children
                drawRecursive(((UIContainer) child).getChildren(), delta, tx + bounds.getX(), ty + bounds.getY());

                // pop rendering engine transformation stack
                re.pop();
            }
        }
    }

    /**
     * Handle user interaction with given component
     *
     * @param component
     * @param tx        current X-axis transform offset
     * @param ty        current Y-axis transform offset
     */
    private void handleUserInteraction(UIComponent component, float tx, float ty) {
        // TODO: probably this doesn't need to run every frame... needs testing
        // is the mouse hovering the component?
        Vector2 mousePos = Mouse.getPosition();
        float mpx = mousePos.getX() * (context.getWindowDimensions().getVirtualWidth() / (float) context.getWindowDimensions().getFrameWidth())
                * context.getWindowDimensions().getPixelRatio();
        float mpy = mousePos.getY() * (context.getWindowDimensions().getVirtualHeight() / (float) context.getWindowDimensions().getFrameHeight())
                * context.getWindowDimensions().getPixelRatio();

        Rectangle bounds = component.getBounds();
        if (Rectangle.intersects(mpx, mpy, 1, 1,
                tx + bounds.getX() - 1, ty + bounds.getY() - 1, bounds.getWidth() + 2, bounds.getHeight() + 2)) {
            // mouse is hovering the component..
            component.setState(ComponentState.HOVER);

        } else {
            component.setState(ComponentState.ACTIVE);
        }
    }

    /**
     * @param component
     * @return
     */
    private boolean isVisible(UIComponent component) {
        if (!component.isEnabled() || !component.isHidden()) {
            return false;
        }

        Rectangle bounds = component.getBounds();

        if (component.getParent() == null) {
            // check with viewport bounds:
            return (Rectangle.intersects(0, 0, context.getViewportWidth(), context.getViewportHeight(),
                    bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));

        } else if (component.getParent() != null && component.getParent().getParent() == null) {
            return Rectangle.intersects(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(),
                    0, 0, component.getParent().getBounds().getWidth(), component.getParent().getBounds().getHeight());

        } else if (component.getParent() != null && component.getParent().getParent() != null) {
            // nested visibility check:
            Rectangle pBounds1 = component.getParent().getBounds();
            Rectangle pBounds2 = component.getParent().getParent().getBounds();
            return Rectangle.intersects(bounds.getX() + pBounds1.getX(), bounds.getY() + pBounds1.getY(), bounds.getWidth(), bounds.getHeight(),
                    0, 0, pBounds2.getWidth(), pBounds2.getHeight());
        }

        return true;
    }

    /**
     * @param components
     */
    public void addChild(UIComponent... components) {
        for (UIComponent component : components) {
            if (context.getStyle() != null) {
                component.setStyle(context.getStyle());
            }
            this.components.add(component);
        }
    }

    /**
     * @param component
     * @return
     */
    public boolean removeChild(UIComponent component) {
        return this.components.remove(component);
    }

    /**
     * @return
     */
    public List<UIComponent> getChildren() {
        return this.components;
    }

    /**
     * @param context
     */
    public void setContext(UIContext context) {
        this.context = context;
    }
}
