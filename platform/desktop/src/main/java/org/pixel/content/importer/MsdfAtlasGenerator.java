package org.pixel.content.importer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.msdfgen.MSDFGenBitmap;
import org.lwjgl.util.msdfgen.MSDFGenBounds;
import org.lwjgl.util.msdfgen.MSDFGenTransform;
import org.lwjgl.util.msdfgen.MSDFGenVector2;
import org.pixel.graphics.text.GlyphPacker;
import org.pixel.graphics.text.SdfFontData;
import org.pixel.graphics.text.SdfFontSettings;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.util.msdfgen.MSDFGen.*;
import static org.lwjgl.util.msdfgen.MSDFGenExt.*;

/**
 * Generates MTSDF glyph atlases from TrueType/OpenType data with msdfgen (shapes loaded through FreeType). Vertical
 * metrics and kerning come from stb_truetype, which also reads GPOS pair kerning.
 */
final class MsdfAtlasGenerator {

    private static final int MAX_ATLAS_SIZE = 8192;
    private static final double EDGE_COLORING_ANGLE = 3.0; // radians, msdfgen's default

    private MsdfAtlasGenerator() {
    }

    /**
     * @param fontData The font file (native buffer; must stay alive during the call).
     * @param settings Generation settings.
     * @return The font data with atlas pixels (RGBA8, rows top to bottom).
     */
    static SdfFontData generate(ByteBuffer fontData, SdfFontSettings settings) {
        int size = settings.getGlyphSize();
        float range = settings.getDistanceRange();
        if (size < 8 || range <= 0f) {
            throw new IllegalArgumentException("Glyph size must be at least 8 and the distance range positive.");
        }
        int[] codepoints = settings.getCharacters().codePoints().distinct().sorted().toArray();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTFontinfo info = STBTTFontinfo.malloc(stack);
            if (!stbtt_InitFont(info, fontData)) {
                throw new IllegalArgumentException("Not a TrueType/OpenType font.");
            }
            float emScale = stbtt_ScaleForMappingEmToPixels(info, 1f);
            IntBuffer ascent = stack.mallocInt(1), descent = stack.mallocInt(1), gap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(info, ascent, descent, gap);

            int[] glyphIndices = new int[codepoints.length];
            int present = 0;
            for (int cp : codepoints) {
                int index = stbtt_FindGlyphIndex(info, cp);
                if (index != 0) {
                    codepoints[present] = cp;
                    glyphIndices[present++] = index;
                }
            }
            codepoints = Arrays.copyOf(codepoints, present);
            glyphIndices = Arrays.copyOf(glyphIndices, present);

            PointerBuffer handle = stack.mallocPointer(1);
            check(msdf_ft_init(handle), "msdf_ft_init");
            long ft = handle.get(0);
            long font = 0;
            long[] shapes = new long[present];
            try {
                check(msdf_ft_load_font_data(ft, fontData, handle), "msdf_ft_load_font_data");
                font = handle.get(0);
                return build(stack, info, emScale, ascent.get(0), descent.get(0), gap.get(0), font, codepoints,
                        glyphIndices, shapes, size, range);
            } finally {
                for (long shape : shapes) {
                    if (shape != 0) {
                        msdf_shape_free(shape);
                    }
                }
                if (font != 0) {
                    msdf_ft_font_destroy(font);
                }
                msdf_ft_deinit(ft);
                Reference.reachabilityFence(fontData); // FreeType and stb_truetype point into it until here
            }
        }
    }

    private static SdfFontData build(MemoryStack stack, STBTTFontinfo info, float emScale, int ascent, int descent,
                                     int lineGap, long font, int[] codepoints, int[] glyphIndices, long[] shapes,
                                     int size, float range) {
        int n = codepoints.length;
        float[] advances = new float[n];
        int[] cells = new int[n * 4]; // x0, y0, x1, y1 in atlas pixels relative to the glyph origin (y up)
        int[] packed = new int[n];
        GlyphPacker packer = new GlyphPacker(1);
        double pad = 0.5 * range / size; // em

        DoubleBuffer advance = stack.mallocDouble(1);
        PointerBuffer shapeOut = stack.mallocPointer(1);
        PointerBuffer count = stack.mallocPointer(1);
        MSDFGenBounds bounds = MSDFGenBounds.malloc(stack);
        for (int i = 0; i < n; i++) {
            check(msdf_ft_font_load_glyph(font, codepoints[i], MSDF_FONT_SCALING_EM_NORMALIZED, advance, shapeOut),
                    "msdf_ft_font_load_glyph");
            long shape = shapeOut.get(0);
            shapes[i] = shape;
            advances[i] = (float) advance.get(0);
            packed[i] = -1;
            check(msdf_shape_get_contour_count(shape, count), "msdf_shape_get_contour_count");
            if (count.get(0) == 0) {
                continue; // blank glyph
            }
            check(msdf_shape_normalize(shape), "msdf_shape_normalize");
            check(msdf_shape_get_bounds(shape, bounds), "msdf_shape_get_bounds");
            if (!(bounds.r() > bounds.l()) || !(bounds.t() > bounds.b())) {
                continue;
            }
            orient(stack, shape, bounds, count.get(0));
            check(msdf_shape_edge_colors_simple(shape, EDGE_COLORING_ANGLE), "msdf_shape_edge_colors_simple");
            int x0 = (int) Math.floor((bounds.l() - pad) * size), x1 = (int) Math.ceil((bounds.r() + pad) * size);
            int y0 = (int) Math.floor((bounds.b() - pad) * size), y1 = (int) Math.ceil((bounds.t() + pad) * size);
            cells[i * 4] = x0;
            cells[i * 4 + 1] = y0;
            cells[i * 4 + 2] = x1;
            cells[i * 4 + 3] = y1;
            packed[i] = packer.add(x1 - x0, y1 - y0);
        }
        packer.pack(MAX_ATLAS_SIZE);

        int width = packer.getAtlasWidth(), height = packer.getAtlasHeight();
        ByteBuffer pixels = MemoryUtil.memCalloc(width * height * 4); // 0 = far outside in every channel
        SdfFontData data = new SdfFontData(range, size, width, height)
                .metrics(ascent * emScale, -descent * emScale, (ascent - descent + lineGap) * emScale);
        try {
            // glyphs are independent (shapes are only read, each writes its own atlas cell): render in parallel
            // failures are collected so every task has finished before shapes and pixels are freed
            AtomicReference<RuntimeException> failure = new AtomicReference<>();
            IntStream.range(0, n).parallel().filter(i -> packed[i] >= 0).forEach(i -> {
                if (failure.get() != null) {
                    return;
                }
                try {
                    int x0 = cells[i * 4], y0 = cells[i * 4 + 1];
                    render(shapes[i], x0, y0, cells[i * 4 + 2] - x0, cells[i * 4 + 3] - y0, size, range, pixels,
                            width, packer.x(packed[i]), packer.y(packed[i]));
                } catch (RuntimeException e) {
                    failure.compareAndSet(null, e);
                }
            });
            if (failure.get() != null) {
                throw failure.get();
            }
            for (int i = 0; i < n; i++) {
                if (packed[i] < 0) {
                    data.addGlyph(codepoints[i], advances[i]);
                    continue;
                }
                int x0 = cells[i * 4], y0 = cells[i * 4 + 1], x1 = cells[i * 4 + 2], y1 = cells[i * 4 + 3];
                int ax = packer.x(packed[i]), ay = packer.y(packed[i]);
                data.addGlyph(codepoints[i], advances[i],
                        (float) x0 / size, (float) -y1 / size, (float) x1 / size, (float) -y0 / size,
                        ax, ay, ax + (x1 - x0), ay + (y1 - y0));
            }

            // kerning (pairs among the included characters)
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    int kern = stbtt_GetGlyphKernAdvance(info, glyphIndices[a], glyphIndices[b]);
                    if (kern != 0) {
                        data.addKerning(codepoints[a], codepoints[b], kern * emScale);
                    }
                }
            }

            ByteBuffer heapPixels = ByteBuffer.allocateDirect(pixels.capacity());
            heapPixels.put(pixels).flip();
            return data.pixels(heapPixels);
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    /** Render one glyph's MTSDF and copy it into the atlas at (ax, ay), flipping it to rows top to bottom. */
    private static void render(long shape, int x0, int y0, int w, int h, int size, float range, ByteBuffer atlas,
                               int atlasWidth, int ax, int ay) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MSDFGenBitmap bitmap = MSDFGenBitmap.calloc(stack);
            check(msdf_bitmap_alloc(MSDF_BITMAP_TYPE_MTSDF, w, h, bitmap), "msdf_bitmap_alloc");
            try {
                MSDFGenTransform transform = MSDFGenTransform.calloc(stack);
                transform.scale().set(size, size);
                // pixel = (shape + translation) * scale, so the cell's corner (x0, y0) maps to pixel (0, 0)
                transform.translation().set((double) -x0 / size, (double) -y0 / size);
                double halfRange = 0.5 * range / size;
                transform.distance_mapping().set(-halfRange, halfRange);
                check(msdf_generate_mtsdf(bitmap, shape, transform), "msdf_generate_mtsdf");

                PointerBuffer data = stack.mallocPointer(1);
                check(msdf_bitmap_get_pixels(bitmap, data), "msdf_bitmap_get_pixels");
                FloatBuffer src = MemoryUtil.memFloatBuffer(data.get(0), w * h * 4);
                for (int y = 0; y < h; y++) {
                    int row = ay + (h - 1 - y); // msdfgen rows go bottom to top
                    for (int x = 0; x < w; x++) {
                        int s = (y * w + x) * 4;
                        int d = (row * atlasWidth + ax + x) * 4;
                        for (int c = 0; c < 4; c++) {
                            float v = src.get(s + c);
                            atlas.put(d + c, (byte) Math.round(Math.max(0f, Math.min(1f, v)) * 255f));
                        }
                    }
                }
            } finally {
                msdf_bitmap_free(bitmap);
            }
        }
    }

    /**
     * Fonts don't agree on contour winding (CFF outlines usually run the other way): measure the distance at a point
     * well outside the glyph and, if it reads as inside, reverse every contour (what msdf-atlas-gen does), so no glyph
     * renders inside out.
     */
    private static void orient(MemoryStack stack, long shape, MSDFGenBounds bounds, long contours) {
        MSDFGenVector2 outside = MSDFGenVector2.malloc(stack).set(bounds.l() - (bounds.r() - bounds.l()) - 1,
                bounds.b() - (bounds.t() - bounds.b()) - 1);
        DoubleBuffer distance = stack.mallocDouble(1);
        check(msdf_shape_one_shot_distance(shape, outside, distance), "msdf_shape_one_shot_distance");
        if (distance.get(0) > 0) {
            PointerBuffer contour = stack.mallocPointer(1);
            for (long c = 0; c < contours; c++) {
                check(msdf_shape_get_contour(shape, c, contour), "msdf_shape_get_contour");
                check(msdf_contour_reverse(contour.get(0)), "msdf_contour_reverse");
            }
        }
    }

    private static void check(int result, String function) {
        if (result != MSDF_SUCCESS) {
            throw new IllegalStateException(function + " failed (error " + result + ").");
        }
    }
}
