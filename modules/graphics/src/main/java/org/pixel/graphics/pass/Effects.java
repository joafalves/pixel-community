package org.pixel.graphics.pass;

import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.RenderTarget;

/**
 * Built-in post effects. Each returns a new effect with setters, so parameters can change every frame:
 * <pre>
 * Effects.Bloom bloom = Effects.bloom(0.7f, 1.2f);
 * Effects.Vignette vignette = Effects.vignette(0.6f);
 * builder.post(scene, bloom, vignette);
 * ...
 * vignette.setStrength(0.8f);
 * </pre>
 * Effects work on premultiplied colors: tints, vignettes and the like change what the source holds. For effects
 * over the whole screen (vignette, sepia), clear the scene target with an opaque color. Light (additive layers,
 * bloom) is stored as color without coverage, so it also shows where the target is transparent.
 */
public final class Effects {

    private Effects() {
    }

    /** Sepia tone, blended with the original by {@code amount} (0 to 1). */
    public static Sepia sepia(float amount) {
        return new Sepia().setAmount(amount);
    }

    /** Multiply by a color (alpha included). */
    public static Tint tint(Color color) {
        return new Tint().setColor(color != null ? color : Color.WHITE);
    }

    /** Square blocks of {@code size} source pixels. */
    public static Pixelate pixelate(float size) {
        return new Pixelate().setSize(size);
    }

    /** Film grain of strength {@code amount} (e.g. 0.08); call {@link Noise#setSeed} every frame to animate it. */
    public static Noise noise(float amount) {
        return new Noise().setAmount(amount);
    }

    /** A frame of {@code width} source pixels around the image. */
    public static Border border(float width, Color color) {
        return new Border().setWidth(width).setColor(color);
    }

    /** Darkened corners: {@code strength} 0 to 1. */
    public static Vignette vignette(float strength) {
        return new Vignette().setStrength(strength);
    }

    /** Glow around bright areas: pixels brighter than {@code threshold} (0 to 1) bleed light, scaled by intensity. */
    public static Bloom bloom(float threshold, float intensity) {
        return new Bloom().setThreshold(threshold).setIntensity(intensity);
    }

    // region effects

    public static final class Sepia extends ShaderEffect {
        Sepia() {
            super("""
                    uniform float uAmount;
                    vec4 effect(vec2 uv) {
                        vec4 c = texture(uSource, uv);
                        vec3 s = vec3(dot(c.rgb, vec3(0.393, 0.769, 0.189)),
                                      dot(c.rgb, vec3(0.349, 0.686, 0.168)),
                                      dot(c.rgb, vec3(0.272, 0.534, 0.131)));
                        return vec4(mix(c.rgb, min(s, vec3(1.0)), uAmount), c.a);
                    }
                    """);
        }

        public Sepia setAmount(float amount) {
            set("uAmount", amount);
            return this;
        }
    }

    public static final class Tint extends ShaderEffect {
        Tint() {
            super("""
                    uniform vec4 uColor;
                    vec4 effect(vec2 uv) {
                        vec4 c = texture(uSource, uv);
                        return vec4(c.rgb * uColor.rgb * uColor.a, c.a * uColor.a);
                    }
                    """);
        }

        public Tint setColor(Color color) {
            set("uColor", color);
            return this;
        }
    }

    public static final class Pixelate extends ShaderEffect {
        Pixelate() {
            super("""
                    uniform float uSize;
                    vec4 effect(vec2 uv) {
                        vec2 block = max(uSize, 1.0) * uTexel;
                        return texture(uSource, (floor(uv / block) + 0.5) * block);
                    }
                    """);
        }

        public Pixelate setSize(float size) {
            set("uSize", size);
            return this;
        }
    }

    public static final class Noise extends ShaderEffect {
        Noise() {
            super("""
                    uniform float uAmount;
                    uniform float uSeed;
                    float hash(vec2 p) {
                        return fract(sin(dot(p, vec2(12.9898, 78.233)) + uSeed * 0.7319) * 43758.5453);
                    }
                    vec4 effect(vec2 uv) {
                        vec4 c = texture(uSource, uv);
                        float n = hash(floor(uv * uResolution)) - 0.5;
                        return vec4(clamp(c.rgb + n * uAmount * c.a, vec3(0.0), vec3(1.0)), c.a);
                    }
                    """);
            setSeed(0f);
        }

        public Noise setAmount(float amount) {
            set("uAmount", amount);
            return this;
        }

        /** A different seed gives a different grain pattern (e.g. the elapsed time, every frame). */
        public Noise setSeed(float seed) {
            set("uSeed", seed);
            return this;
        }
    }

    public static final class Border extends ShaderEffect {
        Border() {
            super("""
                    uniform float uWidth;
                    uniform vec4 uColor;
                    vec4 effect(vec2 uv) {
                        vec4 c = texture(uSource, uv);
                        vec2 p = uv / uTexel, size = 1.0 / uTexel;
                        if (p.x < uWidth || p.y < uWidth || p.x > size.x - uWidth || p.y > size.y - uWidth) {
                            vec4 b = vec4(uColor.rgb * uColor.a, uColor.a);
                            return b + c * (1.0 - b.a);
                        }
                        return c;
                    }
                    """);
        }

        public Border setWidth(float width) {
            set("uWidth", width);
            return this;
        }

        public Border setColor(Color color) {
            set("uColor", color);
            return this;
        }
    }

    public static final class Vignette extends ShaderEffect {
        Vignette() {
            super("""
                    uniform float uStrength;
                    uniform float uRadius;
                    uniform float uSoftness;
                    vec4 effect(vec2 uv) {
                        vec4 c = texture(uSource, uv);
                        float r = length((uv - 0.5) * vec2(uResolution.x / uResolution.y, 1.0));
                        float f = 1.0 - uStrength * smoothstep(uRadius, uRadius + uSoftness, r);
                        return vec4(c.rgb * f, c.a);
                    }
                    """);
            setRadius(0.45f);
            setSoftness(0.5f);
        }

        public Vignette setStrength(float strength) {
            set("uStrength", strength);
            return this;
        }

        /** Distance from the center where darkening starts (0.5 = half the screen height). */
        public Vignette setRadius(float radius) {
            set("uRadius", radius);
            return this;
        }

        public Vignette setSoftness(float softness) {
            set("uSoftness", softness);
            return this;
        }
    }

    /**
     * Bloom in six draws: keep pixels above the threshold (at half resolution), blur them horizontally and vertically
     * (a 9-tap Gaussian, twice: four draws), then add the result to the source (full resolution).
     */
    public static final class Bloom extends PostEffect {
        private final ShaderEffect bright = new ShaderEffect("""
                uniform float uThreshold;
                vec4 effect(vec2 uv) {
                    vec4 c = texture(uSource, uv);
                    float w = smoothstep(uThreshold, uThreshold + 0.2, max(c.r, max(c.g, c.b)));
                    return vec4(c.rgb * w, max(c.r, max(c.g, c.b)) * w);
                }
                """);
        private final ShaderEffect blur = new ShaderEffect("""
                uniform vec2 uDirection; // one texel along the blur axis, times the spread
                vec4 effect(vec2 uv) {
                    vec2 o1 = uDirection * 1.3846153846, o2 = uDirection * 3.2307692308;
                    return texture(uSource, uv) * 0.2270270270
                         + (texture(uSource, uv + o1) + texture(uSource, uv - o1)) * 0.3162162162
                         + (texture(uSource, uv + o2) + texture(uSource, uv - o2)) * 0.0702702703;
                }
                """);
        private final ShaderEffect combine = new ShaderEffect("""
                uniform float uIntensity;
                vec4 effect(vec2 uv) {
                    vec4 c = texture(uSource, uv);
                    return vec4(min(c.rgb + texture(uInput1, uv).rgb * uIntensity, vec3(1.0)), c.a); // added light
                }
                """);
        private float spread = 1.5f;

        Bloom() {
        }

        public Bloom setThreshold(float threshold) {
            bright.set("uThreshold", threshold);
            return this;
        }

        public Bloom setIntensity(float intensity) {
            combine.set("uIntensity", intensity);
            return this;
        }

        /** How far the glow reaches (1 = default, larger = wider). */
        public Bloom setSpread(float spread) {
            this.spread = Math.max(0f, spread) * 1.5f;
            return this;
        }

        @Override
        protected void apply(PostContext context, Texture source, RenderTarget destination, boolean blend) {
            int width = Math.max(1, source.getWidth() / 2), height = Math.max(1, source.getHeight() / 2);
            RenderTarget a = context.temporary(0, width, height), b = context.temporary(1, width, height);
            context.apply(bright, source, a, false);
            for (int pass = 0; pass < 2; pass++) {
                blur.set("uDirection", spread / width, 0f);
                context.apply(blur, a.getTexture(), b, false);
                blur.set("uDirection", 0f, spread / height);
                context.apply(blur, b.getTexture(), a, false);
            }
            combine.input(1, a.getTexture());
            context.apply(combine, source, destination, blend);
        }

        @Override
        public void dispose() {
            bright.dispose();
            blur.dispose();
            combine.dispose();
        }
    }

    // endregion
}
