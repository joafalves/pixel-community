/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.other;

import org.pixel.commons.DeltaTime;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.sprite.SingleSpriteDemo;
import org.pixel.graphics.render.ShaderPostProcessor;
import org.pixel.graphics.shader.effect.SepiaEffectShader;

public class PostProcessingDemo extends SingleSpriteDemo {

    private ShaderPostProcessor pp;

    public PostProcessingDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // note: try using org.pixel.learning.other shaders.. or make your own! :)
        pp = new ShaderPostProcessor(
                new SepiaEffectShader(1f), getVirtualWidth(), getVirtualHeight());
    }

    @Override
    public void draw(DeltaTime delta) {
        // post-processing affects whole renders, therefore they must begin before the drawing phase (of what you wish
        // to have the effect)
        pp.begin();

        // typical drawing phase goes here, using the org.pixel.learning.sprite code as an example
        super.draw(delta);

        // apply and output the render using the selected post processor effect
        pp.apply(delta);
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 320);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        GameWindow window = new PostProcessingDemo(settings);
        window.start();
    }
}
