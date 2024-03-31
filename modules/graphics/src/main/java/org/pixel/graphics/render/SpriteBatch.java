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

public interface SpriteBatch extends Initializable, Disposable {

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     */
    public void draw(Texture texture, Vector2 position);

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color);

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor);

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scale    The scale of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scale);

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
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scaleX, float scaleY,
            float rotation);

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color);

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
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scale,
            float rotation);

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
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
            float scaleY, float rotation);

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
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
            float scaleY, float rotation, int depth);

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea);

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color);

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color, Vector2 anchor, float rotation);

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color);

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
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
            float rotation);

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
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
            float rotation, int depth);

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     */
    public void drawText(Font font, String text, Vector2 position, Color color);

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     * @param fontSize The size of the font.
     */
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize);

    /**
     * Get the active shader.
     *
     * @return The active shader.
     */
    public Shader getShader();

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     */
    public void begin(Matrix4 viewMatrix);

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     * @param blendMode  The blend mode.
     */
    public void begin(Matrix4 viewMatrix, BlendMode blendMode);

    /**
     * End drawing phase.
     */
    public void end();

    /**
     * Resize the buffer batch size. This will reset the buffer write index - make sure to call outside of the drawing loop.
     *
     * @param newSize The new size.
     */
    public void resizeBuffer(int newSize);
}
