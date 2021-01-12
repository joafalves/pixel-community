/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.other;

import org.pixel.core.Game;
import org.pixel.core.GameSettings;
import org.pixel.graphics.render.ShaderPostProcessor;
import org.pixel.graphics.shader.effect.SepiaEffectShader;
import org.pixel.sprite.SingleSpriteDemo;

public class PostProcessingDemo extends SingleSpriteDemo {

    private ShaderPostProcessor pp;

    public PostProcessingDemo(GameSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // note: try using org.pixel.other shaders.. or make your own! :)
        pp = new ShaderPostProcessor(new SepiaEffectShader(1f), getBackgroundColor(),
                getVirtualWidth(), getVirtualHeight());
    }

    @Override
    public void draw(float delta) {
        // post processing affects whole renders, therefore they must begin before the drawing phase (of what you wish
        // to have the effect)
        pp.begin();

        // typical drawing phase goes here, using the org.pixel.sprite code as an example
        super.draw(delta);

        // apply and output the render using the selected post processor effect
        pp.apply(delta);
    }

    public static void main(String[] args) {
        GameSettings settings = new GameSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        Game game = new PostProcessingDemo(settings);
        game.start();
    }
}
