package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.graphics.shader.Shader;

public abstract class SpriteBatch implements Initializable, Disposable {

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     */
    public void draw(Texture texture, Vector2 position) {
        draw(texture, position, Color.WHITE);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color) {
        draw(texture, position, color, new Vector2(0, 0));
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor) {
        draw(texture, position, color, anchor, 1);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scale    The scale of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scale) {
        draw(texture, position, color, anchor, scale, scale, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation) {
        draw(texture, position, new Rectangle(0, 0, texture.getWidth(), texture.getHeight()), color, anchor, scaleX, scaleY, rotation);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color) {
        this.draw(texture, position, source, color, Vector2.ZERO, 1.0f, 1.0f, 0.f, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scale    The scale of the sprite.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scale, float rotation) {
        this.draw(texture, position, source, color, anchor, scale, scale, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation) {
        this.draw(texture, position, source, color, anchor, scaleX, scaleY, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     * @param depth    The drawing depth of the sprite (lower numbers are drawn
     *                 first).
     */
    public abstract void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation, int depth);

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea) {
        this.draw(texture, displayArea, Color.WHITE, Vector2.ZERO, 0.f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color) {
        this.draw(texture, displayArea, color, Vector2.ZERO, 0.f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color, Vector2 anchor, float rotation) {
        this.draw(texture, displayArea, null, color, anchor, rotation);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color) {
        this.draw(texture, displayArea, source, color, Vector2.ZERO, 0f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor, float rotation) {
        this.draw(texture, displayArea, source, color, anchor, rotation, 0);
    }
    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     * @param depth       The drawing depth of the sprite (lower numbers are drawn
     *                    first).
     */
    public abstract void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor, float rotation, int depth);

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     */
    public void drawText(Font font, String text, Vector2 position, Color color) {
        this.drawText(font, text, position, color, font.getFontSize());
    }

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     * @param fontSize The size of the font.
     */
    public abstract void drawText(Font font, String text, Vector2 position, Color color, int fontSize);

    /**
     * Get the active shader.
     *
     * @return The active shader.
     */
    public abstract Shader getShader();

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     */
    public abstract void begin(Matrix4 viewMatrix);

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     * @param blendMode  The blend mode.
     */
    public abstract void begin(Matrix4 viewMatrix, BlendMode blendMode);

    /**
     * End drawing phase.
     */
    public abstract void end();

    /**
     * Resize the buffer batch size. This will reset the buffer write index - make sure to call outside of the drawing loop.
     *
     * @param newSize The new size.
     */
    public abstract void resizeBuffer(int newSize);
}
