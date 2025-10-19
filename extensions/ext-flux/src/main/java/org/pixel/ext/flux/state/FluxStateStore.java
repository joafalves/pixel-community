/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized state management for Flux GUI widgets.
 * 
 * <p>Manages persistent widget state across frames, including:
 * <ul>
 *   <li>Widget-specific state (text fields, checkboxes, etc.)</li>
 *   <li>Panel state (scroll, position, collapsed)</li>
 *   <li>Focus tracking (which widget has keyboard focus)</li>
 *   <li>Active widget (currently being clicked)</li>
 *   <li>Hovered widget (mouse over)</li>
 * </ul>
 * 
 * <p>This centralized approach makes state management predictable and testable.
 * 
 * @see WidgetState
 * @see PanelState
 * @see TextFieldState
 */
public class FluxStateStore {
    
    private final Map<String, WidgetState> widgetStates = new HashMap<>();
    private final Map<String, PanelState> panelStates = new HashMap<>();
    private final Map<String, CollapsingState> collapsingStates = new HashMap<>();
    
    private String focusedWidgetId = null;
    private String activeWidgetId = null;
    private String hoveredWidgetId = null;
    
    /**
     * Create a new state store.
     */
    public FluxStateStore() {
    }
    
    // ========== Widget State ==========
    
    /**
     * Get or create widget state of specified type.
     * 
     * @param id   Widget ID
     * @param type State class type
     * @param <T>  State type
     * @return Widget state instance
     */
    @SuppressWarnings("unchecked")
    public <T extends WidgetState> T getOrCreateWidgetState(String id, Class<T> type) {
        WidgetState state = widgetStates.get(id);
        if (state == null) {
            try {
                state = type.getConstructor(String.class).newInstance(id);
                widgetStates.put(id, state);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create widget state for type: " + type.getName(), e);
            }
        }
        return (T) state;
    }
    
    /**
     * Get widget state if it exists.
     * 
     * @param id Widget ID
     * @return Widget state or null if not found
     */
    public WidgetState getWidgetState(String id) {
        return widgetStates.get(id);
    }
    
    /**
     * Set widget state.
     * 
     * @param id    Widget ID
     * @param state State to set
     */
    public void setWidgetState(String id, WidgetState state) {
        widgetStates.put(id, state);
    }
    
    /**
     * Clear widget state.
     * 
     * @param id Widget ID
     */
    public void clearWidgetState(String id) {
        widgetStates.remove(id);
    }
    
    /**
     * Clear all widget states.
     */
    public void clearAllWidgetStates() {
        widgetStates.clear();
    }
    
    // ========== Panel State ==========
    
    /**
     * Get or create panel state.
     * 
     * @param id Panel ID
     * @return Panel state
     */
    public PanelState getOrCreatePanelState(String id) {
        return panelStates.computeIfAbsent(id, PanelState::new);
    }
    
    /**
     * Get panel state if it exists.
     * 
     * @param id Panel ID
     * @return Panel state or null
     */
    public PanelState getPanelState(String id) {
        return panelStates.get(id);
    }
    
    /**
     * Set panel state.
     * 
     * @param id    Panel ID
     * @param state Panel state
     */
    public void setPanelState(String id, PanelState state) {
        panelStates.put(id, state);
    }
    
    /**
     * Get all panel states.
     * 
     * @return Map of panel ID to state
     */
    public Map<String, PanelState> getAllPanelStates() {
        return panelStates;
    }
    
    // ========== Collapsing State ==========
    
    /**
     * Get or create collapsing section state.
     * 
     * @param id Section ID
     * @return Collapsing state
     */
    public CollapsingState getOrCreateCollapsingState(String id) {
        return collapsingStates.computeIfAbsent(id, CollapsingState::new);
    }
    
    /**
     * Get collapsing state if it exists.
     * 
     * @param id Section ID
     * @return Collapsing state or null
     */
    public CollapsingState getCollapsingState(String id) {
        return collapsingStates.get(id);
    }
    
    /**
     * Get all collapsing states.
     * 
     * @return Map of section ID to state
     */
    public Map<String, CollapsingState> getAllCollapsingStates() {
        return collapsingStates;
    }
    
    // ========== Focus Management ==========
    
    /**
     * Get the ID of the focused widget.
     * 
     * @return Focused widget ID or null
     */
    public String getFocusedWidgetId() {
        return focusedWidgetId;
    }
    
    /**
     * Set the focused widget.
     * 
     * @param id Widget ID to focus (null to clear)
     */
    public void setFocusedWidget(String id) {
        this.focusedWidgetId = id;
    }
    
    /**
     * Check if a widget is focused.
     * 
     * @param id Widget ID
     * @return True if focused
     */
    public boolean isWidgetFocused(String id) {
        return id != null && id.equals(focusedWidgetId);
    }
    
    /**
     * Clear focus.
     */
    public void clearFocus() {
        this.focusedWidgetId = null;
    }
    
    // ========== Active Widget (Being Clicked) ==========
    
    /**
     * Get the ID of the active widget (being clicked).
     * 
     * @return Active widget ID or null
     */
    public String getActiveWidgetId() {
        return activeWidgetId;
    }
    
    /**
     * Set the active widget.
     * 
     * @param id Widget ID (null to clear)
     */
    public void setActiveWidget(String id) {
        this.activeWidgetId = id;
    }
    
    /**
     * Check if a widget is active.
     * 
     * @param id Widget ID
     * @return True if active
     */
    public boolean isWidgetActive(String id) {
        return id != null && id.equals(activeWidgetId);
    }
    
    // ========== Hovered Widget ==========
    
    /**
     * Get the ID of the hovered widget.
     * 
     * @return Hovered widget ID or null
     */
    public String getHoveredWidgetId() {
        return hoveredWidgetId;
    }
    
    /**
     * Set the hovered widget.
     * 
     * @param id Widget ID (null to clear)
     */
    public void setHoveredWidget(String id) {
        this.hoveredWidgetId = id;
    }
    
    /**
     * Check if a widget is hovered.
     * 
     * @param id Widget ID
     * @return True if hovered
     */
    public boolean isWidgetHovered(String id) {
        return id != null && id.equals(hoveredWidgetId);
    }
    
    // ========== Frame Lifecycle ==========
    
    /**
     * Called at the beginning of a frame.
     * Resets per-frame state like hovered widget.
     */
    public void beginFrame() {
        hoveredWidgetId = null;
    }
    
    /**
     * Called at the end of a frame.
     * Can be used for cleanup or validation.
     */
    public void endFrame() {
        // Future: Could add state validation or cleanup here
    }
}
