package org.pixel.graphics.canvas;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.graphics.draw.Colors;
import org.pixel.graphics.draw.LayerMode;
import org.pixel.graphics.draw.ShapeType;
import org.pixel.graphics.testing.RecordingBackend;
import org.pixel.graphics.testing.RecordingBackend.Batch;
import org.pixel.graphics.testing.RecordingBackend.FullVertex;
import org.pixel.graphics.testing.TestTexture;
import org.pixel.math.Matrix4;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class CanvasTest {

    private static final float EPS = 1e-4f;
    private static final float AA = 2f; // Canvas.AA_MARGIN at scale 1

    private final RecordingBackend backend = new RecordingBackend(16, 16384, true);

    private RecordingBackend.Pass draw(Consumer<Canvas> body) {
        return draw(LayerMode.ORDERED, body);
    }

    private RecordingBackend.Pass draw(LayerMode mode, Consumer<Canvas> body) {
        CanvasBatch batch = new CanvasBatch(backend, mode);
        Canvas canvas = batch.begin(new Matrix4());
        body.accept(canvas);
        batch.end();
        return backend.lastPass();
    }

    private static void assertCorner(FullVertex v, float x, float y) {
        assertEquals(x, v.x(), EPS, "x");
        assertEquals(y, v.y(), EPS, "y");
    }

    @Test
    void rectanglesCarryHalfSizeRadiiAndAnAntialiasingMargin() {
        List<FullVertex> v = draw(c -> c.rect(10, 20, 100, 50).radius(4, 5, 6, 7).fill(Color.RED)).shapes();

        assertEquals(4, v.size());
        assertCorner(v.get(0), 10 - AA, 20 - AA);
        assertCorner(v.get(2), 110 + AA, 70 + AA);
        for (FullVertex vertex : v) {
            assertEquals(ShapeType.RECT.code(), vertex.type());
            assertEquals(50f, vertex.u());
            assertEquals(25f, vertex.v());
            assertEquals(4f, vertex.p0());
            assertEquals(7f, vertex.p3());
            assertEquals(Colors.pack(Color.RED), vertex.rgba());
            assertEquals(0, vertex.style());
            assertEquals(0, vertex.clip());
        }
        assertEquals(-50 - AA, v.get(0).localX(), EPS);
        assertEquals(25 + AA, v.get(2).localY(), EPS);
    }

    @Test
    void strokeExtendsTheQuadAndGoesIntoTheStyleTable() {
        RecordingBackend.Pass pass = draw(c -> {
            c.rect(0, 0, 10, 10).stroke(4, Color.GREEN).strokeAlign(StrokeAlign.OUTSIDE);
            c.rect(0, 0, 10, 10).stroke(4, Color.GREEN).strokeAlign(StrokeAlign.OUTSIDE).noFill(); // same style
            c.circle(0, 0, 5).stroke(2, Color.BLUE).strokeAlign(StrokeAlign.INSIDE);
        });

        assertCorner(pass.shapes().get(0), -4 - AA, -4 - AA);
        assertEquals(1, pass.shapes().get(0).style());
        assertEquals(1, pass.shapes().get(4).style());
        assertEquals(0, pass.shapes().get(4).rgba() >>> 24); // no fill
        assertEquals(2, pass.shapes().get(8).style());
        assertCorner(pass.shapes().get(8), -5 - AA, -5 - AA); // inside stroke doesn't grow the quad

        Batch batch = pass.batches().get(0);
        assertEquals(2, batch.styles().size());
        assertArrayEquals(new float[]{0, 1, 0, 1, 4, 2, 0, 0}, batch.styles().get(0), EPS);
        assertArrayEquals(new float[]{0, 0, 1, 1, 2, -1, 0, 0}, batch.styles().get(1), EPS);
    }

    @Test
    void invisibleShapesAreSkipped() {
        RecordingBackend.Pass pass = draw(c -> {
            c.rect(0, 0, 10, 10).noFill();
            c.rect(0, 0, 0, 10);
            c.circle(0, 0, 3).fill(Color.WHITE);
        });
        assertEquals(4, pass.shapes().size()); // only the circle
        assertEquals(ShapeType.ELLIPSE.code(), pass.shapes().get(0).type());
    }

    @Test
    void transformsApplyToCornersAndCommandsCommitBeforeStateChanges() {
        RecordingBackend.Pass pass = draw(c -> {
            c.rect(0, 0, 10, 10);   // committed by translate(), with the identity transform
            c.translate(100, 0);
            c.rotate((float) (Math.PI / 2)); // 90 degrees clockwise on screen: (x, y) -> (-y, x)
            c.rect(0, 0, 10, 10);
        });

        List<FullVertex> v = pass.shapes();
        assertCorner(v.get(0), -AA, -AA);
        // second rect: user-space top-left corner (-2, -2) maps to (100 + 2, -2)
        assertCorner(v.get(4), 102, -2);
        assertCorner(v.get(6), 100 - 12, 12); // user (12, 12) -> (-12 + 100, 12)
        assertEquals(-5 - AA, v.get(4).localX(), EPS); // local coordinates are untouched by the transform
    }

    @Test
    void scaleShrinksTheAntialiasingMarginInLocalUnits() {
        List<FullVertex> v = draw(c -> {
            c.scale(4);
            c.rect(0, 0, 10, 10);
        }).shapes();
        assertEquals(-5 - AA / 4, v.get(0).localX(), EPS); // margin stays ~2 pixels after scaling
        assertCorner(v.get(0), -AA, -AA);
    }

    @Test
    void clipsAreTransformedIntersectedAndRestored() {
        RecordingBackend.Pass pass = draw(c -> {
            c.save();
            c.translate(10, 10);
            c.clip(0, 0, 100, 100);    // (10, 10)-(110, 110)
            c.clip(50, 50, 200, 200);  // intersect: (60, 60)-(110, 110)
            c.rect(0, 0, 5, 5);
            c.restore();
            c.rect(0, 0, 5, 5);        // no clip after restore
            c.clip(0, 0, 10, 10, () -> c.circle(0, 0, 1));
            c.circle(0, 0, 1);         // body restored the state
        });

        List<FullVertex> v = pass.shapes();
        assertEquals(1, v.get(0).clip());
        assertEquals(0, v.get(4).clip());
        assertEquals(2, v.get(8).clip());
        assertEquals(0, v.get(12).clip());
        List<float[]> clips = pass.batches().get(0).clips();
        assertArrayEquals(new float[]{60, 60, 110, 110}, clips.get(0), EPS);
        assertArrayEquals(new float[]{0, 0, 10, 10}, clips.get(1), EPS);
    }

    @Test
    void rotatedClipUsesTheBoundingBox() {
        RecordingBackend.Pass pass = draw(c -> {
            c.rotate((float) (Math.PI / 4));
            c.clip(0, 0, 10, 10);
            c.rect(0, 0, 1, 1);
        });
        float half = (float) (10 / Math.sqrt(2));
        assertArrayEquals(new float[]{-half, 0, half, 2 * half}, pass.batches().get(0).clips().get(0), 1e-3f);
    }

    @Test
    void linesAreSegmentsAlongTheirDirection() {
        List<FullVertex> v = draw(c -> c.line(0, 0, 0, 10).width(4).cap(LineCap.ROUND).color(Color.BLUE)).shapes();
        // vertical line: local x runs along +y; center (0, 5); extents (5 + 2 + AA, 2 + AA)
        for (FullVertex vertex : v) {
            assertEquals(ShapeType.SEGMENT.code(), vertex.type());
            assertEquals(5f, vertex.u());
            assertEquals(2f, vertex.v());
            assertEquals(LineCap.ROUND.code(), vertex.p0(), EPS);
        }
        assertEquals(-(5 + 2 + AA), v.get(0).localX(), EPS);
        assertCorner(v.get(0), 2 + AA, 5 - (5 + 2 + AA)); // local (-ex, -ey) -> (+ey, center - ex)
    }

    @Test
    void imagesUseSourceSizeAnchorAndTint() {
        Texture texture = new TestTexture(64, 32);
        RecordingBackend.Pass pass = draw(c -> c.image(texture, 100, 100).source(16, 8, 32, 16).size(64, 32)
                .anchor(0.5f, 0.5f).tint(Color.RED));

        List<FullVertex> v = pass.shapes();
        assertCorner(v.get(0), 68, 84);
        assertCorner(v.get(2), 132, 116);
        assertEquals(0.25f, v.get(0).u(), EPS);
        assertEquals(0.25f, v.get(0).v(), EPS);
        assertEquals(0.75f, v.get(2).u(), EPS);
        assertEquals(0.75f, v.get(2).v(), EPS);
        assertEquals(ShapeType.TEXTURED.code(), v.get(0).type());
        assertEquals(Colors.pack(Color.RED), v.get(0).rgba());
        assertSame(texture, pass.batches().get(0).textures().get(0));
    }

    @Test
    void sortedCanvasOrdersByDepth() {
        List<FullVertex> v = draw(LayerMode.SORTED, c -> {
            c.circle(1, 0, 1).depth(5);
            c.circle(2, 0, 1).depth(-1);
            c.circle(3, 0, 1).depth(5);
        }).shapes();
        assertEquals(2 - 1 - AA, v.get(0).x(), EPS);
        assertEquals(1 - 1 - AA, v.get(4).x(), EPS);
        assertEquals(3 - 1 - AA, v.get(8).x(), EPS);
    }

    @Test
    void restoreWithoutSaveFails() {
        assertThrows(IllegalStateException.class, () -> draw(Canvas::restore));
    }

    @Test
    void steadyStateCanvasFramesDoNotAllocate() {
        RecordingBackend counting = new RecordingBackend(16, 16384, false);
        CanvasBatch batch = new CanvasBatch(counting);
        Texture texture = new TestTexture(32, 32);
        Matrix4 view = new Matrix4();
        Color fill = new Color(0.2f, 0.4f, 0.8f, 1f);

        Runnable frame = () -> {
            Canvas c = batch.begin(view);
            for (int i = 0; i < 2000; i++) {
                c.save();
                c.translate(i % 100, i / 100f);
                c.rotate(i * 0.01f);
                if (i % 10 == 0) {
                    c.clip(0, 0, 50, 50);
                }
                c.rect(0, 0, 20, 10).radius(3).fill(fill).stroke(1 + i % 3, Color.WHITE);
                c.circle(5, 5, 4).fill(Color.RED);
                c.line(0, 0, 10, 10).width(2).cap(LineCap.ROUND);
                c.image(texture, 0, 0).anchor(0.5f, 0.5f);
                c.restore();
            }
            batch.end();
        };
        for (int i = 0; i < 30; i++) {
            frame.run();
        }

        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < 20; i++) {
            frame.run();
        }
        long allocated = threads.getThreadAllocatedBytes(thread) - before;

        assertEquals(0, allocated, "bytes allocated by 20 frames of 8000 canvas commands");
    }
}
