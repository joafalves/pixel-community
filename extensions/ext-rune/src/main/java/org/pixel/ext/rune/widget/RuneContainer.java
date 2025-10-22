package org.pixel.ext.rune.widget;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.Color;
import org.pixel.ext.rune.core.RuneRenderContext;
import org.pixel.ext.rune.event.RuneKeyEvent;
import org.pixel.ext.rune.event.RuneMouseEvent;
import org.pixel.ext.rune.layout.RuneLayout;
import org.pixel.ext.rune.style.RuneStyle;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static org.pixel.ext.rune.style.StyleProperties.BACKGROUND_COLOR;
import static org.pixel.ext.rune.style.StyleProperties.BORDER_COLOR;

/**
 * A container widget that can hold child widgets.
 * 
 * <p>RuneContainer extends RuneWidget to support child management and layout.
 * This follows the composite pattern - leaf widgets (labels, buttons) don't have
 * children, while containers (panels, windows) do.
 * 
 * <p>Features:
 * <ul>
 *   <li>Child widget management (add, remove, find)</li>
 *   <li>Layout manager support for automatic positioning</li>
 *   <li>Event propagation to children</li>
 *   <li>Optional background color and border radius</li>
 * </ul>
 * 
 * <p>Example:
 * <pre>{@code
 * RuneContainer panel = new RuneContainer("panel");
 * panel.setSize(400, 300);      // Explicit size
 * panel.setPosition(100, 100);
 * panel.setLayout(new VerticalLayout(10));
 * 
 * panel.add(new RuneLabel("label1").text("First Item"));
 * panel.add(new RuneLabel("label2").text("Second Item"));
 * panel.add(new RuneLabel("label3").text("Third Item"));
 * }</pre>
 */
@Getter
@Setter
public class RuneContainer extends RuneWidget {
    
    // Children management
    protected final List<RuneWidget> children = new ArrayList<>();
    
    // Layout
    protected RuneLayout layout;  // Optional layout manager for children
    
    // Visual styling (inline overrides, prefer using style system)
    private Color backgroundColor = null;  // Null = transparent (inline override)
    
    // Overflow & Scrolling
    private Overflow overflow = Overflow.HIDDEN;
    private float scrollX = 0;  // Horizontal scroll offset (0 = leftmost)
    private float scrollY = 0;  // Vertical scroll offset (0 = topmost)
    private float contentWidth = 0;   // Total content width (calculated from children)
    private float contentHeight = 0;  // Total content height (calculated from children)
    
    // Scrollbar styling
    private static final float SCROLLBAR_WIDTH = 12f;
    private static final float SCROLLBAR_MIN_THUMB_SIZE = 30f;
    
    // Scrollbar interaction state
    private boolean isDraggingHScrollbar = false;
    private boolean isDraggingVScrollbar = false;
    private float scrollbarDragStartX = 0;
    private float scrollbarDragStartY = 0;
    private float scrollDragStartOffsetX = 0;
    private float scrollDragStartOffsetY = 0;
    
    /**
     * Create a container with an auto-generated ID.
     * Containers use explicit sizing by default (autoSize = false).
     */
    public RuneContainer() {
        this(null);
    }
    
    /**
     * Create a container with the given ID.
     * Containers use explicit sizing by default (autoSize = false).
     * 
     * @param id Unique identifier (null for auto-generated)
     */
    public RuneContainer(String id) {
        super(id);
        this.autoSize = false;  // Containers use explicit size
    }
    
    // === Child Management ===
    
    /**
     * Add a child widget (fluent).
     * 
     * @param child The widget to add
     * @return This container for method chaining
     */
    public RuneContainer add(RuneWidget child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        if (child.parent != null) {
            throw new IllegalStateException("Widget already has a parent");
        }
        
        child.parent = this;
        children.add(child);
        markLayoutDirty();
        markDirty();
        return this;
    }
    
    /**
     * Remove a child widget (fluent).
     * 
     * @param child The widget to remove
     * @return This container for method chaining
     */
    public RuneContainer remove(RuneWidget child) {
        if (child == null) return this;
        
        if (children.remove(child)) {
            child.parent = null;
            markLayoutDirty();
            markDirty();
        }
        return this;
    }
    
    /**
     * Remove all children (fluent).
     * 
     * @return This container for method chaining
     */
    public RuneContainer clear() {
        for (RuneWidget child : children) {
            child.parent = null;
        }
        children.clear();
        markLayoutDirty();
        markDirty();
        return this;
    }
    
    /**
     * Find child by ID (recursive search).
     * Returns null if not found.
     * 
     * @param id The ID to search for
     * @return The widget with the given ID, or null
     */
    @SuppressWarnings("unchecked")
    public <T extends RuneWidget> T findChild(String id) {
        // Check direct children first
        for (RuneWidget child : children) {
            if (child.id.equals(id)) {
                return (T) child;
            }
        }
        
        // Recursive search in nested containers
        for (RuneWidget child : children) {
            if (child instanceof RuneContainer) {
                T found = ((RuneContainer) child).findChild(id);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    // === Layout Management ===
    
    /**
     * Set the layout manager for this container.
     * The layout manager will automatically position and size child widgets.
     * 
     * @param layout The layout manager, or null to remove layout management
     */
    public void setLayout(RuneLayout layout) {
        this.layout = layout;
        markLayoutDirty();
        markDirty();
    }
    
    /**
     * Set overflow behavior (CSS-like).
     * Controls clipping and scrollbar visibility.
     * 
     * @param overflow Overflow mode (VISIBLE, HIDDEN, SCROLL, etc.)
     */
    public void setOverflow(Overflow overflow) {
        if (this.overflow != overflow) {
            this.overflow = overflow;
            this.scrollX = 0;  // Reset scroll on mode change
            this.scrollY = 0;
            markDirty();
        }
    }
    
    /**
     * Get current horizontal scroll offset (0 = leftmost content visible).
     */
    public float getScrollX() {
        return scrollX;
    }
    
    /**
     * Get current vertical scroll offset (0 = topmost content visible).
     */
    public float getScrollY() {
        return scrollY;
    }
    
    /**
     * Set horizontal scroll offset (will be clamped to valid range).
     */
    public void setScrollX(float scrollX) {
        float maxScrollX = Math.max(0, contentWidth - box.contentBounds.getWidth());
        this.scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
        markDirty();
    }
    
    /**
     * Set vertical scroll offset (will be clamped to valid range).
     */
    public void setScrollY(float scrollY) {
        float maxScrollY = Math.max(0, contentHeight - box.contentBounds.getHeight());
        this.scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        markDirty();
    }
    
    @Override
    protected void onLayout() {
        // If layout manager is set, use it to position children
        if (layout != null && !children.isEmpty()) {
            // Use contentBounds (inner area after padding+border) for layout
            // This ensures children are positioned inside the padding
            layout.layout(this, children, 
                          box.contentBounds.getX(), 
                          box.contentBounds.getY(),
                          box.contentBounds.getWidth(), 
                          box.contentBounds.getHeight());
        }
        // Otherwise children use their explicitly set positions
    }
    
    /**
     * Ensure all children have their box model computed.
     * Must be called BEFORE layout, so children sizes include margins.
     * This recursively ensures ALL descendants have computed boxes.
     * 
     * @param ctx Render context
     */
    private void ensureChildrenBoxComputed(RuneRenderContext ctx) {
        for (RuneWidget child : children) {
            child.ensureBoxComputed(ctx);
            
            // Recursively ensure grandchildren boxes (for nested containers)
            if (child instanceof RuneContainer) {
                ((RuneContainer) child).ensureChildrenBoxComputed(ctx);
            }
        }
    }
    
    @Override
    public void layout() {
        if (!layoutDirty) return;
        
        // Layout this container (positions children)
        onLayout();
        
        // NOTE: We don't layout children here because they need their boxes
        // recomputed after positioning. This is done in draw() after ensureChildrenBoxComputed().
        
        layoutDirty = false;
    }
    
    // === Event Handling ===
    
    @Override
    protected boolean onMouseEvent(RuneMouseEvent event) {
        // Handle scrollbar interaction
        if (overflow == Overflow.SCROLL || overflow == Overflow.SCROLL_HORIZONTAL || overflow == Overflow.SCROLL_VERTICAL) {
            Rectangle bounds = box.contentBounds;
            boolean needsHScroll = (overflow == Overflow.SCROLL || overflow == Overflow.SCROLL_HORIZONTAL) 
                                    && contentWidth > bounds.getWidth();
            boolean needsVScroll = (overflow == Overflow.SCROLL || overflow == Overflow.SCROLL_VERTICAL) 
                                    && contentHeight > bounds.getHeight();
            
            float mx = event.getX();
            float my = event.getY();
            
            // Handle mouse wheel scrolling (only if mouse is over this container)
            if (event.getType() == RuneMouseEvent.Type.SCROLL) {
                // Check if mouse is within container bounds
                if (bounds.contains(mx, my)) {
                    float scrollDelta = event.getScrollDelta();
                    
                    // Scroll vertically if possible, otherwise horizontally
                    if (needsVScroll) {
                        setScrollY(scrollY - scrollDelta * 20); // 20 pixels per scroll unit
                    } else if (needsHScroll) {
                        setScrollX(scrollX - scrollDelta * 20);
                    }
                    
                    return true; // Consume scroll events when scrollable and mouse is over
                }
            }
            
            // Handle scrollbar thumb dragging
            if (event.getType() == RuneMouseEvent.Type.PRESS && event.getButton() == 0) {
                // Check if clicking on horizontal scrollbar thumb
                if (needsHScroll) {
                    float trackY = bounds.getY() + bounds.getHeight() - SCROLLBAR_WIDTH;
                    float trackWidth = bounds.getWidth() - (needsVScroll ? SCROLLBAR_WIDTH : 0);
                    float viewportRatio = bounds.getWidth() / contentWidth;
                    float thumbWidth = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackWidth * viewportRatio);
                    float maxScrollX = contentWidth - bounds.getWidth();
                    float scrollRatio = maxScrollX > 0 ? scrollX / maxScrollX : 0;
                    float thumbX = bounds.getX() + scrollRatio * (trackWidth - thumbWidth);
                    
                    if (isPointOverHScrollThumb(mx, my, thumbX, trackY, thumbWidth, SCROLLBAR_WIDTH)) {
                        isDraggingHScrollbar = true;
                        scrollbarDragStartX = mx;
                        scrollDragStartOffsetX = scrollX;
                        return true;
                    }
                }
                
                // Check if clicking on vertical scrollbar thumb
                if (needsVScroll) {
                    float trackX = bounds.getX() + bounds.getWidth() - SCROLLBAR_WIDTH;
                    float trackHeight = bounds.getHeight() - (needsHScroll ? SCROLLBAR_WIDTH : 0);
                    float viewportRatio = bounds.getHeight() / contentHeight;
                    float thumbHeight = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackHeight * viewportRatio);
                    float maxScrollY = contentHeight - bounds.getHeight();
                    float scrollRatio = maxScrollY > 0 ? scrollY / maxScrollY : 0;
                    float thumbY = bounds.getY() + scrollRatio * (trackHeight - thumbHeight);
                    
                    if (isPointOverVScrollThumb(mx, my, trackX, thumbY, SCROLLBAR_WIDTH, thumbHeight)) {
                        isDraggingVScrollbar = true;
                        scrollbarDragStartY = my;
                        scrollDragStartOffsetY = scrollY;
                        return true;
                    }
                }
            }
            
            // Handle drag motion
            if (event.getType() == RuneMouseEvent.Type.DRAG) {
                if (isDraggingHScrollbar) {
                    float trackWidth = bounds.getWidth() - (needsVScroll ? SCROLLBAR_WIDTH : 0);
                    float viewportRatio = bounds.getWidth() / contentWidth;
                    float thumbWidth = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackWidth * viewportRatio);
                    float maxScrollX = contentWidth - bounds.getWidth();
                    
                    float dragDeltaX = mx - scrollbarDragStartX;
                    float scrollDelta = dragDeltaX / (trackWidth - thumbWidth) * maxScrollX;
                    setScrollX(scrollDragStartOffsetX + scrollDelta);
                    return true;
                }
                
                if (isDraggingVScrollbar) {
                    float trackHeight = bounds.getHeight() - (needsHScroll ? SCROLLBAR_WIDTH : 0);
                    float viewportRatio = bounds.getHeight() / contentHeight;
                    float thumbHeight = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackHeight * viewportRatio);
                    float maxScrollY = contentHeight - bounds.getHeight();
                    
                    float dragDeltaY = my - scrollbarDragStartY;
                    float scrollDelta = dragDeltaY / (trackHeight - thumbHeight) * maxScrollY;
                    setScrollY(scrollDragStartOffsetY + scrollDelta);
                    return true;
                }
            }
            
            // Handle release
            if (event.getType() == RuneMouseEvent.Type.RELEASE) {
                if (isDraggingHScrollbar || isDraggingVScrollbar) {
                    isDraggingHScrollbar = false;
                    isDraggingVScrollbar = false;
                    return true;
                }
            }
        }
        
        return super.onMouseEvent(event);
    }
    
    // === Event Dispatch ===
    
    @Override
    public boolean dispatchMouseEvent(RuneMouseEvent event) {
        if (!visible || !enabled) return false;
        
        // Check if event is within content bounds when overflow is clipping
        if (overflow != Overflow.VISIBLE) {
            Rectangle contentBounds = box.contentBounds;
            float eventX = event.getX();
            float eventY = event.getY();
            
            // If event is outside content bounds, don't dispatch to children
            if (!contentBounds.contains(eventX, eventY)) {
                // Still allow this container to handle event (e.g., for scrollbars)
                if (onMouseEvent(event)) {
                    return true;
                }
                return false;
            }
        }
        
        // Try children first (reverse order = top to bottom in z-order)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).dispatchMouseEvent(event)) {
                return true; // Child consumed event
            }
        }
        
        // Try this container
        if (onMouseEvent(event)) {
            return true; // This container consumed event
        }
        
        return false; // Event not consumed
    }
    
    @Override
    public boolean dispatchKeyEvent(RuneKeyEvent event) {
        if (!visible || !enabled) return false;
        
        // If this container has focus, handle the event
        if (focused && focusable) {
            if (onKeyEvent(event)) {
                return true; // This container consumed event
            }
        }
        
        // Try children
        for (RuneWidget child : children) {
            if (child.dispatchKeyEvent(event)) {
                return true; // Child consumed event
            }
        }
        
        return false; // Event not consumed
    }
    
    // === Styling ===
    
    /**
     * Set the background color.
     * 
     * @param color Background color, or null for transparent
     * @return This container for method chaining
     */
    public RuneContainer setBackgroundColor(Color color) {
        this.backgroundColor = color;
        markDirty();
        return this;
    }
    
    @Override
    public String getTypeName() {
        return "container";
    }
    
    // === Rendering ===
    
    @Override
    protected void onRender(RuneRenderContext ctx) {
        // Get colors from style (already cached in RuneStyle)
        RuneStyle style = getComputedStyle(ctx);
        Color styleBackgroundColor = style.get(BACKGROUND_COLOR);
        Color borderColor = style.get(BORDER_COLOR);
        
        // DEBUG: Log rendering info
        if (box.paddingBounds.getWidth() <= 0 || box.paddingBounds.getHeight() <= 0) {
            System.err.println("WARNING: Container '" + id + "' has zero/negative bounds: " + box.paddingBounds);
            System.err.println("  Total size: " + box.totalSize);
            System.err.println("  Content size: " + box.contentSize);
            System.err.println("  autoSize: " + autoSize + ", width: " + width + ", height: " + height);
        }
        
        // Render background if set (explicit property overrides style)
        Color bgColor = backgroundColor != null ? backgroundColor : styleBackgroundColor;
        if (bgColor != null) {
            ctx.getCanvas().rect(
                    box.paddingBounds.getX(), 
                    box.paddingBounds.getY(),
                    box.paddingBounds.getWidth(),
                    box.paddingBounds.getHeight()
            )
            .withFill(bgColor)
            .withRoundedCorners(box.borderRadius)
            .apply();
        }
        
        // Render border if set
        if (box.borderWidth > 0 && borderColor != null) {
            ctx.getCanvas().rect(
                    box.paddingBounds.getX(),
                    box.paddingBounds.getY(),
                    box.paddingBounds.getWidth(),
                    box.paddingBounds.getHeight()
            )
            .withStroke(box.borderWidth, borderColor)
            .withRoundedCorners(box.borderRadius)
            .apply();
        }
        
        // Children are rendered automatically by the framework
    }
    
    // === Lifecycle ===
    
    @Override
    public void update(org.pixel.commons.DeltaTime delta) {
        if (!visible) return;
        
        onUpdate(delta);
        
        // Update all children
        for (RuneWidget child : children) {
            child.update(delta);
        }
    }
    
    @Override
    public void validateMouseStates(float mouseX, float mouseY) {
        // Validate this container's state
        super.validateMouseStates(mouseX, mouseY);
        
        // Container is responsible for validating its children
        for (RuneWidget child : children) {
            child.validateMouseStates(mouseX, mouseY);
        }
    }
    
    @Override
    public void draw(RuneRenderContext ctx, boolean force) {
        if (!visible) return;
        
        // Ensure box model is computed before layout/render
        ensureBoxComputed(ctx);
        
        // Ensure all children have their boxes computed (for margin-aware layout)
        ensureChildrenBoxComputed(ctx);
        
        // Recalculate layout if needed
        if (layoutDirty) {
            // Step 1: Run this container's layout (positions children)
            layout();
            
            // Step 2: Recompute children boxes with new positions
            // This ensures child containers have correct bounds before their own onLayout() runs
            ensureChildrenBoxComputed(ctx);
            
            // Step 3: Now recursively layout children (they now have correct positions/bounds)
            for (RuneWidget child : children) {
                child.layout();
            }
        }
        
        // Calculate content size (bounding box of all children)
        calculateContentSize();
        
        // Render if forced (game mode) OR if dirty
        if (force || dirty) {
            onRender(ctx);
            dirty = false;
        }
        
        // Draw children with clipping and scrolling
        Rectangle bounds = box.contentBounds;  // Use content bounds (inside padding)
        boolean needsClipping = overflow != Overflow.VISIBLE;
        boolean needsHScroll = (overflow == Overflow.SCROLL || overflow == Overflow.SCROLL_HORIZONTAL) 
                                && contentWidth > bounds.getWidth();
        boolean needsVScroll = (overflow == Overflow.SCROLL || overflow == Overflow.SCROLL_VERTICAL) 
                                && contentHeight > bounds.getHeight();
        
        ctx.getCanvas().save();
        
        // Apply clipping if needed
        if (needsClipping) {
            // Reduce clip area if scrollbars are present
            float clipWidth = bounds.getWidth() - (needsVScroll ? SCROLLBAR_WIDTH : 0);
            float clipHeight = bounds.getHeight() - (needsHScroll ? SCROLLBAR_WIDTH : 0);
            
            ctx.getCanvas().clipRect(
                bounds.getX(),
                bounds.getY(),
                clipWidth,
                clipHeight
            );
        }
        
        // Apply scroll translation for children
        if (overflow != Overflow.VISIBLE && overflow != Overflow.HIDDEN) {
            ctx.getCanvas().translate(-scrollX, -scrollY);
        }
        
        // Draw children (sorted by z-index)
        List<RuneWidget> sortedChildren = new ArrayList<>(children);
        sortedChildren.sort((a, b) -> Integer.compare(a.zIndex, b.zIndex));
        for (RuneWidget child : sortedChildren) {
            child.draw(ctx, force);
        }
        
        // Restore to undo translation (but keep outer save for scrollbar drawing)
        if (overflow != Overflow.VISIBLE && overflow != Overflow.HIDDEN) {
            ctx.getCanvas().restore();
            ctx.getCanvas().save();  // Save again for scrollbar clipping
        }
        
        // Draw scrollbars on top of content (no translation, in container coordinates)
        if (needsHScroll) {
            drawHorizontalScrollbar(ctx, bounds);
        }
        if (needsVScroll) {
            drawVerticalScrollbar(ctx, bounds);
        }
        
        ctx.getCanvas().restore();
    }
    
    /**
     * Calculate the bounding box of all children (content size).
     * Used to determine scrollbar visibility and thumb sizes.
     */
    private void calculateContentSize() {
        if (children.isEmpty()) {
            contentWidth = 0;
            contentHeight = 0;
            return;
        }
        
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        
        for (RuneWidget child : children) {
            if (!child.isVisible()) continue;
            
            Rectangle childBounds = child.getBounds();
            minX = Math.min(minX, childBounds.getX());
            minY = Math.min(minY, childBounds.getY());
            maxX = Math.max(maxX, childBounds.getX() + childBounds.getWidth());
            maxY = Math.max(maxY, childBounds.getY() + childBounds.getHeight());
        }
        
        // Content size is relative to content bounds origin
        float contentOriginX = box.contentBounds.getX();
        float contentOriginY = box.contentBounds.getY();
        
        contentWidth = maxX - contentOriginX;
        contentHeight = maxY - contentOriginY;
    }
    
    /**
     * Draw horizontal scrollbar.
     */
    private void drawHorizontalScrollbar(RuneRenderContext ctx, Rectangle bounds) {
        float trackX = bounds.getX();
        float trackY = bounds.getY() + bounds.getHeight() - SCROLLBAR_WIDTH;
        float trackWidth = bounds.getWidth() - (contentHeight > bounds.getHeight() ? SCROLLBAR_WIDTH : 0);
        float trackHeight = SCROLLBAR_WIDTH;
        
        // Get colors from RuneDarkTheme
        Color trackColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_BG;
        Color thumbColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_THUMB;
        Color thumbHoverColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_THUMB_HOVER;
        
        // Draw track
        ctx.getCanvas().rect(trackX, trackY, trackWidth, trackHeight)
            .withFill(trackColor)
            .apply();
        
        // Calculate thumb size and position
        float viewportRatio = bounds.getWidth() / contentWidth;
        float thumbWidth = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackWidth * viewportRatio);
        float maxScrollX = contentWidth - bounds.getWidth();
        float scrollRatio = maxScrollX > 0 ? scrollX / maxScrollX : 0;
        float thumbX = trackX + scrollRatio * (trackWidth - thumbWidth);
        
        // Check if mouse is over thumb
        float pointerX = ctx.getUi().getInput() != null ? ctx.getUi().getInput().getPointerX() : 0;
        float pointerY = ctx.getUi().getInput() != null ? ctx.getUi().getInput().getPointerY() : 0;
        boolean isHover = isPointOverHScrollThumb(pointerX, pointerY,
                                                   thumbX, trackY, thumbWidth, trackHeight);
        
        // Draw thumb
        Color activeThumbColor = (isHover || isDraggingHScrollbar) ? thumbHoverColor : thumbColor;
        ctx.getCanvas().rect(thumbX, trackY + 2, thumbWidth, trackHeight - 4)
            .withFill(activeThumbColor)
            .withRoundedCorners(3)
            .apply();
    }
    
    /**
     * Draw vertical scrollbar.
     */
    private void drawVerticalScrollbar(RuneRenderContext ctx, Rectangle bounds) {
        float trackX = bounds.getX() + bounds.getWidth() - SCROLLBAR_WIDTH;
        float trackY = bounds.getY();
        float trackWidth = SCROLLBAR_WIDTH;
        float trackHeight = bounds.getHeight() - (contentWidth > bounds.getWidth() ? SCROLLBAR_WIDTH : 0);
        
        // Get colors from RuneDarkTheme
        Color trackColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_BG;
        Color thumbColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_THUMB;
        Color thumbHoverColor = org.pixel.ext.rune.theme.RuneDarkTheme.SCROLLBAR_THUMB_HOVER;
        
        // Draw track
        ctx.getCanvas().rect(trackX, trackY, trackWidth, trackHeight)
            .withFill(trackColor)
            .apply();
        
        // Calculate thumb size and position
        float viewportRatio = bounds.getHeight() / contentHeight;
        float thumbHeight = Math.max(SCROLLBAR_MIN_THUMB_SIZE, trackHeight * viewportRatio);
        float maxScrollY = contentHeight - bounds.getHeight();
        float scrollRatio = maxScrollY > 0 ? scrollY / maxScrollY : 0;
        float thumbY = trackY + scrollRatio * (trackHeight - thumbHeight);
        
        // Check if mouse is over thumb
        float pointerX = ctx.getUi().getInput() != null ? ctx.getUi().getInput().getPointerX() : 0;
        float pointerY = ctx.getUi().getInput() != null ? ctx.getUi().getInput().getPointerY() : 0;
        boolean isHover = isPointOverVScrollThumb(pointerX, pointerY,
                                                   trackX, thumbY, trackWidth, thumbHeight);
        
        // Draw thumb
        Color activeThumbColor = (isHover || isDraggingVScrollbar) ? thumbHoverColor : thumbColor;
        ctx.getCanvas().rect(trackX + 2, thumbY, trackWidth - 4, thumbHeight)
            .withFill(activeThumbColor)
            .withRoundedCorners(3)
            .apply();
    }
    
    private boolean isPointOverHScrollThumb(float px, float py, float thumbX, float thumbY, float thumbW, float thumbH) {
        return px >= thumbX && px <= thumbX + thumbW && py >= thumbY && py <= thumbY + thumbH;
    }
    
    private boolean isPointOverVScrollThumb(float px, float py, float thumbX, float thumbY, float thumbW, float thumbH) {
        return px >= thumbX && px <= thumbX + thumbW && py >= thumbY && py <= thumbY + thumbH;
    }
    
    @Override
    public void dispose() {
        // Dispose all children
        for (RuneWidget child : children) {
            child.dispose();
        }
        children.clear();
        
        super.dispose();
    }
}
