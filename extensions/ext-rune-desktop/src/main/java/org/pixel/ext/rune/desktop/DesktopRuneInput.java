/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.desktop;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.ext.rune.RuneInput;
import org.pixel.ext.rune.input.PointerButton;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardCharListener;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;

/**
 * Desktop implementation of RuneInput using LWJGL3 input.
 * 
 * <p>This implementation handles desktop-specific input polling:
 * <ul>
 *   <li>Pointer position via LWJGL3 Mouse (maps to mouse cursor)</li>
 *   <li>Pointer buttons via mouse buttons (PRIMARY=left, SECONDARY=right, TERTIARY=middle)</li>
 *   <li>Scroll via mouse wheel</li>
 *   <li>Keyboard text input via character listener</li>
 *   <li>Keyboard navigation keys</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * // In your game's load() method:
 * DesktopRuneInput runeInput = new DesktopRuneInput();
 * RuneUI gui = new RuneUI(viewportWidth, viewportHeight);
 * gui.setInput(runeInput);
 * 
 * // RuneUI.update() now automatically polls input!
 * // No manual input wiring needed.
 * 
 * // In your game's dispose() method:
 * runeInput.dispose();
 * </pre>
 */
public class DesktopRuneInput implements RuneInput, KeyboardCharListener, Disposable {
    
    private final StringBuilder textInputBuffer = new StringBuilder();
    
    /**
     * Create a new desktop input provider.
     * Automatically registers as keyboard character listener.
     */
    public DesktopRuneInput() {
        // Register keyboard character listener for text input
        Keyboard.addCharListener(this);
    }
    
    @Override
    public float getPointerX() {
        return Mouse.getX();
    }
    
    @Override
    public float getPointerY() {
        return Mouse.getY();
    }
    
    @Override
    public boolean isPointerDown(PointerButton button) {
        switch (button) {
            case PRIMARY:
                return Mouse.isMouseButtonDown(MouseButton.LEFT);
            case SECONDARY:
                return Mouse.isMouseButtonDown(MouseButton.RIGHT);
            case TERTIARY:
                return Mouse.isMouseButtonDown(MouseButton.MIDDLE);
            default:
                return false;
        }
    }
    
    @Override
    public float getScrollDelta() {
        return Mouse.getWheelDelta();
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
    public boolean isDeletePressed() {
        return Keyboard.isKeyPressed(KeyboardKey.DELETE);
    }
    
    @Override
    public boolean isEnterPressed() {
        return Keyboard.isKeyPressed(KeyboardKey.ENTER) || 
               Keyboard.isKeyPressed(KeyboardKey.KP_ENTER);
    }
    
    @Override
    public boolean isEscapePressed() {
        return Keyboard.isKeyPressed(KeyboardKey.ESCAPE);
    }
    
    @Override
    public boolean isTabPressed() {
        return Keyboard.isKeyPressed(KeyboardKey.TAB);
    }
    
    @Override
    public boolean isLeftArrowDown() {
        return Keyboard.isKeyDown(KeyboardKey.LEFT);
    }
    
    @Override
    public boolean isRightArrowDown() {
        return Keyboard.isKeyDown(KeyboardKey.RIGHT);
    }
    
    @Override
    public boolean isUpArrowDown() {
        return Keyboard.isKeyDown(KeyboardKey.UP);
    }
    
    @Override
    public boolean isDownArrowDown() {
        return Keyboard.isKeyDown(KeyboardKey.DOWN);
    }
    
    @Override
    public boolean isHomePressed() {
        return Keyboard.isKeyPressed(KeyboardKey.HOME);
    }
    
    @Override
    public boolean isEndPressed() {
        return Keyboard.isKeyPressed(KeyboardKey.END);
    }
    
    @Override
    public boolean isControlDown() {
        return Keyboard.isKeyDown(KeyboardKey.LEFT_CONTROL) ||
               Keyboard.isKeyDown(KeyboardKey.RIGHT_CONTROL);
    }
    
    @Override
    public boolean isShiftDown() {
        return Keyboard.isKeyDown(KeyboardKey.LEFT_SHIFT) ||
               Keyboard.isKeyDown(KeyboardKey.RIGHT_SHIFT);
    }
    
    @Override
    public boolean isAltDown() {
        return Keyboard.isKeyDown(KeyboardKey.LEFT_ALT) ||
               Keyboard.isKeyDown(KeyboardKey.RIGHT_ALT);
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
