package org.pixel.ext.flux.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Manages panel-specific state including focus order, z-ordering, dragging, and scrollbar interactions.
 * 
 * <p>This class centralizes panel management logic that was previously scattered throughout the Flux class.
 * It handles:
 * <ul>
 *   <li>Panel z-order/focus management (ImGui-style bring-to-front)</li>
 *   <li>Panel dragging state</li>
 *   <li>Scrollbar dragging state</li>
 *   <li>Hot panel tracking (topmost panel under mouse)</li>
 *   <li>Panel recording stack for deferred rendering</li>
 * </ul>
 * 
 * @since Phase 8 refactoring
 */
public class FluxPanelManager {
    
    // === Panel Focus/Z-Order ===
    private final List<String> panelFocusOrder = new ArrayList<>(); // Panel IDs in focus order (first = back, last = front)
    private final List<String> panelsThisFrame = new ArrayList<>(); // Panels declared this frame
    private final Stack<String> recordingPanelStack = new Stack<>(); // Stack of panel IDs for nested panels (top = current)
    
    // === Hot Panel Tracking ===
    private String hotPanelId = null; // ID of panel mouse is currently over (topmost in focus order)
    private String previousHotPanelId = null; // Hot panel from previous frame (for consistent event handling)
    private boolean shouldBringHotPanelToFront = false; // Flag to bring hot panel to front after all panels declared
    
    // === Current Panel Context ===
    private String currentPanelId = null; // ID of currently active panel (for scroll and event filtering)
    
    // === Panel Drag State ===
    private String panelDragging = null; // ID of panel being dragged
    private float panelDragStartMouseX; // Mouse X when panel drag started
    private float panelDragStartMouseY; // Mouse Y when panel drag started
    private float panelDragStartX; // Panel X when drag started
    private float panelDragStartY; // Panel Y when drag started
    
    // === Scrollbar Drag State ===
    private String scrollbarDragging = null; // ID of panel whose scrollbar is being dragged
    private float scrollbarDragStartMouseY; // Mouse Y when drag started
    private float scrollbarDragStartOffset; // Scroll offset when drag started
    
    // === Configuration ===
    private boolean enablePanelOrdering = true; // Toggle for panel z-order management
    
    /**
     * Called at the start of each frame to reset per-frame state.
     */
    public void beginFrame() {
        previousHotPanelId = hotPanelId;
        hotPanelId = null;
        shouldBringHotPanelToFront = false;
        panelsThisFrame.clear();
        currentPanelId = null;
    }
    
    /**
     * Called at the end of each frame to finalize panel ordering.
     */
    public void endFrame() {
        // Bring hot panel to front if flagged
        if (shouldBringHotPanelToFront && hotPanelId != null && enablePanelOrdering) {
            bringPanelToFront(hotPanelId);
        }
    }
    
    // === Panel Registration ===
    
    /**
     * Register a panel for this frame.
     * Adds to focus order if not already present.
     */
    public void registerPanel(String panelId) {
        panelsThisFrame.add(panelId);
        if (!panelFocusOrder.contains(panelId)) {
            panelFocusOrder.add(panelId);
        }
    }
    
    /**
     * Check if a panel exists in the focus order.
     */
    public boolean isPanelRegistered(String panelId) {
        return panelFocusOrder.contains(panelId);
    }
    
    // === Focus/Z-Order Management ===
    
    /**
     * Move a panel to the front of the z-order.
     */
    public void bringPanelToFront(String panelId) {
        panelFocusOrder.remove(panelId);
        panelFocusOrder.add(panelId);
    }
    
    /**
     * Get the focus index of a panel (higher = more in front).
     * Returns -1 if panel not in focus order.
     */
    public int getPanelFocusIndex(String panelId) {
        return panelFocusOrder.indexOf(panelId);
    }
    
    /**
     * Get all panels in focus order (back to front).
     */
    public List<String> getPanelFocusOrder() {
        return new ArrayList<>(panelFocusOrder);
    }
    
    // === Hot Panel Management ===
    
    /**
     * Set the hot panel (topmost panel under mouse).
     */
    public void setHotPanel(String panelId) {
        this.hotPanelId = panelId;
    }
    
    /**
     * Get the current hot panel ID.
     */
    public String getHotPanelId() {
        return hotPanelId;
    }
    
    /**
     * Get the previous frame's hot panel ID.
     */
    public String getPreviousHotPanelId() {
        return previousHotPanelId;
    }
    
    /**
     * Mark that hot panel should be brought to front at end of frame.
     */
    public void markShouldBringHotPanelToFront() {
        this.shouldBringHotPanelToFront = true;
    }
    
    /**
     * Check if a panel is the hot panel.
     */
    public boolean isHotPanel(String panelId) {
        return panelId != null && panelId.equals(hotPanelId);
    }
    
    // === Current Panel Context ===
    
    /**
     * Set the current panel being processed (for event filtering).
     */
    public void setCurrentPanel(String panelId) {
        this.currentPanelId = panelId;
    }
    
    /**
     * Get the current panel being processed.
     */
    public String getCurrentPanelId() {
        return currentPanelId;
    }
    
    /**
     * Check if currently processing a panel.
     */
    public boolean isInPanel() {
        return currentPanelId != null;
    }
    
    // === Panel Recording Stack ===
    
    /**
     * Push a panel onto the recording stack.
     */
    public void pushRecordingPanel(String panelId) {
        recordingPanelStack.push(panelId);
    }
    
    /**
     * Pop a panel from the recording stack.
     */
    public void popRecordingPanel() {
        if (!recordingPanelStack.isEmpty()) {
            recordingPanelStack.pop();
        }
    }
    
    /**
     * Check if recording stack is empty.
     */
    public boolean isRecordingStackEmpty() {
        return recordingPanelStack.isEmpty();
    }
    
    /**
     * Get the current recording panel (top of stack).
     */
    public String getCurrentRecordingPanel() {
        return recordingPanelStack.isEmpty() ? null : recordingPanelStack.peek();
    }
    
    // === Panel Dragging ===
    
    /**
     * Start dragging a panel.
     */
    public void startPanelDrag(String panelId, float mouseX, float mouseY, float panelX, float panelY) {
        this.panelDragging = panelId;
        this.panelDragStartMouseX = mouseX;
        this.panelDragStartMouseY = mouseY;
        this.panelDragStartX = panelX;
        this.panelDragStartY = panelY;
    }
    
    /**
     * Stop dragging the current panel.
     */
    public void stopPanelDrag() {
        this.panelDragging = null;
    }
    
    /**
     * Check if a panel is being dragged.
     */
    public boolean isPanelDragging(String panelId) {
        return panelId != null && panelId.equals(panelDragging);
    }
    
    /**
     * Get the ID of the panel being dragged.
     */
    public String getPanelDragging() {
        return panelDragging;
    }
    
    /**
     * Calculate panel drag delta X.
     */
    public float getPanelDragDeltaX(float currentMouseX) {
        return currentMouseX - panelDragStartMouseX;
    }
    
    /**
     * Calculate panel drag delta Y.
     */
    public float getPanelDragDeltaY(float currentMouseY) {
        return currentMouseY - panelDragStartMouseY;
    }
    
    /**
     * Get the panel's X position when drag started.
     */
    public float getPanelDragStartX() {
        return panelDragStartX;
    }
    
    /**
     * Get the panel's Y position when drag started.
     */
    public float getPanelDragStartY() {
        return panelDragStartY;
    }
    
    // === Scrollbar Dragging ===
    
    /**
     * Start dragging a scrollbar.
     */
    public void startScrollbarDrag(String panelId, float mouseY, float scrollOffset) {
        this.scrollbarDragging = panelId;
        this.scrollbarDragStartMouseY = mouseY;
        this.scrollbarDragStartOffset = scrollOffset;
    }
    
    /**
     * Stop dragging the current scrollbar.
     */
    public void stopScrollbarDrag() {
        this.scrollbarDragging = null;
    }
    
    /**
     * Check if a scrollbar is being dragged.
     */
    public boolean isScrollbarDragging(String panelId) {
        return panelId != null && panelId.equals(scrollbarDragging);
    }
    
    /**
     * Get the ID of the panel whose scrollbar is being dragged.
     */
    public String getScrollbarDragging() {
        return scrollbarDragging;
    }
    
    /**
     * Get the mouse Y position when scrollbar drag started.
     */
    public float getScrollbarDragStartMouseY() {
        return scrollbarDragStartMouseY;
    }
    
    /**
     * Get the scroll offset when scrollbar drag started.
     */
    public float getScrollbarDragStartOffset() {
        return scrollbarDragStartOffset;
    }
    
    // === Configuration ===
    
    /**
     * Enable or disable automatic panel z-order management.
     */
    public void setEnablePanelOrdering(boolean enable) {
        this.enablePanelOrdering = enable;
    }
    
    /**
     * Check if panel ordering is enabled.
     */
    public boolean isEnablePanelOrdering() {
        return enablePanelOrdering;
    }
}
