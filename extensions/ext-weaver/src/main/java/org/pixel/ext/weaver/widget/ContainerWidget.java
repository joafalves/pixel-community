package org.pixel.ext.weaver.widget;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.Styleable;

import java.util.List;
import java.util.Set;

public class ContainerWidget extends Widget {

    private static final String STYLE_TYPE = "container";

    @Override
    public void update(DeltaTime delta, WeaverContext ctx) {

    }

    @Override
    public void draw(DeltaTime delta, WeaverContext ctx) {

    }

    @Override
    public String getStyleType() {
        return STYLE_TYPE;
    }

    @Override
    public Set<String> getPseudoClasses() {
        return Set.of();
    }

    @Override
    public Styleable getStyleableParent() {
        return null;
    }

    @Override
    public List<Styleable> getStyleableChildren() {
        return List.of();
    }
}
