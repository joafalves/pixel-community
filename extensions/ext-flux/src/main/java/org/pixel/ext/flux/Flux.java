package org.pixel.ext.flux;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.ext.flux.core.FluxInputProcessor;
import org.pixel.ext.flux.core.FluxLayoutManager;
import org.pixel.ext.flux.core.FluxPanelManager;
import org.pixel.ext.flux.core.FluxViewport;
import org.pixel.ext.flux.state.CollapsingState;
import org.pixel.ext.flux.state.FluxStateStore;
import org.pixel.ext.flux.state.PanelState;
import org.pixel.ext.flux.state.TextFieldState;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final FluxViewport viewport;
    private final FluxStateStore stateStore;
    private final FluxLayoutManager layoutManager;
    private final FluxInputProcessor inputProcessor;
    private final FluxPanelManager panelManager;
    private FluxTheme theme;
    private FluxInput input; // Input provider (set by user)

    // === Layout State (managed by FluxLayoutManager) ===
    private float spacing; // Cache of theme spacing value

    // === Widget State (now managed by FluxStateStore) ===
    // Removed: Map<String, WidgetState> widgetStates - use stateStore instead
    // Removed: Map<String, PanelState> panelStates - use stateStore instead
    // Removed: Map<String, CollapsingState> collapsingStates - use stateStore instead
    // Removed: focusedWidgetId, hoveredWidgetId, activeWidgetId - use stateStore instead
    // Removed: currentPanelId, panelDragging, scrollbarDragging, panelFocusOrder, etc. - use panelManager instead

    // === Frame State ===
    private boolean frameActive = false;

    // === Menu State ===
    private boolean menuBarActive = false;
    private String openMenuId = null; // ID of currently open menu
    private boolean menuOpenedThisFrame = false; // Flag to prevent immediate close
    private float menuPopupStartY = 0; // Y position where menu content starts (for height calculation)
    private int menuItemsStartIndex = -1; // Index in rootCommands where menu items start
    private float menuBarCursorX = 0; // Saved cursor X position in menu bar (before entering popup)

    // === Panel Rendering (deferred until end of frame) ===
    private final Map<String, PanelRenderData> panelRenderQueue = new HashMap<>(); // Deferred panel rendering
    private final List<Runnable> rootCommands = new ArrayList<>(); // Commands outside any panel

    // === Menu State ===
    private float menuBarHeight = 0;
    private float menuPopupX = 0;
    private float menuPopupY = 0;

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
     * Create a new Flux instance with the given viewport size.
     * Creates an internal Canvas for rendering.
     *
     * @param viewportWidth Initial viewport width
     * @param viewportHeight Initial viewport height
     */
    public Flux(int viewportWidth, int viewportHeight) {
        this.viewport = new FluxViewport(viewportWidth, viewportHeight);
        this.canvas = Canvas.create(viewportWidth, viewportHeight);
        this.stateStore = new FluxStateStore();
        this.layoutManager = new FluxLayoutManager();
        this.inputProcessor = new FluxInputProcessor();
        this.panelManager = new FluxPanelManager();
        this.theme = new DefaultFluxTheme();
        this.spacing = theme.getItemSpacing();
        
        // Sync layout manager defaults with theme
        layoutManager.setDefaultSpacing(spacing);
        layoutManager.setDefaultPadding(theme.getWindowPadding());
    }

    /**
     * Update viewport size (e.g., when window is resized).
     *
     * @param width New viewport width
     * @param height New viewport height
     */
    public void setViewport(int width, int height) {
        viewport.resize(width, height);
        canvas.setViewport(width, height);
    }

    /**
     * Get the viewport.
     *
     * @return The FluxViewport instance
     */
    public FluxViewport getViewport() {
        return viewport;
    }

    /**
     * Get the state store.
     *
     * @return The FluxStateStore instance
     */
    public FluxStateStore getStateStore() {
        return stateStore;
    }
    
    /**
     * Get the layout manager.
     *
     * @return The FluxLayoutManager instance
     */
    public FluxLayoutManager getLayoutManager() {
        return layoutManager;
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
     * Set the input provider for this Flux instance.
     * Must be called before begin() is called.
     * 
     * @param input Input provider implementation
     */
    public void setInput(FluxInput input) {
        this.input = input;
    }

    /**
     * Enable or disable automatic panel z-ordering.
     * When enabled (default), clicking a panel brings it to front.
     * When disabled, panels render in declaration order.
     *
     * @param enable True to enable panel ordering (like Dear ImGui)
     */
    public void setEnablePanelOrdering(boolean enable) {
        panelManager.setEnablePanelOrdering(enable);
    }

    /**
     * Check if panel ordering is enabled.
     *
     * @return True if panel ordering is enabled
     */
    public boolean isEnablePanelOrdering() {
        return panelManager.isEnablePanelOrdering();
    }

    /**
     * Begin a new GUI frame.
     * Must be called before any widget methods.
     * Requires input provider to be set via {@link #setInput(FluxInput)}.
     */
    public void begin() {
        if (input == null) {
            throw new IllegalStateException("Input provider not set. Call setInput() before begin().");
        }
        
        if (frameActive) {
            throw new IllegalStateException("begin() called twice without end()");
        }
        frameActive = true;

        // Gather input from provider
        inputProcessor.updateFrame(
            input.getMouseX(), input.getMouseY(),
            input.isMousePressed(), input.isMouseDown(),
            input.getMouseWheelDelta(), input.getTextInput()
        );
        // Note: viewport managed separately via setViewport()

        // Clear drag states if mouse was released
        if (inputProcessor.isMouseReleased()) {
            panelManager.stopScrollbarDrag();
            panelManager.stopPanelDrag();
        }

        // Handle backspace for text fields
        String focusedId = stateStore.getFocusedWidgetId();
        if (input.isBackspacePressed() && focusedId != null) {
            TextFieldState state = (TextFieldState) stateStore.getWidgetState(focusedId);
            if (state != null && !state.getValue().isEmpty()) {
                state.setValue(state.getValue().substring(0, state.getValue().length() - 1));
            }
        }

        // Begin frame in state store (resets hovered widget, etc.)
        stateStore.beginFrame();

        // Reset menu state for this frame
        menuOpenedThisFrame = false;

        // Reset panel tracking for this frame (via panel manager)
        panelManager.beginFrame();
        panelRenderQueue.clear();
        rootCommands.clear();
        
        // PRE-COMPUTE hot panel using previous frame's panel bounds
        // This prevents click-through by determining which panel is hot BEFORE any widgets check
        // Walk through all panels that exist from last frame and find topmost one under mouse
        for (Map.Entry<String, org.pixel.ext.flux.state.PanelState> entry : stateStore.getAllPanelStates().entrySet()) {
            String panelId = entry.getKey();
            org.pixel.ext.flux.state.PanelState state = entry.getValue();
            
            // Only check panels that have bounds stored and are in focus order
            if (state.getWidth() > 0 && state.getHeight() > 0 && panelManager.isPanelRegistered(panelId)) {
                Vector2 pos = state.getPosition();
                Rectangle panelBounds = new Rectangle(pos.getX(), pos.getY(), state.getWidth(), state.getHeight());
                
                if (isMouseOver(panelBounds)) {
                    // Pick panel with highest focus index (topmost)
                    String currentHot = panelManager.getHotPanelId();
                    if (currentHot == null || panelManager.getPanelFocusIndex(panelId) > panelManager.getPanelFocusIndex(currentHot)) {
                        panelManager.setHotPanel(panelId);
                    }
                }
            }
        }

        // Reset layout state
        layoutManager.reset(theme.getWindowPadding());

        // Start Canvas rendering
        canvas.begin();
    }

    /**
     * Begin a new GUI frame with context (legacy/advanced API).
     * Most users should use {@link #begin()} with {@link #setInput(FluxInput)} instead.
     * 
     * @param ctx Context for this frame (input, viewport, camera, etc.)
     * @deprecated Use {@link #begin()} with {@link #setInput(FluxInput)} instead
     */
    @Deprecated
    public void begin(FluxContext ctx) {
        if (frameActive) {
            throw new IllegalStateException("begin() called twice without end()");
        }
        frameActive = true;

        // Update input processor with frame data
        inputProcessor.updateFrame(
            ctx.getMouseX(), ctx.getMouseY(),
            ctx.isMouseDown(), // mousePressed
            ctx.isMouseHeld(), // mouseDown
            ctx.getMouseWheelDelta(),
            ctx.getTextInput()
        );

        // Update viewport from context (deprecated path)
        viewport.resize((int) ctx.getViewportWidth(), (int) ctx.getViewportHeight());

        // Clear drag states if mouse was released
        if (inputProcessor.isMouseReleased()) {
            panelManager.stopScrollbarDrag();
            panelManager.stopPanelDrag();
        }

        // Handle backspace for text fields
        if (ctx.isBackspace() && stateStore.getFocusedWidgetId() != null) {
            TextFieldState state = stateStore.getOrCreateWidgetState(stateStore.getFocusedWidgetId(), TextFieldState.class);
            if (state != null && !state.getValue().isEmpty()) {
                state.setValue(state.getValue().substring(0, state.getValue().length() - 1));
            }
        }

        // Reset hovered widget
        stateStore.setHoveredWidget(null);

        // Reset menu state for this frame
        menuOpenedThisFrame = false;

        // Reset panel tracking for this frame (via panel manager)
        panelManager.beginFrame();
        panelRenderQueue.clear();
        rootCommands.clear();
        
        // PRE-COMPUTE hot panel using previous frame's panel bounds
        // This prevents click-through by determining which panel is hot BEFORE any widgets check
        // Walk through all panels that exist from last frame and find topmost one under mouse
        for (Map.Entry<String, PanelState> entry : stateStore.getAllPanelStates().entrySet()) {
            String panelId = entry.getKey();
            PanelState state = entry.getValue();
            
            // Only check panels that have bounds stored and are in focus order
            if (state.getWidth() > 0 && state.getHeight() > 0 && panelManager.isPanelRegistered(panelId)) {
                Rectangle panelBounds = new Rectangle(state.getPosition().getX(), state.getPosition().getY(), 
                                                     state.getWidth(), state.getHeight());
                
                if (isMouseOver(panelBounds)) {
                    // Pick panel with highest focus index (topmost)
                    String currentHot = panelManager.getHotPanelId();
                    if (currentHot == null || panelManager.getPanelFocusIndex(panelId) > panelManager.getPanelFocusIndex(currentHot)) {
                        panelManager.setHotPanel(panelId);
                    }
                }
            }
        }

        // Reset layout state
        layoutManager.reset(theme.getWindowPadding());

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
        panelManager.endFrame();

        // Render panels in focus order (if ordering enabled)
        if (panelManager.isEnablePanelOrdering() && !panelRenderQueue.isEmpty()) {
            // Filter to only root-level panels (nested panels are in parent's commands)
            List<String> rootPanels = new ArrayList<>();
            List<String> panelsThisFrame = panelManager.getPanelFocusOrder();
            for (String panelId : panelsThisFrame) {
                PanelRenderData renderData = panelRenderQueue.get(panelId);
                if (renderData != null && renderData.parentId == null) {
                    rootPanels.add(panelId);
                }
            }

            // Sort root panels by focus order
            rootPanels.sort((a, b) ->
                Integer.compare(panelManager.getPanelFocusIndex(a), panelManager.getPanelFocusIndex(b))
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
        } else if (!panelManager.isEnablePanelOrdering()) {
            // If ordering disabled, render only root panels in declaration order
            // Nested panels are in parent's commands
            List<String> panelsThisFrame = panelManager.getPanelFocusOrder();
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
        boolean active = stateStore.isWidgetActive(id);
        boolean clicked = false;

        if (hovered) {
            stateStore.setHoveredWidget(id);
        }

        // Handle interaction (only if we can process events)
        if (hovered && inputProcessor.isMouseClicked() && canProcessMouseEvents()) {
            stateStore.setActiveWidget(id);
        }

        if (active && inputProcessor.isMouseReleased()) {
            if (hovered) {
                clicked = true;
            }
            stateStore.setActiveWidget(null);
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
        TextFieldState state = stateStore.getOrCreateWidgetState(id, TextFieldState.class);

        Rectangle bounds = new Rectangle(x, y, width, height);
        boolean hovered = isMouseOver(bounds);

        if (hovered) {
            stateStore.setHoveredWidget(id);
        }

        boolean isFocused = stateStore.isWidgetFocused(id);

        // Handle focus
        if (hovered && inputProcessor.isMouseClicked()) {
            stateStore.setFocusedWidget(id);
        } else if (inputProcessor.isMouseClicked() && !hovered && isFocused) {
            stateStore.clearFocus();
        }

        // Handle text input if focused
        String textInput = inputProcessor.getTextInput();
        if (stateStore.isWidgetFocused(id) && textInput.length() > 0) {
            state.setValue(state.getValue() + textInput);
        }

        // Capture final variables for lambda
        final float finalX = x;
        final float finalY = y;
        final float finalWidth = width;
        final float finalHeight = height;
        final String finalTextValue = state.getValue();
        final boolean finalFocused = stateStore.isWidgetFocused(id);

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

        return state.getValue();
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
        boolean result = button(text, layoutManager.getCursorX(), layoutManager.getCursorY(), width, height);
        layoutManager.advanceCursor(width, height);
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
        label(text, layoutManager.getCursorX(), layoutManager.getCursorY());

        float height = theme.getFontSize();
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(text, theme.getFont());
            height = textSize.getHeight();
        }
        layoutManager.advanceCursor(0, height);
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
        String result = textField(id, layoutManager.getCursorX(), layoutManager.getCursorY(), width, height);
        layoutManager.advanceCursor(width, height);
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
        layoutManager.sameLine();
    }

    /**
     * Keep the next widget on the same line with custom horizontal spacing.
     *
     * @param offsetX Horizontal offset from previous widget (instead of default spacing)
     */
    public void sameLine(float offsetX) {
        layoutManager.sameLine(offsetX);
    }

    /**
     * Add vertical spacing.
     *
     * @param height Height of spacing in pixels
     */
    public void spacing(float height) {
        layoutManager.spacing(height);
    }

    /**
     * Add a horizontal separator line.
     */
    public void separator() {
        float x = layoutManager.getCursorX();
        float y = layoutManager.getCursorY() + spacing;
        float width;

        if (!layoutManager.isLayoutStackEmpty()) {
            FluxLayoutManager.LayoutContext ctx = layoutManager.peekContext();
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
        boolean active = stateStore.isWidgetActive(widgetId);

        // Handle interaction using active pattern (like button)
        if (hovered) {
            stateStore.setHoveredWidget(widgetId);
        }

        if (hovered && inputProcessor.isMouseClicked() && stateStore.getActiveWidgetId() == null && canProcessMouseEvents()) {
            stateStore.setActiveWidget(widgetId);
        }

        if (active && inputProcessor.isMouseReleased()) {
            if (hovered) {
                checked = !checked;
            }
            stateStore.setActiveWidget(null);
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
        boolean result = checkbox(id, label, checked, layoutManager.getCursorX(), layoutManager.getCursorY());

        float checkboxSize = 16;
        float labelSpacing = 8;
        float width = checkboxSize;
        float height = checkboxSize;

        if (label != null && !label.isEmpty() && theme.getFont() != null) {
            Size labelSize = canvas.measureText(label, theme.getFont());
            width = checkboxSize + labelSpacing + labelSize.getWidth();
            height = Math.max(checkboxSize, labelSize.getHeight());
        }

        layoutManager.advanceCursor(width, height);
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
        boolean active = stateStore.isWidgetActive(widgetId);

        // Handle interaction
        if (thumbHovered && inputProcessor.isMouseClicked() && canProcessMouseEvents()) {
            stateStore.setActiveWidget(widgetId);
        }

        if (active) {
            // Dragging the slider
            float mouseX = inputProcessor.getMouseX();
            float newValue = (mouseX - x) / (width - thumbWidth);
            value = Math.max(0, Math.min(1, newValue));

            if (inputProcessor.isMouseReleased()) {
                stateStore.setActiveWidget(null);
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
        float result = slider(id, value, layoutManager.getCursorX(), layoutManager.getCursorY(), width);
        float sliderHeight = 20;
        layoutManager.advanceCursor(width, sliderHeight);
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
        CollapsingState state = stateStore.getOrCreateCollapsingState(id);

        // Initialize default state on first use
        if (!state.isInitialized()) {
            state.setOpen(defaultOpen);
        }

        String widgetId = generateId("collapsing", id, layoutManager.getCursorX(), layoutManager.getCursorY());

        float headerHeight = theme.getFontSize() + theme.getPadding() * 2;
        float arrowSize = 8;
        float arrowPadding = theme.getPadding();

        Rectangle headerBounds = new Rectangle(layoutManager.getCursorX(), layoutManager.getCursorY(),
                                               layoutManager.isLayoutStackEmpty() ? 200 : layoutManager.peekContext().contentWidth - theme.getWindowPadding() * 2,
                                               headerHeight);
        boolean hovered = isMouseOver(headerBounds) && canProcessMouseEvents();
        boolean active = stateStore.isWidgetActive(widgetId);

        // Handle interaction using active pattern (like button)
        if (hovered) {
            stateStore.setHoveredWidget(widgetId);
        }

        if (hovered && inputProcessor.isMouseClicked() && stateStore.getActiveWidgetId() == null && canProcessMouseEvents()) {
            stateStore.setActiveWidget(widgetId);
        }

        if (active && inputProcessor.isMouseReleased()) {
            if (hovered) {
                state.setOpen(!state.isOpen());
            }
            stateStore.setActiveWidget(null);
        }

        // Capture final variables for lambda
        final float finalCursorX = layoutManager.getCursorX();
        final float finalCursorY = layoutManager.getCursorY();
        final float finalHeaderHeight = headerHeight;
        final float finalArrowSize = arrowSize;
        final float finalArrowPadding = arrowPadding;
        final float finalArrowX = finalCursorX + finalArrowPadding;
        final float finalArrowY = finalCursorY + finalHeaderHeight / 2;
        final boolean finalOpen = state.isOpen();
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
        layoutManager.advanceCursor(headerBounds.getWidth(), headerHeight);

        // Return whether section is open (content should be rendered)
        return state.isOpen();
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
        final float finalViewportWidth = viewport.getWidth();
        final float finalMenuBarHeight = menuBarHeight;

        // Record menu bar background drawing
        recordCommand(() -> {
            canvas.rect(0, 0, finalViewportWidth, finalMenuBarHeight)
                    .withFill(theme.getPanelBg())
                    .withStroke(1, theme.getBorder())
                    .apply();
        });

        // Set cursor to start of menu bar
        layoutManager.setCursor(theme.getPadding(), theme.getPadding());
        layoutManager.setLineHeight(0);

        return true;
    }

    /**
     * End the main menu bar.
     */
    public void endMenuBar() {
        menuBarActive = false;

        // Close menu if we have an open menu and conditions are met
        if (openMenuId != null && !menuOpenedThisFrame) {
            Rectangle menuBarBounds = new Rectangle(0, 0, viewport.getWidth(), menuBarHeight);
            // Use generous bounds for popup (will be refined in endMenu, but we need something for click detection)
            Rectangle popupBounds = new Rectangle(menuPopupX, menuPopupY, 200, 300);

            boolean mouseOnMenuBar = isMouseOver(menuBarBounds);
            boolean mouseOnPopup = isMouseOver(popupBounds);

            // Close menu if:
            // 1. User clicked outside both the menu bar and popup
            if (inputProcessor.isMouseClicked() && !mouseOnMenuBar && !mouseOnPopup) {
                openMenuId = null;
                layoutManager.setInsideMenuPopup(false); // Exit menu popup mode
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
        String menuId = generateId("menu", label, layoutManager.getCursorX(), layoutManager.getCursorY());

        float menuWidth = 80;
        if (theme.getFont() != null) {
            Size textSize = canvas.measureText(label, theme.getFont());
            menuWidth = textSize.getWidth() + theme.getPadding() * 2;
        }

        float menuHeight = menuBarHeight - theme.getPadding() * 2;

        Rectangle menuBounds = new Rectangle(layoutManager.getCursorX(), layoutManager.getCursorY(), menuWidth, menuHeight);
        boolean hovered = isMouseOver(menuBounds);
        boolean isOpen = menuId.equals(openMenuId);

        // Handle click to open/close
        // Don't toggle if we just opened this menu this frame (prevents immediate re-close)
        if (hovered && inputProcessor.isMouseClicked() && !menuOpenedThisFrame) {
            if (isOpen) {
                openMenuId = null;
                isOpen = false; // Update state
                layoutManager.setInsideMenuPopup(false); // Exit menu popup mode
            } else {
                openMenuId = menuId;
                menuPopupX = layoutManager.getCursorX();
                menuPopupY = menuBarHeight;
                isOpen = true; // Update state
                menuOpenedThisFrame = true; // Prevent toggle this frame
            }
        }

        // Capture final variables for lambda
        final float finalCursorX = layoutManager.getCursorX();
        final float finalCursorY = layoutManager.getCursorY();
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
        layoutManager.setCursorX(layoutManager.getCursorX() + menuWidth + theme.getItemSpacing());

        // If menu is open, prepare for popup content
        if (isOpen) {
            // Save the index where menu item commands will start
            menuItemsStartIndex = rootCommands.size();

            // Save current menu bar cursor position (for restoration in endMenu)
            menuBarCursorX = layoutManager.getCursorX();

            // Start popup content (background will be drawn in endMenu after we know height)
            layoutManager.setCursor(menuPopupX + theme.getPadding(), menuPopupY + theme.getPadding());
            layoutManager.setMenuPopupCursorX(layoutManager.getCursorX()); // Save popup cursor X for preserving across lines
            menuPopupStartY = layoutManager.getCursorY(); // Save start position for height calculation
            layoutManager.setInsideMenuPopup(true); // Mark that we're inside a menu popup
            layoutManager.setLineHeight(0);

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
            final float popupHeight = (layoutManager.getCursorY() - menuPopupStartY) + theme.getPadding(); // Actual content height
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
            layoutManager.setCursor(menuBarCursorX, theme.getPadding()); // Restore X position in menu bar
            layoutManager.setInsideMenuPopup(false); // Exit menu popup mode
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

        String itemId = generateId("menuitem", label, layoutManager.getCursorX(), layoutManager.getCursorY());

        float itemWidth = 180;
        float itemHeight = theme.getFontSize() + theme.getPadding();

        Rectangle itemBounds = new Rectangle(layoutManager.getCursorX(), layoutManager.getCursorY(), itemWidth, itemHeight);
        boolean hovered = isMouseOver(itemBounds);
        boolean clicked = false;

        // Handle interaction
        if (hovered && inputProcessor.isMouseClicked()) {
            clicked = true;
            openMenuId = null; // Close menu on click
            layoutManager.setInsideMenuPopup(false); // Exit menu popup mode immediately
        }

        // Capture final variables for lambda
        final float finalCursorX = layoutManager.getCursorX();
        final float finalCursorY = layoutManager.getCursorY();
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
        layoutManager.setCursorY(layoutManager.getCursorY() + itemHeight);

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
        return new PanelBuilder(this, id, layoutManager.getCursorX(), layoutManager.getCursorY(), width, height);
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
        panelManager.registerPanel(id);

        // Get or create panel state
        PanelState panelState = stateStore.getOrCreatePanelState(id);

        // Initialize panel position if draggable and not yet initialized
        if (isDraggable && !panelState.isPositionInitialized()) {
            panelState.setPosition(x, y);
        }

        // Use stored position for draggable panels
        if (isDraggable) {
            x = panelState.getPosition().getX();
            y = panelState.getPosition().getY();
        }
        
        // Store panel dimensions for next frame's hot panel pre-computation
        panelState.setWidth(width);
        panelState.setHeight(height);

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

        // Update hotPanelId:
        // 1. If no hot panel set yet, this panel becomes hot
        // 2. If mouse is over this panel AND this panel has higher focus than current hot, update
        // 3. If mouse is NOT over current hot panel anymore, this panel can become hot
        if (mouseOverPanel) {
            String currentHot = panelManager.getHotPanelId();
            if (currentHot == null) {
                // No hot panel yet, this becomes hot
                panelManager.setHotPanel(id);
            } else if (panelManager.getPanelFocusIndex(id) > panelManager.getPanelFocusIndex(currentHot)) {
                // This panel has higher focus (more in front), it becomes hot
                panelManager.setHotPanel(id);
            } else {
                // Check if mouse is still over the current hot panel
                // If not, allow this panel to become hot even if it has lower focus
                PanelState currentHotState = stateStore.getPanelState(currentHot);
                if (currentHotState != null && currentHotState.getWidth() > 0) {
                    Vector2 hotPos = currentHotState.getPosition();
                    Rectangle hotBounds = new Rectangle(hotPos.getX(), hotPos.getY(), 
                                                       currentHotState.getWidth(), currentHotState.getHeight());
                    if (!isMouseOver(hotBounds)) {
                        // Mouse no longer over previous hot panel, this panel becomes hot
                        panelManager.setHotPanel(id);
                    }
                }
            }
        }

        // Mark that we should bring hot panel to front on click
        // Don't do it here - defer until end() after all panels have been declared
        if (mouseOverPanel && inputProcessor.isMouseClicked()) {
            panelManager.markShouldBringHotPanelToFront();
        }

        // Handle panel dragging
        if (isDraggable && title != null && !title.isEmpty()) {
            Rectangle titleBarBounds = new Rectangle(x, y, width, titleBarHeight);

            // Check if we're currently dragging this panel
            if (panelManager.isPanelDragging(id)) {
                // Update panel position based on mouse movement
                float deltaX = panelManager.getPanelDragDeltaX(inputProcessor.getMouseX());
                float deltaY = panelManager.getPanelDragDeltaY(inputProcessor.getMouseY());

                panelState.setPosition(panelManager.getPanelDragStartX() + deltaX, panelManager.getPanelDragStartY() + deltaY);

                // Update local positions for rendering
                x = panelState.getPosition().getX();
                y = panelState.getPosition().getY();
                contentStartY = y + titleBarHeight;

            } else if (isMouseOver(titleBarBounds) && inputProcessor.isMouseClicked() && stateStore.getActiveWidgetId() == null) {
                // Start dragging on title bar click (only if no other widget is active)
                // Make this panel hot AND start dragging
                panelManager.setHotPanel(id);
                panelManager.markShouldBringHotPanelToFront();
                panelManager.startPanelDrag(id, inputProcessor.getMouseX(), inputProcessor.getMouseY(), 
                                           panelState.getPosition().getX(), panelState.getPosition().getY());
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
        renderData.parentId = panelManager.isRecordingStackEmpty() ? null : panelManager.getCurrentRecordingPanel(); // Track parent
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
        panelManager.pushRecordingPanel(id);

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
        if (hasScroll && panelManager.isScrollbarDragging(id)) {
            // Calculate scrollbar dimensions (same as in renderScrollbar)
            float scrollbarHeight = height - titleBarHeight - 4; // padding * 2

            // Calculate how much the mouse moved
            float mouseDeltaY = inputProcessor.getMouseY() - panelManager.getScrollbarDragStartMouseY();

            // Convert mouse delta to scroll offset delta
            // The scrollbar thumb moves within the track, so we need to scale appropriately
            float contentHeight = panelState.getContentHeight();
            float viewportHeight = height - titleBarHeight;
            float scrollableHeight = Math.max(0, contentHeight - viewportHeight + theme.getWindowPadding() * 2);

            if (scrollableHeight > 0) {
                float thumbHeightRatio = Math.min(1.0f, viewportHeight / contentHeight);
                float thumbHeight = Math.max(20, scrollbarHeight * thumbHeightRatio);
                float trackHeight = scrollbarHeight - thumbHeight;

                // Mouse delta in track space -> scroll offset delta
                float scrollDelta = (mouseDeltaY / trackHeight) * scrollableHeight;
                panelState.setScrollOffset(panelManager.getScrollbarDragStartOffset() + scrollDelta);

                // Clamp scroll offset
                panelState.setScrollOffset(Math.max(0, Math.min(panelState.getScrollOffset(), scrollableHeight)));
            }
        }

        // Handle mouse wheel scrolling if panel is hovered AND is the hot panel
        if (hasScroll && !panelManager.isScrollbarDragging(id)) { // Don't wheel scroll while dragging
            Rectangle panelContentBounds = new Rectangle(x, contentStartY, width, height - titleBarHeight);
            float wheelDelta = inputProcessor.getMouseWheelDelta();
            if (isMouseOver(panelContentBounds) && wheelDelta != 0 && panelManager.isHotPanel(id)) {
                float scrollSpeed = 20f; // Pixels per wheel notch
                panelState.setScrollOffset(panelState.getScrollOffset() - wheelDelta * scrollSpeed);

                // Clamp scroll offset to valid range (will be refined in endPanel)
                panelState.setScrollOffset(Math.max(0, panelState.getScrollOffset()));
            }
        }

        // Store current panel ID for scroll tracking
        panelManager.setCurrentPanel(id);

        // Save current layout context
        FluxLayoutManager.LayoutContext context = new FluxLayoutManager.LayoutContext(
                layoutManager.getCursorX(), layoutManager.getCursorY(), layoutManager.getLineHeight(),
                x, contentStartY, contentWidth, height - titleBarHeight,
                x, width,
                hasScroll, hasScrollbar
        );
        layoutManager.pushContext(context);

        // Set up content area with padding and scroll offset
        float padding = theme.getWindowPadding();
        layoutManager.setCursor(x + padding, contentStartY + padding - (hasScroll ? panelState.getScrollOffset() : 0));
        layoutManager.setLineHeight(0);
    }

    /**
     * End a panel container.
     * Restores previous layout context and canvas clipping state.
     */
    public void endPanel() {
        if (layoutManager.isLayoutStackEmpty()) {
            throw new IllegalStateException("endPanel() called without matching beginPanel()");
        }

        FluxLayoutManager.LayoutContext ctx = layoutManager.peekContext(); // Peek to calculate content height before popping

        // Update panel state with content height and clamp scroll
        PanelState panelState = null;
        boolean shouldRenderScrollbar = false;
        String currentPanelId = panelManager.getCurrentPanelId();
        if (currentPanelId != null && ctx.hasScroll) {
            panelState = stateStore.getPanelState(currentPanelId);
            if (panelState != null) {
                // Calculate actual content height (how far down the cursor went)
                // We need to add back the scroll offset because cursorY started with it subtracted
                float contentHeight = (layoutManager.getCursorY() + panelState.getScrollOffset()) - (ctx.contentY + theme.getWindowPadding());
                panelState.setContentHeight(contentHeight);

                // Clamp scroll offset: can't scroll past content
                float maxScroll = Math.max(0, contentHeight - ctx.contentHeight + theme.getWindowPadding() * 2);
                panelState.setScrollOffset(Math.max(0, Math.min(panelState.getScrollOffset(), maxScroll)));

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
            final boolean wasHotPanel = finalPanelId != null && panelManager.isHotPanel(finalPanelId);
            final PanelState finalPanelState = panelState;
            final FluxLayoutManager.LayoutContext finalCtx = ctx;
            recordCommand(() -> renderScrollbar(finalPanelId, finalCtx, finalPanelState, wasHotPanel));
        }

        // Stop recording to this panel (pop from stack)
        String poppedPanelId = null;
        if (!panelManager.isRecordingStackEmpty()) {
            poppedPanelId = panelManager.getCurrentRecordingPanel();
            panelManager.popRecordingPanel();
        }
        
        // If this was a nested panel, add its commands to the parent's command list
        if (poppedPanelId != null && !panelManager.isRecordingStackEmpty()) {
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
        layoutManager.popContext();
        layoutManager.setCursor(ctx.cursorX, ctx.cursorY);
        layoutManager.setLineHeight(ctx.lineHeight);

        // Clear current panel ID
        panelManager.setCurrentPanel(null);

        // Advance cursor past the panel
        float panelHeight = ctx.contentHeight;
        layoutManager.advanceCursor(0, panelHeight);
    }

    /**
     * Render a scrollbar for a panel.
     */
    private void renderScrollbar(String panelId, FluxLayoutManager.LayoutContext ctx, PanelState panelState, boolean wasHotPanel) {
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
        float contentHeight = panelState.getContentHeight();
        float viewportHeight = ctx.contentHeight;
        float scrollableHeight = Math.max(0, contentHeight - viewportHeight + theme.getWindowPadding() * 2);

        if (scrollableHeight > 0) {
            // Thumb height proportional to viewport/content ratio
            float thumbHeightRatio = Math.min(1.0f, viewportHeight / contentHeight);
            float thumbHeight = Math.max(20, scrollbarHeight * thumbHeightRatio);

            // Thumb position based on scroll offset
            float scrollRatio = panelState.getScrollOffset() / scrollableHeight;
            float thumbY = scrollbarY + scrollRatio * (scrollbarHeight - thumbHeight);

            // Check for scrollbar thumb interaction
            Rectangle thumbBounds = new Rectangle(scrollbarX, thumbY, scrollbarWidth, thumbHeight);
            boolean thumbHovered = isMouseOver(thumbBounds);

            // Start drag on click (only if no other widget is active AND this was the hot panel)
            if (thumbHovered && inputProcessor.isMouseClicked() && panelId != null && stateStore.getActiveWidgetId() == null && wasHotPanel) {
                panelManager.startScrollbarDrag(panelId, inputProcessor.getMouseY(), panelState.getScrollOffset());
            }

            // Render thumb with hover feedback
            Color thumbColor = thumbHovered || panelManager.isScrollbarDragging(panelId)
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
        return inputProcessor.isMouseOver(bounds);
    }

    /**
     * Generate a unique ID for a widget.
     */
    private String generateId(String type, String label, float x, float y) {
        return type + ":" + label + ":" + (int) x + ":" + (int) y;
    }

    /**
     * Advance the layout cursor after drawing a widget.
     * This method is now deprecated - use layoutManager.advanceCursor() instead.
     *
     * @param width  Width of the widget just drawn
     * @param height Height of the widget just drawn
     */
    private void advanceCursor(float width, float height) {
        // Delegate to layout manager
        layoutManager.advanceCursor(width, height);
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
        String currentPanelId = panelManager.getCurrentPanelId();
        if (currentPanelId == null) {
            return true;
        }

        // Use previous frame's hot panel if current frame's isn't determined yet
        // This handles the case where early-declared panels check events before
        // later-declared (but higher-focus) panels update hotPanelId
        String hotPanelId = panelManager.getHotPanelId();
        String previousHotPanelId = panelManager.getPreviousHotPanelId();
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
        if (!panelManager.isRecordingStackEmpty()) {
            // Inside a panel - add to the current panel's command list (top of stack)
            String currentPanel = panelManager.getCurrentRecordingPanel();
            PanelRenderData renderData = panelRenderQueue.get(currentPanel);
            if (renderData != null) {
                renderData.commands.add(command);
            }
        } else {
            // Outside any panel - add to root commands
            rootCommands.add(command);
        }
    }

    @Override
    public void dispose() {
        canvas.dispose();
    }
}
