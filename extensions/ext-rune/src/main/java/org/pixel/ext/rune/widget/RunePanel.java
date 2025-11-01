package org.pixel.ext.rune.widget;

/**
 * Panel widget with optional title bar and body container.
 *
 * <p>RunePanel is a specialized container that provides:
 * <ul>
 *   <li>Optional title bar that serves as a drag handle</li>
 *   <li>Body container for child widgets</li>
 *   <li>Automatic styling via CSS-like classes: "panel", "panel-title", "panel-body"</li>
 *   <li>Draggable via title bar (when title is provided)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * // Panel with title (draggable by title bar)
 * RunePanel panel = new RunePanel("File Explorer");
 * panel.setBounds(100, 100, 300, 400);
 * panel.setBodyLayout(new VerticalLayout(10));
 * panel.add(new RuneLabel().text("Content goes here"));
 *
 * // Panel without title (not draggable by default)
 * RunePanel panel = new RunePanel(null);
 * panel.setBounds(100, 100, 300, 400);
 * }</pre>
 *
 * <p>Designed for use with docking systems and professional application UIs.
 */
public class RunePanel extends RuneContainer {

    private String title;
    protected RuneContainer titleContainer;  // Full-width title bar container (null if no title)
    protected RuneLabel titleLabel;  // Label inside title container (null if no title)
    protected RuneContainer bodyContainer;

    /**
     * Create a panel with an optional title.
     * If title is provided, the panel becomes draggable via the title bar.
     *
     * @param title Panel title text (null for no title bar)
     */
    public RunePanel(String title) {
        super(title != null ? title.toLowerCase().replace(" ", "_") : null);
        this.title = title;

        // Add panel style class
        addClass("panel");

        // No layout - we handle positioning manually in onLayout()
        setLayout(null);

        // Create title bar container if title provided
        if (title != null && !title.isEmpty()) {
            // Title container is the full-width header bar
            titleContainer = new RuneContainer(id + "_title");
            titleContainer.addClass("panel-title");
            titleContainer.setAutoSize(false);  // We'll size it manually to full width

            // Title label goes inside the title container
            titleLabel = new RuneLabel();
            titleLabel.setText(title);
            titleLabel.setPosition(0, 0);  // Position at top-left of title container
            titleContainer.add(titleLabel);

            super.add(titleContainer);

            // Enable dragging via title bar
            setDraggable(true);
        }

        // Create body container - will be sized by us
        bodyContainer = new RuneContainer(id + "_body");
        bodyContainer.addClass("panel-body");
        bodyContainer.setAutoSize(false);  // We'll size it in onLayout
        super.add(bodyContainer);
    }

    @Override
    protected void onLayout() {
        // Manual layout: title at top (full width), body fills remaining space
        float contentWidth = box.contentBounds.getWidth();
        float contentHeight = box.contentBounds.getHeight();

        float currentY = 0;

        // Size and position title bar at top (if it exists)
        if (titleContainer != null) {
            // Title height = label's total bounds height + title container's padding
            float labelTotalHeight = titleLabel.getBox().totalBounds.getHeight();
            float titleHeight = labelTotalHeight +
                                titleContainer.getBox().paddingTop +
                                titleContainer.getBox().paddingBottom;

            titleContainer.setPosition(0, currentY);
            titleContainer.setSize(contentWidth, titleHeight);
            currentY += titleHeight;
        }

        // Body fills remaining space
        float bodyHeight = contentHeight - currentY;
        bodyContainer.setPosition(0, currentY);
        bodyContainer.setSize(contentWidth, Math.max(0, bodyHeight));
    }

    /**
     * Create a panel without a title.
     */
    public RunePanel() {
        this(null);
    }

    /**
     * Override to check if drag handle (title bar) was hit.
     * The entire title bar container is draggable (full width).
     *
     * @param localX X coordinate relative to this panel's content area
     * @param localY Y coordinate relative to this panel's content area
     * @return true if point is over title bar zone
     */
    @Override
    protected boolean isDragHandleHit(float localX, float localY) {
        // Only draggable if we have a title container
        if (titleContainer == null) {
            return false;
        }

        // The entire title bar zone (full width, from top to bottom of title container)
        float titleHeight = titleContainer.getHeight();

        return localY >= 0 && localY < titleHeight;
    }

    /**
     * Override mouse event handling to block events from reaching widgets underneath.
     * Allows internal events (like scrolling in body) to work normally.
     *
     * @param event Mouse event
     * @return true if event was handled/consumed
     */
    @Override
    protected boolean onMouseEvent(org.pixel.ext.rune.event.RuneMouseEvent event) {
        // Let parent and children handle the event normally
        boolean consumed = super.onMouseEvent(event);

        // If the event occurred within our bounds, always consume it
        // (even if children didn't consume it) to block widgets underneath
        if (getBounds().contains(event.getX(), event.getY())) {
            return true;
        }

        return consumed;
    }

    /**
     * Override mouse state validation to prevent hover bleeding through to widgets below.
     * When mouse is over this panel, widgets underneath should not receive hover states.
     *
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    @Override
    public void validateMouseStates(float mouseX, float mouseY) {
        // Validate this panel and its children normally
        super.validateMouseStates(mouseX, mouseY);

        // Note: This doesn't actively block widgets below - that's handled by the parent
        // UI system respecting event consumption. This just ensures our own states are correct.
    }

    /**
     * Add a widget to the panel body (not the title area).
     * Delegates to the internal body container.
     *
     * @param child Widget to add to body
     * @return This panel for method chaining
     */
    @Override
    public RuneContainer add(RuneWidget child) {
        bodyContainer.add(child);
        return this;
    }

    /**
     * Remove a widget from the panel body.
     *
     * @param child Widget to remove
     * @return This panel for method chaining
     */
    @Override
    public RuneContainer remove(RuneWidget child) {
        bodyContainer.remove(child);
        return this;
    }

    /**
     * Clear all widgets from the panel body (preserves title).
     *
     * @return This panel for method chaining
     */
    @Override
    public RuneContainer clear() {
        bodyContainer.clear();
        return this;
    }

    /**
     * Get the panel title.
     *
     * @return Panel title or null if no title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the panel title.
     * If changing from null to non-null or vice versa, requires recreation.
     *
     * @param title New title text
     * @return This panel for chaining
     */
    public RunePanel setTitle(String title) {
        this.title = title;
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        return this;
    }

    /**
     * Get the title label widget (for customization).
     *
     * @return Title label or null if no title
     */
    public RuneLabel getTitleLabel() {
        return titleLabel;
    }

    /**
     * Get the body container (for layout customization).
     *
     * @return Body container
     */
    public RuneContainer getBodyContainer() {
        return bodyContainer;
    }

    /**
     * Set the layout for the body container (convenience method).
     *
     * @param layout Layout manager for body content
     * @return This panel for chaining
     */
    public RunePanel setBodyLayout(org.pixel.ext.rune.layout.RuneLayout layout) {
        bodyContainer.setLayout(layout);
        return this;
    }

    /**
     * Set overflow behavior for the body container (convenience method).
     *
     * @param overflow Overflow mode (VISIBLE, HIDDEN, SCROLL)
     * @return This panel for chaining
     */
    public RunePanel setBodyOverflow(Overflow overflow) {
        bodyContainer.setOverflow(overflow);
        return this;
    }

    @Override
    public String getTypeName() {
        return "panel";
    }
}
