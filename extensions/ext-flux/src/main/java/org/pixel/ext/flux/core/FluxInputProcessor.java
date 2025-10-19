/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.core;

import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * Handles input event processing and provides semantic helper methods for widget interactions.
 * Centralizes common input patterns like click detection, hover detection, etc.
 */
public class FluxInputProcessor {
    
    // Current frame input state
    private final Vector2 mousePos = new Vector2();
    private boolean mouseClicked; // Pressed this frame (edge detection)
    private boolean mouseHeld; // Continuously held down
    private boolean mouseReleased; // Released this frame (edge detection)
    private float mouseWheelDelta; // Wheel movement this frame
    private String textInput; // Text input this frame
    
    // Previous frame state (for edge detection)
    private boolean mouseHeldPrev;
    
    // Input filtering
    private boolean canProcessMouse = true; // Can be disabled for certain contexts
    
    /**
     * Update input state for new frame.
     * 
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param mousePressed Mouse button pressed this frame
     * @param mouseDown Mouse button held down
     * @param wheelDelta Mouse wheel delta
     * @param textInput Text input this frame
     */
    public void updateFrame(float mouseX, float mouseY, boolean mousePressed, boolean mouseDown, 
                           float wheelDelta, String textInput) {
        this.mousePos.set(mouseX, mouseY);
        this.mouseClicked = mousePressed;
        this.mouseHeld = mouseDown;
        this.mouseReleased = !mouseDown && mouseHeldPrev;
        this.mouseHeldPrev = mouseDown;
        this.mouseWheelDelta = wheelDelta;
        this.textInput = textInput != null ? textInput : "";
        this.canProcessMouse = true; // Reset filter each frame
    }
    
    /**
     * Check if mouse is over a rectangular area.
     * 
     * @param bounds Rectangle bounds to check
     * @return True if mouse is over bounds
     */
    public boolean isMouseOver(Rectangle bounds) {
        return bounds.contains(mousePos);
    }
    
    /**
     * Check if mouse is over a rectangular area.
     * 
     * @param x X position
     * @param y Y position
     * @param width Width
     * @param height Height
     * @return True if mouse is over bounds
     */
    public boolean isMouseOver(float x, float y, float width, float height) {
        return isMouseOver(new Rectangle(x, y, width, height));
    }
    
    /**
     * Check if mouse button was clicked this frame (edge detection).
     * 
     * @return True if clicked this frame
     */
    public boolean isMouseClicked() {
        return mouseClicked && canProcessMouse;
    }
    
    /**
     * Check if mouse button is held down.
     * 
     * @return True if held down
     */
    public boolean isMouseHeld() {
        return mouseHeld && canProcessMouse;
    }
    
    /**
     * Check if mouse button was released this frame (edge detection).
     * 
     * @return True if released this frame
     */
    public boolean isMouseReleased() {
        return mouseReleased && canProcessMouse;
    }
    
    /**
     * Check if an area is hovered (mouse over and can process events).
     * 
     * @param bounds Rectangle bounds to check
     * @return True if hovered
     */
    public boolean isHovered(Rectangle bounds) {
        return isMouseOver(bounds) && canProcessMouse;
    }
    
    /**
     * Check if an area was clicked this frame.
     * 
     * @param bounds Rectangle bounds to check
     * @return True if clicked
     */
    public boolean isClicked(Rectangle bounds) {
        return isHovered(bounds) && mouseClicked;
    }
    
    /**
     * Check if an area is being pressed (hovered + held).
     * 
     * @param bounds Rectangle bounds to check
     * @return True if pressed
     */
    public boolean isPressed(Rectangle bounds) {
        return isHovered(bounds) && mouseHeld;
    }
    
    /**
     * Get mouse wheel delta for this frame.
     * 
     * @return Wheel delta
     */
    public float getMouseWheelDelta() {
        return mouseWheelDelta;
    }
    
    /**
     * Get text input for this frame.
     * 
     * @return Text input string
     */
    public String getTextInput() {
        return textInput;
    }
    
    /**
     * Get current mouse position.
     * 
     * @return Mouse position vector
     */
    public Vector2 getMousePos() {
        return mousePos;
    }
    
    /**
     * Get mouse X position.
     * 
     * @return Mouse X coordinate
     */
    public float getMouseX() {
        return mousePos.getX();
    }
    
    /**
     * Get mouse Y position.
     * 
     * @return Mouse Y coordinate
     */
    public float getMouseY() {
        return mousePos.getY();
    }
    
    /**
     * Set whether mouse events can be processed.
     * Used for filtering events in certain contexts (e.g., panel click-through prevention).
     * 
     * @param canProcess True to allow mouse event processing
     */
    public void setCanProcessMouse(boolean canProcess) {
        this.canProcessMouse = canProcess;
    }
    
    /**
     * Check if mouse events can be processed.
     * 
     * @return True if mouse events can be processed
     */
    public boolean canProcessMouse() {
        return canProcessMouse;
    }
    
    /**
     * Get raw mouse clicked state (ignoring canProcessMouse filter).
     * Use sparingly - prefer isMouseClicked() for normal cases.
     * 
     * @return True if mouse was clicked this frame
     */
    public boolean getMouseClickedRaw() {
        return mouseClicked;
    }
    
    /**
     * Get raw mouse held state (ignoring canProcessMouse filter).
     * Use sparingly - prefer isMouseHeld() for normal cases.
     * 
     * @return True if mouse is held down
     */
    public boolean getMouseHeldRaw() {
        return mouseHeld;
    }
    
    /**
     * Get raw mouse released state (ignoring canProcessMouse filter).
     * Use sparingly - prefer isMouseReleased() for normal cases.
     * 
     * @return True if mouse was released this frame
     */
    public boolean getMouseReleasedRaw() {
        return mouseReleased;
    }
}
