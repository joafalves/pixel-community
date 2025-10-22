package org.pixel.ext.rune.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.Color;
import org.pixel.ext.rune.core.RuneRenderContext;
import org.pixel.ext.rune.style.RuneStyle;
import org.pixel.graphics.render.canvas.TextAlign;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Size;

import static org.pixel.ext.rune.style.StyleProperties.*;

/**
 * RuneLabel - Simple text display widget.
 * 
 * <p>Labels support two sizing modes (WinForms-style):
 * <ul>
 *   <li>AutoSize = true (default): Size calculated from text content</li>
 *   <li>AutoSize = false: Use explicit width/height with textAlign positioning</li>
 * </ul>
 * 
 * <p>Text alignment is used when autoSize = false to position text within
 * the fixed bounds. This enables WinForms-style "fill parent + center text" pattern.
 * 
 * <p>Example - Auto-sized (default):
 * <pre>
 * RuneLabel label = new RuneLabel("greeting");
 * label.text("Hello, World!");
 * label.setPosition(100, 50);
 * </pre>
 * 
 * <p>Example - Fixed size with centered text:
 * <pre>
 * RuneLabel label = new RuneLabel("title");
 * label.text("Title");
 * label.setSize(200, 100);
 * label.setAutoSize(false);
 * label.textAlign(TextAlign.middleCenter());
 * </pre>
 */
@Getter
@Setter
public class RuneLabel extends RuneWidget {
    
    private String text = "";
    private Color textColor;  // Explicit override (inline style)
    private TextAlign textAlign = TextAlign.TOP_LEFT;  // Used when autoSize = false
    
    // Cached font for text measurement (set during first render)
    private transient SdfFont cachedFont;
    
    /**
     * Create a label with an auto-generated ID.
     */
    public RuneLabel() {
        this(null);
    }
    
    /**
     * Create a label with the given ID.
     * 
     * @param id Unique identifier (null for auto-generated)
     */
    public RuneLabel(String id) {
        super(id);
        this.autoSize = true;  // Labels auto-size by default
    }
    
    @Override
    public String getTypeName() {
        return "label";
    }
    
    /**
     * Set text and mark dirty (fluent).
     */
    public RuneLabel text(String text) {
        if (text == null) text = "";
        
        if (!this.text.equals(text)) {
            this.text = text;
            markLayoutDirty(); // Text size might change
            markDirty();
        }
        return this;
    }
    
    /**
     * Set text color and mark dirty (fluent).
     * Acts as inline style override.
     */
    public RuneLabel textColor(Color color) {
        if (this.textColor != color) {
            this.textColor = color;
            markDirty();
        }
        return this;
    }
    
    /**
     * Set text alignment for fixed-size labels (fluent).
     * Only used when autoSize = false.
     */
    public RuneLabel textAlign(TextAlign align) {
        if (this.textAlign != align) {
            this.textAlign = align;
            markDirty();
        }
        return this;
    }
    
    @Override
    protected void onRender(RuneRenderContext ctx) {
        if (text.isEmpty()) {
            return; // Nothing to render
        }
        
        // Get computed style (combines type style, classes, ID, inline styles)
        RuneStyle style = getComputedStyle(ctx);
        
        // Get colors from style (already cached in RuneStyle)
        Color backgroundColor = style.get(BACKGROUND_COLOR);
        Color borderColor = style.get(BORDER_COLOR);
        
        // Render background if set (uses padding bounds)
        if (backgroundColor != null) {
            ctx.canvas.rect(
                box.paddingBounds.getX(),
                box.paddingBounds.getY(),
                box.paddingBounds.getWidth(),
                box.paddingBounds.getHeight()
            )
            .withFill(backgroundColor)
            .withRoundedCorners(box.borderRadius)
            .apply();
        }
        
        // Render border if set
        if (box.borderWidth > 0 && borderColor != null) {
            ctx.canvas.rect(
                box.paddingBounds.getX(),
                box.paddingBounds.getY(),
                box.paddingBounds.getWidth(),
                box.paddingBounds.getHeight()
            )
            .withStroke(box.borderWidth, borderColor)
            .withRoundedCorners(box.borderRadius)
            .apply();
        }
        
        // Get text color from style or explicit property
        // Explicit property (textColor field) acts as inline override
        Color color = textColor != null ? textColor : style.get(TEXT_COLOR);
        
        // Get font family and size from style
        String fontFamily = style.get(FONT_FAMILY);
        Float fontSize = style.get(FONT_SIZE);
        
        // Lookup font from RuneUI registry
        SdfFont font = ctx.ui.getFont(fontFamily);
        if (font == null) {
            return; // No font available - cannot render text
        }
        
        // Cache font for text measurement during layout
        cachedFont = font;
        
        // Render text in content bounds (pre-calculated, zero allocation!)
        // When autoSize = false, textAlign positions text within fixed content area
        // When autoSize = true, textAlign is ignored (text determines size)
        var textOp = ctx.canvas.text(text, font, 
            box.contentBounds.getX(), 
            box.contentBounds.getY())
            .withFill(color);
        
        if (fontSize != null && fontSize > 0) {
            textOp.withSize(fontSize);
        }
        
        // Apply text alignment if not auto-sizing
        if (!autoSize) {
            textOp.withAlign(textAlign);
        }
        
        textOp.apply();
    }
    
    @Override
    protected Size measureContent(RuneRenderContext ctx) {
        if (text.isEmpty()) {
            return new Size(0, 0);
        }
        
        // Get font and fontSize from style
        RuneStyle style = getComputedStyle(ctx);
        String fontFamily = style.get(FONT_FAMILY);
        Float fontSize = style.get(FONT_SIZE);
        SdfFont font = ctx.ui.getFont(fontFamily);
        
        if (font != null && fontSize != null && fontSize > 0) {
            // Use canvas measurement - accounts for fontSize scale
            // This matches rendering exactly (includes default letterSpacing)
            return ctx.canvas.measureText(text, font, fontSize);
        }
        
        // Fallback: rough estimate (will be replaced after first render)
        return new Size(text.length() * 8, 16);
    }
}
