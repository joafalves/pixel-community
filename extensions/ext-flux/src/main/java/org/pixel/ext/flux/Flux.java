package org.pixel.ext.flux;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Flux - Immediate Mode GUI system for Pixel Framework.
 *
 * <p>Flux embodies the constant state of change and redrawing inherent to immediate mode.
 * UI is declared every frame, making state management simple and intuitive.
 *
 * <p>Features:
 * <ul>
 *   <li>Zero-GC widget state pooling</li>
 *   <li>Canvas-based rendering (platform-agnostic)</li>
 *   <li>Themeable via {@link FluxTheme}</li>
 *   <li>Simple, declarative API</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * Flux flux = new Flux(canvas);
 *
 * flux.begin(mouseX, mouseY, mouseDown);
 *
 * if (flux.button("Click Me", 100, 100, 200, 40)) {
 *     System.out.println("Button clicked!");
 * }
 *
 * flux.label("Score: " + score, 100, 150);
 *
 * flux.end();
 * </pre>
 */
public class Flux implements Disposable {

    private final Canvas canvas;
    private FluxTheme theme;
    private int viewportWidth;
    private int viewportHeight;

    // === Input State ===
    private final Vector2 mousePos = new Vector2();
    private boolean mouseClicked; // From Mouse.isMouseButtonPressed() via context (edge)
    private boolean mouseHeld; // From Mouse.isMouseButtonDown() via context (continuous)
    private boolean mouseHeldPrev; // Previous frame's held state (for release detection)
    private boolean mouseReleased; // Detected release edge
    private String textInputThisFrame = "";

    // === Layout State ===
    private float cursorX;
    private float cursorY;
    private float lineHeight; // Height of current line (max widget height)
    private float lastWidgetX; // X position where last widget was drawn
    private float lastWidgetY; // Y position where last widget was drawn
    private float lastWidgetWidth; // Width of last drawn widget (for sameLine)
    private float lastWidgetHeight; // Height of last drawn widget (for sameLine)
    private float spacing;
    private final Stack<LayoutContext> layoutStack = new Stack<>();

    // === Widget State ===
    private final Map<String, WidgetState> widgetStates = new HashMap<>();
    private final Map<String, PanelState> panelStates = new HashMap<>(); // Panel ID -> panel state
    private final Map<String, CollapsingState> collapsingStates = new HashMap<>(); // Collapsing ID -> state
    private String focusedWidgetId = null;
    private String hoveredWidgetId = null;
    private String activeWidgetId = null; // Currently being clicked
    private String currentPanelId = null; // ID of currently active panel (for scroll)

    // === Frame State ===
    private boolean frameActive = false;
    private float mouseWheelDelta = 0; // This frame's mouse wheel delta

    // === Scrollbar Drag State ===
    private String scrollbarDragging = null; // ID of panel whose scrollbar is being dragged
    private float scrollbarDragStartMouseY; // Mouse Y when drag started
    private float scrollbarDragStartOffset; // Scroll offset when drag started

    // === Panel Drag State ===
    private String panelDragging = null; // ID of panel being dragged
    private float panelDragStartMouseX; // Mouse X when panel drag started
    private float panelDragStartMouseY; // Mouse Y when panel drag started
    private float panelDragStartX; // Panel X when drag started
    private float panelDragStartY; // Panel Y when drag started

    // === Menu State ===
    private boolean menuBarActive = false;
    private String openMenuId = null; // ID of currently open menu
    private boolean menuOpenedThisFrame = false; // Flag to prevent immediate close
    private boolean insideMenuPopup = false; // Flag to preserve cursor X inside menu popups
    private float menuPopupStartY = 0; // Y position where menu content starts (for height calculation)
    private int menuItemsStartIndex = -1; // Index in rootCommands where menu items start
    private float menuBarCursorX = 0; // Saved cursor X position in menu bar (before entering popup)
    private float menuPopupCursorX = 0; // Cursor X position inside popup (preserved across lines)

    // === Panel Focus/Z-Order (like Dear ImGui) ===
    private final List<String> panelFocusOrder = new ArrayList<>(); // Panel IDs in focus order (first = back, last = front)
    private final List<String> panelsThisFrame = new ArrayList<>(); // Panels declared this frame
    private final Map<String, PanelRenderData> panelRenderQueue = new HashMap<>(); // Deferred panel rendering
    private final List<Runnable> rootCommands = new ArrayList<>(); // Commands outside any panel
    private final Stack<String> recordingPanelStack = new Stack<>(); // Stack of panel IDs for nested panels (top = current)
    private String hotPanelId = null; // ID of panel mouse is currently over (topmost in focus order)
    private String previousHotPanelId = null; // Hot panel from previous frame (for consistent event handling)
    private boolean enablePanelOrdering = true; // Toggle for panel z-order management
    private boolean shouldBringHotPanelToFront = false; // Flag to bring hot panel to front after all panels declared

    // === Menu State ===
    private float menuBarHeight = 0;
    private float menuPopupX = 0;
    private float menuPopupY = 0;

    /**
     * Widget persistent state across frames.
     */
    private static class WidgetState {
        boolean focused;
        String textValue = "";
        int cursorPosition = 0;
    }

    /**
     * Panel persistent state across frames.
     */
    private static class PanelState {
        float scrollOffset = 0;
        float contentHeight = 0; // Total content height (for scroll range calculation)
        float x = 0; // Panel X position (for draggable panels)
        float y = 0; // Panel Y position (for draggable panels)
        float width = 0; // Panel width (stored for hot panel pre-computation)
        float height = 0; // Panel height (stored for hot panel pre-computation)
        boolean positionInitialized = false; // Whether position has been set
    }

    /**
     * Collapsing section persistent state across frames.
     */
    private static class CollapsingState {
        boolean open = false;
        boolean initialized = false;
    }

    /**
     * Deferred panel render data.
     * Panels are collected during the immediate pass, then rendered in focus order.
     */
    private static class PanelRenderData {
        String id;
        String parentId; // ID of parent panel (null if root-level)
        List<Runnable> commands = new ArrayList<>(); // All drawing commands for this panel

        // Captured panel visual data for deferred rendering
        float x, y, width, height;
        String title;
        float titleBarHeight, contentStartY;
        boolean hasBorder, hasBackground, hasScroll, hasScrollbar;
        float contentWidth;
        PanelState state;

        PanelRenderData(String id) {
            this.id = id;
        }
    }

    /**
     * Layout context for nested containers.
     */
    private static class LayoutContext {
        float cursorX;
        float cursorY;
        float lineHeight;
        float contentX; // Content area X offset
        float contentY; // Content area Y offset
        float contentWidth; // Available width for content
        float contentHeight; // Available height for content
        float panelX; // Panel X position (for scrollbar positioning)
        float panelWidth; // Panel full width (for scrollbar positioning)
        boolean hasScroll; // Whether scrolling is enabled
        boolean hasScrollbar; // Whether to show scrollbar

        LayoutContext(float cursorX, float cursorY, float lineHeight,
                     float contentX, float contentY, float contentWidth, float contentHeight,
                     float panelX, float panelWidth,
                     boolean hasScroll, boolean hasScrollbar) {
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.lineHeight = lineHeight;
            this.contentX = contentX;
            this.contentY = contentY;
            this.contentWidth = contentWidth;
            this.contentHeight = contentHeight;
            this.panelX = panelX;
            this.panelWidth = panelWidth;
            this.hasScroll = hasScroll;
            this.hasScrollbar = hasScrollbar;
        }
    }

    /**
     * Create a new Flux instance with the given viewport size.
     * Creates an internal Canvas for rendering.
     *
     * @param viewportWidth Initial viewport width
     * @param viewportHeight Initial viewport height
     */
    public Flux(int viewportWidth, int viewportHeight) {
        this.canvas = Canvas.create(viewportWidth, viewportHeight);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.theme = new DefaultFluxTheme();
        this.spacing = theme.getItemSpacing();
    }

    /**
     * Update viewport size (e.g., when window is resized).
     *
     * @param width New viewport width
     * @param height New viewport height
     */
    public void setViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        canvas.setViewport(width, height);
    }

    /**
     * Get the current theme.
     *
     * @return The current theme
     */
    public FluxTheme getTheme() {
        return theme;
    }

    /**
     * Set the theme.
     *
     * @param theme The theme to use
     */
    public void setTheme(FluxTheme theme) {
        this.theme = theme;
        this.spacing = theme.getItemSpacing();
    }

    /**
     * Enable or disable automatic panel z-ordering.
     * When enabled (default), clicking a panel brings it to front.
     * When disabled, panels render in declaration order.
     *
     * @param enable True to enable panel ordering (like Dear ImGui)
     */
    public void setEnablePanelOrdering(boolean enable) {
        this.enablePanelOrdering = enable;
    }

    /**
     * Check if panel ordering is enabled.
     *
     * @return True if panel ordering is enabled
     */
    public boolean isEnablePanelOrdering() {
        return enablePanelOrdering;
    }

    /**
     * Begin a new GUI frame with context.
     * Must be called before any widget methods.
     *
     * @param ctx Context for this frame (input, viewport, camera, etc.)
     */
    public void begin(FluxContext ctx) {
        if (frameActive) {
            throw new IllegalStateException("begin() called twice without end()");
        }
        frameActive = true;

        // Update mouse position
        mousePos.set(ctx.getMouseX(), ctx.getMouseY());

        // Use mouse state from context (edge detection done by Mouse class)
        mouseClicked = ctx.isMouseDown(); // Actually "pressed" from Mouse.isMouseButtonPressed()
        mouseHeld = ctx.isMouseHeld(); // Continuous hold state from Mouse.isMouseButtonDown()
        mouseReleased = !mouseHeld && mouseHeldPrev; // Release edge: was held, now not
        mouseHeldPrev = mouseHeld; // Save for next frame

        // Store text input for this frame
        textInputThisFrame = ctx.getTextInput();

        // Store mouse wheel delta for this frame
        mouseWheelDelta = ctx.getMouseWheelDelta();

        // Store viewport dimensions
        viewportWidth = (int) ctx.getViewportWidth();
        viewportHeight = (int) ctx.getViewportHeight();

        // Clear drag states if mouse was released
        if (mouseReleased) {
            scrollbarDragging = null;
            panelDragging = null;
        }

        // Handle backspace for text fields
        if (ctx.isBackspace() && focusedWidgetId != null) {
            WidgetState state = widgetStates.get(focusedWidgetId);
            if (state != null && !state.textValue.isEmpty()) {
                state.textValue = state.textValue.substring(0, state.textValue.length() - 1);
                state.cursorPosition = state.textValue.length();
            }
        }

        // Reset hovered widget
        hoveredWidgetId = null;

        // Reset menu state for this frame
        menuOpenedThisFrame = false;

        // Reset panel tracking for this frame
        panelsThisFrame.clear();
        panelRenderQueue.clear();
        rootCommands.clear();
        recordingPanelStack.clear();
        previousHotPanelId = hotPanelId; // Save previous frame's hot panel
        hotPanelId = null;
        
        // PRE-COMPUTE hot panel using previous frame's panel bounds
        // This prevents click-through by determining which panel is hot BEFORE any widgets check
        // Walk through all panels that exist from last frame and find topmost one under mouse
        for (Map.Entry<String, PanelState> entry : panelStates.entrySet()) {
            String panelId = entry.getKey();
            PanelState state = entry.getValue();
            
            // Only check panels that have bounds stored and are in focus order
            if (state.width > 0 && state.height > 0 && panelFocusOrder.contains(panelId)) {
                Rectangle panelBounds = new Rectangle(state.x, state.y, state.width, state.height);
                
                if (isMouseOver(panelBounds)) {
                    // Pick panel with highest focus index (topmost)
                    if (hotPanelId == null || getPanelFocusIndex(panelId) > getPanelFocusIndex(hotPanelId)) {
                        hotPanelId = panelId;
                    }
                }
            }
        }
        
        shouldBringHotPanelToFront = false;

        // Reset layout state
        cursorX = theme.getWindowPadding();
        cursorY = theme.getWindowPadding();
        lineHeight = 0;
        lastWidgetX = 0;
        lastWidgetY = 0;
        lastWidgetWidth = 0;
        lastWidgetHeight = 0;
        layoutStack.clear();

        // Start Canvas rendering with optional camera view matrix
        if (ctx.getViewMatrix() != null) {
            canvas.begin(ctx.getViewMatrix());
        } else {
            canvas.begin();
        }
    }

    /**
     * Begin a new GUI frame with input state (legacy API).
     *
     * @param mouseX    Current mouse X position
     * @param mouseY    Current mouse Y position
     * @param mouseDown Whether the left mouse button is currently pressed
     * @param textInput Text input this frame (characters typed)
     * @param backspace Whether backspace was pressed this frame
     * @deprecated Use {@link #begin(FluxContext)} instead
     */
    @Deprecated
    public void begin(float mouseX, float mouseY, boolean mouseDown, String textInput, boolean backspace) {
        FluxContext ctx = new FluxContext(mouseX, mouseY, mouseDown)
                .setTextInput(textInput)
                .setBackspace(backspace);
        begin(ctx);
    }

    /**
     * Begin a new GUI frame (simplified version without text input, legacy API).
     *
     * @param mouseX    Current mouse X position
     * @param mouseY    Current mouse Y position
     * @param mouseDown Whether the left mouse button is currently pressed
     * @deprecated Use {@link #begin(FluxContext)} instead
     */
    @Deprecated
    public void begin(float mouseX, float mouseY, boolean mouseDown) {
        begin(new FluxContext(mouseX, mouseY, mouseDown));
    }

    /**
     * End the current GUI frame.
     * Flushes all rendering commands.
     */
    public void end() {
        if (!frameActive) {
            throw new IllegalStateException("end() called without begin()");
        }

        // Bring hot panel to front if clicked (after all panels have been declared)
        // This ensures the correct panel (by z-order) gets focus, not the first one in code
        if (shouldBringHotPanelToFront && hotPanelId != null) {
            bringPanelToFront(hotPanelId);
        }

        // Render panels in focus order (if ordering enabled)
        if (enablePanelOrdering && !panelRenderQueue.isEmpty()) {
            // Filter to only root-level panels (nested panels are in parent's commands)
            List<String> rootPanels = new ArrayList<>();
            for (String panelId : panelsThisFrame) {
                PanelRenderData renderData = panelRenderQueue.get(panelId);
                if (renderData != null && renderData.parentId == null) {
                    rootPanels.add(panelId);
                }
            }

            // Sort root panels by focus order
            rootPanels.sort((a, b) ->
                Integer.compare(getPanelFocusIndex(a), getPanelFocusIndex(b))
            );

            // Render each root panel in order (back to front)
            // Nested panels will be rendered as part of their parent's commands
            for (String panelId : rootPanels) {
                PanelRenderData renderData = panelRenderQueue.get(panelId);
                if (renderData != null) {
                    // Execute all recorded commands for this panel (includes nested panels)
                    for (Runnable cmd : renderData.commands) {
                        cmd.run();
                    }
                }
            }
        } else if (!enablePanelOrdering) {
            // If ordering disabled, render only root panels in declaration order
            // Nested panels are in parent's commands
            for (String panelId : panelsThisFrame) {
                PanelRenderData renderData = panelRenderQueue.get(panelId);
                if (renderData != null && renderData.parentId == null) {
                    for (Runnable cmd : renderData.commands) {
                        cmd.run();
                    }
                }
            }
        }

        // Execute root commands LAST (menu bar, menus, labels outside panels)
        // This ensures menus render on top of all panels
        for (Runnable cmd : rootCommands) {
            cmd.run();
        }

        canvas.end();
        frameActive = false;
    }

    /**
     * Draw a button widget.
     *
     * @param text   The button text
     * @param x      X position
     * @param y      Y position
     * @param width  Button width
     * @param height Button height
     * @return true if the button was clicked this frame
     */
    public boolean button(String text, float x, float y, float width, float height) {
        String id = generateId("button", text, x, y);

        Rectangle bounds = new Rectangle(x, y, width, height);
        boolean hovered = isMouseOver(bounds) && canProcessMouseEvents();
        boolean active = activeWidgetId != null && activeWidgetId.equals(id);
        boolean clicked = false;

        if (hovered) {
            hoveredWidgetId = id;
        }

        // Handle interaction (only if we can process events)
        if (hovered && mouseClicked && canProcessMouseEvents()) {
            activeWidgetId = id;
        }

        if (active && mouseReleased) {
            if (hovered) {
                clicked = true;
            }
            activeWidgetId = null;
        }

        // Determine button color
        Color bgColor = theme.getButtonBg();
        if (active) {
            bgColor = theme.getButtonActiveBg();
        } else if (hovered) {
            bgColor = theme.getButtonHoverBg();
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalY = y;
        final float finalWidth = width;
        final float finalHeight = height;
        final String finalText = text;
        final Color finalBgColor = bgColor;

        // Record button background drawing
        recordCommand(() -> {
            canvas.rect(finalX, finalY, finalWidth, finalHeight)
                    .withFill(finalBgColor)
                    .withStroke(theme.getButtonBorderWidth(), theme.getButtonBorder())
                    .withRoundedCorners(theme.getButtonRadius())
                    .apply();
        });

        // Record button text drawing (centered)
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(finalText, theme.getFont());
            final float textX = finalX + (finalWidth - textSize.getWidth()) / 2;
            final float textY = finalY + (finalHeight - textSize.getHeight()) / 2;

            recordCommand(() -> {
                canvas.text(finalText, theme.getFont(), textX, textY)
                        .withFill(theme.getButtonText())
                        .apply();
            });
        }

        return clicked;
    }

    /**
     * Draw a label widget.
     *
     * @param text The label text
     * @param x    X position
     * @param y    Y position
     */
    public void label(String text, float x, float y) {
        if (theme.getFont() != null) {
            // Capture final variables for lambda
            final float finalX = x;
            final float finalY = y;
            final String finalText = text;

            recordCommand(() -> {
                canvas.text(finalText, theme.getFont(), finalX, finalY)
                        .withFill(theme.getLabelText())
                        .apply();
            });
        }
    }

    /**
     * Draw a text field widget.
     *
     * @param id     Unique identifier for this text field
     * @param x      X position
     * @param y      Y position
     * @param width  Text field width
     * @param height Text field height
     * @return The current text value
     */
    public String textField(String id, float x, float y, float width, float height) {
        WidgetState state = widgetStates.computeIfAbsent(id, k -> new WidgetState());

        Rectangle bounds = new Rectangle(x, y, width, height);
        boolean hovered = isMouseOver(bounds);

        if (hovered) {
            hoveredWidgetId = id;
        }

        // Handle focus
        if (hovered && mouseClicked) {
            focusedWidgetId = id;
            state.focused = true;
        } else if (mouseClicked && !hovered && state.focused) {
            state.focused = false;
            if (id.equals(focusedWidgetId)) {
                focusedWidgetId = null;
            }
        }

        // Handle text input if focused
        if (state.focused && textInputThisFrame.length() > 0) {
            state.textValue += textInputThisFrame;
            state.cursorPosition = state.textValue.length();
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalY = y;
        final float finalWidth = width;
        final float finalHeight = height;
        final String finalTextValue = state.textValue;
        final boolean finalFocused = state.focused;

        // Determine border color
        Color borderColor = finalFocused ? theme.getTextFieldFocusBorder() : theme.getTextFieldBorder();
        final Color finalBorderColor = borderColor;

        // Record text field background drawing
        recordCommand(() -> {
            canvas.rect(finalX, finalY, finalWidth, finalHeight)
                    .withFill(theme.getTextFieldBg())
                    .withStroke(theme.getBorderWidth(), finalBorderColor)
                    .withRoundedCorners(theme.getTextFieldRadius())
                    .apply();
        });

        // Record text drawing
        if (theme.getFont() != null && !finalTextValue.isEmpty()) {
            recordCommand(() -> {
                float textX = finalX + theme.getFramePadding();
                float textY = finalY + theme.getFramePadding();

                canvas.text(finalTextValue, theme.getFont(), textX, textY)
                        .withFill(theme.getTextFieldText())
                        .apply();
            });
        }

        // Record cursor drawing if focused
        if (finalFocused && theme.getFont() != null) {
            // Simple blinking cursor
            final boolean cursorVisible = System.currentTimeMillis() % 1000 < 500;
            if (cursorVisible) {
                recordCommand(() -> {
                    Size textSize = finalTextValue.isEmpty() ? new Size(0, theme.getFontSize()) :
                            canvas.measureText(finalTextValue, theme.getFont());
                    float cursorX = finalX + theme.getFramePadding() + textSize.getWidth();
                    float cursorY = finalY + theme.getFramePadding();
                    float cursorHeight = textSize.getHeight();

                    canvas.line(cursorX, cursorY, cursorX, cursorY + cursorHeight)
                            .withStroke(1, theme.getTextFieldCursor())
                            .apply();
                });
            }
        }

        return state.textValue;
    }

    // === Auto-Layout Widget Overloads ===

    /**
     * Draw a button widget using auto-layout.
     *
     * @param text   The button text
     * @param width  Button width
     * @param height Button height
     * @return true if the button was clicked this frame
     */
    public boolean button(String text, float width, float height) {
        boolean result = button(text, cursorX, cursorY, width, height);
        advanceCursor(width, height);
        return result;
    }

    /**
     * Draw a button widget using auto-layout with default size.
     *
     * @param text The button text
     * @return true if the button was clicked this frame
     */
    public boolean button(String text) {
        float width = 120;
        float height = 30;
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(text, theme.getFont());
            width = textSize.getWidth() + theme.getPadding() * 4;
            height = textSize.getHeight() + theme.getPadding() * 2;
        }
        return button(text, width, height);
    }

    /**
     * Draw a label widget using auto-layout.
     *
     * @param text The label text
     */
    public void label(String text) {
        label(text, cursorX, cursorY);

        float height = theme.getFontSize();
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(text, theme.getFont());
            height = textSize.getHeight();
        }
        advanceCursor(0, height);
    }

    /**
     * Draw a text field widget using auto-layout.
     *
     * @param id     Unique identifier for this text field
     * @param width  Text field width
     * @param height Text field height
     * @return The current text value
     */
    public String textField(String id, float width, float height) {
        String result = textField(id, cursorX, cursorY, width, height);
        advanceCursor(width, height);
        return result;
    }

    /**
     * Draw a text field widget using auto-layout with default size.
     *
     * @param id Unique identifier for this text field
     * @return The current text value
     */
    public String textField(String id) {
        return textField(id, 200, 30);
    }

    // === Layout Methods ===

    /**
     * Keep the next widget on the same line as the previous widget.
     * Call this AFTER a widget to place the next widget horizontally.
     *
     * <p>Example:
     * <pre>
     * gui.button("A");
     * gui.sameLine();
     * gui.button("B");  // B will be on same line as A
     * gui.sameLine();
     * gui.button("C");  // C will be on same line as A and B
     * </pre>
     */
    public void sameLine() {
        // Move cursor back to the same Y as the last widget
        cursorY = lastWidgetY;
        // Move cursor right past the last widget
        cursorX = lastWidgetX + lastWidgetWidth + spacing;
        // Track the tallest widget on this line
        lineHeight = Math.max(lineHeight, lastWidgetHeight);
    }

    /**
     * Keep the next widget on the same line with custom horizontal spacing.
     *
     * @param offsetX Horizontal offset from previous widget (instead of default spacing)
     */
    public void sameLine(float offsetX) {
        cursorY = lastWidgetY;
        cursorX = lastWidgetX + lastWidgetWidth + offsetX;
        lineHeight = Math.max(lineHeight, lastWidgetHeight);
    }

    /**
     * Add vertical spacing.
     *
     * @param height Height of spacing in pixels
     */
    public void spacing(float height) {
        advanceCursor(0, height);
    }

    /**
     * Add a horizontal separator line.
     */
    public void separator() {
        float x = cursorX;
        float y = cursorY + spacing;
        float width;

        if (!layoutStack.isEmpty()) {
            LayoutContext ctx = layoutStack.peek();
            width = ctx.contentWidth - theme.getWindowPadding() * 2;
        } else {
            // Default separator width when not in a container
            width = 200;
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalY = y;
        final float finalWidth = width;

        // Record separator line drawing
        recordCommand(() -> {
            canvas.line(finalX, finalY, finalX + finalWidth, finalY)
                    .withStroke(1, theme.getBorder())
                    .apply();
        });

        advanceCursor(0, spacing * 2 + 1);
    }

    /**
     * Force advance to next line.
     * Useful to explicitly end a horizontal group started with sameLine().
     */
    public void newLine() {
        advanceCursor(0, 0);
    }

    /**
     * Draw a checkbox widget.
     *
     * @param id      Unique identifier for this checkbox
     * @param label   Label text to display next to checkbox
     * @param checked Current checked state
     * @param x       X position
     * @param y       Y position
     * @return New checked state (toggled if clicked)
     */
    public boolean checkbox(String id, String label, boolean checked, float x, float y) {
        String widgetId = generateId("checkbox", id, x, y);

        float checkboxSize = 16;
        float labelSpacing = 8;

        // Calculate total bounds (checkbox + label for full clickable area)
        float totalWidth = checkboxSize;
        float totalHeight = checkboxSize;

        if (label != null && !label.isEmpty() && theme.getFont() != null) {
            Size labelSize = canvas.measureText(label, theme.getFont());
            totalWidth = checkboxSize + labelSpacing + labelSize.getWidth();
            totalHeight = Math.max(checkboxSize, labelSize.getHeight());
        }

        // Use full bounds for hit testing (checkbox + label)
        Rectangle fullBounds = new Rectangle(x, y, totalWidth, totalHeight);
        boolean hovered = isMouseOver(fullBounds) && canProcessMouseEvents();
        boolean active = activeWidgetId != null && activeWidgetId.equals(widgetId);

        // Handle interaction using active pattern (like button)
        if (hovered) {
            hoveredWidgetId = widgetId;
        }

        if (hovered && mouseClicked && activeWidgetId == null && canProcessMouseEvents()) {
            activeWidgetId = widgetId;
        }

        if (active && mouseReleased) {
            if (hovered) {
                checked = !checked;
            }
            activeWidgetId = null;
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalY = y;
        final float finalCheckboxSize = checkboxSize;
        final float finalLabelSpacing = labelSpacing;
        final String finalLabel = label;
        final boolean finalChecked = checked;
        final boolean finalHovered = hovered;

        // Record checkbox box drawing
        Color borderColor = finalHovered ? theme.getButtonHoverBg() : theme.getBorder();
        final Color finalBorderColor = borderColor;
        recordCommand(() -> {
            canvas.rect(finalX, finalY, finalCheckboxSize, finalCheckboxSize)
                    .withFill(theme.getTextFieldBg())
                    .withStroke(1, finalBorderColor)
                    .withRoundedCorners(2)
                    .apply();
        });

        // Record checkmark drawing if checked
        if (finalChecked) {
            recordCommand(() -> {
                float padding = 3;
                float checkX1 = finalX + padding;
                float checkY1 = finalY + finalCheckboxSize / 2;
                float checkX2 = finalX + finalCheckboxSize / 2 - 1;
                float checkY2 = finalY + finalCheckboxSize - padding;
                float checkX3 = finalX + finalCheckboxSize - padding;
                float checkY3 = finalY + padding;

                // Draw checkmark as two lines
                canvas.line(checkX1, checkY1, checkX2, checkY2)
                        .withStroke(2, theme.getButtonBg())
                        .apply();
                canvas.line(checkX2, checkY2, checkX3, checkY3)
                        .withStroke(2, theme.getButtonBg())
                        .apply();
            });
        }

        // Record label drawing
        if (finalLabel != null && !finalLabel.isEmpty() && theme.getFont() != null) {
            recordCommand(() -> {
                float labelX = finalX + finalCheckboxSize + finalLabelSpacing;
                float labelY = finalY;
                canvas.text(finalLabel, theme.getFont(), labelX, labelY)
                        .withFill(theme.getText())
                        .apply();
            });
        }

        return checked;
    }

    /**
     * Draw a checkbox widget using auto-layout.
     *
     * @param id      Unique identifier for this checkbox
     * @param label   Label text to display next to checkbox
     * @param checked Current checked state
     * @return New checked state (toggled if clicked)
     */
    public boolean checkbox(String id, String label, boolean checked) {
        boolean result = checkbox(id, label, checked, cursorX, cursorY);

        float checkboxSize = 16;
        float labelSpacing = 8;
        float width = checkboxSize;
        float height = checkboxSize;

        if (label != null && !label.isEmpty() && theme.getFont() != null) {
            Size labelSize = canvas.measureText(label, theme.getFont());
            width = checkboxSize + labelSpacing + labelSize.getWidth();
            height = Math.max(checkboxSize, labelSize.getHeight());
        }

        advanceCursor(width, height);
        return result;
    }

    /**
     * Draw a horizontal slider widget.
     *
     * @param id    Unique identifier for this slider
     * @param value Current value (0.0 to 1.0)
     * @param x     X position
     * @param y     Y position
     * @param width Slider width
     * @return New value
     */
    public float slider(String id, float value, float x, float y, float width) {
        String widgetId = generateId("slider", id, x, y);

        float sliderHeight = 20;
        float trackHeight = 4;
        float thumbWidth = 12;
        float thumbHeight = 16;

        float trackY = y + (sliderHeight - trackHeight) / 2;
        Rectangle trackBounds = new Rectangle(x, trackY, width, trackHeight);

        // Calculate thumb position
        float thumbX = x + (width - thumbWidth) * value;
        float thumbY = y + (sliderHeight - thumbHeight) / 2;
        Rectangle thumbBounds = new Rectangle(thumbX, thumbY, thumbWidth, thumbHeight);

        boolean thumbHovered = isMouseOver(thumbBounds) && canProcessMouseEvents();
        boolean active = activeWidgetId != null && activeWidgetId.equals(widgetId);

        // Handle interaction
        if (thumbHovered && mouseClicked && canProcessMouseEvents()) {
            activeWidgetId = widgetId;
        }

        if (active) {
            // Dragging the slider
            float mouseX = mousePos.getX();
            float newValue = (mouseX - x) / (width - thumbWidth);
            value = Math.max(0, Math.min(1, newValue));

            if (mouseReleased) {
                activeWidgetId = null;
            }
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalTrackY = trackY;
        final float finalWidth = width;
        final float finalTrackHeight = trackHeight;
        final float finalThumbWidth = thumbWidth;
        final float finalThumbHeight = thumbHeight;
        final float finalThumbX = thumbX;
        final float finalThumbY = thumbY;
        final float finalValue = value;
        final boolean finalActive = active;
        final boolean finalThumbHovered = thumbHovered;

        // Record track drawing
        recordCommand(() -> {
            canvas.rect(finalX, finalTrackY, finalWidth, finalTrackHeight)
                    .withFill(theme.getTextFieldBg())
                    .withStroke(1, theme.getBorder())
                    .withRoundedCorners(finalTrackHeight / 2)
                    .apply();
        });

        // Record filled portion drawing
        final float filledWidth = (finalWidth - finalThumbWidth) * finalValue + finalThumbWidth / 2;
        if (filledWidth > 0) {
            recordCommand(() -> {
                canvas.rect(finalX, finalTrackY, filledWidth, finalTrackHeight)
                        .withFill(theme.getButtonBg())
                        .withRoundedCorners(finalTrackHeight / 2)
                        .apply();
            });
        }

        // Record thumb drawing
        Color thumbColor = finalActive ? theme.getButtonActiveBg() :
                          finalThumbHovered ? theme.getButtonHoverBg() : theme.getButtonBg();
        final Color finalThumbColor = thumbColor;
        recordCommand(() -> {
            canvas.rect(finalThumbX, finalThumbY, finalThumbWidth, finalThumbHeight)
                    .withFill(finalThumbColor)
                    .withStroke(1, theme.getButtonBorder())
                    .withRoundedCorners(4)
                    .apply();
        });

        return value;
    }

    /**
     * Draw a horizontal slider widget using auto-layout.
     *
     * @param id    Unique identifier for this slider
     * @param value Current value (0.0 to 1.0)
     * @param width Slider width
     * @return New value
     */
    public float slider(String id, float value, float width) {
        float result = slider(id, value, cursorX, cursorY, width);
        float sliderHeight = 20;
        advanceCursor(width, sliderHeight);
        return result;
    }

    /**
     * Draw a horizontal slider widget using auto-layout with default width.
     *
     * @param id    Unique identifier for this slider
     * @param value Current value (0.0 to 1.0)
     * @return New value
     */
    public float slider(String id, float value) {
        return slider(id, value, 150);
    }

    // === Collapsing Section Methods ===

    /**
     * Create a collapsing section builder with fluent configuration API.
     *
     * <p>Example:
     * <pre>
     * if (gui.collapsing("section1", "Section Title")
     *         .withDefaultOpen(true)
     *         .begin()) {
     *     // Content only rendered when open
     *     gui.label("Hidden content");
     * }
     * </pre>
     *
     * @param id    Unique identifier for this collapsing section
     * @param title Section title
     * @return A CollapsingBuilder for fluent configuration
     */
    public CollapsingBuilder collapsing(String id, String title) {
        return new CollapsingBuilder(this, id, title);
    }

    /**
     * Internal method to begin a collapsing section.
     * Called by CollapsingBuilder.
     *
     * @param id          Unique identifier
     * @param title       Section title
     * @param defaultOpen Default open state (first frame only)
     * @return True if section is open (render content)
     */
    boolean beginCollapsingInternal(String id, String title, boolean defaultOpen) {
        // Get or create collapsing state
        CollapsingState state = collapsingStates.computeIfAbsent(id, k -> new CollapsingState());

        // Initialize default state on first use
        if (!state.initialized) {
            state.open = defaultOpen;
            state.initialized = true;
        }

        String widgetId = generateId("collapsing", id, cursorX, cursorY);

        float headerHeight = theme.getFontSize() + theme.getPadding() * 2;
        float arrowSize = 8;
        float arrowPadding = theme.getPadding();

        Rectangle headerBounds = new Rectangle(cursorX, cursorY,
                                               layoutStack.isEmpty() ? 200 : layoutStack.peek().contentWidth - theme.getWindowPadding() * 2,
                                               headerHeight);
        boolean hovered = isMouseOver(headerBounds) && canProcessMouseEvents();
        boolean active = activeWidgetId != null && activeWidgetId.equals(widgetId);

        // Handle interaction using active pattern (like button)
        if (hovered) {
            hoveredWidgetId = widgetId;
        }

        if (hovered && mouseClicked && activeWidgetId == null && canProcessMouseEvents()) {
            activeWidgetId = widgetId;
        }

        if (active && mouseReleased) {
            if (hovered) {
                state.open = !state.open;
            }
            activeWidgetId = null;
        }

        // Capture final variables for lambda
        final float finalCursorX = cursorX;
        final float finalCursorY = cursorY;
        final float finalHeaderHeight = headerHeight;
        final float finalArrowSize = arrowSize;
        final float finalArrowPadding = arrowPadding;
        final float finalArrowX = finalCursorX + finalArrowPadding;
        final float finalArrowY = finalCursorY + finalHeaderHeight / 2;
        final boolean finalOpen = state.open;
        final boolean finalHovered = hovered;
        final String finalTitle = title;
        final float finalHeaderWidth = headerBounds.getWidth();
        final float finalHeaderBoundsY = headerBounds.getY();

        // Record header background drawing
        Color bgColor = finalHovered ? new Color(0.15f, 0.15f, 0.15f, 1f) : new Color(0.1f, 0.1f, 0.1f, 0.5f);
        final Color finalBgColor = bgColor;
        recordCommand(() -> {
            canvas.rect(finalCursorX, finalHeaderBoundsY, finalHeaderWidth, finalHeaderHeight)
                    .withFill(finalBgColor)
                    .withRoundedCorners(2)
                    .apply();
        });

        // Record arrow indicator drawing
        if (finalOpen) {
            // Record down-pointing arrow (triangle)
            recordCommand(() -> {
                float arrowLeft = finalArrowX;
                float arrowRight = finalArrowX + finalArrowSize;
                float arrowTop = finalArrowY - finalArrowSize / 4;
                float arrowBottom = finalArrowY + finalArrowSize / 4;
                float arrowMidX = finalArrowX + finalArrowSize / 2;

                // Draw as lines to form a triangle pointing down
                canvas.line(arrowLeft, arrowTop, arrowMidX, arrowBottom)
                        .withStroke(2, theme.getText())
                        .apply();
                canvas.line(arrowMidX, arrowBottom, arrowRight, arrowTop)
                        .withStroke(2, theme.getText())
                        .apply();
            });
        } else {
            // Record right-pointing arrow (triangle)
            recordCommand(() -> {
                float arrowLeft = finalArrowX;
                float arrowTop = finalArrowY - finalArrowSize / 2;
                float arrowBottom = finalArrowY + finalArrowSize / 2;
                float arrowRight = finalArrowX + finalArrowSize / 2;

                // Draw as lines to form a triangle pointing right
                canvas.line(arrowLeft, arrowTop, arrowRight, finalArrowY)
                        .withStroke(2, theme.getText())
                        .apply();
                canvas.line(arrowRight, finalArrowY, arrowLeft, arrowBottom)
                        .withStroke(2, theme.getText())
                        .apply();
            });
        }

        // Record title text drawing
        if (finalTitle != null && !finalTitle.isEmpty() && theme.getFont() != null) {
            recordCommand(() -> {
                float textX = finalArrowX + finalArrowSize + finalArrowPadding * 2;
                float textY = finalCursorY + theme.getPadding();
                canvas.text(finalTitle, theme.getFont(), textX, textY)
                        .withFill(theme.getText())
                        .apply();
            });
        }

        // Advance cursor past header
        advanceCursor(headerBounds.getWidth(), headerHeight);

        // Return whether section is open (content should be rendered)
        return state.open;
    }

    // === Menu Bar Methods ===

    /**
     * Begin a main menu bar at the top of the viewport.
     * Automatically positions and sizes the menu bar.
     *
     * @return True if menu bar is visible (always true for now)
     */
    public boolean beginMenuBar() {
        menuBarActive = true;
        menuBarHeight = theme.getFontSize() + theme.getPadding() * 2;

        // Capture final variables for lambda
        final float finalViewportWidth = viewportWidth;
        final float finalMenuBarHeight = menuBarHeight;

        // Record menu bar background drawing
        recordCommand(() -> {
            canvas.rect(0, 0, finalViewportWidth, finalMenuBarHeight)
                    .withFill(theme.getPanelBg())
                    .withStroke(1, theme.getBorder())
                    .apply();
        });

        // Set cursor to start of menu bar
        cursorX = theme.getPadding();
        cursorY = theme.getPadding();
        lineHeight = 0;

        return true;
    }

    /**
     * End the main menu bar.
     */
    public void endMenuBar() {
        menuBarActive = false;

        // Close menu if we have an open menu and conditions are met
        if (openMenuId != null && !menuOpenedThisFrame) {
            Rectangle menuBarBounds = new Rectangle(0, 0, viewportWidth, menuBarHeight);
            // Use generous bounds for popup (will be refined in endMenu, but we need something for click detection)
            Rectangle popupBounds = new Rectangle(menuPopupX, menuPopupY, 200, 300);

            boolean mouseOnMenuBar = isMouseOver(menuBarBounds);
            boolean mouseOnPopup = isMouseOver(popupBounds);

            // Close menu if:
            // 1. User clicked outside both the menu bar and popup
            if (mouseClicked && !mouseOnMenuBar && !mouseOnPopup) {
                openMenuId = null;
                insideMenuPopup = false; // Exit menu popup mode
            }
        }
    }

    /**
     * Begin a menu dropdown.
     *
     * @param label Menu label
     * @return True if menu is open
     */
    public boolean beginMenu(String label) {
        String menuId = generateId("menu", label, cursorX, cursorY);

        float menuWidth = 80;
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(label, theme.getFont());
            menuWidth = textSize.getWidth() + theme.getPadding() * 2;
        }

        float menuHeight = menuBarHeight - theme.getPadding() * 2;

        Rectangle menuBounds = new Rectangle(cursorX, cursorY, menuWidth, menuHeight);
        boolean hovered = isMouseOver(menuBounds);
        boolean isOpen = menuId.equals(openMenuId);

        // Handle click to open/close
        // Don't toggle if we just opened this menu this frame (prevents immediate re-close)
        if (hovered && mouseClicked && !menuOpenedThisFrame) {
            if (isOpen) {
                openMenuId = null;
                isOpen = false; // Update state
                insideMenuPopup = false; // Exit menu popup mode
            } else {
                openMenuId = menuId;
                menuPopupX = cursorX;
                menuPopupY = menuBarHeight;
                isOpen = true; // Update state
                menuOpenedThisFrame = true; // Prevent toggle this frame
            }
        }

        // Capture final variables for lambda
        final float finalCursorX = cursorX;
        final float finalCursorY = cursorY;
        final float finalMenuWidth = menuWidth;
        final float finalMenuHeight = menuHeight;
        final String finalLabel = label;
        final boolean finalHovered = hovered;
        final boolean finalIsOpen = isOpen;

        // Record menu button drawing
        Color bgColor = (finalHovered || finalIsOpen) ? theme.getButtonHoverBg() : Color.TRANSPARENT;
        final Color finalBgColor = bgColor;
        recordCommand(() -> {
            canvas.rect(finalCursorX, finalCursorY, finalMenuWidth, finalMenuHeight)
                    .withFill(finalBgColor)
                    .withRoundedCorners(2)
                    .apply();
        });

        // Record menu label drawing
        if (theme.getFont() != null) {
            recordCommand(() -> {
                float textX = finalCursorX + theme.getPadding();
                float textY = finalCursorY;
                canvas.text(finalLabel, theme.getFont(), textX, textY)
                        .withFill(theme.getText())
                        .apply();
            });
        }

        // Move cursor for next menu
        cursorX += menuWidth + theme.getItemSpacing();

        // If menu is open, prepare for popup content
        if (isOpen) {
            // Save the index where menu item commands will start
            menuItemsStartIndex = rootCommands.size();

            // Save current menu bar cursor position (for restoration in endMenu)
            menuBarCursorX = cursorX;

            // Start popup content (background will be drawn in endMenu after we know height)
            cursorX = menuPopupX + theme.getPadding();
            cursorY = menuPopupY + theme.getPadding();
            menuPopupCursorX = cursorX; // Save popup cursor X for preserving across lines
            menuPopupStartY = cursorY; // Save start position for height calculation
            insideMenuPopup = true; // Mark that we're inside a menu popup
            lineHeight = 0;

            return true;
        }

        return false;
    }

    /**
     * End a menu dropdown.
     */
    public void endMenu() {
        // Draw popup background NOW that we know the actual content height
        if (openMenuId != null && menuItemsStartIndex >= 0) {
            final float popupWidth = 200;
            final float popupHeight = (cursorY - menuPopupStartY) + theme.getPadding(); // Actual content height
            final float finalMenuPopupX = menuPopupX;
            final float finalMenuPopupY = menuPopupY;

            // Create background command
            Runnable backgroundCmd = () -> {
                canvas.rect(finalMenuPopupX, finalMenuPopupY, popupWidth, popupHeight)
                        .withFill(theme.getPanelBg())
                        .withStroke(1, theme.getBorder())
                        .withRoundedCorners(theme.getPanelRadius())
                        .apply();
            };

            // Insert background AT the position where menu items start (so it renders behind them)
            rootCommands.add(menuItemsStartIndex, backgroundCmd);

            // Restore cursor to menu bar for next menu button
            cursorX = menuBarCursorX; // Restore X position in menu bar
            cursorY = theme.getPadding(); // Back to menu bar Y
            insideMenuPopup = false; // Exit menu popup mode
        }
    }

    /**
     * Draw a menu item.
     *
     * @param label Menu item label
     * @return True if clicked
     */
    public boolean menuItem(String label) {
        return menuItem(label, null);
    }

    /**
     * Draw a menu item with keyboard shortcut display.
     *
     * @param label    Menu item label
     * @param shortcut Keyboard shortcut text (e.g., "Ctrl+N")
     * @return True if clicked
     */
    public boolean menuItem(String label, String shortcut) {
        if (openMenuId == null) {
            return false; // Not in an open menu
        }

        String itemId = generateId("menuitem", label, cursorX, cursorY);

        float itemWidth = 180;
        float itemHeight = theme.getFontSize() + theme.getPadding();

        Rectangle itemBounds = new Rectangle(cursorX, cursorY, itemWidth, itemHeight);
        boolean hovered = isMouseOver(itemBounds);
        boolean clicked = false;

        // Handle interaction
        if (hovered && mouseClicked) {
            clicked = true;
            openMenuId = null; // Close menu on click
            insideMenuPopup = false; // Exit menu popup mode immediately
        }

        // Capture final variables for lambda
        final float finalCursorX = cursorX;
        final float finalCursorY = cursorY;
        final float finalItemWidth = itemWidth;
        final float finalItemHeight = itemHeight;
        final String finalLabel = label;
        final String finalShortcut = shortcut;
        final boolean finalHovered = hovered;

        // Record item background drawing
        if (finalHovered) {
            recordCommand(() -> {
                canvas.rect(finalCursorX, finalCursorY, finalItemWidth, finalItemHeight)
                        .withFill(theme.getButtonHoverBg())
                        .withRoundedCorners(2)
                        .apply();
            });
        }

        // Record label drawing
        if (theme.getFont() != null) {
            recordCommand(() -> {
                float textX = finalCursorX + theme.getPadding();
                float textY = finalCursorY;
                canvas.text(finalLabel, theme.getFont(), textX, textY)
                        .withFill(theme.getText())
                        .apply();

                // Draw shortcut text (right-aligned)
                if (finalShortcut != null && !finalShortcut.isEmpty()) {
                    Size shortcutSize = canvas.measureText(finalShortcut, theme.getFont());
                    float shortcutX = finalCursorX + finalItemWidth - shortcutSize.getWidth() - theme.getPadding();
                    canvas.text(finalShortcut, theme.getFont(), shortcutX, textY)
                            .withFill(theme.getTextDisabled())
                            .apply();
                }
            });
        }

        // Advance cursor
        cursorY += itemHeight;

        return clicked;
    }

    // === Container Methods ===

    /**
     * Create a panel builder with fluent configuration API (manual positioning).
     *
     * <p>Example:
     * <pre>
     * gui.panel("settings", 100, 100, 300, 400)
     *     .withTitle("Settings")
     *     .withScroll(true)
     *     .begin();
     * // ... content ...
     * gui.endPanel();
     * </pre>
     *
     * @param id     Unique identifier for the panel
     * @param x      X position
     * @param y      Y position
     * @param width  Panel width
     * @param height Panel height
     * @return A PanelBuilder for fluent configuration
     */
    public PanelBuilder panel(String id, float x, float y, float width, float height) {
        return new PanelBuilder(this, id, x, y, width, height);
    }

    /**
     * Create a panel builder with auto-layout positioning.
     * Panel will be placed at current cursor position.
     *
     * <p>Example:
     * <pre>
     * gui.panel("settings", 300, 400)
     *     .withTitle("Settings")
     *     .withScroll(true)
     *     .begin();
     * // ... content ...
     * gui.endPanel();
     * </pre>
     *
     * @param id     Unique identifier for the panel
     * @param width  Panel width
     * @param height Panel height
     * @return A PanelBuilder for fluent configuration
     */
    public PanelBuilder panel(String id, float width, float height) {
        return new PanelBuilder(this, id, cursorX, cursorY, width, height);
    }

    /**
     * Begin a panel container (simple API without builder).
     * For fluent API with title/scroll, use {@link #panel(String, float, float, float, float)}.
     *
     * @param id     Unique identifier for the panel
     * @param x      X position
     * @param y      Y position
     * @param width  Panel width
     * @param height Panel height
     */
    public void beginPanel(String id, float x, float y, float width, float height) {
        beginPanelInternal(id, x, y, width, height, null, false, true, true, true, false);
    }

    /**
     * Internal method to begin a panel with full configuration.
     * Called by PanelBuilder or directly by beginPanel().
     */
    void beginPanelInternal(String id, float x, float y, float width, float height,
                            String title, boolean hasScroll, boolean hasScrollbar, boolean hasBorder, boolean hasBackground,
                            boolean isDraggable) {

        // Track this panel for this frame
        panelsThisFrame.add(id);

        // Ensure panel is in focus order (add to back if new)
        if (!panelFocusOrder.contains(id)) {
            panelFocusOrder.add(id);
        }

        // Get or create panel state
        PanelState panelState = panelStates.computeIfAbsent(id, k -> new PanelState());

        // Initialize panel position if draggable and not yet initialized
        if (isDraggable && !panelState.positionInitialized) {
            panelState.x = x;
            panelState.y = y;
            panelState.positionInitialized = true;
        }

        // Use stored position for draggable panels
        if (isDraggable) {
            x = panelState.x;
            y = panelState.y;
        }
        
        // Store panel dimensions for next frame's hot panel pre-computation
        panelState.width = width;
        panelState.height = height;

        float titleBarHeight = 0;
        float contentStartY = y;

        // Calculate title bar height if present
        if (title != null && !title.isEmpty()) {
            titleBarHeight = theme.getFontSize() + theme.getWindowPadding() * 2;
            contentStartY = y + titleBarHeight;
        }

        // Check if mouse is over this panel
        Rectangle panelBounds = new Rectangle(x, y, width, height);
        boolean mouseOverPanel = isMouseOver(panelBounds);

        // Update hotPanelId based on focus order (if not already determined in pre-pass)
        // Only update if this panel has HIGHER focus than current hotPanelId
        // This prevents lower-focus panels from stealing hot status during declaration
        if (mouseOverPanel) {
            if (hotPanelId == null || getPanelFocusIndex(id) > getPanelFocusIndex(hotPanelId)) {
                hotPanelId = id;
            }
        }

        // Mark that we should bring hot panel to front on click
        // Don't do it here - defer until end() after all panels have been declared
        if (mouseOverPanel && mouseClicked) {
            shouldBringHotPanelToFront = true;
        }

        // Handle panel dragging
        if (isDraggable && title != null && !title.isEmpty()) {
            Rectangle titleBarBounds = new Rectangle(x, y, width, titleBarHeight);

            // Check if we're currently dragging this panel
            if (id.equals(panelDragging)) {
                // Update panel position based on mouse movement
                float deltaX = mousePos.getX() - panelDragStartMouseX;
                float deltaY = mousePos.getY() - panelDragStartMouseY;

                panelState.x = panelDragStartX + deltaX;
                panelState.y = panelDragStartY + deltaY;

                // Update local positions for rendering
                x = panelState.x;
                y = panelState.y;
                contentStartY = y + titleBarHeight;

            } else if (isMouseOver(titleBarBounds) && mouseClicked && activeWidgetId == null && id.equals(hotPanelId)) {
                // Start dragging on title bar click (only if no other widget is active AND this is the hot panel)
                panelDragging = id;
                panelDragStartMouseX = mousePos.getX();
                panelDragStartMouseY = mousePos.getY();
                panelDragStartX = panelState.x;
                panelDragStartY = panelState.y;
            }
        }

        // Reserve space for scrollbar if needed
        float scrollbarWidth = 8;
        float contentWidth = width;
        if (hasScroll && hasScrollbar) {
            contentWidth -= scrollbarWidth + theme.getWindowPadding();
        }

        // Create panel render data and capture all visual state
        PanelRenderData renderData = new PanelRenderData(id);
        renderData.parentId = recordingPanelStack.isEmpty() ? null : recordingPanelStack.peek(); // Track parent
        renderData.x = x;
        renderData.y = y;
        renderData.width = width;
        renderData.height = height;
        renderData.title = title;
        renderData.titleBarHeight = titleBarHeight;
        renderData.contentStartY = contentStartY;
        renderData.hasBorder = hasBorder;
        renderData.hasBackground = hasBackground;
        renderData.hasScroll = hasScroll;
        renderData.hasScrollbar = hasScrollbar;
        renderData.contentWidth = contentWidth;
        renderData.state = panelState;
        panelRenderQueue.put(id, renderData);

        // Start recording commands to this panel (push to stack for nested panel support)
        recordingPanelStack.push(id);

        // Record panel background/title drawing as first commands
        final float finalX = x;
        final float finalY = y;
        final float finalWidth = width;
        final float finalHeight = height;
        final float finalContentStartY = contentStartY;
        final String finalTitle = title;
        final float finalTitleBarHeight = titleBarHeight;

        // Record canvas save (for clipping)
        recordCommand(() -> canvas.save());

        // Record panel background
        if (hasBackground) {
            recordCommand(() -> {
                canvas.rect(finalX, finalY, finalWidth, finalHeight)
                        .withFill(theme.getPanelBg())
                        .withStroke(hasBorder ? theme.getBorderWidth() : 0, theme.getPanelBorder())
                        .withRoundedCorners(theme.getPanelRadius())
                        .apply();
            });
        }

        // Record title text and separator
        if (finalTitle != null && !finalTitle.isEmpty()) {
            if (theme.getFont() != null) {
                recordCommand(() -> {
                    float textX = finalX + theme.getWindowPadding();
                    float textY = finalY + theme.getWindowPadding();
                    canvas.text(finalTitle, theme.getFont(), textX, textY)
                            .withFill(theme.getText())
                            .apply();
                });
            }

            recordCommand(() -> {
                float separatorY = finalContentStartY - 1;
                canvas.line(finalX, separatorY, finalX + finalWidth, separatorY)
                        .withStroke(1, theme.getBorder())
                        .apply();
            });
        }

        // Record clipping rect
        final float finalContentWidth = contentWidth;
        recordCommand(() -> {
            canvas.clipRect(finalX, finalContentStartY, finalContentWidth, finalHeight - finalTitleBarHeight);
        });

        // Handle scrollbar dragging if this panel's scrollbar is being dragged
        if (hasScroll && id.equals(scrollbarDragging)) {
            // Calculate scrollbar dimensions (same as in renderScrollbar)
            float scrollbarHeight = height - titleBarHeight - 4; // padding * 2

            // Calculate how much the mouse moved
            float mouseDeltaY = mousePos.getY() - scrollbarDragStartMouseY;

            // Convert mouse delta to scroll offset delta
            // The scrollbar thumb moves within the track, so we need to scale appropriately
            float contentHeight = panelState.contentHeight;
            float viewportHeight = height - titleBarHeight;
            float scrollableHeight = Math.max(0, contentHeight - viewportHeight + theme.getWindowPadding() * 2);

            if (scrollableHeight > 0) {
                float thumbHeightRatio = Math.min(1.0f, viewportHeight / contentHeight);
                float thumbHeight = Math.max(20, scrollbarHeight * thumbHeightRatio);
                float trackHeight = scrollbarHeight - thumbHeight;

                // Mouse delta in track space -> scroll offset delta
                float scrollDelta = (mouseDeltaY / trackHeight) * scrollableHeight;
                panelState.scrollOffset = scrollbarDragStartOffset + scrollDelta;

                // Clamp scroll offset
                panelState.scrollOffset = Math.max(0, Math.min(panelState.scrollOffset, scrollableHeight));
            }
        }

        // Handle mouse wheel scrolling if panel is hovered AND is the hot panel
        if (hasScroll && !id.equals(scrollbarDragging)) { // Don't wheel scroll while dragging
            Rectangle panelContentBounds = new Rectangle(x, contentStartY, width, height - titleBarHeight);
            if (isMouseOver(panelContentBounds) && mouseWheelDelta != 0 && id.equals(hotPanelId)) {
                float scrollSpeed = 20f; // Pixels per wheel notch
                panelState.scrollOffset -= mouseWheelDelta * scrollSpeed;

                // Clamp scroll offset to valid range (will be refined in endPanel)
                panelState.scrollOffset = Math.max(0, panelState.scrollOffset);
            }
        }

        // Store current panel ID for scroll tracking
        currentPanelId = id;

        // Save current layout context
        layoutStack.push(new LayoutContext(
                cursorX, cursorY, lineHeight,
                x, contentStartY, contentWidth, height - titleBarHeight,
                x, width,
                hasScroll, hasScrollbar
        ));

        // Set up content area with padding and scroll offset
        float padding = theme.getWindowPadding();
        cursorX = x + padding;
        // Apply scroll offset only if scrolling is enabled
        cursorY = contentStartY + padding - (hasScroll ? panelState.scrollOffset : 0);
        lineHeight = 0;
    }

    /**
     * End a panel container.
     * Restores previous layout context and canvas clipping state.
     */
    public void endPanel() {
        if (layoutStack.isEmpty()) {
            throw new IllegalStateException("endPanel() called without matching beginPanel()");
        }

        LayoutContext ctx = layoutStack.peek(); // Peek to calculate content height before popping

        // Update panel state with content height and clamp scroll
        PanelState panelState = null;
        boolean shouldRenderScrollbar = false;
        if (currentPanelId != null && ctx.hasScroll) {
            panelState = panelStates.get(currentPanelId);
            if (panelState != null) {
                // Calculate actual content height (how far down the cursor went)
                // We need to add back the scroll offset because cursorY started with it subtracted
                float contentHeight = (cursorY + panelState.scrollOffset) - (ctx.contentY + theme.getWindowPadding());
                panelState.contentHeight = contentHeight;

                // Clamp scroll offset: can't scroll past content
                float maxScroll = Math.max(0, contentHeight - ctx.contentHeight + theme.getWindowPadding() * 2);
                panelState.scrollOffset = Math.max(0, Math.min(panelState.scrollOffset, maxScroll));

                // Check if scrollbar should be rendered
                boolean contentOverflows = contentHeight > ctx.contentHeight - theme.getWindowPadding() * 2;
                shouldRenderScrollbar = ctx.hasScrollbar && contentOverflows;
            }
        }

        // Record canvas restore (removes clipping)
        recordCommand(() -> canvas.restore());

        // Record scrollbar rendering AFTER restore so it's not clipped
        if (shouldRenderScrollbar && panelState != null) {
            final String finalPanelId = currentPanelId;
            final boolean wasHotPanel = finalPanelId != null && finalPanelId.equals(hotPanelId);
            final PanelState finalPanelState = panelState;
            final LayoutContext finalCtx = ctx;
            recordCommand(() -> renderScrollbar(finalPanelId, finalCtx, finalPanelState, wasHotPanel));
        }

        // Stop recording to this panel (pop from stack)
        String poppedPanelId = null;
        if (!recordingPanelStack.isEmpty()) {
            poppedPanelId = recordingPanelStack.pop();
        }
        
        // If this was a nested panel, add its commands to the parent's command list
        if (poppedPanelId != null && !recordingPanelStack.isEmpty()) {
            PanelRenderData nestedRenderData = panelRenderQueue.get(poppedPanelId);
            if (nestedRenderData != null && !nestedRenderData.commands.isEmpty()) {
                // Create a single command that executes all nested panel commands
                final List<Runnable> nestedCommands = new ArrayList<>(nestedRenderData.commands);
                recordCommand(() -> {
                    for (Runnable cmd : nestedCommands) {
                        cmd.run();
                    }
                });
            }
        }

        // Restore previous layout context
        layoutStack.pop();
        cursorX = ctx.cursorX;
        cursorY = ctx.cursorY;
        lineHeight = ctx.lineHeight;

        // Clear current panel ID
        currentPanelId = null;

        // Advance cursor past the panel
        float panelHeight = ctx.contentHeight;
        advanceCursor(0, panelHeight);
    }

    /**
     * Render a scrollbar for a panel.
     */
    private void renderScrollbar(String panelId, LayoutContext ctx, PanelState panelState, boolean wasHotPanel) {
        float scrollbarWidth = 8;
        float scrollbarPadding = 4;

        // Position scrollbar at the right edge, inside panel bounds
        float scrollbarX = ctx.panelX + ctx.panelWidth - scrollbarWidth - scrollbarPadding - theme.getBorderWidth();
        float scrollbarY = ctx.contentY + scrollbarPadding;
        float scrollbarHeight = ctx.contentHeight - scrollbarPadding * 2;

        // Background track
        canvas.rect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight)
                .withFill(new Color(0.1f, 0.1f, 0.1f, 0.3f))
                .withRoundedCorners(scrollbarWidth / 2)
                .apply();

        // Calculate thumb size and position
        float contentHeight = panelState.contentHeight;
        float viewportHeight = ctx.contentHeight;
        float scrollableHeight = Math.max(0, contentHeight - viewportHeight + theme.getWindowPadding() * 2);

        if (scrollableHeight > 0) {
            // Thumb height proportional to viewport/content ratio
            float thumbHeightRatio = Math.min(1.0f, viewportHeight / contentHeight);
            float thumbHeight = Math.max(20, scrollbarHeight * thumbHeightRatio);

            // Thumb position based on scroll offset
            float scrollRatio = panelState.scrollOffset / scrollableHeight;
            float thumbY = scrollbarY + scrollRatio * (scrollbarHeight - thumbHeight);

            // Check for scrollbar thumb interaction
            Rectangle thumbBounds = new Rectangle(scrollbarX, thumbY, scrollbarWidth, thumbHeight);
            boolean thumbHovered = isMouseOver(thumbBounds);

            // Start drag on click (only if no other widget is active AND this was the hot panel)
            if (thumbHovered && mouseClicked && panelId != null && activeWidgetId == null && wasHotPanel) {
                scrollbarDragging = panelId;
                scrollbarDragStartMouseY = mousePos.getY();
                scrollbarDragStartOffset = panelState.scrollOffset;
            }

            // Render thumb with hover feedback
            Color thumbColor = thumbHovered || panelId.equals(scrollbarDragging)
                    ? theme.getText()
                    : theme.getBorder();

            canvas.rect(scrollbarX, thumbY, scrollbarWidth, thumbHeight)
                    .withFill(thumbColor)
                    .withRoundedCorners(scrollbarWidth / 2)
                    .apply();
        }
    }

    // === Helper Methods ===

    /**
     * Check if mouse is over a rectangle.
     */
    private boolean isMouseOver(Rectangle bounds) {
        return bounds.contains(mousePos);
    }

    /**
     * Generate a unique ID for a widget.
     */
    private String generateId(String type, String label, float x, float y) {
        return type + ":" + label + ":" + (int) x + ":" + (int) y;
    }

    /**
     * Advance the layout cursor after drawing a widget.
     *
     * @param width  Width of the widget just drawn
     * @param height Height of the widget just drawn
     */
    private void advanceCursor(float width, float height) {
        // Save widget position and dimensions BEFORE moving cursor
        lastWidgetX = cursorX;
        lastWidgetY = cursorY;
        lastWidgetWidth = width;
        lastWidgetHeight = height;

        // Move cursor down to next line
        if (lineHeight > 0) {
            cursorY += lineHeight + spacing;
        } else {
            cursorY += height + spacing;
        }

        // Reset to left margin (unless we're inside a menu popup)
        if (insideMenuPopup) {
            // Inside menu popup - preserve the popup cursor X
            cursorX = menuPopupCursorX;
        } else if (layoutStack.isEmpty()) {
            cursorX = theme.getWindowPadding();
        } else {
            LayoutContext ctx = layoutStack.peek();
            cursorX = ctx.contentX + theme.getWindowPadding();
        }

        lineHeight = 0;
    }

    /**
     * Check if mouse events should be processed for the current panel context.
     * Returns false if mouse is over a different panel (blocks event propagation).
     * 
     * This method prevents click-through by checking:
     * 1. If we're in the hot panel (topmost panel under mouse) - allow
     * 2. If we're in a descendant of the hot panel - allow
     * 3. Otherwise - block
     * 
     * Uses previous frame's hot panel as a guide when current frame's hot panel
     * hasn't been fully determined yet (prevents first-declared panels from
     * incorrectly responding to events meant for later-declared front panels).
     */
    private boolean canProcessMouseEvents() {
        // If we're not in a panel, allow events (root-level widgets)
        if (currentPanelId == null) {
            return true;
        }

        // Use previous frame's hot panel if current frame's isn't determined yet
        // This handles the case where early-declared panels check events before
        // later-declared (but higher-focus) panels update hotPanelId
        String effectiveHotPanel = (hotPanelId != null) ? hotPanelId : previousHotPanelId;
        
        // If no hot panel determined (first frame or no panels under mouse), block panel events
        if (effectiveHotPanel == null) {
            return false;
        }

        // Allow events if this panel is the hot panel
        if (currentPanelId.equals(effectiveHotPanel)) {
            return true;
        }

        // Allow events if this panel is a child/descendant of the hot panel
        // Walk up the parent chain to see if we find the hot panel
        PanelRenderData currentRenderData = panelRenderQueue.get(currentPanelId);
        while (currentRenderData != null && currentRenderData.parentId != null) {
            if (currentRenderData.parentId.equals(effectiveHotPanel)) {
                return true; // Current panel is a descendant of hot panel
            }
            currentRenderData = panelRenderQueue.get(currentRenderData.parentId);
        }

        // This panel is not the hot panel and not a child of it - block events
        return false;
    }

    /**
     * Record a drawing command to the appropriate command list.
     * If inside a panel, records to that panel's list.
     * Otherwise, records to root command list.
     */
    private void recordCommand(Runnable command) {
        if (!recordingPanelStack.isEmpty()) {
            // Inside a panel - add to the current panel's command list (top of stack)
            String currentPanel = recordingPanelStack.peek();
            PanelRenderData renderData = panelRenderQueue.get(currentPanel);
            if (renderData != null) {
                renderData.commands.add(command);
            }
        } else {
            // Outside any panel - add to root commands
            rootCommands.add(command);
        }
    }

    /**
     * Bring a panel to the front (highest z-order).
     * Moves the panel to the end of the focus order list.
     */
    private void bringPanelToFront(String panelId) {
        // Remove from current position
        panelFocusOrder.remove(panelId);
        // Add to end (front)
        panelFocusOrder.add(panelId);
    }

    /**
     * Get the focus index of a panel (higher = more in front).
     * Returns -1 if panel not in focus order.
     */
    private int getPanelFocusIndex(String panelId) {
        return panelFocusOrder.indexOf(panelId);
    }

    @Override
    public void dispose() {
        canvas.dispose();
    }
}
