package org.pixel.ext.flux;

import org.pixel.math.Matrix4;

/**
 * Context container for Flux GUI system.
 *
 * <p>Stores all context data for a single frame including input state, viewport dimensions,
 * and optional camera transformation. This design keeps the API clean and scalable as new
 * context types are added.
 *
 * <p>Inspired by ImGui's ImGuiIO structure.
 */
public class FluxContext {

    // === Mouse State ===
    private float mouseX;
    private float mouseY;
    private boolean mouseDown; // Currently being used for "pressed" (click edge)
    private boolean mouseHeld; // Continuous hold state for dragging

    // === Keyboard State ===
    private String textInput = "";
    private boolean backspace;

    // === Mouse Wheel ===
    private float mouseWheelDelta;

    // === Viewport ===
    private float viewportWidth;
    private float viewportHeight;

    // === Camera/Transform ===
    private Matrix4 viewMatrix = null; // Optional camera view matrix for world-space GUI

    // === Future: Additional Input ===
    // private boolean mouseRightDown;
    // private boolean[] keys;

    /**
     * Create empty context.
     */
    public FluxContext() {
    }

    /**
     * Create context with mouse data.
     *
     * @param mouseX    Mouse X position
     * @param mouseY    Mouse Y position
     * @param mouseDown Left mouse button state
     */
    public FluxContext(float mouseX, float mouseY, boolean mouseDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseDown = mouseDown;
    }

    // === Fluent Setters ===

    public FluxContext setMouse(float x, float y, boolean down) {
        this.mouseX = x;
        this.mouseY = y;
        this.mouseDown = down;
        return this;
    }

    public FluxContext setMousePosition(float x, float y) {
        this.mouseX = x;
        this.mouseY = y;
        return this;
    }

    public FluxContext setMouseDown(boolean down) {
        this.mouseDown = down;
        return this;
    }

    public FluxContext setMouseHeld(boolean held) {
        this.mouseHeld = held;
        return this;
    }

    public FluxContext setTextInput(String text) {
        this.textInput = text != null ? text : "";
        return this;
    }

    public FluxContext setBackspace(boolean backspace) {
        this.backspace = backspace;
        return this;
    }

    public FluxContext setMouseWheel(float delta) {
        this.mouseWheelDelta = delta;
        return this;
    }

    public FluxContext setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        return this;
    }

    /**
     * Set an optional camera view matrix for world-space GUI rendering.
     * When set, Flux will pass this matrix to Canvas.begin(viewMatrix),
     * allowing GUI elements to be positioned in world space using the camera.
     *
     * @param viewMatrix Camera view-projection matrix (null for screen-space)
     * @return This context for chaining
     */
    public FluxContext setViewMatrix(Matrix4 viewMatrix) {
        this.viewMatrix = viewMatrix;
        return this;
    }

    // === Getters ===

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public boolean isMouseDown() {
        return mouseDown;
    }

    public boolean isMouseHeld() {
        return mouseHeld;
    }

    public String getTextInput() {
        return textInput;
    }

    public boolean isBackspace() {
        return backspace;
    }

    public float getMouseWheelDelta() {
        return mouseWheelDelta;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public Matrix4 getViewMatrix() {
        return viewMatrix;
    }

    /**
     * Reset all context state to defaults.
     * Useful for clearing context between frames if needed.
     */
    public void reset() {
        mouseX = 0;
        mouseY = 0;
        mouseDown = false;
        textInput = "";
        backspace = false;
        mouseWheelDelta = 0;
        viewMatrix = null;
    }
}
