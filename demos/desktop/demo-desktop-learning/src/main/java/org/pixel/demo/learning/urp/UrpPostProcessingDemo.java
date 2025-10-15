package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.urp.shader.GrayscaleShader;
import org.pixel.graphics.render.RenderPipeline;
import org.pixel.graphics.render.RenderTarget;
import org.pixel.graphics.render.SpriteRenderable;
import org.pixel.graphics.render.opengl.GLRenderTarget;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class UrpPostProcessingDemo extends DemoGame {

    private static final int SPRITE_COUNT = 1000;

    private ContentManager content;
    private RenderPipeline renderPipeline;

    private List<SpriteRenderable> sceneObjects;
    private RenderTarget sceneTarget;
    private SpriteRenderable screenQuad; // Renderable for applying post-processing to the render target
    private Shader grayscaleShader;

    private final Timer debugTimer = new Timer(1000);

    public UrpPostProcessingDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ServiceProvider.get(ContentManager.class);

        // 1. Create the pipeline
        renderPipeline = new RenderPipeline();

        // 2. Create the off-screen render target
        sceneTarget = new GLRenderTarget(getVirtualWidth(), getVirtualHeight());

        // 3. Load content
        Texture starTexture = content.loadTexture("images/earth-48x48.png");
        grayscaleShader = new GrayscaleShader();

        // 4. Create the objects for the main scene
        // Position sprites centered around (0, 0) to match camera's view
        sceneObjects = new ArrayList<>();
        for (int i = 0; i < SPRITE_COUNT; i++) {
            var starCmd = new SpriteRenderable()
                    .setTexture(starTexture)
                    .setPosition(new Vector2(
                        MathHelper.random(-getVirtualWidth() / 2f, getVirtualWidth() / 2f), 
                        MathHelper.random(-getVirtualHeight() / 2f, getVirtualHeight() / 2f)))
                    .setColor(Color.random())
                    .setAnchor(Vector2.half())
                    .setScale(MathHelper.random(0.5f, 2.0f));
            sceneObjects.add(starCmd);
        }
        
        // 5. Create a screen quad for post-processing
        // This will draw the render target texture with the grayscale shader
        screenQuad = new SpriteRenderable()
                .setTexture(sceneTarget.getTexture())
                .setPosition(new Vector2(-getVirtualWidth() / 2f, -getVirtualHeight() / 2f))
                .setAnchor(Vector2.ZERO)
                .setScale(1, 1)
                .setShader(grayscaleShader);
    }

    @Override
    public void update(DeltaTime delta) {
        // Animate one of the objects to show movement
        sceneObjects.get(sceneObjects.size() -1).setRotation(delta.getTotalElapsed());
        
        // Animate the grayscale intensity
        float intensity = (float) (Math.sin(delta.getTotalElapsed() * 0.8f) + 1) / 2f; // Pulse between 0.0 and 1.0
        screenQuad.setUniform("u_intensity", intensity);

        if (debugTimer.elapsed()) {
            log.info("FPS: {0} - Smoothed FPS: {1}.", getFps(), getSmoothedFps());
        }

        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        // Clear the screen
        clear(); 
        
        // Render the scene to the render target
        // Submit all scene objects to the pipeline
        for (SpriteRenderable renderable : sceneObjects) {
            renderPipeline.submit(renderable);
        }
        renderPipeline.render(gameCamera.getViewMatrix(), sceneTarget);
        renderPipeline.clear(); // Clear the pipeline for the next frame
        
        // Clear the screen
        clear(); 

        //  Draw the render target with post-processing effect
        // Reuse the same pipeline (it was cleared after the previous render)
        renderPipeline.submit(screenQuad);
        renderPipeline.render(gameCamera.getViewMatrix());
        renderPipeline.clear(); // Clear the pipeline for the next frame
    }

    @Override
    public void dispose() {
        super.dispose();
        renderPipeline.dispose();
        grayscaleShader.dispose();
        sceneTarget.dispose();
        content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setVsync(false);
        settings.setTitle("Post-Processing with RenderTarget Demo");
        settings.setAutoClear(false);

        var game = new UrpPostProcessingDemo(settings);
        game.start();
    }
}
