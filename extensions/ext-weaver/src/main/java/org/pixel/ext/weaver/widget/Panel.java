package org.pixel.ext.weaver.widget;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.Widget;
import org.pixel.ext.weaver.WeaverContext;

public class Panel extends Widget {

    private final static String STYLE_TYPE = "panel";

    @Override
    public String getStyleType() {
        return STYLE_TYPE;
    }

    @Override
    protected void drawContent(DeltaTime delta, WeaverContext ctx) {

    }
}
