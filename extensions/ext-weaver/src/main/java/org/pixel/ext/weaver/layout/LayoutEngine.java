package org.pixel.ext.weaver.layout;

import lombok.Getter;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.ext.weaver.style.property.model.Measurement;
import org.pixel.ext.weaver.style.property.type.BoxSizingType;
import org.pixel.ext.weaver.widget.Widget;

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
        var top = style.get(StyleProperties.TOP);
        var left = style.get(StyleProperties.LEFT);
        var width = style.get(StyleProperties.WIDTH);
        var height = style.get(StyleProperties.HEIGHT);

        // calculate CONTENT-BOX:


        // Recursively layout child widgets
        for (Widget child : widget.getChildren()) {
            layoutWidget(child, ctx);
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

}
