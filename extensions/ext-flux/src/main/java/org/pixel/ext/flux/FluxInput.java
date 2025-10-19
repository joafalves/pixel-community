package org.pixel.ext.flux;

/**
 * Input state container for Flux.
 *
 * <p>Stores all input data for a single frame. This design keeps the API clean
 * and scalable as new input types are added (gamepad, touch, etc.).
 *
 * <p>Inspired by ImGui's ImGuiIO structure.
 */
public class FluxInput {

    // === Mouse State ===
    private float mouseX;
    private float mouseY;
    private boolean mouseDown;

    // === Keyboard State ===
    private String textInput = "";
    private boolean backspace;

    // === Mouse Wheel ===
    private float mouseWheelDelta;

    // === Viewport ===
    private float viewportWidth;
    private float viewportHeight;

    // === Future: Additional Input ===
    // private boolean mouseRightDown;
    // private boolean[] keys;

    /**
     * Create empty input state.
     */
    public FluxInput() {
    }

    /**
     * Create input state with mouse data.
     *
     * @param mouseX    Mouse X position
     * @param mouseY    Mouse Y position
     * @param mouseDown Left mouse button state
     */
    public FluxInput(float mouseX, float mouseY, boolean mouseDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseDown = mouseDown;
    }

    // === Fluent Setters ===

    public FluxInput setMouse(float x, float y, boolean down) {
        this.mouseX = x;
        this.mouseY = y;
        this.mouseDown = down;
        return this;
    }

    public FluxInput setMousePosition(float x, float y) {
        this.mouseX = x;
        this.mouseY = y;
        return this;
    }

    public FluxInput setMouseDown(boolean down) {
        this.mouseDown = down;
        return this;
    }

    public FluxInput setTextInput(String text) {
        this.textInput = text != null ? text : "";
        return this;
    }

    public FluxInput setBackspace(boolean backspace) {
        this.backspace = backspace;
        return this;
    }

    public FluxInput setMouseWheel(float delta) {
        this.mouseWheelDelta = delta;
        return this;
    }

    public FluxInput setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
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

    /**
     * Reset all input state to defaults.
     * Useful for clearing input between frames if needed.
     */
    public void reset() {
        mouseX = 0;
        mouseY = 0;
        mouseDown = false;
        textInput = "";
        backspace = false;
        mouseWheelDelta = 0;
    }
}
