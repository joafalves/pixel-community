/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.rune;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.rune.RuneUI;
import org.pixel.ext.rune.desktop.DesktopRuneInput;
import org.pixel.ext.rune.layout.Alignment;
import org.pixel.ext.rune.layout.HorizontalLayout;
import org.pixel.ext.rune.layout.VerticalLayout;
import org.pixel.ext.rune.style.StyleProperties;
import org.pixel.ext.rune.theme.RuneDarkTheme;
import org.pixel.ext.rune.widget.*;
import org.pixel.math.Rectangle;

import java.util.ArrayList;

public class RuneDemo extends DemoGame {

    private RuneUI gui;
    private DesktopRuneInput runeInput;

    public RuneDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        
        // Create Rune UI with dark theme stylesheet
        gui = new RuneUI(getViewportWidth(), getViewportHeight(), RuneDarkTheme.create());
        
        // Create desktop input provider (auto-wires keyboard/mouse)
        runeInput = new DesktopRuneInput();
        gui.setInput(runeInput);
        
        buildUI();
    }

    private void buildUI() {
        // Panel: Interactive Buttons (draggable panel with title)
        RuneButton disabledBtn = new RuneButton();
        disabledBtn.text("Disabled");
        disabledBtn.setEnabled(false);
        
        RuneButton toggleBtn = new RuneButton();
        toggleBtn.text("Toggle Me");
        toggleBtn.onClick(() -> {
            boolean newState = !disabledBtn.isEnabled();
            disabledBtn.setEnabled(newState);
            toggleBtn.text(newState ? "Disable" : "Enable");
        });
        
        RuneContainer buttonRow = new RuneContainer();
        buttonRow.setSize(720, 55);
        buttonRow.setLayout(new HorizontalLayout(10).align(Alignment.topLeft()));
        buttonRow.add(new RuneButton().text("Normal Button")
                .onClick(() -> System.out.println("Normal button clicked!")))
            .add(new RuneButton().text("Primary")
                .setStyle(StyleProperties.BACKGROUND_COLOR, RuneDarkTheme.PRIMARY)
                .onClick(() -> System.out.println("Primary button clicked!")))
            .add(new RuneButton().text("Success")
                .setStyle(StyleProperties.BACKGROUND_COLOR, RuneDarkTheme.SUCCESS)
                .setStyle(":hover", StyleProperties.BACKGROUND_COLOR, Color.RED)
                .onClick(() -> System.out.println("Success button clicked!")))
            .add(disabledBtn)
            .add(toggleBtn);
        
        // Use RunePanel with title
        RuneContainer panel1 = new RuneContainer("Interactive Buttons");
        panel1.setBounds(20, 20, 740, 100);
        panel1.setLayout(new VerticalLayout(10).align(Alignment.topLeft()));
        panel1.add(buttonRow);
        gui.add(panel1);
        
        // Panel: SCROLLABLE CONTENT (draggable panel with title)
        RuneContainer scrollPanel = new RuneContainer("Scrollable Content");
        scrollPanel.setBounds(20, 140, 300, 460);
        scrollPanel.setDraggable(true);
        scrollPanel.setDragHandles(new ArrayList<>(0) {{
            add(new Rectangle(0, 0,300, 460));
        }});
        scrollPanel.setOverflow(Overflow.SCROLL);

        scrollPanel.setLayout(new VerticalLayout(8).align(Alignment.topLeft()));
        
        // Add header
        scrollPanel.add(new RuneLabel().text("Use mouse wheel or drag scrollbars!")
                .textColor(RuneDarkTheme.INFO));
        
        // Add lots of content to demonstrate scrolling
        for (int i = 1; i <= 20; i++) {
            Color itemColor = switch (i % 5) {
                case 0 -> RuneDarkTheme.PRIMARY;
                case 1 -> RuneDarkTheme.SECONDARY;
                case 2 -> RuneDarkTheme.SUCCESS;
                case 3 -> RuneDarkTheme.WARNING;
                case 4 -> RuneDarkTheme.ERROR;
                default -> RuneDarkTheme.INFO;
            };
            
            scrollPanel.add(new RuneLabel().text("Scrollable Item #" + i + " - This is a long line of text to show content!")
                    .textColor(itemColor));
        }
        
        scrollPanel.add(new RuneLabel().text("=== END OF CONTENT ===")
                .textColor(RuneDarkTheme.INFO));
        
        gui.add(scrollPanel);

        // Panel: Color Samples
        RuneContainer colorPanel = new RuneContainer("Color Palette");
        colorPanel.setBounds(340, 360, 420, 240);
        colorPanel.setLayout(new VerticalLayout(8).align(Alignment.topLeft()));
        colorPanel.add(new RuneLabel().text("Primary").textColor(RuneDarkTheme.PRIMARY));
        colorPanel.add(new RuneLabel().text("Secondary").textColor(RuneDarkTheme.SECONDARY));
        colorPanel.add(new RuneLabel().text("Success").textColor(RuneDarkTheme.SUCCESS));
        colorPanel.add(new RuneLabel().text("Warning").textColor(RuneDarkTheme.WARNING));
        colorPanel.add(new RuneLabel().text("Error").textColor(RuneDarkTheme.ERROR));
        colorPanel.add(new RuneLabel().text("Info").textColor(RuneDarkTheme.INFO));
        gui.add(colorPanel);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        gui.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        gui.draw(delta);
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        gui.setViewport(width, height);
        super.onWindowSizeChange(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (gui != null) gui.dispose();
        if (runeInput != null) runeInput.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings("Rune UI - Scrolling Demo", 800, 630);
        settings.setVsync(false);
        settings.setWindowResizable(true);
        settings.setBackgroundColor(Color.BLACK);
        RuneDemo game = new RuneDemo(settings);
        game.start();
    }
}
