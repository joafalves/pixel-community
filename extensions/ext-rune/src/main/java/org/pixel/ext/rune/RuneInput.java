/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune;

import org.pixel.ext.rune.input.PointerButton;

/**
 * Input provider interface for Rune GUI system.
 * 
 * <p>Platform-specific implementations provide input state to Rune.
 * This abstraction keeps the Rune extension layer platform-agnostic
 * while allowing desktop, mobile, web, or console implementations.
 * 
 * <p>Uses generic "pointer" terminology instead of "mouse" to support
 * multiple input types: mouse (desktop), touch (mobile), stylus (tablets),
 * gamepad cursor (consoles), etc.
 * 
 * <p>Example desktop implementation:
 * <pre>
 * public class DesktopRuneInput implements RuneInput {
 *     public float getPointerX() {
 *         return Mouse.getX();
 *     }
 *     // ... other methods
 * }
 * </pre>
 * 
 * @see RuneUI#setInput(RuneInput)
 * @see PointerButton
 */
public interface RuneInput {
    
    // === Pointer Position (mouse, touch, stylus, gamepad cursor) ===
    
    /**
     * Get current pointer X position in screen coordinates.
     * Maps to mouse position on desktop, touch position on mobile,
     * stylus position on tablets, etc.
     * 
     * @return Pointer X position
     */
    float getPointerX();
    
    /**
     * Get current pointer Y position in screen coordinates.
     * Maps to mouse position on desktop, touch position on mobile,
     * stylus position on tablets, etc.
     * 
     * @return Pointer Y position
     */
    float getPointerY();
    
    // === Pointer Buttons/Press ===
    
    /**
     * Check if a pointer button is currently held down (level-triggered).
     * Returns true on every frame while the button is down.
     * 
     * <p>Platform mapping:
     * <ul>
     *   <li>Desktop: PRIMARY=left mouse, SECONDARY=right mouse, TERTIARY=middle mouse</li>
     *   <li>Mobile: PRIMARY=touch press, SECONDARY=long press, TERTIARY=two-finger tap</li>
     *   <li>Gamepad: PRIMARY=A button, SECONDARY=B button, TERTIARY=X button</li>
     * </ul>
     * 
     * @param button Button to check
     * @return True if button is currently down
     */
    boolean isPointerDown(PointerButton button);
    
    /**
     * Convenience method to check if primary pointer button is down.
     * Equivalent to {@code isPointerDown(PointerButton.PRIMARY)}.
     * 
     * @return True if primary button is currently down
     */
    default boolean isPointerDown() {
        return isPointerDown(PointerButton.PRIMARY);
    }
    
    // === Scroll/Wheel ===
    
    /**
     * Get scroll delta for this frame.
     * Positive values = scroll up, negative = scroll down.
     * Maps to mouse wheel on desktop, scroll gesture on mobile.
     * Should be cleared after each frame.
     * 
     * @return Scroll delta
     */
    float getScrollDelta();
    
    // === Keyboard Text Input ===
    
    /**
     * Get text input captured this frame.
     * Should return all characters typed since last frame.
     * Useful for text fields.
     * 
     * @return Text input string (empty if none)
     */
    String getTextInput();
    
    // === Keyboard Special Keys ===
    
    /**
     * Check if backspace key was pressed this frame (edge-triggered).
     * 
     * @return True if backspace was pressed
     */
    boolean isBackspacePressed();
    
    /**
     * Check if delete key was pressed this frame (edge-triggered).
     * 
     * @return True if delete was pressed
     */
    boolean isDeletePressed();
    
    /**
     * Check if enter/return key was pressed this frame (edge-triggered).
     * 
     * @return True if enter was pressed
     */
    boolean isEnterPressed();
    
    /**
     * Check if escape key was pressed this frame (edge-triggered).
     * 
     * @return True if escape was pressed
     */
    boolean isEscapePressed();
    
    /**
     * Check if tab key was pressed this frame (edge-triggered).
     * 
     * @return True if tab was pressed
     */
    boolean isTabPressed();
    
    /**
     * Check if left arrow key is down.
     * 
     * @return True if left arrow is down
     */
    boolean isLeftArrowDown();
    
    /**
     * Check if right arrow key is down.
     * 
     * @return True if right arrow is down
     */
    boolean isRightArrowDown();
    
    /**
     * Check if up arrow key is down.
     * 
     * @return True if up arrow is down
     */
    boolean isUpArrowDown();
    
    /**
     * Check if down arrow key is down.
     * 
     * @return True if down arrow is down
     */
    boolean isDownArrowDown();
    
    /**
     * Check if home key was pressed this frame.
     * 
     * @return True if home was pressed
     */
    boolean isHomePressed();
    
    /**
     * Check if end key was pressed this frame.
     * 
     * @return True if end was pressed
     */
    boolean isEndPressed();
    
    /**
     * Check if control/command key is currently held down.
     * 
     * @return True if ctrl/cmd is down
     */
    boolean isControlDown();
    
    /**
     * Check if shift key is currently held down.
     * 
     * @return True if shift is down
     */
    boolean isShiftDown();
    
    /**
     * Check if alt key is currently held down.
     * 
     * @return True if alt is down
     */
    boolean isAltDown();
}
