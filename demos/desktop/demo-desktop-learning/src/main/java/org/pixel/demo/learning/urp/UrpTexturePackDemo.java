package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.TexturePack;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.RenderPipeline;
import org.pixel.graphics.render.renderable.SpriteRenderable;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class UrpTexturePackDemo extends DemoGame {

    private static final float RENDER_SCALE = 0.25f;
    private static final int SPRITES = 512;
    private static final float CHARACTER_SPEED = 20f;
    private static final float WOBBLE_SPEED = 8f;
    private static final float WOBBLE_ROTATION = 0.08f; // Rotation in radians (±0.08 rad ≈ ±4.6 degrees)
    private static final float JUMP_HEIGHT = 1.5f; // How high they bounce

    private ContentManager contentManager;
    private RenderPipeline render;
    private Camera2D camera;
    private List<CharacterState> characters;

    /**
     * Inner class to track character state including position, velocity, and animation
     */
    private static class CharacterState {
        SpriteRenderable sprite;
        Vector2 position;
        Vector2 direction;
        float wobbleTime;
        float phaseOffset; // Random offset so characters don't sync up

        CharacterState(SpriteRenderable sprite) {
            this.sprite = sprite;
            this.position = new Vector2(sprite.getPosition());
            this.wobbleTime = 0f;
            // Random phase offset between 0 and 2π so all characters wobble independently
            this.phaseOffset = MathHelper.random(0f, (float) Math.PI * 2f);

            // Generate random direction (normalized)
            this.direction = new Vector2(
                    MathHelper.random(-1f, 1f),
                    MathHelper.random(-1f, 1f)
            );
            this.direction.normalize();
        }

        void update(DeltaTime delta, float cameraWidth, float cameraHeight) {
            // Update wobble animation
            wobbleTime += delta.getElapsed();
            float animationPhase = wobbleTime * WOBBLE_SPEED + phaseOffset;

            // Calculate wobble rotation (rotating left and right)
            float wobbleRotation = (float) Math.sin(animationPhase) * WOBBLE_ROTATION;

            // Calculate jumping motion (vertical bob)
            float jumpBob = (float) Math.abs(Math.sin(animationPhase)) * JUMP_HEIGHT;

            // Update position based on direction
            position.add(direction.getX() * CHARACTER_SPEED * delta.getElapsed(),
                    direction.getY() * CHARACTER_SPEED * delta.getElapsed());

            // Check bounds and reverse direction if needed
            if (position.getX() <= 0 || position.getX() >= cameraWidth) {
                direction.setX(-direction.getX());
                // Clamp position to bounds
                position.setX(Math.max(0, Math.min(cameraWidth, position.getX())));
            }
            if (position.getY() <= 0 || position.getY() >= cameraHeight) {
                direction.setY(-direction.getY());
                // Clamp position to bounds
                position.setY(Math.max(0, Math.min(cameraHeight, position.getY())));
            }

            // Apply position and rotation for wobble effect, with jumping motion
            sprite.setPosition(position.getX(), position.getY() - jumpBob);
            sprite.setRotation(wobbleRotation);

            // Flip sprite based on horizontal direction (flip horizontally if moving right)
            float scaleX = direction.getX() > 0 ? -1f : 1f;
            sprite.setScale(scaleX, 1f);
        }
    }

    public UrpTexturePackDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        final var renderWidth = getViewportWidth() * RENDER_SCALE;
        final var renderHeight = getViewportHeight() * RENDER_SCALE;

        contentManager = ServiceProvider.get(ContentManager.class);
        render = new RenderPipeline();
        camera = new Camera2D(renderWidth, renderHeight);
        characters = new ArrayList<>();

        TexturePack charTexturePack =
                contentManager.load("images/chars-16x16.pack.json", TexturePack.class);

        String[] frames = new String[]{
                "char-01", "char-02"
        };

        for (int i = 0; i < SPRITES; i++) {
            var randomFrame = frames[MathHelper.random(0, frames.length)];

            var sprite = new SpriteRenderable();
            sprite.setPosition(MathHelper.random(0, camera.getWidth()), MathHelper.random(0, camera.getHeight()));
            sprite.setTexture(charTexturePack.getTexture());
            sprite.setSource(charTexturePack.getFrame(randomFrame).getSource());
            sprite.setAnchor(0.5f, 1.0f);

            render.submit(sprite);
            characters.add(new CharacterState(sprite));
        }
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        // Update all characters
        for (CharacterState character : characters) {
            character.update(delta, camera.getWidth(), camera.getHeight());
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        render.render(camera.getViewMatrix());
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        // Update camera size based on new window dimensions and render scale (keeping the same scale)
        camera.setSize(width * RENDER_SCALE, height * RENDER_SCALE);
        // Sync graphics viewport size with new window dimensions
        syncViewportSize();
    }

    @Override
    public void dispose() {
        contentManager.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(800, 640);
        settings.setTitle("URP Texture Pack Demo");
        settings.setWindowResizable(true);
        settings.setBackgroundColor(Color.fromHex("#20232A"));

        UrpTexturePackDemo game = new UrpTexturePackDemo(settings);
        game.start();
    }
}
