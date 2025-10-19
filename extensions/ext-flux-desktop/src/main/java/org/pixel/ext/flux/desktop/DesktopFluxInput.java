/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.desktop;

import org.pixel.ext.flux.FluxInput;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardCharListener;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;

/**
 * Desktop implementation of FluxInput using LWJGL3 input.
 * 
 * <p>This implementation handles desktop-specific input polling:
 * <ul>
 *   <li>Mouse position and button state via LWJGL3 Mouse</li>
 *   <li>Keyboard text input via character listener</li>
 *   <li>Mouse wheel scrolling</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * // In your game's load() method:
 * DesktopFluxInput fluxInput = new DesktopFluxInput();
 * Flux gui = new Flux(viewportWidth, viewportHeight);
 * gui.setInput(fluxInput);
 * 
 * // In your game's draw() method:
 * gui.begin();  // Clean API - no context needed!
 * // ... GUI code ...
 * gui.end();
 * 
 * // In your game's dispose() method:
 * fluxInput.dispose();
 * </pre>
 */
public class DesktopFluxInput implements FluxInput, KeyboardCharListener {
    
    private final StringBuilder textInputBuffer = new StringBuilder();
    
    /**
     * Create a new desktop input provider.
     */
    public DesktopFluxInput() {
        // Register keyboard character listener for text input
        Keyboard.addCharListener(this);
    }
    
    @Override
    public float getMouseX() {
        return Mouse.getX();
    }
    
    @Override
    public float getMouseY() {
        return Mouse.getY();
    }
    
    @Override
    public boolean isMousePressed() {
        return Mouse.isMouseButtonPressed(MouseButton.LEFT);
    }
    
    @Override
    public boolean isMouseDown() {
        return Mouse.isMouseButtonDown(MouseButton.LEFT);
    }
    
    @Override
    public String getTextInput() {
        // Get accumulated text input and clear buffer for next frame
        String result = textInputBuffer.toString();
        textInputBuffer.setLength(0);
        return result;
    }
    
    @Override
    public boolean isBackspacePressed() {
        return Keyboard.isKeyPressed(KeyboardKey.BACKSPACE);
    }
    
    @Override
    public float getMouseWheelDelta() {
        return Mouse.getWheelDelta();
    }
    
    @Override
    public void onCharacter(char character) {
        // Accumulate character input for this frame
        textInputBuffer.append(character);
    }
    
    @Override
    public void dispose() {
        Keyboard.removeCharListener(this);
    }
}
