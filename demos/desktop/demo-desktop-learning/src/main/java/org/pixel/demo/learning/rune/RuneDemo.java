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
import org.pixel.ext.rune.widget.Overflow;
import org.pixel.ext.rune.widget.RuneButton;
import org.pixel.ext.rune.widget.RuneContainer;
import org.pixel.ext.rune.widget.RuneLabel;

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
        // Panel 1: Interactive Buttons (moved to top)
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
        buttonRow.setStyle(StyleProperties.BACKGROUND_COLOR, new Color(0x55555555));
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
        
        RuneContainer panel1 = new RuneContainer("panel1");
        panel1.setOverflow(Overflow.HIDDEN);
        panel1.setBounds(20, 20, 740, 100);
        panel1.setLayout(new VerticalLayout(10).align(Alignment.topLeft()));
        panel1.add(new RuneLabel().text("Interactive Buttons"));
        panel1.add(buttonRow);
        gui.add(panel1);
        
        // Panel 2: SCROLLABLE CONTENT
        RuneContainer scrollPanel = new RuneContainer("scrollPanel");
        scrollPanel.setOverflow(Overflow.SCROLL);
        scrollPanel.setStyle(StyleProperties.PADDING, 4f);
        scrollPanel.setBounds(20, 140, 300, 460);
        scrollPanel.setLayout(new VerticalLayout(8).align(Alignment.topLeft()));
        
        // Add header
        scrollPanel.add(new RuneLabel().text("=== SCROLLABLE PANEL (Use mouse wheel or drag scrollbars!) ===")
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
    public void dispose() {
        super.dispose();
        if (gui != null) gui.dispose();
        if (runeInput != null) runeInput.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings("Rune UI - Scrolling Demo", 800, 630);
        settings.setVsync(false);
        settings.setBackgroundColor(Color.BLACK);
        RuneDemo game = new RuneDemo(settings);
        game.start();
    }
}
