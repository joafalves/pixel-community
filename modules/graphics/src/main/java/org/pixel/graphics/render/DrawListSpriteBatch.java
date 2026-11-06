package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.State;
import org.pixel.content.Font;
import org.pixel.content.FontGlyph;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.draw.Batcher;
import org.pixel.graphics.draw.Colors;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.LayerMode;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * {@link SpriteBatch} built on the URP v2 draw list (docs/design/urp-v2.md).
 * <p>
 * Same API and visual result as the classic sprite batch, with these differences:
 * <ul>
 *   <li>Everything between {@code begin} and {@code end} is recorded, sorted by depth as a whole (stable: equal
 *   depth keeps submission order) and then drawn with as few draw calls as texture slots allow. The classic batch
 *   only sorted within each 256-sprite chunk.</li>
 *   <li>Sprites and bitmap-font text share batches (the font atlas is just another texture).</li>
 *   <li>Positions, colors and source rectangles are copied when recorded, so callers may reuse their objects.</li>
 * </ul>
 * No GPU resources are owned here: the {@link RenderBackend} (owned by the graphics device) does all GPU work.
 */
public class DrawListSpriteBatch extends SpriteBatch {

    private final RenderBackend backend;
    private final DrawList drawList = new DrawList(LayerMode.SORTED);
    private final VertexStream vertices = new VertexStream(VertexLayout.SPRITE, 4096);
    private final BatchList batches;
    private final PassState passState = new PassState();

    private State state = State.NEW;
    private boolean drawing;

    /**
     * Constructor.
     *
     * @param backend The render backend of the graphics device.
     */
    public DrawListSpriteBatch(RenderBackend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Render backend cannot be null.");
        }
        this.backend = backend;
        this.batches = new BatchList(backend.capabilities().maxTextureSlots());
    }

    @Override
    public boolean init() {
        if (state.hasInitialized()) {
            return false;
        }
        state = State.INITIALIZED;
        return true;
    }

    @Override
    public void dispose() {
        drawList.clear(); // release texture references; all GPU resources belong to the backend
        drawing = false;
        state = State.DISPOSED;
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        begin(viewMatrix, BlendMode.NORMAL_BLEND);
    }

    @Override
    public void begin(Matrix4 viewMatrix, BlendMode blendMode) {
        if (!state.isActive()) {
            throw new IllegalStateException("SpriteBatch is not initialized or has been disposed.");
        }
        if (drawing) {
            throw new IllegalStateException("begin() called twice without end().");
        }
        passState.set(viewMatrix, blendMode);
        drawList.clear();
        drawing = true;
    }

    @Override
    public void end() {
        if (!drawing) {
            throw new IllegalStateException("end() called without begin().");
        }
        drawing = false;
        if (drawList.size() == 0) {
            return;
        }

        drawList.prepare();
        Batcher.build(drawList, backend.capabilities(), vertices, batches);

        backend.beginPass(passState);
        try {
            backend.submit(vertices, batches);
        } finally {
            backend.endPass();
        }
        drawList.clear();
    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
                     float scaleY, float rotation, int depth) {
        float width = source != null ? source.getWidth() * scaleX : texture.getWidth() * scaleX;
        float height = source != null ? source.getHeight() * scaleY : texture.getHeight() * scaleY;
        record(texture, position.getX(), position.getY(), width, height, source, color, anchor, rotation, depth);
    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
                     float rotation, int depth) {
        record(texture, displayArea.getX(), displayArea.getY(), displayArea.getWidth(), displayArea.getHeight(),
                source, color, anchor, rotation, depth);
    }

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {
        checkDrawing();
        Texture atlas = font.getTexture();
        float atlasWidth = atlas.getWidth();
        float atlasHeight = atlas.getHeight();
        int rgba = Colors.pack(color != null ? color : Color.WHITE);

        // same glyph placement as the classic sprite batch (integer pen position)
        float scale = fontSize / (float) font.getFontSize();
        int lineHeight = (int) (font.getFontSize() * scale + font.getVerticalSpacing());
        int x = (int) position.getX();
        int y = (int) (position.getY() + font.getFontSize() * scale + font.getVerticalSpacing());
        for (int i = 0, n = text.length(); i < n; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') { // handled before the glyph lookup: fonts usually have no glyph for '\n'
                y += lineHeight;
                x = (int) position.getX();
                continue;
            }
            FontGlyph glyph = font.getGlyph(ch);
            if (glyph == null) {
                continue;
            }

            float gx = x + glyph.getXOffset() * scale;
            float gy = y + glyph.getYOffset() * scale;
            float gw = glyph.getWidth() * scale;
            float gh = glyph.getHeight() * scale;
            drawList.addQuad(atlas,
                    gx, gy, gx + gw, gy, gx + gw, gy + gh, gx, gy + gh,
                    glyph.getX() / atlasWidth, glyph.getY() / atlasHeight,
                    (glyph.getX() + glyph.getWidth()) / atlasWidth, (glyph.getY() + glyph.getHeight()) / atlasHeight,
                    rgba, 0f);

            x += (int) (glyph.getXAdvance() * scale + font.getHorizontalSpacing());
        }
    }

    @Override
    public void resizeBuffer(int newSize) {
        if (newSize <= 0) {
            throw new IllegalArgumentException("Invalid buffer size, must be greater than zero");
        }
        drawList.ensureCapacity(newSize);
    }

    private void record(Texture texture, float x, float y, float width, float height, Rectangle source, Color color,
                        Vector2 anchor, float rotation, int depth) {
        checkDrawing();
        float ax = anchor != null ? anchor.getX() : 0f;
        float ay = anchor != null ? anchor.getY() : 0f;

        // quad corners relative to the anchor point (top-left, top-right, bottom-right, bottom-left)
        float left = -width * ax;
        float top = -height * ay;
        float right = left + width;
        float bottom = top + height;

        float x0, y0, x1, y1, x2, y2, x3, y3;
        if (rotation == 0f) {
            x0 = x + left;
            y0 = y + top;
            x1 = x + right;
            y1 = y0;
            x2 = x1;
            y2 = y + bottom;
            x3 = x0;
            y3 = y2;
        } else {
            // rotate around the anchor (same convention as Matrix4.rotate around +z used by the classic batch).
            // Math (intrinsic) rather than MathHelper/StrictMath: StrictMath's fdlibm port allocates during argument
            // reduction, and rendering does not need bit-exact cross-platform trigonometry.
            float c = (float) Math.cos(rotation);
            float s = (float) Math.sin(rotation);
            x0 = x + left * c - top * s;
            y0 = y + left * s + top * c;
            x1 = x + right * c - top * s;
            y1 = y + right * s + top * c;
            x2 = x + right * c - bottom * s;
            y2 = y + right * s + bottom * c;
            x3 = x + left * c - bottom * s;
            y3 = y + left * s + bottom * c;
        }

        float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;
        if (source != null) {
            float tw = texture.getWidth();
            float th = texture.getHeight();
            u0 = source.getX() / tw;
            v0 = source.getY() / th;
            u1 = (source.getX() + source.getWidth()) / tw;
            v1 = (source.getY() + source.getHeight()) / th;
        }

        drawList.addQuad(texture, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1,
                Colors.pack(color != null ? color : Color.WHITE), depth);
    }

    private void checkDrawing() {
        if (!drawing) {
            throw new IllegalStateException("Draw calls must happen between begin() and end().");
        }
    }
}
