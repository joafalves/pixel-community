/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.component;

import org.pixel.commons.DeltaTime;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.gui.common.UIContext;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.properties.ColorStyle;
import org.pixel.gui.style.properties.FontStyle;
import org.pixel.gui.style.properties.TextStyle;

public class UIButton extends UIBoxComponent {

    public static final String ID = "button";

    private String text;

    private ColorStyle colorStyle;
    private FontStyle fontStyle;
    private TextStyle textStyle;

    /**
     * Constructor
     */
    public UIButton() {
        super(ID);
        this.text = "";
    }

    @Override
    public void updateStyle() {
        super.updateStyle();
        this.colorStyle = StyleUtils.getStyleProperty(this, ColorStyle.class);
        this.fontStyle = StyleUtils.getStyleProperty(this, FontStyle.class);
        this.textStyle = StyleUtils.getStyleProperty(this, TextStyle.class);
    }

    @Override
    public void draw(UIContext ctx, DeltaTime delta) {
        super.draw(ctx, delta);

        RenderEngine2D re = context.getRenderEngine();
        re.setFontName(fontStyle.getFontFamily());
        re.setFontSize(fontStyle.getFontSize());
        re.setTextAlignment(textStyle.getHorizontalAlignment(), textStyle.getVerticalAlignment());
        re.fillColor(colorStyle.getColor());
        re.fillText(text, getBounds().getX(), getBounds().getY() + getBounds().getHeight() * 0.5f, getBounds().getWidth());
    }

    /**
     * Set button text
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

}
