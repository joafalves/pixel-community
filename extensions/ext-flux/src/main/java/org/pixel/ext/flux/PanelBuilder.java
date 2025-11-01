package org.pixel.ext.flux;

/**
 * Fluent builder for panel configuration.
 *
 * <p>Similar to Canvas DrawOps, this builder allows fluent configuration
 * of panel properties before starting the panel.
 *
 * <p>Example usage:
 * <pre>
 * gui.panel("settings", 100, 100, 300, 400)
 *     .withTitle("Settings")
 *     .withScroll(true)
 *     .begin();
 *
 * // ... panel content ...
 *
 * gui.endPanel();
 * </pre>
 */
public class PanelBuilder {

    private final Flux flux;
    private final String id;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    // Optional properties
    private String title;
    private boolean hasScroll;
    private boolean hasScrollbar = true; // Show scrollbar by default if scrolling enabled
    private boolean hasBorder = true;
    private boolean hasBackground = true;
    private boolean isDraggable = false;

    /**
     * Create a new panel builder.
     *
     * @param flux   The Flux instance
     * @param id     Panel unique identifier
     * @param x      X position
     * @param y      Y position
     * @param width  Panel width
     * @param height Panel height
     */
    public PanelBuilder(Flux flux, String id, float x, float y, float width, float height) {
        this.flux = flux;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Add a title bar to this panel.
     * Title bar shows at the top and can be used for dragging if draggable is enabled.
     *
     * @param title The title text
     * @return This builder for chaining
     */
    public PanelBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Make this panel draggable by its title bar.
     * Requires a title to be set via withTitle().
     *
     * @param draggable True to enable dragging
     * @return This builder for chaining
     */
    public PanelBuilder withDraggable(boolean draggable) {
        this.isDraggable = draggable;
        return this;
    }

    /**
     * Enable scrolling for this panel.
     * Content that overflows panel height will be scrollable.
     *
     * @param scroll True to enable scrolling
     * @return This builder for chaining
     */
    public PanelBuilder withScroll(boolean scroll) {
        this.hasScroll = scroll;
        return this;
    }

    /**
     * Control whether to show a scrollbar when scrolling is enabled.
     * Only has effect if withScroll(true) is also set.
     *
     * @param scrollbar True to show scrollbar (default when scroll enabled)
     * @return This builder for chaining
     */
    public PanelBuilder withScrollbar(boolean scrollbar) {
        this.hasScrollbar = scrollbar;
        return this;
    }

    /**
     * Control whether the panel has a border.
     *
     * @param border True to show border (default)
     * @return This builder for chaining
     */
    public PanelBuilder withBorder(boolean border) {
        this.hasBorder = border;
        return this;
    }

    /**
     * Control whether the panel has a background.
     *
     * @param background True to show background (default)
     * @return This builder for chaining
     */
    public PanelBuilder withBackground(boolean background) {
        this.hasBackground = background;
        return this;
    }

    /**
     * Begin the panel with configured properties.
     * Call this after setting all desired properties.
     */
    public void begin() {
        flux.beginPanelInternal(id, x, y, width, height, title, hasScroll, hasScrollbar, hasBorder, hasBackground, isDraggable);
    }

    // Getters for internal use
    String getTitle() {
        return title;
    }

    boolean hasScroll() {
        return hasScroll;
    }

    boolean hasScrollbar() {
        return hasScrollbar;
    }

    boolean hasBorder() {
        return hasBorder;
    }

    boolean hasBackground() {
        return hasBackground;
    }

    boolean isDraggable() {
        return isDraggable;
    }
}
