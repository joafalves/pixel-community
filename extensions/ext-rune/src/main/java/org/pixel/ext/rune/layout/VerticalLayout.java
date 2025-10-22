/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.layout;

import lombok.Getter;
import lombok.Setter;
import org.pixel.ext.rune.widget.RuneContainer;
import org.pixel.ext.rune.widget.RuneWidget;
import org.pixel.math.Size;

import java.util.List;

/**
 * Layout that stacks widgets vertically (top to bottom).
 * Similar to CSS flexbox with flex-direction: column.
 * 
 * <p>Features:
 * <ul>
 *   <li>Vertical spacing between widgets</li>
 *   <li>Horizontal alignment (LEFT, CENTER, RIGHT, STRETCH)</li>
 *   <li>Padding around content</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * VerticalLayout layout = new VerticalLayout();
 * layout.setSpacing(10);
 * layout.setAlignment(Alignment.topCenter());
 * layout.setPadding(15, 15, 15, 15);
 * 
 * container.setLayout(layout);
 * </pre>
 */
@Getter
@Setter
public class VerticalLayout implements RuneLayout {
    
    private float spacing = 0;  // Vertical space between widgets
    private Alignment alignment = Alignment.topLeft();
    
    // Padding: top, right, bottom, left
    private float paddingTop = 0;
    private float paddingRight = 0;
    private float paddingBottom = 0;
    private float paddingLeft = 0;
    
    /**
     * Create vertical layout with default settings.
     */
    public VerticalLayout() {
    }
    
    /**
     * Create vertical layout with spacing.
     */
    public VerticalLayout(float spacing) {
        this.spacing = spacing;
    }
    
    /**
     * Create vertical layout with spacing and alignment.
     */
    public VerticalLayout(float spacing, Alignment alignment) {
        this.spacing = spacing;
        this.alignment = alignment;
    }
    
    /**
     * Set alignment (fluent API alias for setAlignment).
     * 
     * @param alignment The alignment
     * @return This layout for method chaining
     */
    public VerticalLayout align(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }
    
    /**
     * Set padding for all sides.
     */
    public VerticalLayout setPadding(float all) {
        this.paddingTop = all;
        this.paddingRight = all;
        this.paddingBottom = all;
        this.paddingLeft = all;
        return this;
    }
    
    /**
     * Set padding for horizontal and vertical.
     */
    public VerticalLayout setPadding(float horizontal, float vertical) {
        this.paddingTop = vertical;
        this.paddingRight = horizontal;
        this.paddingBottom = vertical;
        this.paddingLeft = horizontal;
        return this;
    }
    
    /**
     * Set padding for each side individually.
     */
    public VerticalLayout setPadding(float top, float right, float bottom, float left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        return this;
    }
    
    @Override
    public Size measure(RuneWidget container, List<RuneWidget> children, SizeConstraints constraints) {
        if (children.isEmpty()) {
            // Empty container - just padding
            float width = paddingLeft + paddingRight;
            float height = paddingTop + paddingBottom;
            return constraints.constrain(width, height);
        }
        
        // Measure each child
        float maxChildWidth = 0;
        float totalHeight = 0;
        
        for (int i = 0; i < children.size(); i++) {
            RuneWidget child = children.get(i);
            if (!child.isVisible()) continue;
            
            // Child constraints: unbounded to allow children to report their natural size
            // Parent may clip or scroll if children overflow
            SizeConstraints childConstraints = new SizeConstraints(
                0, 0,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY
            );
            
            // Get child's preferred size
            Size childSize = child.getPreferredSize(childConstraints);
            
            maxChildWidth = Math.max(maxChildWidth, childSize.getWidth());
            totalHeight += childSize.getHeight();
            
            // Add spacing (except after last child)
            if (i < children.size() - 1) {
                totalHeight += spacing;
            }
        }
        
        // Container size = max child width + total height + padding
        float containerWidth = maxChildWidth + paddingLeft + paddingRight;
        float containerHeight = totalHeight + paddingTop + paddingBottom;
        
        return constraints.constrain(containerWidth, containerHeight);
    }
    
    @Override
    public void layout(RuneWidget container, List<RuneWidget> children,
                      float x, float y, float width, float height) {
        if (children.isEmpty()) {
            return;
        }

        // Note: (x, y, width, height) represent the parent's CONTENT bounds in ABSOLUTE coords
        // We need to emit RELATIVE positions for children (relative to parent's content origin)
        // So we work in a 0,0-based coordinate system

        // Calculate layout area (inside padding) - still in relative 0,0 space
        float layoutX = paddingLeft;
        float layoutY = paddingTop;
        float layoutWidth = width - paddingLeft - paddingRight;
        float layoutHeight = height - paddingTop - paddingBottom;

        // Position children vertically starting from top
        float cursorY = layoutY;

        for (RuneWidget child : children) {
            if (!child.isVisible()) continue;

            // Measure child constraints:
            // - STRETCH: fixed width (must fit parent)
            // - NON-STRETCH: unbounded (can overflow parent)
            SizeConstraints childConstraints = alignment.stretchesHorizontally()
                ? SizeConstraints.fixed(layoutWidth, Float.POSITIVE_INFINITY)
                : SizeConstraints.loose(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

            Size childSize = child.getPreferredSize(childConstraints);

            // If alignment stretches horizontally, force layout width
            // Otherwise, use child's preferred width (may overflow parent)
            float childWidth = alignment.stretchesHorizontally()
                ? layoutWidth
                : childSize.getWidth();

            float childHeight = childSize.getHeight();

            // Calculate x position based on alignment (in relative 0,0 space)
            float childX = alignment.calculateX(layoutX, layoutWidth, childWidth);

            // Position child using RELATIVE coordinates (both containers and leaf widgets)
            if (child instanceof RuneContainer) {
                ((RuneContainer) child).setBounds(childX, cursorY, childWidth, childHeight);
            } else {
                child.setPosition(childX, cursorY);
            }

            // Move cursor down
            cursorY += childSize.getHeight() + spacing;
        }
    }
}
