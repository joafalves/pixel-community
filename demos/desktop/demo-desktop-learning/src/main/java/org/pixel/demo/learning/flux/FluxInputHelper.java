/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.flux;

import org.pixel.ext.flux.FluxContext;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardCharListener;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;

/**
 * Helper class for gathering desktop input and creating FluxContext.
 * 
 * <p>This class demonstrates how to properly gather input from platform-specific
 * input sources (Mouse/Keyboard) and create a FluxContext for Flux GUI.
 * 
 * <p>Since Flux is platform-agnostic (in the extensions layer), it cannot directly
 * depend on desktop-specific input classes. This helper bridges that gap for desktop applications.
 */
public class FluxInputHelper implements KeyboardCharListener {
    
    private final StringBuilder textInputBuffer = new StringBuilder();
    private int viewportWidth;
    private int viewportHeight;
    
    public FluxInputHelper(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        
        // Register keyboard character listener for text input
        Keyboard.addCharListener(this);
    }
    
    /**
     * Create a FluxContext with current frame's input state.
     * Call this at the start of your draw loop.
     * 
     * @return FluxContext with gathered input state
     */
    public FluxContext createContext() {
        // Gather mouse state
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();
        boolean mousePressed = Mouse.isMouseButtonPressed(MouseButton.LEFT);
        boolean mouseHeld = Mouse.isMouseButtonDown(MouseButton.LEFT);
        float mouseWheel = Mouse.getWheelDelta();
        
        // Gather keyboard state
        boolean backspace = Keyboard.isKeyPressed(KeyboardKey.BACKSPACE);
        
        // Get accumulated text input and clear buffer
        String textInput = textInputBuffer.toString();
        textInputBuffer.setLength(0);
        
        // Build context
        return new FluxContext(mouseX, mouseY, mousePressed)
                .setMouseHeld(mouseHeld)
                .setTextInput(textInput)
                .setBackspace(backspace)
                .setMouseWheel(mouseWheel)
                .setViewport(viewportWidth, viewportHeight);
    }
    
    /**
     * Update viewport size when window is resized.
     * 
     * @param width New viewport width
     * @param height New viewport height
     */
    public void setViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }
    
    @Override
    public void onCharacter(char character) {
        // Accumulate character input for this frame
        textInputBuffer.append(character);
    }
    
    /**
     * Clean up resources.
     * Should be called when no longer needed.
     */
    public void dispose() {
        Keyboard.removeCharListener(this);
    }
}
