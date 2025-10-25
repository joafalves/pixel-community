package org.pixel.ext.weaver;

import lombok.Getter;
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

public class Weaver implements Updatable, Drawable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(Weaver.class);

    private final WeaverContext context;
    private final ContentManager contentManager;
    private final StyleEngine styleEngine;
    private final StyleSheetParser styleSheetParser;

    private Widget root;

    public Weaver(int viewportWidth, int viewportHeight) {
        this.styleSheetParser = new StyleSheetParser();
        this.styleEngine = new StyleEngine(); // TODO: apply base style
        this.contentManager = ContentManager.create();
        this.context = WeaverContext.builder()
                .canvas(Canvas.create(viewportWidth, viewportHeight))
                .viewportWidth(viewportWidth)
                .viewportHeight(viewportHeight)
                .styleEngine(styleEngine)
                .build();
    }

    @Override
    public void update(DeltaTime delta) {
        if (root != null && root.isEnabled()) {
            root.update(delta, context);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        if (root != null && root.isEnabled()) {
            context.getCanvas().begin();
            root.draw(delta, context);
            context.getCanvas().end();
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

    /**
     * Sets the root container widget for this Weaver instance.
     *
     * @param root New root container widget
     * @return Previous root container widget if any, null otherwise
     */
    public Widget setContent(Widget root) {
        final var previousRoot = this.root;
        this.root = root;
        this.root.detach(); // root has no parent

        return previousRoot;
    }

    /**
     * Gets the root container widget for this Weaver instance.
     *
     * @return Root container widget
     */
    public Widget getContent() {
        return this.root;
    }

    /**
     * Clears all applied styles associated to this Weaver instance.
     */
    public void clearStyles() {
        styleEngine.clear();
    }

    /**
     * Applies style from a resource path.
     *
     * @param resourcePath Path to the CSS resource
     * @return True if the style was applied successfully, false otherwise
     */
    public boolean loadStyleSheetFromResources(String resourcePath) {
        var css = contentManager.loadText(resourcePath);
        if (css == null || css.isEmpty()) {
            LOG.warn("Could not load CSS from resource: " + resourcePath);
            return false;
        }
        return loadStyleSheet(css);
    }

    /**
     * Applies style from a CSS string.
     *
     * @param css CSS string
     * @return True if the style was applied successfully, false otherwise
     */
    public boolean loadStyleSheet(String css) {
        var styleSheet = styleSheetParser.parse(css);
        if (styleSheet.rules().isEmpty() && styleSheet.variables().isEmpty() && styleSheet.resources().isEmpty()) {
            LOG.warn("Could not apply style: parsed stylesheet is empty or malformed.");
            return false;
        }

        styleEngine.loadStyleSheet(styleSheet);

        return true;
    }

    /**
     * Sets the viewport size for this Weaver instance.
     *
     * @param width  New viewport width
     * @param height New viewport height
     */
    public void setViewport(int width, int height) {
        context.setViewportWidth(width);
        context.setViewportHeight(height);
        context.getCanvas().setViewport(width, height);
        // TODO: invalidate layout of widgets
    }

}
