package org.pixel.ext.weaver;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.ext.weaver.style.StyleEngine;
import org.pixel.ext.weaver.style.parser.StyleSheetParser;
import org.pixel.ext.weaver.widget.Widget;
import org.pixel.graphics.render.canvas.Canvas;

import java.util.List;

public class Weaver implements Updatable, Drawable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(Weaver.class);

    private final WeaverContext context;
    private final ContentManager contentManager;
    private final StyleEngine styleEngine;
    private final StyleSheetParser styleSheetParser;
    private final List<Widget> widgets;

    public Weaver(int viewportWidth, int viewportHeight) {
        this.widgets = List.of();
        this.context = WeaverContext.builder()
                .canvas(Canvas.create(viewportWidth, viewportHeight))
                .viewportWidth(viewportWidth)
                .viewportHeight(viewportHeight)
                .build();
        this.styleSheetParser = new StyleSheetParser();
        this.styleEngine = new StyleEngine(); // TODO: apply base style
        this.contentManager = ContentManager.create();
    }

    @Override
    public void update(DeltaTime delta) {
        for (var widget : widgets) {
            if (widget.isEnabled()) {
                widget.update(delta, context);
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        for (var widget : widgets) {
            if (widget.isEnabled()) {
                widget.draw(delta, context);
            }
        }
    }

    @Override
    public void dispose() {
        if (context != null) {
            context.dispose();
        }
        if (contentManager != null) {
            contentManager.dispose();
        }
    }

    public boolean applyStyleFromResources(String resourcePath) {
        var css = contentManager.loadText(resourcePath);
        if (css == null || css.isEmpty()) {
            LOG.warn("Could not load CSS from resource: " + resourcePath);
            return false;
        }
        return applyStyle(css);
    }

    public boolean applyStyle(String css) {
        var styleSheet = styleSheetParser.parse(css);
        if (styleSheet.rules().isEmpty() && styleSheet.variables().isEmpty() && styleSheet.resources().isEmpty()) {
            LOG.warn("Could not apply style: parsed stylesheet is empty or malformed.");
            return false;
        }

        styleEngine.loadStyleSheet(styleSheet);

        return true;
    }

    public void setViewport(int width, int height) {
        context.setViewportWidth(width);
        context.setViewportHeight(height);
        context.getCanvas().setViewport(width, height);
        // TODO: invalidate layout of widgets
    }

}
