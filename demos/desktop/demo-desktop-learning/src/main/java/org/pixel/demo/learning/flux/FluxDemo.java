/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.flux;

import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.flux.Flux;
import org.pixel.ext.flux.FluxContext;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Demo showcasing the Flux GUI extension.
 */
public class FluxDemo extends DemoGame {

    private Flux gui;
    private FluxInputHelper inputHelper;
    private ContentManager content;

    private int clickCount = 0;
    private String textFieldValue = "";
    private boolean checkbox1 = false;
    private boolean checkbox2 = true;
    private float sliderValue = 0.5f;

    public FluxDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create content manager and load font
        content = ContentManager.create();
        SdfFont font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(16, 2));

        // Create input helper
        inputHelper = new FluxInputHelper(getViewportWidth(), getViewportHeight());

        // Create GUI with internal canvas
        gui = new Flux(getViewportWidth(), getViewportHeight());
        gui.getTheme().setFont(font);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        // Gather input state using helper and create context
        FluxContext ctx = inputHelper.createContext();

        // Begin GUI frame
        gui.begin(ctx);

        // === Menu Bar ===
        if (gui.beginMenuBar()) {
            if (gui.beginMenu("File")) {
                if (gui.menuItem("New", "Ctrl+N")) {
                    System.out.println("New file!");
                }
                if (gui.menuItem("Open", "Ctrl+O")) {
                    System.out.println("Open file!");
                }
                if (gui.menuItem("Save", "Ctrl+S")) {
                    System.out.println("Save file!");
                }
                gui.separator();
                if (gui.menuItem("Exit", "Alt+F4")) {
                    System.out.println("Exit!");
                    quit();
                }
                gui.endMenu();
            }

            if (gui.beginMenu("Edit")) {
                if (gui.menuItem("Undo", "Ctrl+Z")) {
                    System.out.println("Undo!");
                }
                if (gui.menuItem("Redo", "Ctrl+Y")) {
                    System.out.println("Redo!");
                }
                gui.separator();
                if (gui.menuItem("Cut", "Ctrl+X")) {
                    System.out.println("Cut!");
                }
                if (gui.menuItem("Copy", "Ctrl+C")) {
                    System.out.println("Copy!");
                }
                if (gui.menuItem("Paste", "Ctrl+V")) {
                    System.out.println("Paste!");
                }
                gui.endMenu();
            }

            if (gui.beginMenu("View")) {
                if (gui.menuItem("Zoom In", "Ctrl++")) {
                    System.out.println("Zoom in!");
                }
                if (gui.menuItem("Zoom Out", "Ctrl+-")) {
                    System.out.println("Zoom out!");
                }
                if (gui.menuItem("Reset Zoom", "Ctrl+0")) {
                    System.out.println("Reset zoom!");
                }
                gui.endMenu();
            }

            gui.endMenuBar();
        }

        // === Manual Positioning Demo ===
        // gui.label("Pixel Flux Demo - Containers & Layouts", 20, 40);

        // === Auto-Layout Demo Panel (with fluent API) ===
        gui.panel("panel1", 20, 60, 300, 400)
                .withTitle("Drag Me! - Widgets")
                .withDraggable(true)
                .withScroll(true)
                .begin();

        // Auto-sized buttons stacked vertically
        if (gui.button("Auto Button 1")) {
            clickCount++;
            System.out.println("Auto Button 1 clicked! Count: " + clickCount);
        }

        gui.spacing(5);

        if (gui.button("Auto Button 2")) {
            System.out.println("Auto Button 2 clicked!");
        }

        gui.spacing(10);
        gui.label("Click count: " + clickCount);

        gui.separator();
        gui.spacing(5);

        // Text field with label
        gui.label("Enter text:");
        textFieldValue = gui.textField("textfield1");
        gui.label("You typed: " + textFieldValue);

        gui.separator();
        gui.spacing(5);

        // Collapsing section for checkboxes
        if (gui.collapsing("checkbox_section", "Checkbox Options").withDefaultOpen(true).begin()) {
            checkbox1 = gui.checkbox("cb1", "Enable feature A", checkbox1);
            checkbox2 = gui.checkbox("cb2", "Enable feature B", checkbox2);
        }

        gui.spacing(10);

        // Collapsing section for slider
        if (gui.collapsing("slider_section", "Slider Control").withDefaultOpen(true).begin()) {
            gui.label("Adjust value:");
            sliderValue = gui.slider("slider1", sliderValue);
            gui.label(String.format("Value: %.2f", sliderValue));
        }

        gui.endPanel();

        // === Horizontal Layout Demo Panel ===
        gui.panel("panel2", 360, 60, 400, 150)
                .withTitle("Drag Me! - Horizontal Layout")
                .withDraggable(true)
                .begin();

        // Buttons on same line
        if (gui.button("A", 80, 30)) {
            System.out.println("Button A clicked!");
        }
        gui.sameLine();

        if (gui.button("B", 80, 30)) {
            System.out.println("Button B clicked!");
        }
        gui.sameLine();

        if (gui.button("C-XL", 120, 30)) {
            System.out.println("Button C clicked!");
        }

        gui.spacing(10);
        gui.label("Buttons arranged horizontally!");

        gui.endPanel();

        // === Mixed Layout Demo Panel ===
        gui.panel("panel3", 360, 240, 400, 130)
                .withTitle("Drag me! - Mixed Layout")
                .withDraggable(true)
                .begin();

        if (gui.button("Vertical Button 1")) {
            System.out.println("Vertical 1 clicked!");
        }

        if (gui.button("Short", 100, 25)) {
            System.out.println("Short clicked!");
        }
        gui.sameLine();
        gui.label("← Inline label");

        gui.endPanel();

        // === Scrollable Panel Demo ===
        gui.panel("scroll1", 20, 490, 350, 250)
                .withTitle("Drag Me! - Scrollable")
                .withScroll(true)
                .withDraggable(true)
                .begin();

        gui.label("This panel has scrollable content!");
        gui.separator();

        // Collapsing sections inside scrollable panel
        if (gui.collapsing("scroll_section1", "Group 1 (Items 1-10)").withDefaultOpen(true).begin()) {
            for (int i = 1; i <= 10; i++) {
                if (gui.button("Item " + i)) {
                    System.out.println("Clicked Item " + i);
                }
            }
        }

        gui.spacing(5);

        if (gui.collapsing("scroll_section2", "Group 2 (Items 11-20)").begin()) {
            for (int i = 11; i <= 20; i++) {
                if (gui.button("Item " + i)) {
                    System.out.println("Clicked Item " + i);
                }
            }
        }

        gui.endPanel();

        // === Nested Panel Demo ===
        gui.panel("outer", 400, 490, 360, 250)
                .withTitle("Drag Me! - Outer Panel")
                .withDraggable(true)
                .begin();

        gui.label("This is the outer panel");

        // Nested panel inside outer panel
        gui.panel("inner", 250, 120)
                .withTitle("Nested Panel")
                .withBorder(true)
                .begin();

        gui.label("I'm inside another panel!");
        if (gui.button("Nested Button")) {
            System.out.println("Nested button clicked!");
        }

        gui.endPanel(); // End inner panel
        gui.endPanel(); // End outer panel

        // End GUI frame
        gui.end();
    }

    @Override
    public void dispose() {
        inputHelper.dispose();
        gui.dispose();
        content.dispose();

        super.dispose();
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        super.onWindowSizeChange(width, height);

        // Update GUI viewport
        if (gui != null) {
            gui.setViewport(width, height);
        }
        
        // Update input helper viewport
        if (inputHelper != null) {
            inputHelper.setViewport(width, height);
        }
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings("Pixel - Flux Demo", 1200, 800);
        settings.setVsync(true);

        FluxDemo demo = new FluxDemo(settings);
        demo.start();
    }
}
