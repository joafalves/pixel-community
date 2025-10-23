package org.pixel.demo.learning.rune;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.rune.RuneUI;
import org.pixel.ext.rune.desktop.DesktopRuneInput;
import org.pixel.ext.rune.layout.VerticalLayout;
import org.pixel.ext.rune.theme.RuneDarkTheme;
import org.pixel.ext.rune.widget.*;

/**
 * Demonstrates RuneWorkspace - a panel management system with z-ordering, active panel tracking, and clipping.
 *
 * This is a fundamentally different way to use RuneUI compared to traditional container layouts.
 *
 * Features demonstrated:
 * - Multiple draggable panels in a workspace
 * - Click-to-front z-ordering
 * - Active panel tracking with :active pseudo-class styling
 * - Content clipping to workspace bounds
 * - Hover blocking (only topmost panel receives hover events)
 * - Manual panel positioning vs auto-layout
 */
public class RuneWorkspaceDemo extends DemoGame {

    private RuneUI ui;
    private DesktopRuneInput runeInput;
    private RuneWorkspace workspace;
    private RuneLabel statusLabel;

    public RuneWorkspaceDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create UI with dark theme
        ui = new RuneUI(getViewportWidth(), getViewportHeight(), RuneDarkTheme.create());

        // Create desktop input provider
        runeInput = new DesktopRuneInput();
        ui.setInput(runeInput);

        // Create workspace (takes up full window)
        workspace = new RuneWorkspace();
        workspace.setBounds(0, 0, getViewportWidth(), getViewportHeight());
        ui.add(workspace);

        // Create Panel 1: Tool Panel (top-left)
        RunePanel toolPanel = new RunePanel("Tools");
        toolPanel.setBounds(50, 50, 300, 400);
        toolPanel.setBodyLayout(new VerticalLayout(8));
        toolPanel.setBodyOverflow(Overflow.SCROLL);

        toolPanel.add(new RuneLabel().text("Select Tool:"));
        toolPanel.add(new RuneButton().text("Brush"));
        toolPanel.add(new RuneButton().text("Eraser"));
        toolPanel.add(new RuneButton().text("Fill Bucket"));
        toolPanel.add(new RuneButton().text("Color Picker"));

        toolPanel.add(new RuneLabel().text("Brush Settings:"));
        toolPanel.add(new RuneLabel().text("Size: 10px"));
        toolPanel.add(new RuneLabel().text("Opacity: 100%"));
        toolPanel.add(new RuneLabel().text("Hardness: 80%"));

        // Add many items to test scrolling
        for (int i = 1; i <= 10; i++) {
            toolPanel.add(new RuneButton().text("Tool " + i));
        }

        workspace.add(toolPanel);

        // Create Panel 2: Properties Panel (top-right)
        RunePanel propertiesPanel = new RunePanel("Properties");
        propertiesPanel.setBounds(700, 50, 350, 500);
        propertiesPanel.setBodyLayout(new VerticalLayout(8));

        propertiesPanel.add(new RuneLabel().text("Object Properties"));
        propertiesPanel.add(new RuneLabel().text("Type: Rectangle"));
        propertiesPanel.add(new RuneLabel().text("Position: 150, 200"));
        propertiesPanel.add(new RuneLabel().text("Size: 100x80"));
        propertiesPanel.add(new RuneButton().text("Edit Transform"));
        propertiesPanel.add(new RuneButton().text("Change Color"));
        propertiesPanel.add(new RuneButton().text("Add Effect"));

        workspace.add(propertiesPanel);

        // Create Panel 3: Timeline Panel (bottom-center)
        RunePanel timelinePanel = new RunePanel("Timeline");
        timelinePanel.setBounds(150, 480, 800, 200);
        timelinePanel.setBodyLayout(new VerticalLayout(6));

        timelinePanel.add(new RuneLabel().text("Frame 1 | Frame 2 | Frame 3 | Frame 4"));
        timelinePanel.add(new RuneButton().text("Add Keyframe"));
        timelinePanel.add(new RuneButton().text("Delete Frame"));
        timelinePanel.add(new RuneLabel().text("FPS: 30 | Duration: 3.5s"));

        workspace.add(timelinePanel);

        // Create Panel 4: Console Panel (bottom-left, smaller)
        RunePanel consolePanel = new RunePanel("Console");
        consolePanel.setBounds(50, 570, 450, 130);
        consolePanel.setBodyLayout(new VerticalLayout(4));

        consolePanel.add(new RuneLabel().text("> Application started"));
        consolePanel.add(new RuneLabel().text("> Loaded 3 panels"));
        consolePanel.add(new RuneLabel().text("> Ready for input"));

        workspace.add(consolePanel);

        // Create status label (outside workspace, fixed position)
        statusLabel = new RuneLabel();
        statusLabel.setAnchor(Anchor.BOTTOM_LEFT);
        statusLabel.text("Instructions: Click panels to bring to front | Drag title bars to reposition | Hover only affects topmost panel");
        statusLabel.setBounds(10, getViewportHeight() - 30, getViewportWidth() - 20, 25);
        ui.add(statusLabel);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        ui.update(delta);

        // Update status with active panel info
        RunePanel activePanel = workspace.getActivePanel();
        if (activePanel != null) {
            String activeName = activePanel.getTitle() != null ? activePanel.getTitle() : "Untitled";
            int panelCount = workspace.getPanels().size();

            statusLabel.text(String.format(
                "Active Panel: %s | Total Panels: %d | Z-Index: %d",
                activeName, panelCount, activePanel.getZIndex()
            ));
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        // Draw UI
        workspace.setBounds(0, 0, getViewportWidth(), getViewportHeight());
        ui.draw(delta);
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        ui.setViewport(width, height);
        super.onWindowSizeChange(width, height);
    }

    @Override
    public void dispose() {
        ui.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings("Rune UI - Scrolling Demo", 800, 630);
        settings.setVsync(false);
        settings.setWindowResizable(true);
        settings.setBackgroundColor(Color.BLACK);
        RuneWorkspaceDemo game = new RuneWorkspaceDemo(settings);
        game.start();
    }
}
