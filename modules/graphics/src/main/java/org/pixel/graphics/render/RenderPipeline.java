package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A sophisticated, unified rendering pipeline that automatically handles different renderer types.
 * Uses a generic renderer registry system - renderables specify which renderer type they need,
 * and the pipeline provides the appropriate renderer instance.
 * Supports optional frustum culling to skip rendering objects outside the camera view.
 *
 * <p><b>Rendering Strategy:</b></p>
 * The pipeline respects submission order by default - if you mix renderer types, it will:
 * <ol>
 *   <li>Render all consecutive items of the same renderer type in one batch</li>
 *   <li>Switch renderers when the type changes</li>
 *   <li>This preserves visual ordering but may reduce batching efficiency</li>
 * </ol>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * submit(spriteA);        // SpriteBatch begins
 * submit(spriteB);        // Added to same batch
 * submit(shapeC);         // SpriteBatch ends, SdfShapeRenderer renders
 * submit(spriteD);        // NEW SpriteBatch begins (can't batch with A/B!)
 * </pre>
 *
 * <p><b>Performance Tip:</b> Group submissions by renderer type when possible:
 * <pre>
 * // Good - minimal renderer switches
 * submit all sprites first
 * submit all shapes next
 * submit all text last
 *
 * // Bad - constant renderer switching
 * submit(sprite)
 * submit(shape)
 * submit(sprite)
 * submit(shape)
 * </pre>
 *
 * <p>Use {@code setDepth()} to control visual ordering within the same renderer type.</p>
 */
public class RenderPipeline implements Disposable {

    private final List<Renderable<?>> renderQueue;
    private final Comparator<Renderable<?>> depthSorter;
    private final Map<Class<? extends Renderer>, Renderer> renderers;
    private boolean hasDifferentDepths;
    private boolean cullingEnabled;
    private boolean groupByRenderer; // NEW: Optimization flag
    private final ViewFrustum viewFrustum;

    /**
     * Constructor with explicit renderer instances.
     *
     * @param renderers Map of renderer classes to their instances
     */
    public RenderPipeline(Map<Class<? extends Renderer>, Renderer> renderers) {
        this.renderQueue = new ArrayList<>();
        this.depthSorter = Comparator.comparingInt(Renderable::getDepth);
        this.renderers = renderers;
        this.hasDifferentDepths = false;
        this.cullingEnabled = false;
        this.groupByRenderer = false; // Default: preserve submission order
        this.viewFrustum = new ViewFrustum();
    }

    /**
     * Constructor using ServiceProvider for default renderers.
     */
    public RenderPipeline() {
        this(new HashMap<>());
        // Register default renderers from ServiceProvider
        registerRenderer(SpriteBatch.class, SpriteBatch.create());
        registerRenderer(DirectRenderer.class, DirectRenderer.create());
        registerRenderer(SdfTextRenderer.class, SdfTextRenderer.create());
    }

    /**
     * Register a renderer instance for a specific renderer type.
     *
     * @param rendererClass The renderer class
     * @param renderer      The renderer instance
     * @param <R>           The renderer type
     */
    public <R extends Renderer> void registerRenderer(Class<R> rendererClass, R renderer) {
        renderers.put(rendererClass, renderer);
    }

    /**
     * Get a renderer instance for a specific type.
     *
     * @param rendererClass The renderer class
     * @param <R>           The renderer type
     * @return The renderer instance, or null if not registered
     */
    @SuppressWarnings("unchecked")
    public <R extends Renderer> R getRenderer(Class<R> rendererClass) {
        return (R) renderers.get(rendererClass);
    }

    /**
     * Submits a renderable to be rendered in the next render pass.
     *
     * @param renderable The renderable to submit.
     */
    public void submit(Renderable<?> renderable) {
        // Track if we have different depths to optimize sorting
        if (!renderQueue.isEmpty() && !hasDifferentDepths) {
            int firstDepth = renderQueue.get(0).getDepth();
            if (renderable.getDepth() != firstDepth) {
                hasDifferentDepths = true;
            }
        }
        renderQueue.add(renderable);
    }

    /**
     * Renders all submitted renderables to the active render target (screen by default).
     *
     * @param viewMatrix The camera's view-projection matrix.
     */
    public void render(Matrix4 viewMatrix) {
        render(viewMatrix, null);
    }

    /**
     * Renders all submitted renderables to the specified render target.
     *
     * @param viewMatrix The camera's view-projection matrix.
     * @param target     The render target to draw to. If null, renders to the screen.
     */
    @SuppressWarnings("unchecked")
    public void render(Matrix4 viewMatrix, RenderTarget target) {
        if (renderQueue.isEmpty()) {
            return;
        }

        if (target != null) {
            target.begin();
        }

        // Sort all commands by depth (only if needed - optimization!)
        if (hasDifferentDepths) {
            renderQueue.sort(depthSorter);
        }

        // Optional: Group by renderer type for maximum batching efficiency
        if (groupByRenderer) {
            renderQueue.sort(Comparator.comparing(r -> r.getRendererType().getName()));
        }

        // Group renderables by renderer type and render them
        Class<? extends Renderer> currentRendererType = null;
        Renderer currentRenderer = null;
        boolean isBatching = false;

        for (Renderable<?> renderable : renderQueue) {
            // Frustum culling check
            if (cullingEnabled) {
                Rectangle bounds = renderable.getBounds();
                if (bounds != null && !viewFrustum.intersects(bounds)) {
                    continue; // Skip rendering - outside view frustum
                }
            }

            Class<? extends Renderer> rendererType = renderable.getRendererType();

            // Check if we need to switch renderers
            if (currentRendererType == null || !currentRendererType.equals(rendererType)) {
                // End previous renderer if it was batching
                if (isBatching) {
                    ((BatchRenderer) currentRenderer).end();
                    isBatching = false;
                }

                // Switch to new renderer
                currentRendererType = rendererType;
                currentRenderer = renderers.get(rendererType);

                if (currentRenderer == null) {
                    throw new IllegalStateException("Renderer not registered for type: " + rendererType.getName());
                }

                // Start new renderer if it supports batching
                if (currentRenderer instanceof BatchRenderer) {
                    ((BatchRenderer) currentRenderer).begin(viewMatrix);
                    isBatching = true;
                }
            }

            // Render using the appropriate renderer
            ((Renderable<Renderer>) renderable).render(currentRenderer, viewMatrix);
        }

        // End final renderer if it was batching
        if (isBatching) {
            ((BatchRenderer) currentRenderer).end();
        }

        if (target != null) {
            target.end();
        }
    }

    public void setFrustum(float x, float y, float width, float height) {
        viewFrustum.set(x, y, width, height);
    }

    public ViewFrustum getFrustum() {
        return viewFrustum;
    }

    public void setCullingEnabled(boolean enabled) {
        this.cullingEnabled = enabled;
    }

    public boolean isCullingEnabled() {
        return cullingEnabled;
    }

    /**
     * Enable renderer grouping for maximum batching efficiency.
     *
     * <p><b>WARNING:</b> This breaks submission order! Use only when:
     * <ul>
     *   <li>You don't care about visual ordering between different renderer types</li>
     *   <li>You're using depth values to control ordering</li>
     *   <li>Performance is critical (e.g., rendering thousands of objects)</li>
     * </ul>
     *
     * <p>When enabled, all sprites will batch together, all shapes together, etc.,
     * regardless of submission order. This minimizes renderer switches but may
     * cause visual artifacts if you rely on submission order for overlapping.
     *
     * @param enabled True to group by renderer type, false to preserve submission order
     */
    public void setGroupByRenderer(boolean enabled) {
        this.groupByRenderer = enabled;
    }

    public boolean isGroupByRenderer() {
        return groupByRenderer;
    }

    public List<Renderable<?>> getQueue() {
        return renderQueue;
    }

    public void clear() {
        renderQueue.clear();
        hasDifferentDepths = false;
    }

    @Override
    public void dispose() {
        for (Renderer renderer : renderers.values()) {
            if (renderer instanceof Disposable) {
                ((Disposable) renderer).dispose();
            }
        }
        renderers.clear();
    }
}
