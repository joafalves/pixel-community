/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.state;

/**
 * Base class for widget-specific persistent state.
 * 
 * <p>Each widget that needs to maintain state across frames should define
 * a subclass of WidgetState with its specific state fields.
 * 
 * @see org.pixel.ext.flux.state.FluxStateStore
 */
public abstract class WidgetState {
    
    protected final String id;
    
    /**
     * Create widget state with the given ID.
     * 
     * @param id Widget identifier
     */
    protected WidgetState(String id) {
        this.id = id;
    }
    
    /**
     * Get the widget ID this state belongs to.
     * 
     * @return Widget ID
     */
    public String getId() {
        return id;
    }
}
