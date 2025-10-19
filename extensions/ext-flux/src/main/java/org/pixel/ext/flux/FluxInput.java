/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux;

/**
 * Input provider interface for Flux GUI system.
 * 
 * <p>Platform-specific implementations provide input state to Flux.
 * This abstraction keeps the Flux extension layer platform-agnostic
 * while allowing desktop, mobile, or web implementations.
 * 
 * <p>Example desktop implementation:
 * <pre>
 * public class DesktopFluxInput implements FluxInput {
 *     public float getMouseX() {
 *         return Mouse.getX();
 *     }
 *     // ... other methods
 * }
 * </pre>
 * 
 * @see Flux#setInput(FluxInput)
 */
public interface FluxInput {
    
    /**
     * Get current mouse X position in screen coordinates.
     * 
     * @return Mouse X position
     */
    float getMouseX();
    
    /**
     * Get current mouse Y position in screen coordinates.
     * 
     * @return Mouse Y position
     */
    float getMouseY();
    
    /**
     * Check if left mouse button was pressed this frame (edge-triggered).
     * Should return true only on the frame the button transitions from up to down.
     * 
     * @return True if mouse was pressed this frame
     */
    boolean isMousePressed();
    
    /**
     * Check if left mouse button is currently held down (level-triggered).
     * Returns true on every frame while the button is down.
     * 
     * @return True if mouse is currently down
     */
    boolean isMouseDown();
    
    /**
     * Get text input captured this frame.
     * Should return all characters typed since last frame.
     * 
     * @return Text input string (empty if none)
     */
    String getTextInput();
    
    /**
     * Check if backspace key was pressed this frame.
     * 
     * @return True if backspace was pressed
     */
    boolean isBackspacePressed();
    
    /**
     * Get mouse wheel delta for this frame.
     * Positive values = scroll up, negative = scroll down.
     * 
     * @return Mouse wheel delta
     */
    float getMouseWheelDelta();
    
    /**
     * Dispose of resources held by this input provider.
     * Called when the input provider is no longer needed.
     */
    void dispose();
}
