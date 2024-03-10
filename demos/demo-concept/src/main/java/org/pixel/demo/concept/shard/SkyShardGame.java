package org.pixel.demo.concept.shard;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.SceneManager;
import org.pixel.graphics.DesktopGameSettings;
import org.pixel.graphics.DesktopGameWindow;

public class SkyShardGame extends DesktopGameWindow {

    private SceneManager sceneManager;

    public SkyShardGame(DesktopGameSettings settings) {
        super(settings);
    }

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
        var settings = new DesktopGameSettings(virtualWidth, virtualHeight);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDebugMode(false);
        settings.setWindowWidth(windowWidth);
        settings.setWindowHeight(windowHeight);

        var window = new SkyShardGame(settings);
        window.start();
    }
}
