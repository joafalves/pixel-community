package org.pixel.graphics.material;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.MaterialProgram;
import org.pixel.graphics.backend.RenderBackend;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A custom shader for canvas commands: {@code canvas.image(...).material(dissolve)}.
 * <p>
 * <b>Effect materials</b> ({@link #effect}) are one GLSL function that post-processes the color the built-in shader
 * computed for a fragment, so clipping, texture slots, anti-aliasing, strokes and text keep working:
 * <pre>
 * uniform sampler2D uNoise;   // a material texture
 * vec4 effect(vec4 color, vec2 uv, Fragment f) {
 *     if (texture(uNoise, uv).r &lt; f.data.x) discard;   // f.data: per-command values
 *     return color;
 * }
 * </pre>
 * {@code color} is premultiplied and the result must be too. {@code uv} are the command's texture coordinates
 * (images, glyphs; shape dimensions for shapes). {@code Fragment} has {@code position} (layer space), {@code local}
 * (shape-local coordinates: from the center of shapes, images and nine-patches, in drawing units; text units for
 * text; not meaningful for paths, use {@code position}), {@code data} (the command's material data, see below) and
 * {@code type} (the shape type); {@code sampleImage(vec2 uv)} samples the command's own texture (premultiplied), for
 * distortions and outlines. Effects run on every fragment of the command's quad, including its transparent margin:
 * scale what they add by {@code color.a} to stay inside. The function shares the built-in shader's namespace: don't
 * redeclare its inputs ({@code v*}), uniforms ({@code uTextures}, {@code uClips}, {@code uStyles},
 * {@code uPremultiplied}) or helpers ({@code coverage}, {@code median}, {@code premultiply}, {@code sampleSlot}, ...,
 * see {@code shape.frag.glsl}).
 * <p>
 * <b>Raw materials</b> ({@link #raw}) are a complete vertex and fragment shader for the shape vertex layout (see
 * {@code VertexLayout.FULL}: attributes 0-8 as documented there, {@code uMatrix}); the fragment shader gets
 * {@code uTextures[PIXEL_TEXTURE_SLOTS]}, {@code uPremultiplied} and {@code vec4 pixelSample(int slot, vec2 uv)}
 * (premultiplied), and must write premultiplied colors. Use them for what an effect can't do, such as moving vertices.
 * They replace the built-in shader entirely: clipping, strokes, shadows, gradients and anti-aliased shape edges are
 * whatever the raw shader implements (the clip and style tables are bound if it declares {@code ClipBlock} /
 * {@code StyleBlock} as the built-in shader does).
 * <p>
 * <b>Values.</b> Uniforms ({@link #set}) and textures ({@link #texture}) belong to the material: they are read when
 * the frame is rendered, so every command drawn with the material in a frame sees the same values. Values that differ
 * per command go in the command's <b>material data</b>, a vec4 ({@code .material(m, x, y, z, w)} on canvas commands,
 * read as {@code f.data}): commands with the same material and different data still share a batch. A shader must not
 * sample a texture uniform whose texture was removed: what it reads is unspecified.
 * <p>
 * <b>Batching.</b> Consecutive commands with the same material share draw calls; a different material starts a new
 * batch. Sorted layers can group equal-depth commands by material ({@code Layer.batchByMaterial()}). Blending stays a
 * property of the layer.
 * <p>
 * <b>Hot reload.</b> {@link #setSource} replaces the shader; it is compiled on next use. If it fails to compile, the
 * previous program keeps running and {@link #getError()} says why (see {@link SourceWatcher} to reload from a file);
 * if the GPU context is lost meanwhile, the last source that compiled is rebuilt.
 * <p>
 * Created by the game and disposed by it. Use from the render thread, with one render backend (a material keeps one
 * compiled program: drawing it through another backend compiles it again for that one).
 */
public final class Material implements Disposable {

    private static final Logger log = LoggerFactory.getLogger(Material.class);
    private static final AtomicInteger SERIAL = new AtomicInteger();

    /**
     * Textures a material can have ({@link #texture}).
     */
    public static final int MAX_TEXTURES = BackendCapabilities.MATERIAL_TEXTURES;

    /**
     * What a material's source is.
     */
    public enum Kind {
        /** A function {@code vec4 effect(vec4 color, vec2 uv, Fragment f)} injected into the built-in shader. */
        EFFECT,
        /** A complete vertex and fragment shader. */
        RAW
    }

    private final Kind kind;
    private final int id;
    private String source;        // effect function, or the raw fragment shader
    private String vertexSource;  // raw only
    private int version;

    private String[] names = new String[4];
    private float[] values = new float[16];
    private int[] sizes = new int[4];
    private int uniformCount;
    private final String[] textureNames = new String[MAX_TEXTURES];
    private final Texture[] textures = new Texture[MAX_TEXTURES];
    private int textureCount;
    private int valuesSerial;

    private MaterialProgram program;
    private RenderBackend compiledFor;
    private int compiledGeneration;
    private int compiledVersion;
    private int failedVersion = -1;
    private String goodSource, goodVertexSource; // the last sources that compiled
    private int goodVersion;
    private String error;
    private boolean disposed;

    private Material(Kind kind, String vertexSource, String source) {
        this.kind = kind;
        this.vertexSource = vertexSource;
        this.source = source;
        this.id = SERIAL.getAndIncrement() % 0xFFFF + 1; // 1..65535: 0 means "no material" in sort keys
    }

    /**
     * An effect material.
     *
     * @param source GLSL defining {@code vec4 effect(vec4 color, vec2 uv, Fragment f)} (and its own uniforms).
     */
    public static Material effect(String source) {
        return new Material(Kind.EFFECT, null, checkEffect(source));
    }

    /**
     * A raw material.
     *
     * @param vertexSource   The vertex shader (no {@code #version}).
     * @param fragmentSource The fragment shader (no {@code #version}).
     */
    public static Material raw(String vertexSource, String fragmentSource) {
        checkRaw(vertexSource, fragmentSource);
        return new Material(Kind.RAW, vertexSource, fragmentSource);
    }

    // region values

    public Material set(String uniform, float x) {
        return put(uniform, 1, x, 0f, 0f, 0f);
    }

    public Material set(String uniform, float x, float y) {
        return put(uniform, 2, x, y, 0f, 0f);
    }

    public Material set(String uniform, float x, float y, float z, float w) {
        return put(uniform, 4, x, y, z, w);
    }

    /**
     * Set a vec4 uniform to a color (straight alpha).
     */
    public Material set(String uniform, Color color) {
        return set(uniform, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Bind a texture to a {@code sampler2D} uniform of the shader (at most {@link #MAX_TEXTURES}). Sampled as
     * stored ({@code texture(uNoise, uv)}): image textures hold straight alpha.
     *
     * @param uniform The sampler's name.
     * @param texture The texture ({@code null} removes it).
     */
    public Material texture(String uniform, Texture texture) {
        checkName(uniform);
        int i = 0;
        while (i < textureCount && !textureNames[i].equals(uniform)) {
            i++;
        }
        if (texture == null) {
            if (i < textureCount) {
                System.arraycopy(textureNames, i + 1, textureNames, i, textureCount - i - 1);
                System.arraycopy(textures, i + 1, textures, i, textureCount - i - 1);
                textureCount--;
                textureNames[textureCount] = null;
                textures[textureCount] = null;
                valuesSerial++;
            }
            return this;
        }
        if (i == textureCount) {
            if (textureCount == MAX_TEXTURES) {
                throw new IllegalStateException("A material has at most " + MAX_TEXTURES + " textures.");
            }
            textureNames[textureCount++] = uniform;
        }
        textures[i] = texture;
        valuesSerial++;
        return this;
    }

    private Material put(String name, int size, float x, float y, float z, float w) {
        checkName(name);
        int i = 0;
        while (i < uniformCount && !names[i].equals(name)) {
            i++;
        }
        if (i == uniformCount) {
            if (uniformCount == names.length) {
                names = Arrays.copyOf(names, uniformCount * 2);
                sizes = Arrays.copyOf(sizes, uniformCount * 2);
                values = Arrays.copyOf(values, uniformCount * 2 * 4);
            }
            names[uniformCount++] = name;
        }
        sizes[i] = size;
        values[i * 4] = x;
        values[i * 4 + 1] = y;
        values[i * 4 + 2] = z;
        values[i * 4 + 3] = w;
        valuesSerial++;
        return this;
    }

    private static void checkName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Uniform name cannot be empty.");
        }
    }

    // endregion

    // region source and hot reload

    /**
     * Replace the effect function (an effect material); compiled on next use.
     */
    public void setSource(String source) {
        if (kind != Kind.EFFECT) {
            throw new IllegalStateException("A raw material needs both shaders: use setSource(vertex, fragment).");
        }
        this.source = checkEffect(source);
        version++;
    }

    /**
     * Replace the shaders (a raw material); compiled on next use.
     */
    public void setSource(String vertexSource, String fragmentSource) {
        if (kind != Kind.RAW) {
            throw new IllegalStateException("An effect material has one function: use setSource(source).");
        }
        checkRaw(vertexSource, fragmentSource);
        this.vertexSource = vertexSource;
        this.source = fragmentSource;
        version++;
    }

    /**
     * @return Why the last source failed to compile (the previous program is still in use), or {@code null}.
     */
    public String getError() {
        return error;
    }

    private static String checkEffect(String source) {
        if (source == null || !source.contains("effect")) {
            throw new IllegalArgumentException(
                    "An effect material must define vec4 effect(vec4 color, vec2 uv, Fragment f).");
        }
        return source;
    }

    private static void checkRaw(String vertexSource, String fragmentSource) {
        if (vertexSource == null || fragmentSource == null || !vertexSource.contains("main")
                || !fragmentSource.contains("main")) {
            throw new IllegalArgumentException("A raw material needs a vertex and a fragment shader with main().");
        }
    }

    // endregion

    // region backend access

    /**
     * For backends: the program for this backend, compiled on first use and again after a source change or a lost
     * context. A source that fails to compile keeps the previous program (if it belongs to the current context) and
     * records the error; otherwise the failure is thrown.
     */
    public MaterialProgram program(RenderBackend backend) {
        if (disposed) {
            throw new IllegalStateException("Material has been disposed.");
        }
        int generation = backend.getContextGeneration();
        boolean live = program != null && compiledFor == backend && compiledGeneration == generation;
        if (live && (compiledVersion == version || failedVersion == version)) {
            return program;
        }
        MaterialProgram fresh;
        int freshVersion = version;
        try {
            fresh = backend.compileMaterial(this, vertexSource, source);
            goodSource = source;
            goodVertexSource = vertexSource;
            goodVersion = version;
            error = null;
        } catch (RuntimeException e) {
            failedVersion = version;
            error = e.getMessage();
            if (live) {
                log.error("Material source failed to compile; keeping the previous version.\n{0}", error);
                return program;
            }
            if (goodSource == null || (goodSource.equals(source) && java.util.Objects.equals(goodVertexSource,
                    vertexSource))) {
                throw e; // nothing that compiled to fall back to
            }
            // a new context while the latest edit is broken: rebuild the last version that compiled
            log.error("Material source failed to compile; using the last version that compiled.\n{0}", error);
            fresh = backend.compileMaterial(this, goodVertexSource, goodSource);
            freshVersion = goodVersion;
        }
        if (live) {
            program.dispose();
        } // otherwise it belongs to another (or a lost) context: never deleted from this one
        program = fresh;
        compiledFor = backend;
        compiledGeneration = generation;
        compiledVersion = freshVersion;
        return program;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return The effect function (effect materials) or the fragment shader (raw materials).
     */
    public String getSource() {
        return source;
    }

    /**
     * @return The vertex shader (raw materials), or {@code null}.
     */
    public String getVertexSource() {
        return vertexSource;
    }

    /**
     * @return A small number identifying this material (1 to 65535; may repeat after 65535 materials), used to group
     * commands by material.
     */
    public int getId() {
        return id;
    }

    /**
     * @return Changes whenever a uniform or texture changes (so backends upload values only when needed).
     */
    public int getValuesSerial() {
        return valuesSerial;
    }

    public int getUniformCount() {
        return uniformCount;
    }

    public String getUniformName(int index) {
        return names[index];
    }

    /** Components of a uniform (1, 2 or 4). */
    public int getUniformSize(int index) {
        return sizes[index];
    }

    public float getUniformValue(int index, int component) {
        return values[index * 4 + component];
    }

    public int getTextureCount() {
        return textureCount;
    }

    public String getTextureName(int index) {
        return textureNames[index];
    }

    public Texture getTexture(int index) {
        return textures[index];
    }

    // endregion

    /**
     * Release the compiled program. The material can't be drawn afterwards.
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (program != null && compiledGeneration == compiledFor.getContextGeneration()) {
            program.dispose();
        }
        program = null;
        compiledFor = null;
        Arrays.fill(textures, null);
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public String toString() {
        return "Material[" + kind + " #" + id + "]";
    }
}
