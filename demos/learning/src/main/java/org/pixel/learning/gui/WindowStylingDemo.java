package org.pixel.learning.gui;

import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.gui.UIScene;
import org.pixel.gui.UIView;
import org.pixel.gui.component.UILabel;
import org.pixel.gui.component.UIPanel;
import org.pixel.learning.common.DemoGame;

public class WindowStylingDemo extends DemoGame {

    private UIView view;

    /**
     * Constructor
     *
     * @param settings
     */
    public WindowStylingDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // the UIView object manages and holds UI components
        view = new UIView(this);
        view.load();
        view.loadStyle("css/basic.css");

        UIPanel panel = view.createComponent(UIPanel.class);
        panel.setStyleName("background");
        panel.addStateChangeListener((newState, oldState) -> log.debug("Panel state changed: " + newState));

        UILabel label = view.createComponent(UILabel.class);
        label.setText("Hello, World!");
        label.setStyleName("text");
        panel.addChild(label);

        UIScene scene = view.createScene();
        scene.addChild(panel);

        view.setScene(scene); // swap active scene
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        view.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        view.draw(delta);
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowTitle("Simple Window DEMO");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new WindowStylingDemo(settings);
        window.start();
    }
}
