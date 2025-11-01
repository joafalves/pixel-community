package org.pixel.ext.weaver;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.ext.weaver.layout.LayoutEngine;
import org.pixel.ext.weaver.style.StyleEngine;
import org.pixel.ext.weaver.style.parser.StyleSheetParser;
import org.pixel.ext.weaver.style.resource.FontResource;
import org.pixel.ext.weaver.style.resource.Resource;
import org.pixel.ext.weaver.style.resource.ResourceStore;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Rectangle;

import java.util.List;

public class Weaver implements Updatable, Drawable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(Weaver.class);

    private final WeaverContext context;
    private final ContentManager contentManager;
    private final StyleSheetParser styleSheetParser;

    private Widget root;

    public Weaver(int viewportWidth, int viewportHeight) {
        this.styleSheetParser = new StyleSheetParser();
        this.contentManager = ContentManager.create();
        this.context = WeaverContext.builder()
                .canvas(Canvas.create(viewportWidth, viewportHeight))
                .viewport(new Rectangle(0, 0, viewportWidth, viewportHeight))
                .styleEngine(new StyleEngine())
                .layoutEngine(new LayoutEngine())
                .fontStore(new ResourceStore<>())
                .build();
    }

    @Override
    public void update(DeltaTime delta) {
        // Call layout engine to update layouts if needed
        context.getLayoutEngine().layout(context);

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

        if (root != null) {
            this.root = root;
            this.root.detach(); // ensure root has no parent
            this.root.setContext(this.context); // Set context on root
            propagateContextToDescendants(this.root); // Propagate to all descendants
            this.context.getLayoutEngine().setRootWidget(root);

        } else {
            this.context.getLayoutEngine().clear();
        }

        return previousRoot;
    }

    /**
     * Propagate context to all descendants recursively.
     */
    private void propagateContextToDescendants(Widget widget) {
        for (Widget child : widget.getChildren()) {
            child.setContext(this.context);
            propagateContextToDescendants(child);
        }
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
        this.context.getStyleEngine().clear();
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

        context.getStyleEngine().loadStyleSheet(styleSheet);

        if (styleSheet.resources() != null && !styleSheet.resources().isEmpty()) {
            loadResources(styleSheet.resources());
        }

        return true;
    }

    /**
     * Sets the viewport size for this Weaver instance.
     *
     * @param width  New viewport width
     * @param height New viewport height
     */
    public void setViewport(int width, int height) {
        context.setViewport(width, height);
        context.getLayoutEngine().markNeedsLayout(); // Trigger layout recalculation on viewport change
    }

    private void loadResources(List<Resource> resources) {
        for (Resource resource : resources) {
            if (resource instanceof FontResource fontResource) {
                LOG.debug("Loading font resource from path: {0}.", fontResource.getPath());

                var font = contentManager.load(fontResource.getPath(), SdfFont.class);
                if (font != null) {
                    context.getFontStore().put(fontResource.getName(), font);
                } else {
                    LOG.warn("Could not load font resource from path: {0}.", fontResource.getPath());
                }
            }
        }
    }
}
