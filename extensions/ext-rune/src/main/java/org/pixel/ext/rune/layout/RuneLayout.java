/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.layout;

import org.pixel.ext.rune.widget.RuneWidget;
import org.pixel.math.Size;

import java.util.List;

/**
 * Interface for layout managers that arrange child widgets within a container.
 * 
 * <p>Layout process follows a two-phase approach (similar to Flutter):
 * <ol>
 *   <li><b>Measure</b>: Calculate preferred size based on constraints and children</li>
 *   <li><b>Layout</b>: Position and size children within allocated space</li>
 * </ol>
 * 
 * <p>Example implementations:
 * <ul>
 *   <li>{@code VerticalLayout} - Stack widgets vertically</li>
 *   <li>{@code HorizontalLayout} - Stack widgets horizontally</li>
 *   <li>{@code GridLayout} - Arrange in rows and columns</li>
 *   <li>{@code BorderLayout} - Position at edges (top/bottom/left/right/center)</li>
 * </ul>
 */
public interface RuneLayout {
    
    /**
     * Calculate the preferred size for the container given constraints and children.
     * This is called during the measure phase before positioning children.
     * 
     * @param container The container widget using this layout
     * @param children List of child widgets to lay out
     * @param constraints Size constraints for the container
     * @return Preferred size for the container
     */
    Size measure(RuneWidget container, List<RuneWidget> children, SizeConstraints constraints);
    
    /**
     * Position and size all child widgets within the container.
     * This is called after measure, when the container's size is finalized.
     * 
     * @param container The container widget using this layout
     * @param children List of child widgets to lay out
     * @param x Container's x position
     * @param y Container's y position
     * @param width Container's width
     * @param height Container's height
     */
    void layout(RuneWidget container, List<RuneWidget> children, 
                float x, float y, float width, float height);
}
