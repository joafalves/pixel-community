package org.pixel.demo.learning.primitive;

import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.NvgRenderEngine;
import org.pixel.graphics.render.RenderEngine2D;

public class PrimitivesDemo extends DemoGame {

    private RenderEngine2D re;
    private Color fillColor;

    /**
     * Constructor
     *
     * @param settings
     */
    public PrimitivesDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        re = new NvgRenderEngine(getViewportWidth(), getViewportHeight());
        fillColor = Color.GOLD;
    }

    @Override
    public void draw(DeltaTime delta) {
        re.begin();
        re.strokeWidth(6);
        re.fillColor(fillColor);

        // rectangle
        re.beginPath();
        re.rectangle(10,10, 50, 50);
        re.endPath();
        re.stroke(); // apply stroke
        re.fill(); // apply fill

        // circle
        re.beginPath();
        re.circle(getViewportWidth() / 2.f, getViewportHeight() / 2.f, 50);
        re.endPath();
        re.stroke(); // apply stroke
        re.fill(); // apply fill

        // triangle
        re.beginPath();
        re.moveTo(100, 60);
        re.lineTo(120, 80);
        re.lineTo(80, 80);
        re.endPath();

        re.stroke(); // apply stroke
        re.fill(); // apply fill

        re.end();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);

        PixelWindow window = new PrimitivesDemo(settings);
        window.start();
    }
}
