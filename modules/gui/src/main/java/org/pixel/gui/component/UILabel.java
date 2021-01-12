/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.component;

import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.gui.common.UIContext;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.properties.ColorStyle;
import org.pixel.gui.style.properties.FontStyle;
import org.pixel.gui.style.properties.TextStyle;

public class UILabel extends UIBoxComponent {

    public static final String NAME = "label";

    private String text;

    private ColorStyle colorStyleProp;
    private FontStyle fontStyle;
    private TextStyle textStyle;

    /**
     * Constructor
     */
    public UILabel() {
        super(NAME);
        this.text = "";
    }

    /*@Override
    protected FSize getComputedSize() {
        RenderEngine2D re = context.getRenderEngine();
        re.setFontName(fontStyle.getFontFamily());
        re.setFontSize(fontStyle.getFontSize());
        re.setTextAlignment(textStyle.getHorizontalAlignment(), textStyle.getVerticalAlignment());

        return context.getRenderEngine().measureText(org.pixel.text,
                new Vector2(bounds.getX(), bounds.getY() + bounds.getHeight() * 0.5f), bounds.getWidth());
    }*/

    @Override
    public void updateStyle() {
        super.updateStyle();
        colorStyleProp = StyleUtils.getStyleProperty(this, ColorStyle.class);
        fontStyle = StyleUtils.getStyleProperty(this, FontStyle.class);
        textStyle = StyleUtils.getStyleProperty(this, TextStyle.class);
    }

    /**
     * @param ctx
     * @param delta
     */
    @Override
    public void draw(UIContext ctx, float delta) {
        super.draw(ctx, delta);

        RenderEngine2D re = context.getRenderEngine();
        re.setFontName(fontStyle.getFontFamily());
        re.setFontSize(fontStyle.getFontSize());
        re.setTextAlignment(textStyle.getHorizontalAlignment(), textStyle.getVerticalAlignment());
        re.fillColor(colorStyleProp.getColor());
        re.fillText(text, getBounds().getX(), getBounds().getY() + getBounds().getHeight() * 0.5f, getBounds().getWidth());
    }

    /**
     * Set label org.pixel.text
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
}
