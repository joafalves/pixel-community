/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.Color;
import org.pixel.ext.rune.core.RuneRenderContext;
import org.pixel.ext.rune.event.RuneMouseEvent;
import org.pixel.ext.rune.style.RuneStyle;
import org.pixel.graphics.render.canvas.TextAlign;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;

import static org.pixel.ext.rune.style.StyleProperties.*;

/**
 * RuneButton - Interactive button widget with hover and press states.
 * 
 * <p>Buttons support auto-sizing by default (sized from text + padding).
 * 
 * <p>Features:
 * <ul>
 *   <li>Hover/Press/Disabled visual states</li>
 *   <li>Theme-based coloring</li>
 *   <li>Automatic sizing from text (default)</li>
 *   <li>Click event support via base class</li>
 *   <li>Rounded corners</li>
 *   <li>Optional icon support (future)</li>
 * </ul>
 * 
 * <p>Example Usage:
 * <pre>
 * RuneButton button = new RuneButton("submit");
 * button.text("Submit")
 *     .setPosition(100, 50)
 *     .setAnchor(Anchor.CENTER)
 *     .onClick(() -> {
 *         System.out.println("Button clicked!");
 *         // Handle submission
 *     });
 * 
 * // Disable button conditionally
 * button.setEnabled(!form.hasErrors());
 * </pre>
 * 
 * @see RuneWidget
 * @see org.pixel.ext.rune.theme.RuneDarkTheme
 */
@Getter
@Setter
public class RuneButton extends RuneWidget {
    
    // === Content ===
    
    private String text = "";
    
    // === Internal State ===
    
    private transient SdfFont cachedFont;  // For text measurement
    private boolean wasPressed = false;    // Track press state for click detection
    
    /**
     * Create a button with an auto-generated ID.
     */
    public RuneButton() {
        this(null);
    }
    
    /**
     * Create a button with the given ID.
     * 
     * @param id Unique widget identifier (null for auto-generated)
     */
    public RuneButton(String id) {
        super(id);
        this.autoSize = true;  // Buttons auto-size by default
    }
    
    @Override
    public String getTypeName() {
        return "button";
    }
    
    // === Fluent API ===
    
    /**
     * Set button text (fluent).
     * 
     * @param text Button text
     * @return This button for chaining
     */
    public RuneButton text(String text) {
        if (text == null) text = "";
        
        if (!this.text.equals(text)) {
            this.text = text;
            markLayoutDirty(); // Text size affects button size
            markDirty();
        }
        return this;
    }
    
    // === Lifecycle ===
    
    @Override
    protected void onRender(RuneRenderContext ctx) {
        // Get computed style
        RuneStyle style = getComputedStyle(ctx);
        
        // Determine colors based on state
        Color bgColor = style.get(BACKGROUND_COLOR);
        Color txtColor = style.get(TEXT_COLOR);
        Color brdColor = style.get(BORDER_COLOR);
        
        // Render background using padding bounds (excludes margin+border stroke)
        ctx.canvas.rect(
            box.paddingBounds.getX(),
            box.paddingBounds.getY(),
            box.paddingBounds.getWidth(),
            box.paddingBounds.getHeight()
        )
        .withFill(bgColor)
        .withRoundedCorners(
            box.getEffectiveBorderRadiusTopLeft(),
            box.getEffectiveBorderRadiusTopRight(),
            box.getEffectiveBorderRadiusBottomRight(),
            box.getEffectiveBorderRadiusBottomLeft()
        )
        .apply();
        
        // Render border if set
        if (box.borderWidth > 0 && brdColor != null) {
            ctx.canvas.rect(
                box.paddingBounds.getX(),
                box.paddingBounds.getY(),
                box.paddingBounds.getWidth(),
                box.paddingBounds.getHeight()
            )
            .withStroke(box.borderWidth, brdColor)
            .withRoundedCorners(
                box.getEffectiveBorderRadiusTopLeft(),
                box.getEffectiveBorderRadiusTopRight(),
                box.getEffectiveBorderRadiusBottomRight(),
                box.getEffectiveBorderRadiusBottomLeft()
            )
            .apply();
        }
        
        // Render text if present (centered in content bounds)
        if (!text.isEmpty()) {
            // Get font from RuneUI registry
            String fontFamily = style.get(FONT_FAMILY);
            Float fontSize = style.get(FONT_SIZE);
            SdfFont font = ctx.ui.getFont(fontFamily);
            
            if (font != null) {
                cachedFont = font; // Cache for measurement
                
                // Calculate center point of content bounds for text positioning
                // With MIDDLE_CENTER alignment, the x/y coordinates represent the center point
                float centerX = box.contentBounds.getX() + box.contentBounds.getWidth() / 2;
                float centerY = box.contentBounds.getY() + box.contentBounds.getHeight() / 2;
                
                var textOp = ctx.canvas.text(text, font, centerX, centerY)
                    .withFill(txtColor)
                    .withAlign(TextAlign.MIDDLE_CENTER);
                
                if (fontSize != null && fontSize > 0) {
                    textOp.withSize(fontSize);
                }
                
                textOp.apply();
            }
        }
    }
    
    @Override
    protected Size measureContent(RuneRenderContext ctx) {
        // Measure text if we have a font
        float textWidth = 0;
        float textHeight = 0;
        
        if (!text.isEmpty()) {
            // Get font and fontSize from style
            RuneStyle style = getComputedStyle(ctx);
            String fontFamily = style.get(FONT_FAMILY);
            Float fontSize = style.get(FONT_SIZE);
            SdfFont font = ctx.ui.getFont(fontFamily);
            
            if (font != null && fontSize != null && fontSize > 0) {
                // Use canvas measurement - accounts for fontSize scale + transform scale
                // This is zero-GC and matches rendering exactly
                Size textSize = ctx.canvas.measureText(text, font, fontSize);
                textWidth = textSize.getWidth();
                textHeight = textSize.getHeight();
            } else {
                // No font available - rough estimate
                textWidth = text.length() * 8;
                textHeight = 16;
            }
        }
        
        // Return content size only (padding will be added by box model)
        // Minimum content size
        float contentWidth = Math.max(textWidth, 48);
        float contentHeight = Math.max(textHeight, 16);
        
        return new Size(contentWidth, contentHeight);
    }
    
    // === Event Handling ===
    
    @Override
    protected boolean onMouseEvent(RuneMouseEvent event) {
        // Let base class fire listeners first
        boolean consumed = super.onMouseEvent(event);
        
        // Don't handle events if disabled
        if (!isEnabled()) {
            return consumed;
        }
        
        Rectangle bounds = getBounds();
        boolean inBounds = bounds.contains(event.getX(), event.getY());
        
        switch (event.getType()) {
            case MOVE:
                // Update hover state
                State newState = inBounds ? State.HOVER : State.NORMAL;
                if (state != newState && state != State.PRESSED) {
                    state = newState;
                    setHovered(state == State.HOVER);  // Sync with pseudo-class system
                    markDirty();
                }
                break;
                
            case PRESS:
                // Button pressed
                if (inBounds && event.getButton() == 0) { // Left button only
                    wasPressed = true;
                    setPressed(true);  // Automatically updates state via base class
                    return true; // Consume event
                }
                break;
                
            case RELEASE:
                // Button released
                if (wasPressed) {
                    wasPressed = false;
                    setPressed(false);  // Automatically updates state via base class
                    setHovered(inBounds);  // Automatically updates state via base class
                    
                    // Click already handled by base class if in bounds
                    return true; // Consume event
                }
                break;
                
            case EXIT:
                // Mouse left widget bounds
                setHovered(false);  // Automatically updates state via base class
                break;
                
            case ENTER:
                // Mouse entered widget bounds
                if (!wasPressed) {
                    setHovered(true);  // Automatically updates state via base class
                }
                break;
        }
        
        return consumed;
    }
}
