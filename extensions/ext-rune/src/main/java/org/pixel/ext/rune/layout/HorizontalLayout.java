package org.pixel.ext.rune.layout;

import lombok.Getter;
import lombok.Setter;
import org.pixel.ext.rune.widget.RuneContainer;
import org.pixel.ext.rune.widget.RuneWidget;
import org.pixel.math.Size;

import java.util.List;

/**
 * Layout that arranges widgets horizontally (left-to-right).
 * 
 * <p>Features:
 * <ul>
 *   <li>Horizontal spacing between widgets</li>
 *   <li>Vertical alignment (TOP, CENTER, BOTTOM, STRETCH)</li>
 *   <li>Horizontal alignment for positioning within extra space</li>
 *   <li>Padding around all edges</li>
 * </ul>
 * 
 * <p>Example:
 * <pre>{@code
 * RuneWidget container = new RuneContainer("panel");
 * HorizontalLayout layout = new HorizontalLayout(10, Alignment.centerLeft());
 * layout.setPaddingLeft(20);
 * layout.setPaddingRight(20);
 * container.setLayout(layout);
 * 
 * container.add(new RuneLabel("label1", "First"));
 * container.add(new RuneLabel("label2", "Second"));
 * container.add(new RuneLabel("label3", "Third"));
 * }</pre>
 */
@Getter
@Setter
public class HorizontalLayout implements RuneLayout {
    
    private float spacing;
    private Alignment alignment;
    
    // Padding
    private float paddingTop = 0;
    private float paddingRight = 0;
    private float paddingBottom = 0;
    private float paddingLeft = 0;
    
    /**
     * Create a horizontal layout with default settings.
     */
    public HorizontalLayout() {
        this(0, Alignment.centerLeft());
    }
    
    /**
     * Create a horizontal layout with spacing.
     * 
     * @param spacing Space between widgets (in pixels)
     */
    public HorizontalLayout(float spacing) {
        this(spacing, Alignment.centerLeft());
    }
    
    /**
     * Create a horizontal layout with spacing and alignment.
     * 
     * @param spacing Space between widgets (in pixels)
     * @param alignment Alignment for positioning widgets
     */
    public HorizontalLayout(float spacing, Alignment alignment) {
        this.spacing = spacing;
        this.alignment = alignment;
    }
    
    /**
     * Set alignment (fluent API alias for setAlignment).
     * 
     * @param alignment The alignment
     * @return This layout for method chaining
     */
    public HorizontalLayout align(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }
    
    /**
     * Set padding for all edges.
     * 
     * @param padding Padding in pixels
     * @return This layout for method chaining
     */
    public HorizontalLayout setPadding(float padding) {
        this.paddingTop = padding;
        this.paddingRight = padding;
        this.paddingBottom = padding;
        this.paddingLeft = padding;
        return this;
    }
    
    /**
     * Set horizontal padding (left and right).
     * 
     * @param horizontal Padding in pixels
     * @return This layout for method chaining
     */
    public HorizontalLayout setPaddingHorizontal(float horizontal) {
        this.paddingLeft = horizontal;
        this.paddingRight = horizontal;
        return this;
    }
    
    /**
     * Set vertical padding (top and bottom).
     * 
     * @param vertical Padding in pixels
     * @return This layout for method chaining
     */
    public HorizontalLayout setPaddingVertical(float vertical) {
        this.paddingTop = vertical;
        this.paddingBottom = vertical;
        return this;
    }
    
    @Override
    public Size measure(RuneWidget container, List<RuneWidget> children, SizeConstraints constraints) {
        if (children.isEmpty()) {
            return new Size(paddingLeft + paddingRight, paddingTop + paddingBottom);
        }
        
        // Calculate available space after padding
        float availableWidth = constraints.getMaxWidth() - paddingLeft - paddingRight;
        float availableHeight = constraints.getMaxHeight() - paddingTop - paddingBottom;
        
        // Create constraints for children (accounting for spacing)
        float totalSpacing = spacing * (children.size() - 1);
        float childAvailableWidth = availableWidth - totalSpacing;
        
        SizeConstraints childConstraints = SizeConstraints.builder()
                .maxWidth(childAvailableWidth)
                .maxHeight(availableHeight)
                .build();
        
        // Measure all children
        float totalWidth = 0;
        float maxHeight = 0;
        
        for (RuneWidget child : children) {
            if (!child.isVisible()) continue;
            
            Size childSize = child.getPreferredSize(childConstraints);
            totalWidth += childSize.getWidth();
            maxHeight = Math.max(maxHeight, childSize.getHeight());
        }
        
        // Add spacing between visible children
        int visibleCount = 0;
        for (RuneWidget child : children) {
            if (child.isVisible()) visibleCount++;
        }
        if (visibleCount > 1) {
            totalWidth += spacing * (visibleCount - 1);
        }
        
        // Add padding
        totalWidth += paddingLeft + paddingRight;
        maxHeight += paddingTop + paddingBottom;
        
        return new Size(totalWidth, maxHeight);
    }
    
    @Override
    public void layout(RuneWidget container, List<RuneWidget> children,
                      float x, float y, float width, float height) {
        if (children.isEmpty()) return;

        // Note: (x, y, width, height) represent the parent's CONTENT bounds in ABSOLUTE coords
        // We need to emit RELATIVE positions for children (relative to parent's content origin)
        // So we work in a 0,0-based coordinate system

        // Calculate layout area (inside padding) - in relative 0,0 space
        float layoutX = paddingLeft;
        float layoutY = paddingTop;
        float layoutWidth = width - paddingLeft - paddingRight;
        float layoutHeight = height - paddingTop - paddingBottom;
        
        // Count visible children
        int visibleCount = 0;
        for (RuneWidget child : children) {
            if (child.isVisible()) visibleCount++;
        }
        if (visibleCount == 0) return;
        
        // Handle horizontal STRETCH: divide width equally among children
        if (alignment.stretchesHorizontally()) {
            float totalSpacing = spacing * (visibleCount - 1);
            float childWidth = (layoutWidth - totalSpacing) / visibleCount;
            float currentX = layoutX;

            for (RuneWidget child : children) {
                if (!child.isVisible()) continue;

                // Get child's natural height
                SizeConstraints childConstraints = SizeConstraints.builder()
                        .maxWidth(childWidth)
                        .maxHeight(layoutHeight)
                        .build();
                Size childSize = child.getPreferredSize(childConstraints);
                float childHeight = childSize.getHeight();

                // Apply vertical alignment (in relative 0,0 space)
                float childY;
                switch (alignment.getVertical()) {
                    case TOP:
                        childY = layoutY;
                        break;
                    case CENTER:
                        childY = layoutY + (layoutHeight - childHeight) / 2;
                        break;
                    case BOTTOM:
                        childY = layoutY + layoutHeight - childHeight;
                        break;
                    case STRETCH:
                        childY = layoutY;
                        childHeight = layoutHeight;
                        break;
                    default:
                        childY = layoutY;
                        break;
                }

                // Set child position using RELATIVE coordinates
                if (child instanceof RuneContainer) {
                    ((RuneContainer) child).setBounds(currentX, childY, childWidth, childHeight);
                } else {
                    child.setPosition(currentX, childY);
                }
                currentX += childWidth + spacing;
            }
            return;
        }
        
        // Normal flow: use natural child sizes
        // Measure children to get their natural sizes
        SizeConstraints childConstraints = SizeConstraints.builder()
                .maxWidth(layoutWidth)
                .maxHeight(layoutHeight)
                .build();

        // Calculate total width of all visible children
        float totalChildrenWidth = 0;
        for (RuneWidget child : children) {
            if (!child.isVisible()) continue;

            Size childSize = child.getPreferredSize(childConstraints);
            totalChildrenWidth += childSize.getWidth();
        }

        // Add spacing between visible children
        if (visibleCount > 1) {
            totalChildrenWidth += spacing * (visibleCount - 1);
        }

        // Calculate starting X position based on horizontal alignment (in relative 0,0 space)
        float currentX = alignment.calculateX(layoutX, layoutWidth, totalChildrenWidth);

        // Position each child
        for (RuneWidget child : children) {
            if (!child.isVisible()) continue;

            Size childSize = child.getPreferredSize(childConstraints);
            float childWidth = childSize.getWidth();
            float childHeight = childSize.getHeight();

            // Apply vertical alignment (in relative 0,0 space)
            float childY;
            switch (alignment.getVertical()) {
                case TOP:
                    childY = layoutY;
                    break;
                case CENTER:
                    childY = layoutY + (layoutHeight - childHeight) / 2;
                    break;
                case BOTTOM:
                    childY = layoutY + layoutHeight - childHeight;
                    break;
                case STRETCH:
                    childY = layoutY;
                    childHeight = layoutHeight;
                    break;
                default:
                    childY = layoutY;
                    break;
            }

            // Set child position using RELATIVE coordinates
            if (child instanceof RuneContainer) {
                ((RuneContainer) child).setBounds(currentX, childY, childWidth, childHeight);
            } else {
                child.setPosition(currentX, childY);
            }

            // Move to next position
            currentX += childWidth + spacing;
        }
    }
}
