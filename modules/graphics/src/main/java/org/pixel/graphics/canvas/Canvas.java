package org.pixel.graphics.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.graphics.draw.Colors;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.ShapeType;
import org.pixel.graphics.draw.StyleTable;
import org.pixel.math.Rectangle;

import java.util.Arrays;

/**
 * Vector-style 2D drawing (shapes, strokes, images) recorded into a {@link DrawList}. GL-free.
 * <p>
 * Commands are fluent builders: {@code canvas.rect(10, 10, 200, 80).radius(8).fill(c).stroke(2, s)}. The builder
 * is a single reused object per shape kind; a command is <b>committed</b> when the next command starts, when any
 * state changes ({@link #translate}, {@link #clip}, {@link #save}, ...), or on {@link #flush()}. So setters can be
 * called in any order, nothing is allocated, and builder references must not be kept.
 * <p>
 * Shapes are signed distance fields evaluated per pixel: edges are anti-aliased at any scale or rotation, and fill
 * and stroke are drawn together by one quad. Clipping is axis-aligned and per vertex (a clip change never breaks a
 * batch); under a rotated transform the clip falls back to the bounding box of the rotated rectangle.
 */
public final class Canvas {

    private static final Logger log = LoggerFactory.getLogger(Canvas.class);
    private static final float AA_MARGIN = 2f; // quad margin around shapes for the anti-aliased edge (pixels)

    private final DrawList list;
    private final Affine transform = new Affine();
    private int clipId;
    private float clipX0, clipY0, clipX1, clipY1;

    // save()/restore() stack: 6 transform floats + clip id + 4 clip floats per entry
    private static final int STATE_FLOATS = 11;
    private float[] stack = new float[STATE_FLOATS * 8];
    private int depth;

    private final Rect rect = new Rect();
    private final Ellipse ellipse = new Ellipse();
    private final Line line = new Line();
    private final Image image = new Image();
    private final NinePatch ninePatch = new NinePatch();
    private Command pending;
    private boolean warnedRotatedClip;

    /**
     * Constructor.
     *
     * @param list The draw list commands are recorded into.
     */
    public Canvas(DrawList list) {
        if (list == null) {
            throw new IllegalArgumentException("Draw list cannot be null.");
        }
        this.list = list;
    }

    /**
     * Reset transform, clip and the save stack (keeps recorded commands). Called by the owner at the start of a
     * frame.
     */
    public void reset() {
        pending = null;
        transform.identity();
        clipId = 0;
        depth = 0;
    }

    /**
     * Commit the pending command (if any).
     */
    public void flush() {
        if (pending != null) {
            Command command = pending;
            pending = null;
            command.commit();
        }
    }

    // region state

    /**
     * Push the current transform and clip.
     */
    public void save() {
        flush();
        int base = depth * STATE_FLOATS;
        if (base + STATE_FLOATS > stack.length) {
            stack = Arrays.copyOf(stack, stack.length * 2);
        }
        stack[base] = transform.a;
        stack[base + 1] = transform.b;
        stack[base + 2] = transform.c;
        stack[base + 3] = transform.d;
        stack[base + 4] = transform.tx;
        stack[base + 5] = transform.ty;
        stack[base + 6] = clipId;
        stack[base + 7] = clipX0;
        stack[base + 8] = clipY0;
        stack[base + 9] = clipX1;
        stack[base + 10] = clipY1;
        depth++;
    }

    /**
     * Pop the transform and clip pushed by the matching {@link #save()}.
     */
    public void restore() {
        flush();
        if (depth == 0) {
            throw new IllegalStateException("restore() without matching save().");
        }
        depth--;
        int base = depth * STATE_FLOATS;
        transform.a = stack[base];
        transform.b = stack[base + 1];
        transform.c = stack[base + 2];
        transform.d = stack[base + 3];
        transform.tx = stack[base + 4];
        transform.ty = stack[base + 5];
        clipId = (int) stack[base + 6];
        clipX0 = stack[base + 7];
        clipY0 = stack[base + 8];
        clipX1 = stack[base + 9];
        clipY1 = stack[base + 10];
    }

    public void translate(float x, float y) {
        flush();
        transform.translate(x, y);
    }

    /**
     * @param radians Rotation; positive values rotate clockwise on screen (y down), like sprite rotation.
     */
    public void rotate(float radians) {
        flush();
        transform.rotate(radians);
    }

    public void scale(float sx, float sy) {
        flush();
        transform.scale(sx, sy);
    }

    public void scale(float s) {
        scale(s, s);
    }

    public void resetTransform() {
        flush();
        transform.identity();
    }

    /**
     * Intersect the clip with a rectangle (in current coordinates). Drawing outside it is discarded. Undo with
     * {@link #restore()} or {@link #resetClip()}.
     */
    public void clip(float x, float y, float width, float height) {
        flush();
        float x0 = transform.mapX(x, y), y0 = transform.mapY(x, y);
        float x1 = transform.mapX(x + width, y), y1 = transform.mapY(x + width, y);
        float x2 = transform.mapX(x + width, y + height), y2 = transform.mapY(x + width, y + height);
        float x3 = transform.mapX(x, y + height), y3 = transform.mapY(x, y + height);
        if (!transform.isAxisAligned() && !warnedRotatedClip) {
            warnedRotatedClip = true;
            log.warn("Canvas clip under a rotated transform uses the bounding box of the rotated rectangle.");
        }
        float minX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        float minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        float maxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        float maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        if (clipId != 0) {
            minX = Math.max(minX, clipX0);
            minY = Math.max(minY, clipY0);
            maxX = Math.min(maxX, clipX1);
            maxY = Math.min(maxY, clipY1);
        }
        clipX0 = minX;
        clipY0 = minY;
        clipX1 = Math.max(minX, maxX); // empty intersections clip everything
        clipY1 = Math.max(minY, maxY);
        clipId = list.clips().add(clipX0, clipY0, clipX1, clipY1);
    }

    /**
     * Run {@code body} with an additional clip, restoring the previous state afterwards.
     */
    public void clip(float x, float y, float width, float height, Runnable body) {
        save();
        try {
            clip(x, y, width, height);
            body.run();
        } finally {
            restore();
        }
    }

    /**
     * Remove the clip (until the next {@link #restore()} brings back a saved one).
     */
    public void resetClip() {
        flush();
        clipId = 0;
    }

    // endregion

    // region commands

    /**
     * Start a rectangle. Default: white fill, no stroke, square corners.
     */
    public Rect rect(float x, float y, float width, float height) {
        flush();
        rect.reset(x, y, width, height);
        pending = rect;
        return rect;
    }

    /**
     * Start a rounded rectangle (same as {@code rect(...).radius(radius)}).
     */
    public Rect roundRect(float x, float y, float width, float height, float radius) {
        return rect(x, y, width, height).radius(radius);
    }

    /**
     * Start a circle. Default: white fill, no stroke.
     */
    public Ellipse circle(float centerX, float centerY, float radius) {
        return ellipse(centerX, centerY, radius, radius);
    }

    /**
     * Start an ellipse. Default: white fill, no stroke.
     */
    public Ellipse ellipse(float centerX, float centerY, float radiusX, float radiusY) {
        flush();
        ellipse.reset(centerX, centerY, Math.abs(radiusX), Math.abs(radiusY));
        pending = ellipse;
        return ellipse;
    }

    /**
     * Start a line. Default: white, 1 unit wide, butt caps.
     */
    public Line line(float x1, float y1, float x2, float y2) {
        flush();
        line.reset(x1, y1, x2, y2);
        pending = line;
        return line;
    }

    /**
     * Start an image at (x, y) with the texture's size. Default: no tint, top-left anchor, whole texture.
     */
    public Image image(Texture texture, float x, float y) {
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null.");
        }
        flush();
        image.reset(texture, x, y);
        pending = image;
        return image;
    }

    /**
     * Start a nine-patch image: the texture is split by {@link NinePatch#insets} into 3x3 parts; corners keep their
     * size, edges stretch along one axis and the center stretches both ways (panels, buttons, frames).
     */
    public NinePatch ninePatch(Texture texture, float x, float y, float width, float height) {
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null.");
        }
        flush();
        ninePatch.reset(texture, x, y, width, height);
        pending = ninePatch;
        return ninePatch;
    }

    // endregion

    private float aaMargin() {
        float scale = transform.minScale();
        return scale > 1e-6f ? AA_MARGIN / scale : AA_MARGIN;
    }

    private void addShapeQuad(ShapeType type, float cx, float cy, float ux, float uy, float ex, float ey,
                              float dimX, float dimY, float p0, float p1, float p2, float p3,
                              int rgba, int styleId, float depthValue) {
        // local frame: origin (cx, cy), x axis (ux, uy), y axis (-uy, ux); corners at (+-ex, +-ey)
        float ax = ux * ex, ay = uy * ex;   // local x extent in user space
        float bx = -uy * ey, by = ux * ey;  // local y extent in user space
        float x0 = cx - ax - bx, y0 = cy - ay - by;
        float x1 = cx + ax - bx, y1 = cy + ay - by;
        float x2 = cx + ax + bx, y2 = cy + ay + by;
        float x3 = cx - ax + bx, y3 = cy - ay + by;
        Affine m = transform;
        list.addShape(type, null,
                m.mapX(x0, y0), m.mapY(x0, y0), m.mapX(x1, y1), m.mapY(x1, y1),
                m.mapX(x2, y2), m.mapY(x2, y2), m.mapX(x3, y3), m.mapY(x3, y3),
                dimX, dimY, dimX, dimY, ex, ey, p0, p1, p2, p3, rgba, styleId, clipId, depthValue);
    }

    private abstract static class Command {
        abstract void commit();
    }

    /**
     * Paint, stroke, shadow and depth settings shared by all shapes.
     * <p>
     * Coordinates of gradients are in the same space as the shape's own coordinates (before the canvas transform);
     * shadow offsets and blur are in that space too.
     *
     * @param <T> The concrete builder (for fluent chaining).
     */
    public abstract class Shape<T extends Shape<T>> extends Command {
        int fillRgba;
        boolean hasStroke;
        int strokeRgba;
        float strokeWidth;
        StrokeAlign strokeAlign;
        float depthValue;
        // gradient
        float paintType;
        float gx0, gy0, gx1, gy1, gRadius;
        int gradientEndRgba;
        // shadow
        boolean hasShadow;
        int shadowRgba;
        float shadowX, shadowY, shadowBlur, shadowSpread;
        // dash (exposed by the shapes that support it)
        float dashLength, dashGap, dashPhase;

        void resetStyle() {
            fillRgba = Colors.pack(1f, 1f, 1f, 1f);
            hasStroke = false;
            strokeAlign = StrokeAlign.CENTER;
            depthValue = 0f;
            paintType = StyleTable.PAINT_SOLID;
            hasShadow = false;
            dashLength = 0f;
            dashGap = 0f;
            dashPhase = 0f;
        }

        @SuppressWarnings("unchecked")
        T self() {
            return (T) this;
        }

        /**
         * Solid fill color ({@code null} = no fill).
         */
        public T fill(Color color) {
            fillRgba = color != null ? Colors.pack(color) : 0;
            paintType = StyleTable.PAINT_SOLID;
            return self();
        }

        /**
         * No fill (stroke and/or shadow only).
         */
        public T noFill() {
            fillRgba = 0;
            paintType = StyleTable.PAINT_SOLID;
            return self();
        }

        /**
         * Linear gradient fill from (x0, y0) to (x1, y1); colors are clamped beyond the end points.
         */
        public T fillLinear(float x0, float y0, Color start, float x1, float y1, Color end) {
            fillRgba = Colors.pack(start != null ? start : Color.WHITE);
            gradientEndRgba = Colors.pack(end != null ? end : Color.WHITE);
            gx0 = x0;
            gy0 = y0;
            gx1 = x1;
            gy1 = y1;
            paintType = StyleTable.PAINT_LINEAR;
            return self();
        }

        /**
         * Radial gradient fill: {@code center} color at (cx, cy), {@code edge} color at {@code radius} and beyond.
         */
        public T fillRadial(float cx, float cy, float radius, Color center, Color edge) {
            fillRgba = Colors.pack(center != null ? center : Color.WHITE);
            gradientEndRgba = Colors.pack(edge != null ? edge : Color.WHITE);
            gx0 = cx;
            gy0 = cy;
            gRadius = Math.max(radius, 1e-4f);
            paintType = StyleTable.PAINT_RADIAL;
            return self();
        }

        /**
         * Stroke the edge.
         *
         * @param width The stroke width (local units).
         * @param color The stroke color ({@code null} = no stroke).
         */
        public T stroke(float width, Color color) {
            hasStroke = color != null && width > 0f;
            if (hasStroke) {
                strokeWidth = width;
                strokeRgba = Colors.pack(color);
            }
            return self();
        }

        /**
         * Where the stroke sits relative to the edge (default {@link StrokeAlign#CENTER}).
         */
        public T strokeAlign(StrokeAlign align) {
            strokeAlign = align != null ? align : StrokeAlign.CENTER;
            return self();
        }

        /**
         * Soft drop shadow drawn below the shape (like CSS {@code box-shadow}).
         *
         * @param offsetX Horizontal offset.
         * @param offsetY Vertical offset.
         * @param blur    Blur radius (0 = hard shadow); the Gaussian sigma is half of it.
         * @param color   Shadow color ({@code null} = no shadow).
         */
        public T shadow(float offsetX, float offsetY, float blur, Color color) {
            return shadow(offsetX, offsetY, blur, 0f, color);
        }

        /**
         * Soft drop shadow drawn below the shape (like CSS {@code box-shadow}).
         *
         * @param offsetX Horizontal offset.
         * @param offsetY Vertical offset.
         * @param blur    Blur radius (0 = hard shadow); the Gaussian sigma is half of it.
         * @param spread  Grows (positive) or shrinks (negative) the shadow shape.
         * @param color   Shadow color ({@code null} = no shadow).
         */
        public T shadow(float offsetX, float offsetY, float blur, float spread, Color color) {
            hasShadow = color != null;
            if (hasShadow) {
                shadowRgba = Colors.pack(color);
                shadowX = offsetX;
                shadowY = offsetY;
                shadowBlur = Math.max(0f, blur);
                shadowSpread = spread;
            }
            return self();
        }

        /**
         * Depth (only meaningful in a sorted layer).
         */
        public T depth(float depth) {
            depthValue = depth;
            return self();
        }

        boolean isVisible() {
            return (fillRgba >>> 24) != 0 || (paintType != StyleTable.PAINT_SOLID && (gradientEndRgba >>> 24) != 0)
                    || hasStroke || hasShadow;
        }

        /**
         * Quad margin around the shape: the larger of the outside stroke and the shadow's reach.
         */
        float outsideExtent() {
            float extent = hasStroke ? strokeAlign.outsideExtent(strokeWidth) : 0f;
            if (hasShadow) {
                float reach = Math.max(Math.abs(shadowX), Math.abs(shadowY)) + Math.max(0f, shadowSpread)
                        + 1.5f * shadowBlur; // ~3 sigma
                extent = Math.max(extent, reach);
            }
            return extent;
        }

        /**
         * Build and intern the style record. Gradient points and the shadow offset are converted into the shape's
         * local frame: origin (cx, cy), x axis (ux, uy).
         *
         * @return The style id, or 0 for a plain fill.
         */
        int styleId(float cx, float cy, float ux, float uy) {
            boolean dashed = dashLength > 0f;
            if (!hasStroke && !hasShadow && !dashed && paintType == StyleTable.PAINT_SOLID) {
                return 0;
            }
            float[] r = styleRecord;
            java.util.Arrays.fill(r, 0f);
            if (hasStroke) {
                unpack(strokeRgba, r, StyleTable.STROKE_COLOR);
                r[StyleTable.STROKE_WIDTH] = strokeWidth;
                r[StyleTable.STROKE_OFFSET] = strokeAlign.offset(strokeWidth);
            }
            if (dashed) {
                r[StyleTable.DASH_LENGTH] = dashLength;
                r[StyleTable.DASH_GAP] = dashGap;
                r[StyleTable.DASH_PHASE] = dashPhase;
            }
            r[StyleTable.PAINT_TYPE] = paintType;
            if (paintType != StyleTable.PAINT_SOLID) {
                r[StyleTable.GRADIENT_GEOMETRY] = localX(gx0 - cx, gy0 - cy, ux, uy);
                r[StyleTable.GRADIENT_GEOMETRY + 1] = localY(gx0 - cx, gy0 - cy, ux, uy);
                if (paintType == StyleTable.PAINT_LINEAR) {
                    r[StyleTable.GRADIENT_GEOMETRY + 2] = localX(gx1 - cx, gy1 - cy, ux, uy);
                    r[StyleTable.GRADIENT_GEOMETRY + 3] = localY(gx1 - cx, gy1 - cy, ux, uy);
                } else {
                    r[StyleTable.GRADIENT_GEOMETRY + 2] = gRadius;
                }
                unpack(gradientEndRgba, r, StyleTable.GRADIENT_END_COLOR);
            }
            if (hasShadow) {
                unpack(shadowRgba, r, StyleTable.SHADOW_COLOR);
                r[StyleTable.SHADOW_OFFSET_X] = localX(shadowX, shadowY, ux, uy);
                r[StyleTable.SHADOW_OFFSET_Y] = localY(shadowX, shadowY, ux, uy);
                r[StyleTable.SHADOW_BLUR] = shadowBlur;
                r[StyleTable.SHADOW_SPREAD] = shadowSpread;
            }
            return list.styles().intern(r);
        }
    }

    private final float[] styleRecord = new float[StyleTable.FLOATS];

    private static float localX(float dx, float dy, float ux, float uy) {
        return dx * ux + dy * uy;
    }

    private static float localY(float dx, float dy, float ux, float uy) {
        return -dx * uy + dy * ux;
    }

    private static void unpack(int rgba, float[] dest, int offset) {
        dest[offset] = (rgba & 0xFF) / 255f;
        dest[offset + 1] = ((rgba >>> 8) & 0xFF) / 255f;
        dest[offset + 2] = ((rgba >>> 16) & 0xFF) / 255f;
        dest[offset + 3] = ((rgba >>> 24) & 0xFF) / 255f;
    }

    /**
     * Rectangle builder (see {@link #rect}).
     */
    public final class Rect extends Shape<Rect> {
        private float x, y, width, height;
        private float rTopLeft, rTopRight, rBottomRight, rBottomLeft;

        void reset(float x, float y, float width, float height) {
            resetStyle();
            this.x = width < 0 ? x + width : x;
            this.y = height < 0 ? y + height : y;
            this.width = Math.abs(width);
            this.height = Math.abs(height);
            rTopLeft = rTopRight = rBottomRight = rBottomLeft = 0f;
        }

        /**
         * Same radius on all corners (clamped to half the shorter side).
         */
        public Rect radius(float radius) {
            return radius(radius, radius, radius, radius);
        }

        /**
         * Per-corner radii (clamped to half the shorter side).
         */
        public Rect radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            rTopLeft = Math.max(0f, topLeft);
            rTopRight = Math.max(0f, topRight);
            rBottomRight = Math.max(0f, bottomRight);
            rBottomLeft = Math.max(0f, bottomLeft);
            return this;
        }

        @Override
        void commit() {
            if (width == 0f || height == 0f || !isVisible()) {
                return;
            }
            float pad = outsideExtent() + aaMargin();
            float hw = width * 0.5f, hh = height * 0.5f;
            float cx = x + hw, cy = y + hh;
            addShapeQuad(ShapeType.RECT, cx, cy, 1f, 0f, hw + pad, hh + pad, hw, hh,
                    rTopLeft, rTopRight, rBottomRight, rBottomLeft, fillRgba, styleId(cx, cy, 1f, 0f), depthValue);
        }
    }

    /**
     * Ellipse/circle builder (see {@link #ellipse}, {@link #circle}).
     */
    public final class Ellipse extends Shape<Ellipse> {
        private float cx, cy, rx, ry;

        void reset(float cx, float cy, float rx, float ry) {
            resetStyle();
            this.cx = cx;
            this.cy = cy;
            this.rx = rx;
            this.ry = ry;
        }

        /**
         * Dash the stroke: {@code length} drawn, {@code gap} skipped, measured along the outline (exact for
         * circles, approximated with the mean radius for ellipses). Requires a stroke.
         */
        public Ellipse dash(float length, float gap) {
            dashLength = Math.max(0f, length);
            dashGap = Math.max(0f, gap);
            return this;
        }

        /**
         * Shift the dash pattern along the outline (animate it for "marching ants").
         */
        public Ellipse dashPhase(float phase) {
            dashPhase = phase;
            return this;
        }

        @Override
        void commit() {
            if (rx == 0f || ry == 0f || !isVisible()) {
                return;
            }
            float pad = outsideExtent() + aaMargin();
            addShapeQuad(ShapeType.ELLIPSE, cx, cy, 1f, 0f, rx + pad, ry + pad, rx, ry, 0, 0, 0, 0,
                    fillRgba, styleId(cx, cy, 1f, 0f), depthValue);
        }
    }

    /**
     * Line builder (see {@link #line}). The fill color is the line color.
     */
    public final class Line extends Shape<Line> {
        private float x1, y1, x2, y2;
        private float width;
        private LineCap cap;

        void reset(float x1, float y1, float x2, float y2) {
            resetStyle();
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            width = 1f;
            cap = LineCap.BUTT;
        }

        /**
         * Line color (same as {@link #fill(Color)}).
         */
        public Line color(Color color) {
            return fill(color);
        }

        public Line width(float width) {
            this.width = Math.max(0f, width);
            return this;
        }

        public Line cap(LineCap cap) {
            this.cap = cap != null ? cap : LineCap.BUTT;
            return this;
        }

        /**
         * Dashed line: {@code length} drawn, {@code gap} skipped, starting at the first point. Dash ends are flat.
         */
        public Line dash(float length, float gap) {
            dashLength = Math.max(0f, length);
            dashGap = Math.max(0f, gap);
            return this;
        }

        /**
         * Shift the dash pattern along the line (animate it for "marching ants").
         */
        public Line dashPhase(float phase) {
            dashPhase = phase;
            return this;
        }

        @Override
        void commit() {
            if (width == 0f || !isVisible()) {
                return;
            }
            float dx = x2 - x1, dy = y2 - y1;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float ux = length > 0f ? dx / length : 1f;
            float uy = length > 0f ? dy / length : 0f;
            float halfLength = length * 0.5f;
            float halfWidth = width * 0.5f;
            float capExtent = cap == LineCap.BUTT ? 0f : halfWidth;
            float pad = outsideExtent() + aaMargin();
            float cx = (x1 + x2) * 0.5f, cy = (y1 + y2) * 0.5f;
            addShapeQuad(ShapeType.SEGMENT, cx, cy, ux, uy,
                    halfLength + capExtent + pad, halfWidth + pad, halfLength, halfWidth, cap.code(), 0, 0, 0,
                    fillRgba, styleId(cx, cy, ux, uy), depthValue);
        }
    }

    /**
     * Image builder (see {@link #image}).
     */
    public final class Image extends Command {
        private Texture texture;
        private float x, y, width, height, anchorX, anchorY;
        private float srcX, srcY, srcWidth, srcHeight;
        private boolean hasSource;
        private int tintRgba;
        private float depthValue;

        void reset(Texture texture, float x, float y) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = texture.getWidth();
            this.height = texture.getHeight();
            anchorX = anchorY = 0f;
            hasSource = false;
            tintRgba = Colors.pack(1f, 1f, 1f, 1f);
            depthValue = 0f;
        }

        /**
         * Destination size (default: the source size, or the texture size).
         */
        public Image size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Draw only part of the texture (pixels). Also sets the size to the source size unless {@link #size} is
         * called afterwards.
         */
        public Image source(float x, float y, float width, float height) {
            srcX = x;
            srcY = y;
            srcWidth = width;
            srcHeight = height;
            hasSource = true;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * @see #source(float, float, float, float)
         */
        public Image source(Rectangle source) {
            return source(source.getX(), source.getY(), source.getWidth(), source.getHeight());
        }

        /**
         * Multiply the texture by a color.
         */
        public Image tint(Color color) {
            tintRgba = Colors.pack(color != null ? color : Color.WHITE);
            return this;
        }

        /**
         * Which point of the image sits at (x, y): (0, 0) top-left (default), (0.5, 0.5) center.
         */
        public Image anchor(float anchorX, float anchorY) {
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            return this;
        }

        /**
         * Depth (only meaningful in a sorted layer).
         */
        public Image depth(float depth) {
            depthValue = depth;
            return this;
        }

        @Override
        void commit() {
            Texture t = texture;
            texture = null; // don't keep the texture reachable after the command is committed
            float left = x - width * anchorX, top = y - height * anchorY;
            float right = left + width, bottom = top + height;
            float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;
            if (hasSource) {
                u0 = srcX / t.getWidth();
                v0 = srcY / t.getHeight();
                u1 = (srcX + srcWidth) / t.getWidth();
                v1 = (srcY + srcHeight) / t.getHeight();
            }
            Affine m = transform;
            list.addShape(ShapeType.TEXTURED, t,
                    m.mapX(left, top), m.mapY(left, top), m.mapX(right, top), m.mapY(right, top),
                    m.mapX(right, bottom), m.mapY(right, bottom), m.mapX(left, bottom), m.mapY(left, bottom),
                    u0, v0, u1, v1, 0f, 0f, 0f, 0f, 0f, 0f, tintRgba, 0, clipId, depthValue);
        }
    }

    /**
     * Nine-patch builder (see {@link #ninePatch}).
     */
    public final class NinePatch extends Command {
        private Texture texture;
        private float x, y, width, height;
        private float left, top, right, bottom;
        private int tintRgba;
        private float depthValue;
        private final float[] xs = new float[4], ys = new float[4], us = new float[4], vs = new float[4];

        void reset(Texture texture, float x, float y, float width, float height) {
            this.texture = texture;
            this.x = width < 0 ? x + width : x;
            this.y = height < 0 ? y + height : y;
            this.width = Math.abs(width);
            this.height = Math.abs(height);
            left = top = right = bottom = 0f;
            tintRgba = Colors.pack(1f, 1f, 1f, 1f);
            depthValue = 0f;
        }

        /**
         * Size of the fixed borders, in texture pixels.
         */
        public NinePatch insets(float left, float top, float right, float bottom) {
            this.left = Math.max(0f, left);
            this.top = Math.max(0f, top);
            this.right = Math.max(0f, right);
            this.bottom = Math.max(0f, bottom);
            return this;
        }

        /**
         * Same border size on every side, in texture pixels.
         */
        public NinePatch insets(float all) {
            return insets(all, all, all, all);
        }

        public NinePatch tint(Color color) {
            tintRgba = Colors.pack(color != null ? color : Color.WHITE);
            return this;
        }

        public NinePatch depth(float depth) {
            depthValue = depth;
            return this;
        }

        @Override
        void commit() {
            Texture t = texture;
            texture = null;
            if (width == 0f || height == 0f || (tintRgba >>> 24) == 0) {
                return;
            }
            float tw = t.getWidth(), th = t.getHeight();
            // corners shrink proportionally when the destination is smaller than the borders
            float sx = left + right > width ? width / (left + right) : 1f;
            float sy = top + bottom > height ? height / (top + bottom) : 1f;
            set4(xs, x, x + left * sx, x + width - right * sx, x + width);
            set4(ys, y, y + top * sy, y + height - bottom * sy, y + height);
            set4(us, 0f, left / tw, 1f - right / tw, 1f);
            set4(vs, 0f, top / th, 1f - bottom / th, 1f);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    patch(t, xs[col], ys[row], xs[col + 1], ys[row + 1], us[col], vs[row], us[col + 1], vs[row + 1]);
                }
            }
        }

        private void set4(float[] a, float v0, float v1, float v2, float v3) {
            a[0] = v0;
            a[1] = v1;
            a[2] = v2;
            a[3] = v3;
        }

        private void patch(Texture t, float l, float tp, float r, float b, float u0, float v0, float u1, float v1) {
            if (r - l <= 0f || b - tp <= 0f) {
                return; // empty part (zero inset)
            }
            Affine m = transform;
            list.addShape(ShapeType.TEXTURED, t,
                    m.mapX(l, tp), m.mapY(l, tp), m.mapX(r, tp), m.mapY(r, tp),
                    m.mapX(r, b), m.mapY(r, b), m.mapX(l, b), m.mapY(l, b),
                    u0, v0, u1, v1, 0f, 0f, 0f, 0f, 0f, 0f, tintRgba, 0, clipId, depthValue);
        }
    }
}
