package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class GLES30SpriteBatch implements SpriteBatch {

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public void draw(Texture texture, Vector2 position) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Color color) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scale) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scale, float rotation) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation) {

    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation, int depth) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Color color) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Color color, Vector2 anchor, float rotation) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor, float rotation) {

    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor, float rotation, int depth) {

    }

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color) {

    }

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {

    }

    @Override
    public Shader getShader() {
        return null;
    }

    @Override
    public void begin(Matrix4 viewMatrix) {

    }

    @Override
    public void begin(Matrix4 viewMatrix, BlendMode blendMode) {

    }

    @Override
    public void end() {

    }

    @Override
    public void resizeBuffer(int newSize) {

    }

    @Override
    public void dispose() {

    }
}
