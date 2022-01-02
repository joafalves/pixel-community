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
    }

    @Override
    public void draw(DeltaTime delta) {
        re.begin();
        re.strokeWidth(6);

        // rectangle
        re.beginPath();
        re.rectangle(10, 10, 280, 180);
        re.endPath();
        re.stroke();
        re.fillColor(Color.RED);
        re.fill();

        // circle
        re.beginPath();
        re.circle(450, 100, 80);
        re.endPath();
        re.stroke();
        re.fillColor(Color.GREEN);
        re.fill();

        // triangle left
        re.beginPath();
        re.moveTo(10, 210);
        re.lineTo(260, 210);
        re.lineTo(10, 390);
        re.endPath();
        re.stroke();
        re.fillColor(Color.BLUE);
        re.fill();

        // triangle right
        re.beginPath();
        re.moveTo(280, 210);
        re.lineTo(280, 390);
        re.lineTo(30, 390);
        re.endPath();
        re.stroke();
        re.fillColor(Color.CORAL);
        re.fill();

        re.beginPath();
        re.moveTo(310, 210);
        re.quadraticCurveTo(450, 390, 590, 210);
        re.stroke();
        re.beginPath();

        re.end();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 400);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);

        PixelWindow window = new PrimitivesDemo(settings);
        window.start();
    }
}
