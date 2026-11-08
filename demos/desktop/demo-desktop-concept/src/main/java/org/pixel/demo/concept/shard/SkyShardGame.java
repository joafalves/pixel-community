package org.pixel.demo.concept.shard;

import org.pixel.core.DesktopLauncher;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.SceneManager;
import org.pixel.core.WindowSettings;
import org.pixel.core.GameApp;

public class SkyShardGame extends GameApp {

    private SceneManager sceneManager;

    @Override
    public void load() {
        sceneManager = new SceneManager();
    }

    @Override
    public void update(DeltaTime delta) {
       sceneManager.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        sceneManager.draw(delta);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
    }

    public static void main(String[] args) {
        final int windowWidth = 1280;
        final int windowHeight = 720;
        final int virtualWidth = 1920;
        final int virtualHeight = 1080;
        var settings = new WindowSettings(virtualWidth, virtualHeight);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDevMode(false);
        settings.setWindowWidth(windowWidth);
        settings.setWindowHeight(windowHeight);

        DesktopLauncher.run(new SkyShardGame(), settings);
    }
}
