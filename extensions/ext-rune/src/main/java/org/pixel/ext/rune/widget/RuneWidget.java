package org.pixel.ext.rune.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.ext.rune.core.RuneRenderContext;
import org.pixel.ext.rune.event.RuneKeyEvent;
import org.pixel.ext.rune.event.RuneMouseEvent;
import org.pixel.ext.rune.layout.SizeConstraints;
import org.pixel.ext.rune.style.RuneStyle;
import org.pixel.ext.rune.style.StyleProperty;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Base class for all Rune widgets.
 * 
 * <p>Rune uses a retained-mode architecture where widgets are created once
 * and persist between frames. The widget tree is hierarchical with parent-child
 * relationships.
 * 
 * <p>Lifecycle:
 * <ol>
 *   <li>Construction - Widget is created</li>
 *   <li>onLayout() - Calculate and apply positions/sizes</li>
 *   <li>onUpdate() - Update animations, timers, etc.</li>
 *   <li>onRender() - Render visual content</li>
 *   <li>dispose() - Clean up resources</li>
 * </ol>
 * 
 * <p>Dirty Flagging:
 * When a widget's visual state changes, call markDirty(). This sets the dirty
 * flag and propagates up the tree. Only dirty widgets re-render.
 */
@Getter
@Setter
public abstract class RuneWidget implements Disposable, Updatable {
    
    /**
     * Widget visual state.
     * These states map to CSS pseudo-classes (e.g., :hover, :pressed, :disabled).
     */
    public enum State {
        NORMAL,    // Default state
        HOVER,     // Pointer over widget
        PRESSED,   // Widget being pressed
        DISABLED   // Widget disabled (non-interactive)
    }
    
    // Auto-increment counter for generating IDs
    private static int nextAutoId = 1;
    
    // Identity
    protected final String id;
    protected RuneWidget parent;
    
    // Styling (CSS-like)
    protected final Set<String> classes = new HashSet<>();        // Style classes
    protected RuneStyle inlineStyle;                               // Inline style overrides
    protected final Set<String> activeStates = new HashSet<>();   // Current states (hover, active, etc.)
    protected transient RuneStyle computedStyle;                  // Cached computed style
    
    // Geometry - Position/Anchor system
    protected float x;
    protected float y;
    protected Anchor anchor = Anchor.TOP_LEFT;
    
    // Size - WinForms-style
    protected float width = 0;           // Explicit width (used when autoSize = false)
    protected float height = 0;          // Explicit height (used when autoSize = false)
    protected boolean autoSize = false;  // True = measure from content, False = use width/height
    
    // Size constraints (optional)
    protected Size minSize;
    protected Size maxSize;
    
    // Box model (CSS-style, cached)
    protected transient ComputedBox box = new ComputedBox();
    protected transient boolean boxDirty = true;  // Protected so containers can invalidate children
    
    // State
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean dirty = true;          // Needs re-render
    protected boolean layoutDirty = true;    // Needs re-layout
    protected boolean focusable = false;     // Can receive keyboard focus
    protected boolean focused = false;       // Currently has keyboard focus
    protected boolean hovered = false;       // Mouse is over widget
    protected boolean pressed = false;       // Mouse button is down on widget
    protected State state = State.NORMAL;    // Current visual state (derived from flags)
    
    // Tooltip
    protected String tooltip;
    protected float tooltipDelay = 0.5f;     // Seconds before tooltip appears
    
    // Z-ordering (higher = rendered later = on top)
    protected int zIndex = 0;
    
    // Dragging support
    protected boolean draggable = false;                          // Whether this widget can be dragged
    protected final List<Rectangle> dragHandles = new ArrayList<>();  // Specific areas that trigger dragging (empty = entire widget)
    private boolean isDragging = false;                           // Currently being dragged
    private float dragStartX, dragStartY;                         // Mouse position when drag started
    private float widgetStartX, widgetStartY;                     // Widget position when drag started
    
    // Event listeners (for external subscription)
    private final List<Consumer<RuneMouseEvent>> mouseListeners = new ArrayList<>();
    private final List<Consumer<RuneKeyEvent>> keyListeners = new ArrayList<>();
    private final List<Runnable> clickListeners = new ArrayList<>();
    
    /**
     * Create a new widget with the given ID.
     * ID must be unique within its parent container.
     * 
     * @param id Widget ID (if null, auto-generates "widget_N")
     */
    public RuneWidget(String id) {
        // Auto-generate ID if not provided
        if (id == null || id.isEmpty()) {
            this.id = getTypeName() + "_" + (nextAutoId++);
        } else {
            this.id = id;
        }
    }
    
    // === Event Listener Subscription (Java Swing/AWT style) ===
    
    /**
     * Add a mouse event listener.
     * The listener will be called for all mouse events on this widget.
     * 
     * @param listener Mouse event listener
     */
    public void addMouseListener(Consumer<RuneMouseEvent> listener) {
        if (listener != null && !mouseListeners.contains(listener)) {
            mouseListeners.add(listener);
        }
    }
    
    /**
     * Remove a mouse event listener.
     * 
     * @param listener Mouse event listener to remove
     */
    public void removeMouseListener(Consumer<RuneMouseEvent> listener) {
        mouseListeners.remove(listener);
    }
    
    /**
     * Add a click listener (convenience for click events).
     * Called when widget receives a proper click (press + release in bounds).
     * 
     * @param listener Click listener
     */
    public void addClickListener(Runnable listener) {
        if (listener != null && !clickListeners.contains(listener)) {
            clickListeners.add(listener);
        }
    }
    
    /**
     * Remove a click listener.
     * 
     * @param listener Click listener to remove
     */
    public void removeClickListener(Runnable listener) {
        clickListeners.remove(listener);
    }
    
    /**
     * Add a keyboard event listener.
     * The listener will be called for all keyboard events when widget has focus.
     * 
     * @param listener Keyboard event listener
     */
    public void addKeyListener(Consumer<RuneKeyEvent> listener) {
        if (listener != null && !keyListeners.contains(listener)) {
            keyListeners.add(listener);
        }
    }
    
    /**
     * Remove a keyboard event listener.
     * 
     * @param listener Keyboard event listener to remove
     */
    public void removeKeyListener(Consumer<RuneKeyEvent> listener) {
        keyListeners.remove(listener);
    }
    
    // === Fluent Event Subscription API ===
    
    /**
     * Add mouse event listener (fluent API).
     * 
     * @param listener Mouse event listener
     * @return This widget for method chaining
     */
    public RuneWidget onMouseEvent(Consumer<RuneMouseEvent> listener) {
        addMouseListener(listener);
        return this;
    }
    
    /**
     * Add click listener (fluent API).
     * 
     * @param listener Click listener
     * @return This widget for method chaining
     */
    public RuneWidget onClick(Runnable listener) {
        addClickListener(listener);
        return this;
    }
    
    /**
     * Add keyboard event listener (fluent API).
     * 
     * @param listener Keyboard event listener
     * @return This widget for method chaining
     */
    public RuneWidget onKeyEvent(Consumer<RuneKeyEvent> listener) {
        addKeyListener(listener);
        return this;
    }
    
    // === Lifecycle Methods (override in subclasses) ===
    
    /**
     * Called when widget needs to recalculate layout.
     * Override to implement custom layout logic.
     */
    protected void onLayout() {
        // Default: no custom layout logic
        // Subclasses (like RuneContainer) override this
    }
    
    /**
     * Called every frame to update widget state.
     * Override to implement animations, timers, etc.
     * 
     * @param delta Time since last frame
     */
    protected void onUpdate(DeltaTime delta) {
        // Default: no custom update logic
    }
    
    /**
     * Called when widget needs to render.
     * Override to implement custom rendering.
     * 
     * @param ctx Rendering context with canvas, theme, etc.
     */
    protected void onRender(RuneRenderContext ctx) {
        // Default: no rendering
    }
    
    /**
     * Handle mouse event.
     * First fires all external listeners, then calls subclass implementation.
     * Override to implement custom mouse interaction (after listeners execute).
     * 
     * @param event Mouse event
     * @return true if event was consumed (stops propagation)
     */
    protected boolean onMouseEvent(RuneMouseEvent event) {
        // Handle dragging if enabled
        if (draggable) {
            if (event.getType() == RuneMouseEvent.Type.PRESS && event.getButton() == 0) {
                // Convert absolute screen coordinates to widget-local coordinates (zero-allocation)
                float localX = absoluteToRelativeX(event.getX());
                float localY = absoluteToRelativeY(event.getY());

                // Check if clicking on a drag handle
                if (isDragHandleHit(localX, localY)) {
                    isDragging = true;
                    dragStartX = event.getX();  // Store absolute coords for drag delta
                    dragStartY = event.getY();
                    widgetStartX = x;  // Store relative position
                    widgetStartY = y;
                    return true; // Consume press event when drag starts
                }
            } else if (event.getType() == RuneMouseEvent.Type.DRAG && isDragging) {
                // Calculate drag delta in absolute (screen) coordinates
                float deltaX = event.getX() - dragStartX;
                float deltaY = event.getY() - dragStartY;

                // Update relative position by adding delta
                // This works because relative coords move 1:1 with absolute coords
                setPosition(widgetStartX + deltaX, widgetStartY + deltaY);
                return true; // Consume drag events
            } else if (event.getType() == RuneMouseEvent.Type.RELEASE && isDragging) {
                isDragging = false;
                return true; // Consume release event
            }
        }
        
        // Fire external mouse listeners first
        boolean consumed = false;
        for (Consumer<RuneMouseEvent> listener : mouseListeners) {
            listener.accept(event);
            consumed = true; // Consider consumed if any listeners exist
        }
        
        // Check if this was a click event (press followed by release in bounds)
        if (isClickEvent(event)) {
            for (Runnable listener : clickListeners) {
                listener.run();
                consumed = true;
            }
        }
        
        return consumed;
    }
    
    /**
     * Handle keyboard event.
     * First fires all external listeners, then calls subclass implementation.
     * Override to implement custom keyboard interaction (after listeners execute).
     * 
     * @param event Keyboard event
     * @return true if event was consumed (stops propagation)
     */
    protected boolean onKeyEvent(RuneKeyEvent event) {
        // Fire external keyboard listeners first
        boolean consumed = false;
        for (Consumer<RuneKeyEvent> listener : keyListeners) {
            listener.accept(event);
            consumed = true; // Consider consumed if any listeners exist
        }
        
        return consumed;
    }
    
    /**
     * Check if mouse event is a valid click (release after press within bounds).
     * 
     * @param event Mouse event to check
     * @return true if this is a click event
     */
    private boolean isClickEvent(RuneMouseEvent event) {
        // Click = release event with button 0 (left button) within bounds
        return event.getType() == RuneMouseEvent.Type.RELEASE &&
               event.getButton() == 0 &&
               getBounds().contains(event.getX(), event.getY());
    }
    
    /**
     * Called when widget gains keyboard focus.
     */
    protected void onFocusGained() {
        // Override in subclasses
    }
    
    /**
     * Called when widget loses keyboard focus.
     */
    protected void onFocusLost() {
        // Override in subclasses
    }
    
    // === Public API (final - framework calls) ===
    
    /**
     * Update this widget.
     * Called by framework - do not override, use onUpdate() instead.
     * Containers override to update children.
     */
    @Override
    public void update(DeltaTime delta) {
        if (!visible) return;
        
        onUpdate(delta);
    }
    
    /**
     * Validate and sync hover/pressed states with actual mouse position.
     * This ensures states are always correct even with fast mouse movement or missed events.
     * Should be called every frame by the UI system before rendering.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    public void validateMouseStates(float mouseX, float mouseY) {
        if (!visible || !enabled) {
            // Clear states if not visible or disabled
            if (hovered) {
                setHovered(false);
            }
            if (pressed) {
                setPressed(false);
            }
            return;
        }
        
        // Check if mouse is actually over this widget
        Rectangle bounds = getBounds();
        boolean mouseIsOver = bounds.contains(mouseX, mouseY);
        
        // Sync hover state (but not if currently pressed/dragging)
        if (!pressed && hovered != mouseIsOver) {
            setHovered(mouseIsOver);
        }
    }
    
    /**
     * Draw this widget.
     * Called by framework - do not override, use onRender() instead.
     * Containers override to draw children.
     * 
     * @param ctx Render context with canvas and theme
     * @param force If true, always render regardless of dirty state (game mode)
     */
    public void draw(RuneRenderContext ctx, boolean force) {
        if (!visible) return;
        
        // Ensure box model is computed before layout/render
        ensureBoxComputed(ctx);
        
        // Recalculate layout if needed
        if (layoutDirty) {
            layout();
        }
        
        // Render if forced (game mode) OR if dirty
        if (force || dirty) {
            onRender(ctx);
            dirty = false;
        }
    }
    
    /**
     * Layout this widget.
     * Called by framework - do not override, use onLayout() instead.
     */
    public void layout() {
        if (!layoutDirty) return;
        
        onLayout();
        
        layoutDirty = false;
    }
    
    /**
     * Dispatch mouse event to this widget.
     * Returns true if event was consumed.
     * Containers override this to dispatch to children first.
     */
    public boolean dispatchMouseEvent(RuneMouseEvent event) {
        if (!visible || !enabled) return false;
        
        // Try this widget
        if (onMouseEvent(event)) {
            return true; // This widget consumed event
        }
        
        return false; // Event not consumed
    }
    
    /**
     * Dispatch keyboard event to focused widget.
     * Returns true if event was consumed.
     * Containers override this to dispatch to children.
     */
    public boolean dispatchKeyEvent(RuneKeyEvent event) {
        if (!visible || !enabled) return false;
        
        // If this widget has focus, handle the event
        if (focused && focusable) {
            if (onKeyEvent(event)) {
                return true; // This widget consumed event
            }
        }
        
        return false; // Event not consumed
    }
    
    /**
     * Get the root widget (traverse up to top).
     */
    public RuneWidget getRoot() {
        RuneWidget root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }
    
    // === State Management ===
    
    /**
     * Mark this widget as dirty (needs re-render).
     * Propagates up the tree and invalidates computed style cache.
     */
    public void markDirty() {
        if (!dirty) {
            dirty = true;
            computedStyle = null; // Invalidate cached style (inlined to avoid circular call)
            if (parent != null) {
                parent.markDirty();
            }
        }
    }
    
    /**
     * Mark layout as dirty (needs re-layout).
     * Also marks box model dirty since layout affects sizing.
     * Propagates up the tree.
     */
    public void markLayoutDirty() {
        if (!layoutDirty) {
            layoutDirty = true;
            boxDirty = true;  // Box depends on layout
            if (parent != null) {
                parent.markLayoutDirty();
            }
        }
    }
    
    /**
     * Set visibility (fluent). Hidden widgets don't update or render.
     */
    public RuneWidget setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            markLayoutDirty();
            markDirty();
        }
        return this;
    }
    
    /**
     * Set enabled state (fluent). Disabled widgets don't respond to input.
     * Automatically syncs with :disabled pseudo-class.
     */
    public RuneWidget setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            
            // Sync with CSS pseudo-class state
            if (enabled) {
                removeState(":disabled");
            } else {
                addState(":disabled");
            }
            
            // Update state enum
            updateState();
            markDirty();
        }
        return this;
    }
    
    /**
     * Set keyboard focus (fluent).
     */
    public RuneWidget setFocused(boolean focused) {
        if (this.focused != focused) {
            this.focused = focused;
            
            // Sync with CSS pseudo-class state
            if (focused) {
                addState(":focused");
                onFocusGained();
            } else {
                removeState(":focused");
                onFocusLost();
            }
            markDirty();
        }
        return this;
    }
    
    /**
     * Set hover state (mouse over widget).
     * Automatically syncs with :hover pseudo-class.
     * 
     * @param hovered True if mouse is over widget
     * @return This widget for chaining
     */
    public RuneWidget setHovered(boolean hovered) {
        if (this.hovered != hovered) {
            this.hovered = hovered;
            
            // Sync with CSS pseudo-class state
            if (hovered) {
                addState(":hover");
            } else {
                removeState(":hover");
            }
            
            // Update state enum
            updateState();
        }
        return this;
    }
    
    /**
     * Set pressed state (mouse button down on widget).
     * Automatically syncs with :pressed pseudo-class.
     * 
     * @param pressed True if mouse button is down on widget
     * @return This widget for chaining
     */
    public RuneWidget setPressed(boolean pressed) {
        if (this.pressed != pressed) {
            this.pressed = pressed;
            
            // Sync with CSS pseudo-class state
            if (pressed) {
                addState(":pressed");
            } else {
                removeState(":pressed");
            }
            
            // Update state enum
            updateState();
        }
        return this;
    }
    
    /**
     * Update the state enum based on current boolean flags.
     * Priority: DISABLED > PRESSED > HOVER > NORMAL
     */
    protected void updateState() {
        State oldState = state;
        
        if (!enabled) {
            state = State.DISABLED;
        } else if (pressed) {
            state = State.PRESSED;
        } else if (hovered) {
            state = State.HOVER;
        } else {
            state = State.NORMAL;
        }
        
        // Mark dirty if state changed
        if (oldState != state) {
            markDirty();
        }
    }
    
    // === Dragging Support ===
    
    /**
     * Set whether this widget can be dragged.
     * 
     * @param draggable True to enable dragging
     * @return This widget for chaining
     */
    public RuneWidget setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }
    
    /**
     * Add a drag handle region (area where dragging can be initiated).
     * If no handles are set, the entire widget is draggable.
     * 
     * @param handle Rectangle defining the drag handle area (in local coordinates)
     * @return This widget for chaining
     */
    public RuneWidget addDragHandle(Rectangle handle) {
        if (handle != null) {
            dragHandles.add(new Rectangle(handle)); // Copy to prevent external modification
        }
        return this;
    }
    
    /**
     * Set multiple drag handles at once, replacing any existing handles.
     * 
     * @param handles List of drag handle rectangles
     * @return This widget for chaining
     */
    public RuneWidget setDragHandles(List<Rectangle> handles) {
        dragHandles.clear();
        if (handles != null) {
            for (Rectangle handle : handles) {
                dragHandles.add(new Rectangle(handle));
            }
        }
        return this;
    }
    
    /**
     * Clear all drag handles. After this, the entire widget becomes draggable.
     * 
     * @return This widget for chaining
     */
    public RuneWidget clearDragHandles() {
        dragHandles.clear();
        return this;
    }
    
    /**
     * Check if a point (in local coordinates) hits a drag handle.
     * Override for custom drag handle logic (e.g., based on child widget bounds).
     * 
     * @param localX X coordinate relative to widget
     * @param localY Y coordinate relative to widget
     * @return True if point is in a drag handle area
     */
    protected boolean isDragHandleHit(float localX, float localY) {
        if (dragHandles.isEmpty()) {
            // No explicit handles = entire widget is draggable
            return true;
        }
        
        for (Rectangle handle : dragHandles) {
            if (handle.contains(localX, localY)) {
                return true;
            }
        }
        return false;
    }
    
    // === Geometry & Box Model ===
    
    /**
     * Ensure box model is computed and up-to-date.
     * Subclasses call this before accessing box fields in onRender().
     * Automatically called during render/layout cycle.
     */
    protected void ensureBoxComputed(RuneRenderContext ctx) {
        if (boxDirty || dirty || layoutDirty) {
            computeBox(ctx);
        }
    }
    
    /**
     * Compute/recalculate the box model (CSS-style padding, margin, border).
     * Called automatically when box is dirty.
     */
    private void computeBox(RuneRenderContext ctx) {
        // Apply style properties to box
        RuneStyle style = getComputedStyle(ctx);
        
        // SAFETY: If style is null, cannot compute box
        if (style == null) {
            System.err.println("WARNING: getComputedStyle returned null for widget: " + id);
            return;
        }
        
        box.applyStyle(style);
        
        // SAFETY: Ensure boxSizing is never null
        if (box.boxSizing == null) {
            System.err.println("WARNING: boxSizing is null after applyStyle for widget: " + id + " (type: " + getTypeName() + ")");
            box.boxSizing = BoxSizing.BORDER_BOX;  // Force default
        }
        
        // Measure content size (WITHOUT padding/border/margin)
        Size content = measureContent(ctx);
        box.contentSize.set(content.getWidth(), content.getHeight());
        
        // Calculate total size based on box-sizing mode
        float totalWidth, totalHeight;
        
        if (autoSize) {
            // Auto-size: content + padding + border
            totalWidth = content.getWidth() 
                + box.paddingHorizontal() 
                + box.borderHorizontal();
            totalHeight = content.getHeight() 
                + box.paddingVertical() 
                + box.borderVertical();
        } else {
            // Fixed size
            if (box.boxSizing == BoxSizing.BORDER_BOX) {
                // width/height includes padding + border (modern CSS)
                totalWidth = width;
                totalHeight = height;
            } else {
                // width/height is content only (CSS default)
                totalWidth = width 
                    + box.paddingHorizontal() 
                    + box.borderHorizontal();
                totalHeight = height 
                    + box.paddingVertical() 
                    + box.borderVertical();
            }
        }
        
        // Store total size (includes padding+border+margin)
        box.totalSize.set(
            totalWidth + box.marginHorizontal(),
            totalHeight + box.marginVertical()
        );
        
        // Calculate all bounds rectangles
        calculateBounds();
        
        boxDirty = false;
    }
    
    /**
     * Calculate all bounds rectangles (total, border, padding, content).
     * Called by computeBox().
     *
     * <p>Coordinate System:
     * - Widget (x, y) fields store RELATIVE coordinates to parent's content bounds
     * - For root widgets (no parent), relative to viewport origin (0, 0)
     * - This method converts relative → absolute by adding parent's content origin
     *
     * <p>Example:
     * - Parent content bounds at (100, 50)
     * - Child has relative position (10, 20)
     * - Child's absolute position = (110, 70)
     */
    private void calculateBounds() {
        float totalW = box.totalSize.getWidth();
        float totalH = box.totalSize.getHeight();

        // Get parent's content origin (where children are positioned relative to)
        // If no parent, origin is (0, 0) - widget is at root level
        float parentContentX = 0;
        float parentContentY = 0;
        if (parent != null) {
            // Parent's content bounds origin is the (0, 0) point for children
            parentContentX = parent.box.contentBounds.getX();
            parentContentY = parent.box.contentBounds.getY();
        }

        // Apply anchor to relative position (excludes margin for positioning)
        // Note: x, y are ALREADY relative to parent, so we apply anchor first
        // Use zero-allocation methods to avoid GC
        float widthWithoutMargin = totalW - box.marginHorizontal();
        float heightWithoutMargin = totalH - box.marginVertical();
        float anchoredX = anchor.calculateX(x, widthWithoutMargin);
        float anchoredY = anchor.calculateY(y, heightWithoutMargin);

        // Convert relative → absolute by adding parent's content origin
        // IMPORTANT: With CONTENT_BOX, x/y represent content position, not border position
        // So we need to offset backward by border+padding to get border-box position
        float absoluteX = anchoredX + parentContentX;
        float absoluteY = anchoredY + parentContentY;

        if (box.boxSizing == BoxSizing.CONTENT_BOX) {
            // With CONTENT_BOX, x/y is the content position
            // Border-box is offset outward (left/up) by border + padding
            absoluteX -= (box.borderWidth + box.paddingLeft);
            absoluteY -= (box.borderWidth + box.paddingTop);
        }
        // With BORDER_BOX, x/y is already the border position (no adjustment needed)

        // Total bounds (including margin, for layout spacing)
        box.totalBounds.set(
            absoluteX - box.marginLeft,
            absoluteY - box.marginTop,
            totalW,
            totalH
        );

        // Border bounds (excluding margin, this is the visual widget box)
        box.borderBounds.set(
            absoluteX,
            absoluteY,
            totalW - box.marginHorizontal(),
            totalH - box.marginVertical()
        );
        
        // Padding bounds (excluding margin + border, background fill area)
        box.paddingBounds.set(
            absoluteX + box.borderWidth,
            absoluteY + box.borderWidth,
            box.borderBounds.getWidth() - box.borderHorizontal(),
            box.borderBounds.getHeight() - box.borderVertical()
        );
        
        // Content bounds (excluding margin + border + padding, content render area)
        // SAFETY: Ensure content area is non-negative (padding can't exceed container size)
        float contentWidth = Math.max(0, box.paddingBounds.getWidth() - box.paddingHorizontal());
        float contentHeight = Math.max(0, box.paddingBounds.getHeight() - box.paddingVertical());
        
        box.contentBounds.set(
            box.paddingBounds.getX() + box.paddingLeft,
            box.paddingBounds.getY() + box.paddingTop,
            contentWidth,
            contentHeight
        );
    }
    
    /**
     * Get widget bounds for layout/hit-testing (includes margin).
     * Returns cached bounds from box model.
     */
    public Rectangle getBounds() {
        return box.totalBounds;
    }
    
    /**
     * Get widget size for layout (includes margin).
     * Returns cached size from box model.
     */
    public Size getSize() {
        return box.totalSize;
    }
    
    /**
     * Get widget width for layout (includes margin).
     */
    public float getWidth() {
        return box.totalSize.getWidth();
    }
    
    /**
     * Get widget height for layout (includes margin).
     */
    public float getHeight() {
        return box.totalSize.getHeight();
    }
    
    /**
     * Set explicit size for this widget.
     * Stores width/height that will be used when autoSize = false.
     * 
     * @param width Widget width in pixels
     * @param height Widget height in pixels
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        markLayoutDirty();
    }
    
    /**
     * Set position and size in one call (zero-allocation convenience).
     * Convenience method for common initialization pattern.
     * 
     * @param x X position
     * @param y Y position
     * @param width Widget width
     * @param height Widget height
     */
    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        boxDirty = true;
        markLayoutDirty();
    }
    
    /**
     * Enable or disable auto-sizing.
     * When true, widget measures size from content (via measureContent).
     * When false, widget uses explicit width/height set via setSize.
     * 
     * @param autoSize True to auto-size from content, false to use explicit size
     */
    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
        markLayoutDirty();
    }
    
    /**
     * Check if auto-sizing is enabled.
     */
    public boolean isAutoSize() {
        return autoSize;
    }
    
    /**
     * Measure content size for auto-sizing mode (WITHOUT padding/border/margin).
     * Subclasses override to calculate size from their content.
     * Default implementation returns current explicit size.
     * 
     * @param ctx Render context for accessing fonts, themes, etc.
     * @return The measured content size
     */
    protected Size measureContent(RuneRenderContext ctx) {
        return new Size(width, height);  // Default: use explicit size
    }
    
    /**
     * Set widget position (RELATIVE to parent's content bounds).
     *
     * <p>Coordinate System:
     * - (x, y) are relative to parent's content area origin
     * - For root widgets (no parent), relative to viewport (0, 0)
     * - Anchor point determines what (x, y) represents (e.g., top-left, center, etc.)
     *
     * <p>Example:
     * <pre>
     * container.add(button.setPosition(10, 20));  // 10px from left, 20px from top of container's content
     * </pre>
     *
     * <p>With relative positioning, moving a parent automatically moves all children
     * because their absolute positions are recalculated from the parent's content origin.
     *
     * @param x X position relative to parent's content origin
     * @param y Y position relative to parent's content origin
     */
    public void setPosition(float x, float y) {
        if (this.x == x && this.y == y) return; // No change

        this.x = x;
        this.y = y;
        boxDirty = true;  // Position affects bounds rectangles
        markDirty();

        // Invalidate children's boxes so they recalculate with new parent offset
        invalidateChildrenBoxes();
    }

    /**
     * Invalidate this widget's children's box models.
     * Called when parent position/size changes, forcing children to recalculate bounds.
     * Containers override this to propagate to their children.
     */
    protected void invalidateChildrenBoxes() {
        // Default: no children, nothing to do
        // Containers override this
    }
    
    /**
     * Set widget anchor point.
     * Anchor determines what (x, y) represents (e.g., top-left, center, etc.).
     */
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
        markDirty();
    }
    
    /**
     * Get X position (relative to parent's content origin).
     */
    public float getX() {
        return x;
    }

    /**
     * Get Y position (relative to parent's content origin).
     */
    public float getY() {
        return y;
    }

    /**
     * Get anchor point.
     */
    public Anchor getAnchor() {
        return anchor;
    }

    /**
     * Get absolute X position in screen/viewport coordinates.
     * This is the actual rendered position after parent offset is applied.
     * Useful for debugging or converting mouse coordinates.
     *
     * @return Absolute X coordinate
     */
    public float getAbsoluteX() {
        return box.borderBounds.getX();
    }

    /**
     * Get absolute Y position in screen/viewport coordinates.
     * This is the actual rendered position after parent offset is applied.
     * Useful for debugging or converting mouse coordinates.
     *
     * @return Absolute Y coordinate
     */
    public float getAbsoluteY() {
        return box.borderBounds.getY();
    }

    /**
     * Convert absolute X coordinate to relative X within this widget's content area.
     * Zero-allocation alternative to absoluteToRelative().
     *
     * @param absoluteX Absolute X coordinate (screen/viewport)
     * @return X relative to this widget's content origin
     */
    public float absoluteToRelativeX(float absoluteX) {
        return absoluteX - box.contentBounds.getX();
    }

    /**
     * Convert absolute Y coordinate to relative Y within this widget's content area.
     * Zero-allocation alternative to absoluteToRelative().
     *
     * @param absoluteY Absolute Y coordinate (screen/viewport)
     * @return Y relative to this widget's content origin
     */
    public float absoluteToRelativeY(float absoluteY) {
        return absoluteY - box.contentBounds.getY();
    }

    /**
     * Convert relative X coordinate to absolute screen coordinate.
     * Zero-allocation alternative to relativeToAbsolute().
     *
     * @param relativeX X relative to this widget's content origin
     * @return Absolute X coordinate in screen/viewport coordinates
     */
    public float relativeToAbsoluteX(float relativeX) {
        return relativeX + box.contentBounds.getX();
    }

    /**
     * Convert relative Y coordinate to absolute screen coordinate.
     * Zero-allocation alternative to relativeToAbsolute().
     *
     * @param relativeY Y relative to this widget's content origin
     * @return Absolute Y coordinate in screen/viewport coordinates
     */
    public float relativeToAbsoluteY(float relativeY) {
        return relativeY + box.contentBounds.getY();
    }
    
    /**
     * Check if point is within widget bounds.
     */
    public boolean contains(float x, float y) {
        return getBounds().contains(x, y);
    }
    

    
    /**
     * Get preferred size (uses current sizing mode).
     * This delegates to getSize() which respects autoSize.
     */
    public Size getPreferredSize() {
        return getSize();
    }
    
    /**
     * Get preferred size with constraints.
     * Used by layout managers to measure widgets within specific bounds.
     * 
     * @param constraints The size constraints to apply
     * @return The preferred size within the constraints
     */
    public Size getPreferredSize(SizeConstraints constraints) {
        Size size = getSize();
        return constraints.constrain(size.getWidth(), size.getHeight());
    }
    
    // === Style System ===
    
    /**
     * Get the widget type name for styling.
     * Used by the style system to apply type-based styles (e.g., "button", "label").
     * 
     * @return The type name, or null if not styled
     */
    public abstract String getTypeName();
    
    /**
     * Add one or more style classes to this widget.
     * Classes are applied in the order they're added.
     * 
     * @param classNames The class names to add
     * @return This widget for chaining
     */
    public RuneWidget addClass(String... classNames) {
        for (String className : classNames) {
            if (className != null && !className.isEmpty()) {
                classes.add(className);
            }
        }
        computedStyle = null; // Invalidate cached style
        markDirty();
        return this;
    }
    
    /**
     * Remove a style class from this widget.
     * 
     * @param className The class name to remove
     * @return This widget for chaining
     */
    public RuneWidget removeClass(String className) {
        if (classes.remove(className)) {
            computedStyle = null; // Invalidate cached style
            markDirty();
        }
        return this;
    }
    
    /**
     * Check if this widget has a style class.
     * 
     * @param className The class name to check
     * @return True if the class is present
     */
    public boolean hasClass(String className) {
        return classes.contains(className);
    }
    
    /**
     * Toggle a style class on this widget.
     * 
     * @param className The class name to toggle
     * @return This widget for chaining
     */
    public RuneWidget toggleClass(String className) {
        if (hasClass(className)) {
            removeClass(className);
        } else {
            addClass(className);
        }
        return this;
    }
    
    /**
     * Add a state to this widget (e.g., "hover", "active", "focused").
     * States affect which style properties are applied.
     * 
     * @param stateName The state to add
     */
    public void addState(String stateName) {
        if (activeStates.add(stateName)) {
            computedStyle = null; // Invalidate cached style
            markDirty();
        }
    }
    
    /**
     * Remove a state from this widget.
     * 
     * @param stateName The state to remove
     */
    public void removeState(String stateName) {
        if (activeStates.remove(stateName)) {
            computedStyle = null; // Invalidate cached style
            markDirty();
        }
    }
    
    /**
     * Check if this widget has a state.
     * 
     * @param stateName The state to check
     * @return True if the state is active
     */
    public boolean hasState(String stateName) {
        return activeStates.contains(stateName);
    }
    
    /**
     * Set or update inline style properties.
     * Inline styles have the highest priority.
     * 
     * @param style The inline style
     * @return This widget for chaining
     */
    public RuneWidget setInlineStyle(RuneStyle style) {
        this.inlineStyle = style;
        computedStyle = null; // Invalidate cached style
        markDirty();
        return this;
    }
    
    /**
     * Get or create inline style for modification.
     * 
     * @return The inline style (creates if doesn't exist)
     */
    public RuneStyle ensureInlineStyle() {
        if (inlineStyle == null) {
            inlineStyle = new RuneStyle();
        }
        return inlineStyle;
    }
    
    /**
     * Clear inline styles.
     * 
     * @return This widget for chaining
     */
    public RuneWidget clearInlineStyle() {
        if (inlineStyle != null) {
            inlineStyle = null;
            computedStyle = null;
            markDirty();
        }
        return this;
    }
    
    /**
     * Set a single inline style property (convenience method).
     * Inline styles have the highest priority.
     * Type-safe: property type must match value type.
     * 
     * @param <T> The type of the property value
     * @param property The style property to set
     * @param value The value to set
     * @return This widget for chaining
     */
    public <T> RuneWidget setStyle(StyleProperty<T> property, T value) {
        ensureInlineStyle().set(property, value);
        computedStyle = null; // Invalidate cached style
        markDirty();
        return this;
    }
    
    /**
     * Set an inline style property for a specific pseudo-class state.
     * Supports CSS-like pseudo-classes: ":hover", ":focused", ":pressed", ":disabled"
     * 
     * <p>Examples:
     * <pre>
     * button.setStyle(":hover", BACKGROUND_COLOR, Color.LIGHT_GRAY);
     * button.setStyle(":focused", BORDER_COLOR, Color.BLUE);
     * button.setStyle(":focused:hover", BACKGROUND_COLOR, Color.WHITE);
     * </pre>
     * 
     * @param <T> The type of the property value
     * @param pseudoClass The pseudo-class selector (e.g., ":hover", ":focused:hover")
     * @param property The style property to set
     * @param value The value to set
     * @return This widget for chaining
     */
    public <T> RuneWidget setStyle(String pseudoClass, StyleProperty<T> property, T value) {
        ensureInlineStyle().set(pseudoClass, property, value);
        computedStyle = null; // Invalidate cached style
        markDirty();
        return this;
    }
    
    /**
     * Force recomputation of style on next access.
     * Called internally when style-affecting properties change.
     */
    protected void invalidateComputedStyle() {
        computedStyle = null;
        markDirty();
    }
    
    /**
     * Get the computed style for this widget.
     * Uses the stylesheet to compute the final style based on
     * type, classes, ID, inline styles, and current states.
     * 
     * @param ctx Render context with stylesheet
     * @return The computed style (cached until invalidated)
     */
    public RuneStyle getComputedStyle(RuneRenderContext ctx) {
        if (computedStyle == null) {
            computedStyle = ctx.styleSheet.computeStyle(this, activeStates);
        }
        return computedStyle;
    }
    
    @Override
    public void dispose() {
        // Cleanup (subclasses override for specific cleanup)
        parent = null;
    }
    
    @Override
    public String toString() {
        Rectangle bounds = getBounds();
        return String.format("%s{id='%s', bounds=%s, visible=%b, enabled=%b}", 
            getClass().getSimpleName(), id, bounds, visible, enabled);
    }
}
