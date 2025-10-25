package org.pixel.ext.weaver.layout;

import lombok.Getter;
import lombok.Setter;
import org.pixel.ext.weaver.widget.Widget;

public class LayoutEngine {

    @Getter
    private Widget rootWidget;

    public LayoutEngine() {

    }

    public void setRootWidget(Widget rootWidget) {
        this.rootWidget = rootWidget;
    }

}
