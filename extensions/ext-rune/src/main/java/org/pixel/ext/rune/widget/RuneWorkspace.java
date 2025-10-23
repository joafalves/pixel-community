package org.pixel.ext.rune.widget;

import org.pixel.ext.rune.event.RuneMouseEvent;
import org.pixel.ext.rune.layout.RuneLayout;
import org.pixel.math.Rectangle;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Workspace container that manages panels with z-ordering, clipping, and active panel tracking.
 * Only accepts RunePanel children.
 *
 * <p>RuneWorkspace provides:
 * <ul>
 *   <li>Panel-only child management (enforced via add())</li>
 *   <li>Automatic z-ordering: clicked panels move to front</li>
 *   <li>Active panel tracking with :active pseudo-class support</li>
 *   <li>Content clipping to workspace bounds</li>
 *   <li>Hover blocking - only topmost panel receives hover events</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * RuneWorkspace workspace = new RuneWorkspace();
 * workspace.setBounds(0, 0, 1280, 720);
 *
 * RunePanel panel1 = new RunePanel("Tool Panel");
 * panel1.setPosition(50, 50);
 * panel1.setSize(300, 400);
 * workspace.add(panel1);  // Auto becomes active panel
 *
 * RunePanel panel2 = new RunePanel("Properties");
 * panel2.setPosition(400, 100);
 * panel2.setSize(350, 500);
 * workspace.add(panel2);  // Auto becomes active panel (replaces panel1)
 * }</pre>
 */
public class RuneWorkspace extends RuneContainer {

    private RunePanel activePanel = null;  // Currently active panel (separate from focused widget)

    /**
     * Create a new workspace.
     */
    public RuneWorkspace() {
        super();
        addClass("workspace");

        // Disable features that conflict with workspace behavior
        setAutoSize(false);              // Workspace has explicit size
        super.setLayout(null);           // Panels are positioned manually, not auto-laid-out
        setOverflow(Overflow.HIDDEN);    // Clip panels to workspace bounds, no scrollbars
    }

    @Override
    public RuneContainer add(RuneWidget child) {
        if (!(child instanceof RunePanel)) {
            throw new IllegalArgumentException(
                    "RuneWorkspace only accepts RunePanel children. Got: " + child.getClass().getSimpleName()
            );
        }

        RunePanel panel = (RunePanel) child;

        // Add panel and bring it to front
        super.add(panel);
        bringToFront(panel);

        return this;
    }

    @Override
    public RuneContainer remove(RuneWidget child) {
        super.remove(child);

        // If removing active panel, clear active state
        if (child == activePanel) {
            activePanel = null;

            // Activate the topmost remaining panel (if any)
            List<RunePanel> panels = getPanels();
            if (!panels.isEmpty()) {
                // Panels are already sorted by z-index in getPanels()
                setActivePanel(panels.get(panels.size() - 1));
            }
        }

        return this;
    }

    @Override
    public RuneContainer clear() {
        activePanel = null;
        return super.clear();
    }

    /**
     * Bring a panel to front by giving it the highest z-index.
     * Also sets it as the active panel.
     * Z-indices are automatically normalized to 0..N-1 to prevent unbounded growth.
     *
     * @param panel Panel to bring to front
     */
    public void bringToFront(RunePanel panel) {
        if (!getChildren().contains(panel)) {
            throw new IllegalArgumentException("Panel is not a child of this workspace");
        }

        List<RunePanel> panels = getPanels(); // Sorted by current z-index (low to high)

        // If this panel is already the topmost (highest z-index), do nothing
        if (!panels.isEmpty() && panels.get(panels.size() - 1) == panel) {
            setActivePanel(panel);
            return; // Already on top
        }

        // Remove the panel from its current position in z-order
        panels.remove(panel);

        // Add it to the end (will be highest z-index)
        panels.add(panel);

        // Renormalize all z-indices to 0, 1, 2, ..., N-1
        // This keeps z-indices bounded and prevents infinite growth
        for (int i = 0; i < panels.size(); i++) {
            panels.get(i).setZIndex(i);
        }

        markDirty();
        setActivePanel(panel);
    }

    /**
     * Set the active panel and update pseudo-class states.
     * The active panel gets the :active pseudo-class for styling.
     *
     * @param panel Panel to set as active (null to clear)
     */
    public void setActivePanel(RunePanel panel) {
        if (activePanel == panel) return;

        // Remove :active from previous active panel
        if (activePanel != null) {
            activePanel.removeState(":active");
        }

        // Set new active panel
        activePanel = panel;

        // Add :active to new active panel
        if (activePanel != null) {
            activePanel.addState(":active");
        }

        markDirty();
    }

    /**
     * Get the currently active panel.
     *
     * @return Active panel, or null if no panels or no active panel
     */
    public RunePanel getActivePanel() {
        return activePanel;
    }

    /**
     * Get all panels in this workspace (typed convenience method).
     *
     * @return List of panels sorted by z-index (lowest to highest)
     */
    public List<RunePanel> getPanels() {
        return getChildren().stream()
                .filter(child -> child instanceof RunePanel)
                .map(child -> (RunePanel) child)
                .sorted((a, b) -> Integer.compare(a.getZIndex(), b.getZIndex()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean dispatchMouseEvent(RuneMouseEvent event) {
        if (!isVisible() || !isEnabled()) return false;

        // Check if event is within content bounds (inherited clipping behavior)
        if (getOverflow() != Overflow.VISIBLE) {
            Rectangle contentBounds = box.contentBounds;
            float eventX = event.getX();
            float eventY = event.getY();

            if (!contentBounds.contains(eventX, eventY)) {
                return false; // Outside workspace bounds
            }
        }

        // CRITICAL: Sort children by z-index for correct event dispatch order
        // Events must go to topmost (highest z-index) panel first
        List<RunePanel> panels = getPanels(); // Already sorted by z-index (lowest to highest)

        // On PRESS, bring clicked panel to front BEFORE dispatching event
        if (event.getType() == RuneMouseEvent.Type.PRESS && event.getButton() == 0) {
            // Check panels in reverse z-order (topmost first)
            for (int i = panels.size() - 1; i >= 0; i--) {
                RunePanel panel = panels.get(i);
                if (panel.getBounds().contains(event.getX(), event.getY())) {
                    bringToFront(panel);
                    break;  // Only bring topmost clicked panel to front
                }
            }
            // Re-fetch sorted panels after z-index change
            panels = getPanels();
        }

        // Dispatch to children in reverse z-order (topmost first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).dispatchMouseEvent(event)) {
                // Child consumed event
                return true;
            }
        }

        // If no child consumed, try this workspace
        return onMouseEvent(event);
    }

    @Override
    public void validateMouseStates(float mouseX, float mouseY) {
        // Validate this workspace's state
        super.validateMouseStates(mouseX, mouseY);

        // Validate children in REVERSE z-order (topmost first)
        // Only the topmost panel under the mouse gets hover
        List<RunePanel> panels = getPanels();
        RunePanel topmostHitPanel = null;

        // Find topmost panel containing mouse
        for (int i = panels.size() - 1; i >= 0; i--) {
            RunePanel panel = panels.get(i);
            if (panel.isVisible() && panel.getBounds().contains(mouseX, mouseY)) {
                topmostHitPanel = panel;
                break;
            }
        }

        // Validate all panels, but only the topmost gets accurate hover
        for (RunePanel panel : panels) {
            if (panel == topmostHitPanel) {
                // This is the topmost panel - validate normally
                panel.validateMouseStates(mouseX, mouseY);
            } else {
                // This panel is NOT topmost - force hover OFF
                // Use a coordinate guaranteed to be outside the panel
                panel.validateMouseStates(-10000, -10000);
            }
        }
    }

    @Override
    protected boolean onMouseEvent(RuneMouseEvent event) {
        // Workspace itself doesn't handle events (only its panel children do)
        return false;
    }

    @Override
    public void setLayout(RuneLayout layout) {
        if (layout != null) {
            throw new UnsupportedOperationException(
                    "RuneWorkspace does not support layout managers. Panels are positioned manually."
            );
        }
        super.setLayout(null);
    }

    @Override
    public String getTypeName() {
        return "workspace";
    }
}
