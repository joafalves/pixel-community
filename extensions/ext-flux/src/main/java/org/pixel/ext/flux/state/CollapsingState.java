/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.state;

/**
 * State for collapsing sections.
 * Stores open/closed state.
 */
public class CollapsingState extends WidgetState {
    
    private boolean open = false;
    private boolean initialized = false;
    
    /**
     * Create collapsing section state.
     * 
     * @param id Section ID
     */
    public CollapsingState(String id) {
        super(id);
    }
    
    /**
     * Check if section is open.
     * 
     * @return True if open
     */
    public boolean isOpen() {
        return open;
    }
    
    /**
     * Set open state.
     * 
     * @param open True to open
     */
    public void setOpen(boolean open) {
        this.open = open;
        this.initialized = true;
    }
    
    /**
     * Check if state has been initialized.
     * 
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
