package org.pixel.ext.rune.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pixel.ext.rune.RuneUI;
import org.pixel.ext.rune.style.RuneStyleSheet;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.math.Rectangle;

/**
 * Rendering context passed to widgets during render phase.
 * Contains all necessary information for rendering.
 */
@Getter
@AllArgsConstructor
public class RuneRenderContext {
    
    /**
     * Canvas for drawing operations.
     */
    private final Canvas canvas;
    
    /**
     * Current stylesheet for styling.
     */
    private final RuneStyleSheet styleSheet;
    
    /**
     * Reference to RuneUI for font lookup and other utilities.
     */
    private final RuneUI ui;
    
    /**
     * Current clip bounds (null = no clipping).
     */
    private Rectangle clipBounds;
}
