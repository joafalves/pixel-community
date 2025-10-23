package org.pixel.ext.rune.widget;

import lombok.ToString;
import org.pixel.ext.rune.style.RuneStyle;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;

import static org.pixel.ext.rune.style.StyleProperties.*;

/**
 * Computed box model (CSS-style padding, margin, border).
 * 
 * <p>This class caches all box model calculations for efficient zero-allocation rendering.
 * All bounds rectangles are pre-calculated and reused. Widget implementations access these
 * directly in onRender() after the framework calls ensureBoxComputed().
 * 
 * <p>Box Model Structure (outside to inside):
 * <pre>
 * ┌─────────────────────────────────────┐
 * │ Margin (outside, spacing)           │ ← totalBounds
 * │  ┌───────────────────────────────┐  │
 * │  │ Border (edge)                 │  │ ← borderBounds
 * │  │  ┌─────────────────────────┐  │  │
 * │  │  │ Padding (inside)        │  │  │ ← paddingBounds
 * │  │  │  ┌───────────────────┐  │  │  │
 * │  │  │  │ Content           │  │  │  │ ← contentBounds
 * │  │  │  │ (text/children)   │  │  │  │
 * │  │  │  └───────────────────┘  │  │  │
 * │  │  └─────────────────────────┘  │  │
 * │  └───────────────────────────────┘  │
 * └─────────────────────────────────────┘
 * </pre>
 */
@ToString
public class ComputedBox {
    // Geometry properties from style (affect layout/bounds calculation)
    public float paddingTop, paddingRight, paddingBottom, paddingLeft;
    public float marginTop, marginRight, marginBottom, marginLeft;
    public float borderWidth;
    public float borderRadiusTopLeft;
    public float borderRadiusTopRight;
    public float borderRadiusBottomRight;
    public float borderRadiusBottomLeft;
    public BoxSizing boxSizing;
    
    // Calculated bounds (updated in calculateBounds())
    public final Rectangle totalBounds = new Rectangle();      // Including margin
    public final Rectangle borderBounds = new Rectangle();     // Excluding margin
    public final Rectangle paddingBounds = new Rectangle();    // Excluding margin+border
    public final Rectangle contentBounds = new Rectangle();    // Excluding margin+border+padding
    
    // Computed sizes
    public final Size contentSize = new Size();   // Content only
    public final Size totalSize = new Size();     // Including padding+border+margin
    
    /**
     * Get total horizontal padding (left + right).
     */
    public float paddingHorizontal() { 
        return paddingLeft + paddingRight; 
    }
    
    /**
     * Get total vertical padding (top + bottom).
     */
    public float paddingVertical() { 
        return paddingTop + paddingBottom; 
    }
    
    /**
     * Get total horizontal margin (left + right).
     */
    public float marginHorizontal() { 
        return marginLeft + marginRight; 
    }
    
    /**
     * Get total vertical margin (top + bottom).
     */
    public float marginVertical() { 
        return marginTop + marginBottom; 
    }
    
    /**
     * Get total horizontal border (left + right sides).
     */
    public float borderHorizontal() { 
        return borderWidth * 2; 
    }
    
    /**
     * Get total vertical border (top + bottom sides).
     */
    public float borderVertical() { 
        return borderWidth * 2; 
    }
    
    /**
     * Get border radius for top-left corner.
     */
    public float getEffectiveBorderRadiusTopLeft() {
        return borderRadiusTopLeft;
    }

    /**
     * Get border radius for top-right corner.
     */
    public float getEffectiveBorderRadiusTopRight() {
        return borderRadiusTopRight;
    }

    /**
     * Get border radius for bottom-right corner.
     */
    public float getEffectiveBorderRadiusBottomRight() {
        return borderRadiusBottomRight;
    }

    /**
     * Get border radius for bottom-left corner.
     */
    public float getEffectiveBorderRadiusBottomLeft() {
        return borderRadiusBottomLeft;
    }

    /**
     * Apply style properties to this box.
     * Reads all box model geometry properties from the style and caches them.
     * Visual properties (colors) should be queried directly from style in onRender().
     * Call this when style changes.
     *
     * @param style The computed style to read from
     */
    public void applyStyle(RuneStyle style) {
        this.paddingTop = style.get(PADDING_TOP);
        this.paddingRight = style.get(PADDING_RIGHT);
        this.paddingBottom = style.get(PADDING_BOTTOM);
        this.paddingLeft = style.get(PADDING_LEFT);

        this.marginTop = style.get(MARGIN_TOP);
        this.marginRight = style.get(MARGIN_RIGHT);
        this.marginBottom = style.get(MARGIN_BOTTOM);
        this.marginLeft = style.get(MARGIN_LEFT);

        this.borderWidth = style.get(BORDER_WIDTH);

        // Border radius - individual properties, expanded from BORDER_RADIUS shorthand by RuneStyle
        this.borderRadiusTopLeft = style.get(BORDER_RADIUS_TOP_LEFT);
        this.borderRadiusTopRight = style.get(BORDER_RADIUS_TOP_RIGHT);
        this.borderRadiusBottomRight = style.get(BORDER_RADIUS_BOTTOM_RIGHT);
        this.borderRadiusBottomLeft = style.get(BORDER_RADIUS_BOTTOM_LEFT);

        this.boxSizing = style.get(BOX_SIZING);
    }
}
