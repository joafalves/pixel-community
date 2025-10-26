package org.pixel.demo.learning.weaver;

import org.pixel.commons.DeltaTime;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.weaver.Weaver;
import org.pixel.ext.weaver.widget.Label;
import org.pixel.ext.weaver.widget.PanelWidget;

public class WeaverDemo extends DemoGame {

    private Weaver ui;

    public WeaverDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        ui = new Weaver(getViewportWidth(), getViewportHeight());
        ui.loadStyleSheetFromResources("css/weaver-demo-style.css");

        var panel = new PanelWidget();

        var label = new Label();
        label.setText("Hello, Weaver!");

        panel.addChild(label);

        ui.setContent(panel);
    }

    @Override
    public void update(DeltaTime delta) {
        ui.update(delta);
        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        ui.draw(delta);
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        ui.setViewport(width, height);
        super.onWindowSizeChange(width, height);
    }

    @Override
    public void dispose() {

    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setVsync(false);

        new WeaverDemo(settings).start();
    }
}
