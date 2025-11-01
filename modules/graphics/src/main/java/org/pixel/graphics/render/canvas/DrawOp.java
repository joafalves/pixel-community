/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

/**
 * Base class for all canvas draw operation builders.
 * Provides fluent API with explicit terminal execution and object pooling support.
 * 
 * <p>Design:
 * <ul>
 *   <li>Builders are pooled and reused to minimize GC pressure</li>
 *   <li>Explicit .apply() terminal method required for execution</li>
 *   <li>Batching preserved - delegates to underlying batch renderer</li>
 * </ul>
 * 
 * @param <T> The concrete builder type (for fluent chaining)
 */
public abstract class DrawOp<T extends DrawOp<T>> {
    
    protected final CanvasRenderer canvas;
    protected boolean executed = false;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer this operation belongs to
     */
    protected DrawOp(CanvasRenderer canvas) {
        this.canvas = canvas;
    }
    
    /**
     * Reset builder state for reuse.
     * Called by canvas before returning builder from pool.
     * Subclasses should override to clear their specific state.
     * 
     * @return This builder for chaining
     */
    protected T reset() {
        executed = false;
        return self();
    }
    
    /**
     * Terminal method to execute the draw operation.
     * Call this after configuring all properties to actually render.
     * 
     * <p>Example:
     * <pre>
     * canvas.rect(x, y, w, h)
     *     .withFill(Color.RED)
     *     .apply();  // Executes here
     * </pre>
     * 
     * @return This builder for potential chaining (though execution is complete)
     */
    public T apply() {
        execute();
        return self();
    }
    
    /**
     * Execute the draw operation.
     * Called by apply() or internally after configuring properties.
     * Idempotent - only executes once per reset cycle.
     */
    protected void execute() {
        if (!executed && isReadyToExecute()) {
            executed = true;
            performDraw();
        }
    }
    
    /**
     * Check if builder has minimum required properties to execute.
     * Subclasses can override to enforce property requirements.
     * 
     * @return True if ready to draw
     */
    protected boolean isReadyToExecute() {
        return true; // Default: always ready
    }
    
    /**
     * Perform the actual drawing operation.
     * Subclasses implement this to delegate to canvas/batch renderer.
     */
    protected abstract void performDraw();
    
    /**
     * Type-safe self reference for fluent chaining.
     * 
     * @return This builder cast to concrete type
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}
