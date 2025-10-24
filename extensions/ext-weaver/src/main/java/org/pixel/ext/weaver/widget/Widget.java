package org.pixel.ext.weaver.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.Styleable;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public abstract class Widget implements Updatable, Drawable, Styleable {

    private final Set<String> classes = new HashSet<>();

    private boolean enabled = true;
    private String id;

    @Override
    public String getStyleId() {
        return id;
    }

    @Override
    public Set<String> getClasses() {
        return classes;
    }

    /**
     * Updates the widget.
     *
     * @param delta Time since last update
     * @param ctx   Weaver context
     */
    public abstract void update(DeltaTime delta, WeaverContext ctx);

    /**
     * Draws the widget.
     *
     * @param delta Time since last draw
     * @param ctx   Weaver context
     */
    public abstract void draw(DeltaTime delta, WeaverContext ctx);

    public void addClass(String className) {
        this.classes.add(className);
    }

    public void removeClass(String className) {
        this.classes.remove(className);
    }
}
