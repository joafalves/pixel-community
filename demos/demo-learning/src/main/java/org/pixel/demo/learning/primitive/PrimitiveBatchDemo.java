package org.pixel.demo.learning.primitive;


import java.util.ArrayList;
import lombok.AllArgsConstructor;
import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.Color;
import org.pixel.graphics.PrimitiveType;
import org.pixel.graphics.render.PrimitiveBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class PrimitiveBatchDemo extends DemoGame {

    private static final float MOVEMENT_SPEED = 256f;
    private PrimitiveBatch primitiveBatch;

    private ArrayList<VertexData> vertices;

    public PrimitiveBatchDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        primitiveBatch = new PrimitiveBatch();

        // vertices setup:
        vertices = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            if (i % 2 == 0) {
                addSquare(
                        new Vector2(MathHelper.random(0, getVirtualWidth()), MathHelper.random(0, getVirtualHeight())),
                        MathHelper.random(50, 100));
            } else {
                addTriangle(
                        new Vector2(MathHelper.random(0, getVirtualWidth()), MathHelper.random(0, getVirtualHeight())),
                        MathHelper.random(50, 100));
            }
        }

        centerWindow();
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        // Keyboard direct state access for org.pixel.input detection:
        if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
            gameCamera.translate(0, -MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically

        } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
            gameCamera.translate(0, MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically
        }

        if (Keyboard.isKeyDown(KeyboardKey.LEFT) || Keyboard.isKeyDown(KeyboardKey.A)) {
            gameCamera.translate(-MOVEMENT_SPEED * delta.getElapsed(), 0); // translate camera horizontally
        } else if (Keyboard.isKeyDown(KeyboardKey.RIGHT) || Keyboard.isKeyDown(KeyboardKey.D)) {
            gameCamera.translate(MOVEMENT_SPEED * delta.getElapsed(), 0); // translate camera horizontally
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // The buffer max size must be multiple of 3 since we need 3 vertices per triangle. Use this setter
        // before the "begin()" call to set up the buffer size accordingly.
        primitiveBatch.setBufferSize(3 * 256);
        primitiveBatch.begin(gameCamera.getViewMatrix(), PrimitiveType.TRIANGLES);

        for (var vertexData : vertices) {
            primitiveBatch.draw(vertexData.position, vertexData.color);
        }

        primitiveBatch.end();
    }

    @Override
    public void dispose() {
        primitiveBatch.dispose();
        super.dispose();
    }

    private void addSquare(Vector2 position, float size) {
        var colorP2 = Color.random();
        var colorP3 = Color.random();

        vertices.add(new VertexData(position, Color.random()));
        vertices.add(new VertexData(new Vector2(position.getX() + size, position.getY()), colorP2));
        vertices.add(new VertexData(new Vector2(position.getX(), position.getY() + size), colorP3));

        vertices.add(new VertexData(new Vector2(position.getX(), position.getY() + size), colorP3));
        vertices.add(new VertexData(new Vector2(position.getX() + size, position.getY() + size), Color.random()));
        vertices.add(new VertexData(new Vector2(position.getX() + size, position.getY()), colorP2));
    }

    private void addTriangle(Vector2 position, float size) {
        vertices.add(new VertexData(position, Color.random()));
        vertices.add(new VertexData(new Vector2(position.getX() + size / 2.f, position.getY() + size / 2.f),
                Color.random()));
        vertices.add(new VertexData(new Vector2(position.getX() - size / 2.f, position.getY() + size / 2.f),
                Color.random()));
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(800, 800);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDebugMode(true);

        PixelWindow window = new PrimitiveBatchDemo(settings);
        window.start();
    }

    @AllArgsConstructor
    private static class VertexData {

        public Vector2 position;
        public Color color;
    }
}
