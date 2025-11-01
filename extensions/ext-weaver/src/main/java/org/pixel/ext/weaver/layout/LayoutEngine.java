package org.pixel.ext.weaver.layout;

import lombok.Getter;
import org.pixel.ext.weaver.Widget;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.Style;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.ext.weaver.style.property.StyleProperty;
import org.pixel.ext.weaver.style.property.model.Measurement;
import org.pixel.ext.weaver.style.property.type.BoxSizingType;
import org.pixel.ext.weaver.style.property.type.MeasurementType;
import org.pixel.ext.weaver.style.property.type.PositionType;
import org.pixel.math.Rectangle;

import java.util.HashSet;
import java.util.Set;

public class LayoutEngine {

    @Getter
    private Widget rootWidget;

    private final Set<Widget> dirtyWidgets = new HashSet<>();

    /**
     * Perform layouting if needed.
     *
     * @param ctx The weaver context.
     */
    public void layout(WeaverContext ctx) {
        if (dirtyWidgets.isEmpty() || rootWidget == null) {
            return;
        }

        // Simple approach: any dirty widget causes full tree recalculation
        // This handles all dependency cases correctly (child affecting parent, etc.)
        layoutWidget(rootWidget, ctx);
        dirtyWidgets.clear();
    }

    private void layoutWidget(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);
        var boxModel = widget.getBoxModel();

        // Step 1: Calculate content box dimensions (accounts for box-sizing)
        float contentWidth = layoutWidgetWidth(widget, ctx);
        float contentHeight = layoutWidgetHeight(widget, ctx);

        // Step 2: Calculate content box position (absolute viewport coordinates)
        var position = calculatePosition(widget, style, ctx, contentWidth, contentHeight);

        // Step 3: Update content box
        boxModel.getContentBox().set(position.x, position.y, contentWidth, contentHeight);

        // Step 4: Calculate padding, border, and margin boxes
        calculateBoxModelLayers(widget, style, boxModel);

        // Step 5: Recursively layout child widgets
        for (Widget child : widget.getChildren()) {
            layoutWidget(child, ctx);
        }
    }

    /**
     * Calculate the absolute position of a widget's content box.
     */
    private Position calculatePosition(Widget widget, Style style, WeaverContext ctx,
                                       float contentWidth, float contentHeight) {
        var positionType = style.get(StyleProperties.POSITION);
        var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
        var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();

        return switch (positionType) {
            case STATIC -> calculateStaticPosition(parentContentBox);
            case RELATIVE -> calculateRelativePosition(widget, style, parentContentBox);
            case ABSOLUTE -> calculateAbsolutePosition(widget, style, ctx);
            case FIXED -> calculateFixedPosition(style, ctx);
        };
    }

    private Position calculateStaticPosition(Rectangle parentContentBox) {
        // STATIC: Normal flow position (no left/top offset applied)
        // TODO: Implement proper flow layout (block stacking, inline flow, etc.)
        // For now, simplified: place at parent's content box origin
        return new Position(parentContentBox.getX(), parentContentBox.getY());
    }

    private Position calculateRelativePosition(Widget widget, Style style, Rectangle parentContentBox) {
        // RELATIVE: Normal flow position + offset
        // Step 1: Calculate flow position
        Position flowPosition = calculateStaticPosition(parentContentBox);

        // Step 2: Apply left/top offset
        var left = style.get(StyleProperties.LEFT);
        var top = style.get(StyleProperties.TOP);
        float offsetX = resolveMeasurement(left, parentContentBox.getWidth());
        float offsetY = resolveMeasurement(top, parentContentBox.getHeight());

        return new Position(flowPosition.x + offsetX, flowPosition.y + offsetY);
    }

    private Position calculateAbsolutePosition(Widget widget, Style style, WeaverContext ctx) {
        // ABSOLUTE: Positioned relative to nearest positioned ancestor's padding box
        Widget positionedAncestor = findPositionedAncestor(widget, ctx);

        Rectangle containingBox;
        if (positionedAncestor != null) {
            containingBox = positionedAncestor.getBoxModel().getPaddingBox();
        } else {
            containingBox = ctx.getViewport();
        }

        var left = style.get(StyleProperties.LEFT);
        var top = style.get(StyleProperties.TOP);
        float offsetX = resolveMeasurement(left, containingBox.getWidth());
        float offsetY = resolveMeasurement(top, containingBox.getHeight());

        return new Position(containingBox.getX() + offsetX, containingBox.getY() + offsetY);
    }

    private Position calculateFixedPosition(Style style, WeaverContext ctx) {
        Rectangle viewport = ctx.getViewport();
        var left = style.get(StyleProperties.LEFT);
        var top = style.get(StyleProperties.TOP);
        float offsetX = resolveMeasurement(left, viewport.getWidth());
        float offsetY = resolveMeasurement(top, viewport.getHeight());

        return new Position(viewport.getX() + offsetX, viewport.getY() + offsetY);
    }

    /**
     * Calculate padding, border, and margin boxes based on content box.
     */
    private void calculateBoxModelLayers(Widget widget, Style style, BoxModel boxModel) {
        Rectangle contentBox = boxModel.getContentBox();

        // Get spacing values
        float paddingTop = getSpacingValue(style, StyleProperties.PADDING_TOP, StyleProperties.PADDING);
        float paddingRight = getSpacingValue(style, StyleProperties.PADDING_RIGHT, StyleProperties.PADDING);
        float paddingBottom = getSpacingValue(style, StyleProperties.PADDING_BOTTOM, StyleProperties.PADDING);
        float paddingLeft = getSpacingValue(style, StyleProperties.PADDING_LEFT, StyleProperties.PADDING);

        float borderTop = getSpacingValue(style, StyleProperties.BORDER_TOP_WIDTH, StyleProperties.BORDER_WIDTH);
        float borderRight = getSpacingValue(style, StyleProperties.BORDER_RIGHT_WIDTH, StyleProperties.BORDER_WIDTH);
        float borderBottom = getSpacingValue(style, StyleProperties.BORDER_BOTTOM_WIDTH, StyleProperties.BORDER_WIDTH);
        float borderLeft = getSpacingValue(style, StyleProperties.BORDER_LEFT_WIDTH, StyleProperties.BORDER_WIDTH);

        float marginTop = getSpacingValue(style, StyleProperties.MARGIN_TOP, StyleProperties.MARGIN);
        float marginRight = getSpacingValue(style, StyleProperties.MARGIN_RIGHT, StyleProperties.MARGIN);
        float marginBottom = getSpacingValue(style, StyleProperties.MARGIN_BOTTOM, StyleProperties.MARGIN);
        float marginLeft = getSpacingValue(style, StyleProperties.MARGIN_LEFT, StyleProperties.MARGIN);

        // Calculate padding box (content + padding)
        boxModel.getPaddingBox().set(
                contentBox.getX() - paddingLeft,
                contentBox.getY() - paddingTop,
                contentBox.getWidth() + paddingLeft + paddingRight,
                contentBox.getHeight() + paddingTop + paddingBottom
        );

        // Calculate border box (padding + border)
        Rectangle paddingBox = boxModel.getPaddingBox();
        boxModel.getBorderBox().set(
                paddingBox.getX() - borderLeft,
                paddingBox.getY() - borderTop,
                paddingBox.getWidth() + borderLeft + borderRight,
                paddingBox.getHeight() + borderTop + borderBottom
        );

        // Calculate margin box (border + margin)
        Rectangle borderBox = boxModel.getBorderBox();
        boxModel.getMarginBox().set(
                borderBox.getX() - marginLeft,
                borderBox.getY() - marginTop,
                borderBox.getWidth() + marginLeft + marginRight,
                borderBox.getHeight() + marginTop + marginBottom
        );
    }

    /**
     * Get spacing value (padding, border, margin) with fallback to shorthand property.
     */
    private float getSpacingValue(Style style, StyleProperty<Measurement> specific,
                                  StyleProperty<Measurement> shorthand) {
        Measurement value = style.get(specific);
        if (value == null || value.type() == MeasurementType.AUTO) {
            value = style.get(shorthand);
        }
        return value != null ? resolveMeasurement(value, 0) : 0;
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

    /**
     * Calculate content box width, accounting for box-sizing.
     */
    private float layoutWidgetWidth(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);
        var width = style.get(StyleProperties.WIDTH);
        var boxSizing = style.get(StyleProperties.BOX_SIZING);

        float styledWidth;
        boolean isIntrinsic = false;

        if (width.type() == MeasurementType.AUTO) {
            styledWidth = widget.getIntrinsicWidth(ctx);
            isIntrinsic = true; // Intrinsic size is already content width
        } else {
            var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
            var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();
            styledWidth = resolveMeasurement(width, parentContentBox.getWidth());
        }

        // Handle box-sizing ONLY for explicit dimensions, NOT intrinsic
        if (!isIntrinsic && boxSizing == BoxSizingType.BORDER_BOX && styledWidth > 0) {
            // Styled width includes padding + border, subtract them to get content width
            float horizontalPadding = getSpacingValue(style, StyleProperties.PADDING_LEFT, StyleProperties.PADDING)
                    + getSpacingValue(style, StyleProperties.PADDING_RIGHT, StyleProperties.PADDING);
            float horizontalBorder = getSpacingValue(style, StyleProperties.BORDER_LEFT_WIDTH, StyleProperties.BORDER_WIDTH)
                    + getSpacingValue(style, StyleProperties.BORDER_RIGHT_WIDTH, StyleProperties.BORDER_WIDTH);

            return Math.max(0, styledWidth - horizontalPadding - horizontalBorder);
        }

        return styledWidth; // intrinsic or content-box: already content width
    }

    /**
     * Calculate content box height, accounting for box-sizing.
     */
    private float layoutWidgetHeight(Widget widget, WeaverContext ctx) {
        var style = ctx.getStyleEngine().getComputedStyle(widget);
        var height = style.get(StyleProperties.HEIGHT);
        var boxSizing = style.get(StyleProperties.BOX_SIZING);

        float styledHeight;
        boolean isIntrinsic = false;

        if (height.type() == MeasurementType.AUTO) {
            styledHeight = widget.getIntrinsicHeight(ctx);
            isIntrinsic = true; // Intrinsic size is already content height

        } else {
            var parentBoxModel = widget.getParent() != null ? widget.getParent().getBoxModel() : null;
            var parentContentBox = parentBoxModel != null ? parentBoxModel.getContentBox() : ctx.getViewport();
            styledHeight = resolveMeasurement(height, parentContentBox.getHeight());
        }

        // Handle box-sizing ONLY for explicit dimensions, NOT intrinsic
        if (!isIntrinsic && boxSizing == BoxSizingType.BORDER_BOX && styledHeight > 0) {
            // Styled height includes padding + border, subtract them to get content height
            float verticalPadding = getSpacingValue(style, StyleProperties.PADDING_TOP, StyleProperties.PADDING)
                    + getSpacingValue(style, StyleProperties.PADDING_BOTTOM, StyleProperties.PADDING);
            float verticalBorder = getSpacingValue(style, StyleProperties.BORDER_TOP_WIDTH, StyleProperties.BORDER_WIDTH)
                    + getSpacingValue(style, StyleProperties.BORDER_BOTTOM_WIDTH, StyleProperties.BORDER_WIDTH);

            return Math.max(0, styledHeight - verticalPadding - verticalBorder);
        }

        return styledHeight; // intrinsic or content-box: already content height
    }

    /**
     * Set the root widget for layouting.
     *
     * @param rootWidget The root widget.
     */
    public void setRootWidget(Widget rootWidget) {
        this.rootWidget = rootWidget;
        if (rootWidget != null) {
            markNeedsLayout();
        }
    }

    /**
     * Clear the layout engine state.
     */
    public void clear() {
        this.rootWidget = null;
        this.dirtyWidgets.clear();
    }

    /**
     * Mark the entire tree for layout recalculation.
     * Used when viewport changes or root widget changes.
     */
    public void markNeedsLayout() {
        if (rootWidget != null) {
            dirtyWidgets.add(rootWidget);
        }
    }

    /**
     * Mark a specific widget as needing layout recalculation.
     * Called when widget properties change (styles, classes, etc.).
     * Implements Level 2 optimization: propagates upward if parent depends on child size.
     *
     * @param widget The widget to mark dirty.
     */
    public void markDirty(Widget widget) {
        if (widget == null || dirtyWidgets.contains(widget)) {
            return; // Already dirty
        }

        dirtyWidgets.add(widget);

        // Propagate upward if parent might be affected by this widget's size change
        Widget parent = widget.getParent();
        if (parent != null && widget.getContext() != null) {
            WeaverContext context = widget.getContext();
            Style parentStyle = context.getStyleEngine().getComputedStyle(parent);
            Measurement parentWidth = parentStyle.get(StyleProperties.WIDTH);
            Measurement parentHeight = parentStyle.get(StyleProperties.HEIGHT);

            // Parent depends on children if it uses intrinsic sizing (AUTO)
            boolean parentDependsOnChildren =
                    parentWidth.type() == MeasurementType.AUTO ||
                    parentHeight.type() == MeasurementType.AUTO;

            if (parentDependsOnChildren) {
                markDirty(parent); // Recursive upward propagation (will also mark parent's descendants)
                return; // Don't mark descendants again - parent call will handle it
            }
        }

        // Mark all descendants dirty (they depend on this widget's size/position)
        // Only if we didn't propagate upward (to avoid duplicate work)
        markDescendantsDirty(widget);
    }

    /**
     * Mark all descendants of a widget as dirty recursively.
     */
    private void markDescendantsDirty(Widget widget) {
        for (Widget child : widget.getChildren()) {
            if (!dirtyWidgets.contains(child)) {
                dirtyWidgets.add(child);
                markDescendantsDirty(child);
            }
        }
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

    /**
     * Simple record to hold x,y position coordinates.
     */
    private record Position(float x, float y) {
    }
}
