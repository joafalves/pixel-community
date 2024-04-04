package org.pixel.demo.learning.render;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.GameWindowSettings;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.graphics.render.nanovg.NvgRenderEngine;

public class PrimitivesRenderDemo extends DemoGame {

    private RenderEngine2D re;

    public PrimitivesRenderDemo(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        re = new NvgRenderEngine(getSettings().getWindowWidth(), getSettings().getWindowHeight());
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

        // rounded-rectangle
        re.beginPath();
        re.roundedRectangle(20, 20, 260, 160, 16);
        re.endPath();
        re.stroke();
        re.fillColor(Color.YELLOW);
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
        re.moveTo(290, 210);
        re.lineTo(290, 390);
        re.lineTo(40, 390);
        re.endPath();
        re.stroke();
        re.fillColor(Color.CORAL);
        re.fill();

        // line examples:
        re.beginPath();
        re.moveTo(310, 390);
        re.quadraticCurveTo(450, 200, 590, 390);
        re.lineTo(310, 390);
        re.endPath();
        re.stroke();
        re.fillColor(Color.ROYAL);
        re.fill();

        re.beginPath();
        re.moveTo(310, 210);
        re.quadraticCurveTo(450, 400, 590, 210);
        re.lineTo(310, 210);
        re.endPath();
        re.stroke();
        re.fillColor(Color.MAROON);
        re.fill();

        re.end();
    }

    @Override
    public void dispose() {
        re.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new GameWindowSettings(600, 400);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);

        var window = new PrimitivesRenderDemo(settings);
        window.start();
    }
}
