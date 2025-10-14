package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A sophisticated, unified rendering pipeline that automatically handles batched and direct rendering.
 * Supports optional frustum culling to skip rendering objects outside the camera view.
 */
public class RenderPipeline implements Disposable {

    private final List<Renderable> renderQueue;
    private final Comparator<Renderable> depthSorter;
    private final SpriteBatch spriteBatch;
    private final DirectRenderer directRenderer;
    private boolean hasDifferentDepths;
    private boolean cullingEnabled;
    private final ViewFrustum viewFrustum;

    /**
     * Constructor.
     *
     * @param spriteBatch  The sprite batcher to use for batched commands.
     * @param directRenderer The direct renderer to use for custom shader commands.
     */
    public RenderPipeline(SpriteBatch spriteBatch, DirectRenderer directRenderer) {
        this.renderQueue = new ArrayList<>();
        this.depthSorter = Comparator.comparingInt(Renderable::getDepth);
        this.spriteBatch = spriteBatch;
        this.directRenderer = directRenderer;
        this.hasDifferentDepths = false;
        this.cullingEnabled = false;
        this.viewFrustum = new ViewFrustum();
    }

    /**
     * Constructor.
     */
    public RenderPipeline() {
        this(ServiceProvider.get(SpriteBatch.class), ServiceProvider.get(DirectRenderer.class));
    }

    /**
     * Submits a renderable to be rendered in the next render pass.
     *
     * @param renderable The renderable to submit.
     */
    public void submit(Renderable renderable) {
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

        // Iterate and render, switching between batched and direct rendering as needed
        boolean isBatching = false;
        
        for (Renderable renderable : renderQueue) {
            // Frustum culling check
            if (cullingEnabled) {
                Rectangle bounds = renderable.getBounds();
                if (bounds != null && !viewFrustum.intersects(bounds)) {
                    continue; // Skip rendering - outside view frustum
                }
            }
            
            if (renderable.canBatch()) {
                // This renderable can be batched
                if (!isBatching) {
                    spriteBatch.begin(viewMatrix);
                    isBatching = true;
                }
                renderable.renderBatched(spriteBatch);

            } else {
                // This renderable needs direct rendering (custom shader)
                if (isBatching) {
                    spriteBatch.end();
                    isBatching = false;
                }
                renderable.renderDirect(directRenderer, viewMatrix);
            }
        }

        // If we were still batching, flush the final batch
        if (isBatching) {
            spriteBatch.end();
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

    public List<Renderable> getQueue() {
        return renderQueue;
    }

    public void clear() {
        renderQueue.clear();
        hasDifferentDepths = false;
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (directRenderer != null) {
            directRenderer.dispose();
        }
    }
}
