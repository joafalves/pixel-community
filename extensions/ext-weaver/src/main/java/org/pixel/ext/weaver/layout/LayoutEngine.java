package org.pixel.ext.weaver.layout;

import lombok.Getter;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.ext.weaver.style.property.model.Measurement;
import org.pixel.ext.weaver.style.property.type.BoxSizingType;
import org.pixel.ext.weaver.style.property.type.MeasurementType;
import org.pixel.ext.weaver.style.property.type.PositionType;
import org.pixel.ext.weaver.widget.Widget;
import org.pixel.math.Rectangle;

public class LayoutEngine {

    @Getter
    private Widget rootWidget;

    private boolean needsLayout = true;

    /**
     * Perform layouting if needed.
     *
     * @param ctx The weaver context.
     */
    public void layout(WeaverContext ctx) {
        if (!needsLayout || rootWidget == null) {
            return;
        }

        layoutWidget(rootWidget, ctx);
    }

    private void layoutWidget(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);

        var boxModel = widget.getBoxModel();
        var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
        var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();

        var boxSizing = style.get(StyleProperties.BOX_SIZING);
        var display = style.get(StyleProperties.DISPLAY);
        var top = style.get(StyleProperties.TOP);
        var left = style.get(StyleProperties.LEFT);
        var position = style.get(StyleProperties.POSITION);

        float actualWidth = layoutWidgetWidth(widget, ctx);
        float actualHeight = layoutWidgetHeight(widget, ctx);
        float absoluteX = 0;
        float absoluteY = 0;

        if (position == PositionType.STATIC) {
            // STATIC: Normal flow position (no left/top applied)
            // Calculate flow position based on previous siblings, parent's content box, etc.
            // For now, simplified:
            absoluteX = parentContentBox.getX();
            absoluteY = parentContentBox.getY();
            // TODO: Add logic for flow layout (stacking, previous sibling positions, etc.)

        } else if (position == PositionType.RELATIVE) {
            // RELATIVE: Normal flow position + offset
            // Step 1: Calculate where it WOULD be in normal flow
            float flowX = parentContentBox.getX(); // TODO: actual flow calculation
            float flowY = parentContentBox.getY();

            // Step 2: Apply left/top offset
            float offsetX = resolveMeasurement(left, parentContentBox.getWidth());
            float offsetY = resolveMeasurement(top, parentContentBox.getHeight());

            absoluteX = flowX + offsetX;
            absoluteY = flowY + offsetY;

        } else if (position == PositionType.ABSOLUTE) {
            // ABSOLUTE: Positioned relative to nearest positioned ancestor's PADDING box
            // (or viewport if no positioned ancestor)

            // Find positioned ancestor
            Widget positionedAncestor = findPositionedAncestor(widget, ctx);

            if (positionedAncestor != null) {
                // Position relative to ancestor's PADDING box (not content box!)
                Rectangle ancestorPaddingBox = positionedAncestor.getBoxModel().getPaddingBox();
                float offsetX = resolveMeasurement(left, ancestorPaddingBox.getWidth());
                float offsetY = resolveMeasurement(top, ancestorPaddingBox.getHeight());

                absoluteX = ancestorPaddingBox.getX() + offsetX;
                absoluteY = ancestorPaddingBox.getY() + offsetY;
            } else {
                // No positioned ancestor - use viewport
                float offsetX = resolveMeasurement(left, ctx.getViewport().getWidth());
                float offsetY = resolveMeasurement(top, ctx.getViewport().getHeight());

                absoluteX = ctx.getViewport().getX() + offsetX;
                absoluteY = ctx.getViewport().getY() + offsetY;
            }

        } else if (position == PositionType.FIXED) {
            // FIXED: Always positioned relative to viewport
            float offsetX = resolveMeasurement(left, ctx.getViewport().getWidth());
            float offsetY = resolveMeasurement(top, ctx.getViewport().getHeight());

            absoluteX = ctx.getViewport().getX() + offsetX;
            absoluteY = ctx.getViewport().getY() + offsetY;
        }

        // Update content-box:
        boxModel.getContentBox().setX(absoluteX);
        boxModel.getContentBox().setY(absoluteY);
        boxModel.getContentBox().setWidth(actualWidth);
        boxModel.getContentBox().setHeight(actualHeight);

        // Recursively layout child widgets
        for (Widget child : widget.getChildren()) {
            layoutWidget(child, ctx);
        }
    }

    private Widget findPositionedAncestor(Widget widget, WeaverContext ctx) {
        Widget parent = widget.getParent();
        while (parent != null) {
            var parentPos = ctx.getStyleEngine()
                    .getComputedStyle(parent)
                    .get(StyleProperties.POSITION);

            if (parentPos != PositionType.STATIC) {
                return parent; // Found positioned ancestor
            }
            parent = parent.getParent();
        }
        return null; // No positioned ancestor
    }

    private float layoutWidgetWidth(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);
        var width = style.get(StyleProperties.WIDTH);

        if (width.type() == MeasurementType.AUTO) {
            return widget.getIntrinsicWidth(ctx);
        } else {
            var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
            var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();
            return resolveMeasurement(width, parentContentBox.getWidth());
        }
    }

    private float layoutWidgetHeight(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);
        var height = style.get(StyleProperties.HEIGHT);

        if (height.type() == MeasurementType.AUTO) {
            return widget.getIntrinsicHeight(ctx);
        } else {
            var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
            var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();
            return resolveMeasurement(height, parentContentBox.getHeight());
        }
    }

    /**
     * Set the root widget for layouting.
     *
     * @param rootWidget The root widget.
     */
    public void setRootWidget(Widget rootWidget) {
        this.rootWidget = rootWidget;
        if (rootWidget != null) {
            this.needsLayout = true;
        }
    }

    /**
     * Clear the layout engine state.
     */
    public void clear() {
        this.rootWidget = null;
        this.needsLayout = false; // there is nothing to layout
    }

    private float resolveMeasurement(Measurement measurement, float parentSize) {
        if (measurement == null) {
            return 0;
        }

        return switch (measurement.type()) {
            case PIXEL -> measurement.value();
            case PERCENTAGE -> (measurement.value() / 100.0f) * parentSize;
            case AUTO -> 0; // Auto handling can be implemented as needed
        };
    }
}
