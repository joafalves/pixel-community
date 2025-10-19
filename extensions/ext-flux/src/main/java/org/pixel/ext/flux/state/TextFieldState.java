/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.state;

/**
 * State for text field widgets.
 * Stores the current text value and cursor position.
 */
public class TextFieldState extends WidgetState {
    
    private String value = "";
    private int cursorPosition = 0;
    
    /**
     * Create text field state.
     * 
     * @param id Widget ID
     */
    public TextFieldState(String id) {
        super(id);
    }
    
    /**
     * Get the current text value.
     * 
     * @return Text value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the text value.
     * 
     * @param value New text value
     */
    public void setValue(String value) {
        this.value = value;
        this.cursorPosition = Math.min(cursorPosition, value.length());
    }
    
    /**
     * Get the cursor position.
     * 
     * @return Cursor position (0-based index)
     */
    public int getCursorPosition() {
        return cursorPosition;
    }
    
    /**
     * Set the cursor position.
     * 
     * @param position New cursor position
     */
    public void setCursorPosition(int position) {
        this.cursorPosition = Math.max(0, Math.min(position, value.length()));
    }
}
