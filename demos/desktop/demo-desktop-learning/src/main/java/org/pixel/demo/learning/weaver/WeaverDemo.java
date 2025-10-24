package org.pixel.demo.learning.weaver;

import org.pixel.commons.DeltaTime;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.weaver.Weaver;

public class WeaverDemo extends DemoGame {

    private Weaver ui;

    public WeaverDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        ui = new Weaver(getViewportWidth(), getViewportHeight());
        ui.applyStyleFromResources("css/weaver-demo-style.css");
    }

    @Override
    public void update(DeltaTime delta) {

        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {

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
