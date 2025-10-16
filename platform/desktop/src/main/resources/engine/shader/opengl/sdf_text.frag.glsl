#version 330 core

// Inputs from vertex shader
in vec2 vTexCoord;

// Uniforms
uniform sampler2D uAtlas;        // SDF texture atlas
uniform vec4 uFillColor;         // Text fill color
uniform vec4 uStrokeColor;       // Stroke/outline color (alpha = 0 means no stroke)
uniform float uStrokeWidth;      // Stroke width (0.0 - 0.5)
uniform float uSmoothness;       // Edge smoothness (lower = sharper)
uniform float uTextEdge;         // SDF edge threshold for text rendering

// Output
out vec4 fragColor;

void main() {
    float dist = texture(uAtlas, vTexCoord).a;
    float onedge = uTextEdge; // Use configurable edge threshold from uniform

    float fillAlpha = smoothstep(onedge - uSmoothness, onedge + uSmoothness, dist);
    float combinedAlpha = fillAlpha;
    vec3 combinedColor = uFillColor.rgb;

    if (uStrokeWidth > 0.0) {
        float strokeEdge = onedge - uStrokeWidth;
        float strokeAlpha = smoothstep(strokeEdge - uSmoothness, strokeEdge + uSmoothness, dist);
        combinedAlpha = max(fillAlpha, strokeAlpha);
        combinedColor = mix(uStrokeColor.rgb, uFillColor.rgb, fillAlpha);
    }

    fragColor = vec4(combinedColor, combinedAlpha * uFillColor.a);

    if (fragColor.a < 0.01) {
        discard;
    }
}
