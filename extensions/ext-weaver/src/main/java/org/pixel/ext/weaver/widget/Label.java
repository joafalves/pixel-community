package org.pixel.ext.weaver.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.Widget;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.graphics.render.canvas.TextAlign;

public class Label extends Widget {

    private final static String STYLE_TYPE = "label";

    @Getter
    @Setter
    private String text;

    @Override
    public void drawContent(DeltaTime delta, WeaverContext ctx) {
        var styleEngine = ctx.getStyleEngine();
        var style = styleEngine.getComputedStyle(this);

        var fontName = style.get(StyleProperties.FONT_FAMILY);
        var fontSize = style.get(StyleProperties.FONT_SIZE);
        var outlineColor = style.get(StyleProperties.OUTLINE_COLOR);
        var outlineWidth = style.get(StyleProperties.OUTLINE_WIDTH);
        var color = style.get(StyleProperties.COLOR);

        var font = ctx.getFontStore().get(fontName);
        if (font == null) {
            return; // TODO: check if this is the best way to handle missing font
        }

        var box = getBoxModel();
        var canvas = ctx.getCanvas();

        var op = canvas.text(text, font, box.getContentBox().getX(), box.getContentBox().getY() + box.getContentBox().getHeight())
                .withAlign(TextAlign.BOTTOM_LEFT)
                .withSize(fontSize.value())
                .withFill(color);

        if (outlineWidth.value() > 0) {
            op.withStroke(outlineColor, outlineWidth.value());
        }

        op.apply();
    }

    @Override
    public String getStyleType() {
        return STYLE_TYPE;
    }

    @Override
    public float getIntrinsicWidth(WeaverContext ctx) {
        var styleEngine = ctx.getStyleEngine();
        var style = styleEngine.getComputedStyle(this);

        var fontName = style.get(StyleProperties.FONT_FAMILY);
        var fontSize = style.get(StyleProperties.FONT_SIZE);
        var font = ctx.getFontStore().get(fontName);
        if (font == null) {
            return 0;
        }

        // TODO: support a more efficient measurement (without GC issues)...
        // TODO: support letter spacing...
        return ctx.getCanvas().measureText(text, font, fontSize.value()).getWidth();
    }

    @Override
    public float getIntrinsicHeight(WeaverContext ctx) {
        var styleEngine = ctx.getStyleEngine();
        var style = styleEngine.getComputedStyle(this);

        var fontName = style.get(StyleProperties.FONT_FAMILY);
        var fontSize = style.get(StyleProperties.FONT_SIZE);
        var font = ctx.getFontStore().get(fontName);
        if (font == null) {
            return 0;
        }

        // TODO: support a more efficient measurement (without GC issues)...
        // TODO: support line spacing...
        return ctx.getCanvas().measureText(text, font, fontSize.value()).getHeight();
    }
}
