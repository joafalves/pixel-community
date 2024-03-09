package org.pixel.demo.learning.gui;

import org.pixel.commons.DeltaTime;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.gui.UIScene;
import org.pixel.gui.UIView;
import org.pixel.gui.component.UIPanel;

public class SimpleWindowDemo extends DemoGame {

    private UIView view;

    public SimpleWindowDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // the UIView object manages and holds UI components
        view = new UIView(this);

        // simple example with panel parenting

        UIPanel panel = view.createComponent(UIPanel.class);
        panel.setBounds(10, 10, 100, 100);
        panel.setCustomStyle("background: #f90");

        UIPanel innerPanel = view.createComponent(UIPanel.class);
        innerPanel.setBounds(5, 5, 200, 200); // part of the inner panel will be scissored
        innerPanel.setCustomStyle("background: blue");
        panel.addChild(innerPanel);

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

    @Override
    public void dispose() {
        view.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowTitle("Simple Window DEMO");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        GameWindow window = new SimpleWindowDemo(settings);
        window.start();
    }
}
