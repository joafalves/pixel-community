package org.pixel.ext.rune;

import lombok.Getter;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.ext.rune.core.RuneRenderContext;
import org.pixel.ext.rune.event.RuneKeyEvent;
import org.pixel.ext.rune.event.RuneMouseEvent;
import org.pixel.ext.rune.style.RuneStyleSheet;
import org.pixel.ext.rune.widget.RuneWidget;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.text.SdfFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RuneUI - Root manager for the Rune GUI system.
 * 
 * <p>This is the main entry point for using Rune. Create a RuneUI instance,
 * add widgets to it, then call update() and render() each frame.
 * 
 * <p>Example usage:
 * <pre>
 * RuneUI ui = new RuneUI(canvas);
 * 
 * RuneLabel label = new RuneLabel("greeting");
 * label.setText("Hello Rune!");
 * ui.add(label);
 * 
 * // In game loop:
 * ui.update(delta);
 * ui.render(delta);
 * </pre>
 */
@Getter
public class RuneUI implements Disposable, Updatable {
    
    // Modal dimming color (static to avoid GC)
    private static final Logger log = LoggerFactory.getLogger(RuneUI.class);
    private static final Color MODAL_DIM_COLOR = new Color(0, 0, 0, 0.5f);
    
    private Canvas canvas;
    private RuneStyleSheet styleSheet;
    private RuneInput input;  // Input provider (platform-specific)
    private ContentManager content;  // Shared content manager for loading Rune resources
    private final List<RuneWidget> rootWidgets = new ArrayList<>();
    private final List<RuneWidget> modalStack = new ArrayList<>();
    
    // Font registry (name/family -> SdfFont)
    private final Map<String, SdfFont> fonts = new HashMap<>();
    
    // Viewport dimensions
    private float viewportWidth;
    private float viewportHeight;
    
    // Rendering mode
    private boolean gameMode = true; // true = always redraw (for games), false = dirty-flag optimization (for tools)
    
    // Input state (enhanced for better event tracking)
    private float mouseX;
    private float mouseY;
    private float prevMouseX;
    private float prevMouseY;
    private boolean mousePressed;
    private boolean mousePressedLastFrame;
    private int mouseButton = 0; // 0 = left, 1 = right, 2 = middle
    private float scrollDelta = 0f;
    
    // Focus management
    private RuneWidget focusedWidget;
    
    /**
     * Create a new RuneUI with the given viewport dimensions.
     * Uses default dark theme stylesheet.
     */
    public RuneUI(float viewportWidth, float viewportHeight) {
        this(viewportWidth, viewportHeight, org.pixel.ext.rune.theme.RuneDarkTheme.create());
    }
    
    /**
     * Create a new RuneUI with custom stylesheet.
     * RuneUI creates and manages Canvas internally.
     */
    public RuneUI(float viewportWidth, float viewportHeight, RuneStyleSheet styleSheet) {
        if (styleSheet == null) {
            throw new IllegalArgumentException("StyleSheet cannot be null");
        }
        
        this.canvas = Canvas.create((int) viewportWidth, (int) viewportHeight);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.styleSheet = styleSheet;
        this.content = ContentManager.create();
        
        // Load default resources (fonts, etc.)
        loadResources();
    }
    
    /**
     * Update the viewport dimensions.
     * Call this when the window is resized.
     * Recreates the internal Canvas with new dimensions.
     */
    public void setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;

        canvas.setViewport(width, height);
        
        // Trigger layout recalculation
        layout();
    }
    
    /**
     * Swap the current stylesheet with a new one.
     * This allows runtime theme switching (e.g., dark/light mode toggle).
     * 
     * @param newStyleSheet The new stylesheet to use
     */
    public void swapStyleSheet(RuneStyleSheet newStyleSheet) {
        if (newStyleSheet == null) {
            throw new IllegalArgumentException("StyleSheet cannot be null");
        }
        
        this.styleSheet = newStyleSheet;
        
        // Mark all widgets dirty to force redraw with new stylesheet
        for (RuneWidget widget : rootWidgets) {
            widget.markDirty();
        }
        for (RuneWidget modal : modalStack) {
            modal.markDirty();
        }
    }
    
    /**
     * Set game mode rendering.
     * 
     * @param gameMode true = always redraw all widgets (for games where screen clears each frame),
     *                 false = only redraw dirty widgets (for standalone tools with persistent background)
     */
    public void setGameMode(boolean gameMode) {
        this.gameMode = gameMode;
    }
    
    /**
     * Register fonts by name/family.
     * Fonts can then be referenced in themes by name (like CSS font-family).
     * 
     * @param fonts Map of font name/family to SdfFont instances
     */
    public void setFonts(Map<String, SdfFont> fonts) {
        this.fonts.clear();
        if (fonts != null) {
            this.fonts.putAll(fonts);
        }
    }
    
    /**
     * Register a single font by name/family.
     * 
     * @param name Font name/family identifier
     * @param font SdfFont instance
     */
    public void setFont(String name, SdfFont font) {
        if (name != null && font != null) {
            fonts.put(name, font);
        }
    }
    
    /**
     * Get font by name/family from the registry.
     * Falls back to first available font if name not found.
     * 
     * @param fontFamily Font name/family to look up
     * @return SdfFont instance, or null if no fonts registered
     */
    public SdfFont getFont(String fontFamily) {
        if (fontFamily != null && fonts.containsKey(fontFamily)) {
            return fonts.get(fontFamily);
        }
        
        // Fallback to first available font
        if (!fonts.isEmpty()) {
            return fonts.values().iterator().next();
        }
        
        return null;
    }
    
    /**
     * Load default Rune resources (fonts, icons, etc.).
     * This is called automatically during RuneUI initialization.
     * Uses the shared ContentManager instance to load bundled resources.
     */
    private void loadResources() {
        try {
            // Load default Roboto font at 32px base size
            // SDF fonts scale beautifully without mipmaps - texture filtering handles quality
            FontImporterSettings settings = new FontImporterSettings(32, 2);
            SdfFont robotoFont = content.load("__rune__/font/roboto-regular.ttf", 
                                               SdfFont.class, settings);
            
            // Register as both "roboto" and "default"
            fonts.put("roboto", robotoFont);
            fonts.put("default", robotoFont);
            
            // Future: Load other resources here (icons, cursors, etc.)
            
        } catch (Exception e) {
            log.error("Failed to load default Rune resources", e);
        }
    }
    
    /**
     * Add a root-level widget.
     */
    public void add(RuneWidget widget) {
        if (widget == null) {
            throw new IllegalArgumentException("Widget cannot be null");
        }
        
        rootWidgets.add(widget);
    }
    
    /**
     * Remove a root-level widget.
     */
    public void remove(RuneWidget widget) {
        rootWidgets.remove(widget);
    }
    
    /**
     * Clear all widgets.
     */
    public void clear() {
        rootWidgets.clear();
        modalStack.clear();
        focusedWidget = null;
    }
    
    /**
     * Push a modal widget (blocks interaction with widgets below).
     */
    public void pushModal(RuneWidget widget) {
        modalStack.add(widget);
    }
    
    /**
     * Pop the topmost modal widget.
     */
    public void popModal() {
        if (!modalStack.isEmpty()) {
            modalStack.remove(modalStack.size() - 1);
        }
    }
    
    /**
     * Set the input provider for this RuneUI.
     * The input provider handles platform-specific input polling.
     * 
     * <p>Usage:
     * <pre>
     * // Desktop:
     * DesktopRuneInput input = new DesktopRuneInput();
     * gui.setInput(input);
     * 
     * // RuneUI will automatically poll input during update()
     * </pre>
     * 
     * @param input Platform-specific input provider
     */
    public void setInput(RuneInput input) {
        this.input = input;
    }
    
    /**
     * Get the current input provider.
     * 
     * @return The input provider, or null if not set
     */
    public RuneInput getInput() {
        return input;
    }
    
    // === Focus Management ===
    
    /**
     * Set keyboard focus to a specific widget.
     * Clears focus from previously focused widget.
     * 
     * @param widget The widget to focus, or null to clear focus
     */
    public void setFocus(RuneWidget widget) {
        if (focusedWidget == widget) {
            return; // Already focused
        }
        
        // Clear previous focus
        if (focusedWidget != null) {
            focusedWidget.setFocused(false);
        }
        
        focusedWidget = widget;
        
        // Set new focus (only if widget is focusable)
        if (widget != null && widget.isFocusable()) {
            widget.setFocused(true);
        } else if (widget != null) {
            // Widget not focusable, clear the focus reference
            focusedWidget = null;
        }
    }
    
    /**
     * Clear keyboard focus from any widget.
     */
    public void clearFocus() {
        setFocus(null);
    }
    
    /**
     * Get the currently focused widget.
     * 
     * @return The focused widget, or null if no widget has focus
     */
    public RuneWidget getFocusedWidget() {
        return focusedWidget;
    }
    
    /**
     * Update input state manually (legacy/fallback method).
     * Prefer using {@link #setInput(RuneInput)} with a platform input provider.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param mousePressed Whether mouse button is pressed
     * @deprecated Use {@link #setInput(RuneInput)} instead
     */
    @Deprecated
    public void setInput(float mouseX, float mouseY, boolean mousePressed) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mousePressed = mousePressed;
        this.mouseButton = 0; // Default to left button
    }
    
    /**
     * Update input state with mouse button information.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     * @param mousePressed Whether mouse button is pressed
     * @param mouseButton Which button (0=left, 1=right, 2=middle)
     */
    public void setInput(float mouseX, float mouseY, boolean mousePressed, int mouseButton) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mousePressed = mousePressed;
        this.mouseButton = mouseButton;
    }
    
    /**
     * Set scroll input (mouse wheel).
     * 
     * @param scrollDelta Scroll amount (positive = up, negative = down)
     */
    public void setScrollInput(float scrollDelta) {
        this.scrollDelta = scrollDelta;
    }
    
    /**
     * Update all widgets.
     * Automatically polls input from the input provider if set.
     */
    @Override
    public void update(DeltaTime delta) {
        // Auto-poll input if provider is set
        if (input != null) {
            pollInputFromProvider();
        }
        
        // Process input events
        processInputEvents();
        
        // Update modals first (they're on top)
        for (RuneWidget modal : modalStack) {
            modal.update(delta);
        }
        
        // Update root widgets
        for (RuneWidget widget : rootWidgets) {
            widget.update(delta);
        }
        
        // Validate mouse states after update, before render
        // This ensures hover/pressed states are always correct even with fast mouse movement
        // Each container is responsible for validating its own children
        for (RuneWidget modal : modalStack) {
            modal.validateMouseStates(mouseX, mouseY);
        }
        for (RuneWidget widget : rootWidgets) {
            widget.validateMouseStates(mouseX, mouseY);
        }
        
        mousePressedLastFrame = mousePressed;
    }
    
    /**
     * Poll input from the input provider.
     * Called automatically during update() if input provider is set.
     */
    private void pollInputFromProvider() {
        prevMouseX = mouseX;
        prevMouseY = mouseY;
        
        mouseX = input.getPointerX();
        mouseY = input.getPointerY();
        mousePressed = input.isPointerDown();
        mouseButton = 0; // Primary button
        scrollDelta = input.getScrollDelta();
        
        // TODO: Add secondary/tertiary button support
        // TODO: Add keyboard event polling for focused widgets
    }
    
    /**
     * Draw all widgets.
     * 
     * @param delta Delta time for animations
     */
    public void draw(DeltaTime delta) {
        canvas.begin();
        
        RuneRenderContext ctx = new RuneRenderContext(canvas, styleSheet, this, null);
        
        // Draw root widgets (sorted by z-index, force redraw if in game mode)
        List<RuneWidget> sortedRoots = new ArrayList<>(rootWidgets);
        sortedRoots.sort((a, b) -> Integer.compare(a.getZIndex(), b.getZIndex()));
        
        for (RuneWidget widget : sortedRoots) {
            widget.draw(ctx, gameMode);
        }
        
        // Draw modals on top (also sorted by z-index)
        for (RuneWidget modal : modalStack) {
            // Darken background for modal (use static color)
            canvas.rect(0, 0, viewportWidth, viewportHeight)
                .withFill(MODAL_DIM_COLOR)
                .apply();
            
            modal.draw(ctx, gameMode);
        }
        
        canvas.end();
    }
    
    /**
     * Process input events and dispatch to widgets.
     */
    private void processInputEvents() {
        // Calculate mouse movement delta
        float deltaX = mouseX - prevMouseX;
        float deltaY = mouseY - prevMouseY;
        boolean mouseMoved = deltaX != 0 || deltaY != 0;
        
        // Check for mouse press
        if (mousePressed && !mousePressedLastFrame) {
            RuneMouseEvent event = RuneMouseEvent.obtain(RuneMouseEvent.Type.PRESS, mouseX, mouseY);
            event.setButton(mouseButton);
            dispatchMouseEvent(event);
            event.dispose();
        }
        
        // Check for mouse release
        if (!mousePressed && mousePressedLastFrame) {
            RuneMouseEvent event = RuneMouseEvent.obtain(RuneMouseEvent.Type.RELEASE, mouseX, mouseY);
            event.setButton(mouseButton);
            dispatchMouseEvent(event);
            event.dispose();
        }
        
        // Check for drag (mouse moved while pressed)
        if (mousePressed && mouseMoved) {
            RuneMouseEvent dragEvent = RuneMouseEvent.obtainDrag(mouseX, mouseY, deltaX, deltaY);
            dragEvent.setButton(mouseButton);
            dispatchMouseEvent(dragEvent);
            dragEvent.dispose();
        }
        
        // Mouse move (only send if mouse moved and not dragging)
        if (mouseMoved && !mousePressed) {
            RuneMouseEvent moveEvent = RuneMouseEvent.obtain(RuneMouseEvent.Type.MOVE, mouseX, mouseY);
            dispatchMouseEvent(moveEvent);
            moveEvent.dispose();
        }
        
        // Scroll event
        if (scrollDelta != 0) {
            RuneMouseEvent scrollEvent = RuneMouseEvent.obtainScroll(mouseX, mouseY, scrollDelta);
            dispatchMouseEvent(scrollEvent);
            scrollEvent.dispose();
            scrollDelta = 0; // Reset after dispatching
        }
        
        // Update previous mouse position
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }
    
    /**
     * Dispatch mouse event to widgets (top to bottom).
     */
    private void dispatchMouseEvent(RuneMouseEvent event) {
        RuneWidget targetWidget = null;
        
        // Try modals first (topmost first)
        for (int i = modalStack.size() - 1; i >= 0; i--) {
            if (modalStack.get(i).dispatchMouseEvent(event)) {
                targetWidget = findEventTarget(modalStack.get(i), event);
                break; // Event consumed
            }
        }
        
        // Try root widgets (reverse order = top to bottom) if not consumed by modal
        if (targetWidget == null) {
            for (int i = rootWidgets.size() - 1; i >= 0; i--) {
                if (rootWidgets.get(i).dispatchMouseEvent(event)) {
                    targetWidget = findEventTarget(rootWidgets.get(i), event);
                    break; // Event consumed
                }
            }
        }
        
        // Handle focus-on-click: if this was a PRESS event and a widget handled it, focus that widget
        if (event.getType() == RuneMouseEvent.Type.PRESS && targetWidget != null && targetWidget.isFocusable()) {
            setFocus(targetWidget);
        }
    }
    
    /**
     * Find the deepest widget that consumed an event (used for focus management).
     * This recursively searches for the actual target, not just the top-level container.
     */
    private RuneWidget findEventTarget(RuneWidget root, RuneMouseEvent event) {
        // For now, just return the root widget that consumed it
        // TODO: In the future, could recursively search children to find the deepest target
        return root;
    }
    
    /**
     * Dispatch keyboard event to focused widget.
     */
    public void dispatchKeyEvent(RuneKeyEvent event) {
        if (focusedWidget != null) {
            focusedWidget.dispatchKeyEvent(event);
        }
    }
    
    /**
     * Find widget by ID (searches all roots and modals).
     */
    public <T extends RuneWidget> T findWidget(String id) {
        // Search root widgets
        for (RuneWidget widget : rootWidgets) {
            if (widget.getId().equals(id)) {
                return (T) widget;
            }
            // If it's a container, search its children
            if (widget instanceof org.pixel.ext.rune.widget.RuneContainer) {
                T found = ((org.pixel.ext.rune.widget.RuneContainer) widget).findChild(id);
                if (found != null) {
                    return found;
                }
            }
        }
        
        // Search modals
        for (RuneWidget modal : modalStack) {
            if (modal.getId().equals(id)) {
                return (T) modal;
            }
            // If it's a container, search its children
            if (modal instanceof org.pixel.ext.rune.widget.RuneContainer) {
                T found = ((org.pixel.ext.rune.widget.RuneContainer) modal).findChild(id);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Trigger layout recalculation for all widgets.
     */
    public void layout() {
        for (RuneWidget widget : rootWidgets) {
            widget.layout();
        }
        for (RuneWidget modal : modalStack) {
            modal.layout();
        }
    }
    
    @Override
    public void dispose() {
        // Dispose Canvas (Rune manages it)
        if (canvas != null) {
            canvas.dispose();
            canvas = null;
        }
        
        // Dispose ContentManager (releases loaded resources)
        if (content != null) {
            content.dispose();
            content = null;
        }
        
        // Dispose all widgets
        for (RuneWidget widget : rootWidgets) {
            widget.dispose();
        }
        for (RuneWidget modal : modalStack) {
            modal.dispose();
        }
        
        rootWidgets.clear();
        modalStack.clear();
        fonts.clear();
        focusedWidget = null;
    }
}
