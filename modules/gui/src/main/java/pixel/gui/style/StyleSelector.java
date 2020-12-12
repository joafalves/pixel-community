/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
public class StyleSelector {

    private String name;
    private StyleSelector parent;
    private List<StyleProperty> properties;
    private List<StyleSelector> children;

    /**
     * @param component
     */
    public void addProperty(StyleProperty component) {
        if (this.properties == null) {
            // only create the children object when necessary
            this.properties = new ArrayList<>();
        }

        this.properties.add(component);
    }

    /**
     * @param component
     */
    public void addChild(StyleSelector component) {
        if (this.children == null) {
            // only create the children object when necessary
            this.children = new ArrayList<>();
        }

        this.children.add(component);

        component.setParent(this);
    }

    public int getChildrenCount() {
        return this.children.size();
    }
}
