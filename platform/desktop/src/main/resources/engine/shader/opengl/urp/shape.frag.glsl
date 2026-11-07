// URP v2 shape fragment shader (VertexLayout.FULL). No #version: the backend prepends the GL / GLES preamble.
// Shapes are signed distance fields (negative inside) in shape-local units; coverage uses fwidth, so edges stay
// one pixel soft at any scale or rotation. Shape type codes match org.pixel.graphics.draw.ShapeType.

in vec2 vTexCoord;   // texture coordinates (TEXTURED) or shape dimensions (SDF shapes)
in vec4 vColor;      // fill color (or tint)
in vec2 vLocal;      // shape-local position
in vec4 vParams;     // shape parameters
in vec2 vLayerPos;   // position in layer space (for clipping)
flat in int vType;
flat in int vSlot;
flat in int vClip;   // 1-based index into uClips, 0 = none
flat in int vStyle;  // 1-based index into uStyles (2 vec4 each), 0 = none

uniform sampler2D uTextures[/*$numTextures*/];

layout(std140) uniform ClipBlock {
    vec4 uClips[/*$maxClips*/];      // x0, y0, x1, y1
};

layout(std140) uniform StyleBlock {
    vec4 uStyles[/*$maxStyleVec4*/]; // [2i] stroke color, [2i+1] (stroke width, stroke offset, 0, 0)
};

out vec4 oFragColor;

float sdBox(vec2 p, vec2 b) {
    vec2 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0);
}

// radii: x = top-left, y = top-right, z = bottom-right, w = bottom-left (y axis points down)
float sdRoundRect(vec2 p, vec2 halfSize, vec4 radii) {
    float r = p.x < 0.0 ? (p.y < 0.0 ? radii.x : radii.w) : (p.y < 0.0 ? radii.y : radii.z);
    r = min(r, min(halfSize.x, halfSize.y));
    vec2 q = abs(p) - halfSize + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

float sdEllipse(vec2 p, vec2 radii) {
    if (abs(radii.x - radii.y) < 1e-4) {
        return length(p) - radii.x; // circle: exact
    }
    // first-order approximation (exact on the boundary, good near it, which is where coverage is computed)
    float k0 = length(p / radii);
    float k1 = length(p / (radii * radii));
    return k1 < 1e-6 ? -min(radii.x, radii.y) : k0 * (k0 - 1.0) / k1;
}

// segment along local x: dims = (half length, half width); cap: 0 butt, 1 round, 2 square
float sdSegment(vec2 p, vec2 dims, float cap) {
    if (cap > 0.5 && cap < 1.5) {
        return length(vec2(max(abs(p.x) - dims.x, 0.0), p.y)) - dims.y;
    }
    return sdBox(p, vec2(dims.x + (cap > 1.5 ? dims.y : 0.0), dims.y));
}

float coverage(float distance, float pixel) {
    return clamp(0.5 - distance / pixel, 0.0, 1.0);
}

void main() {
    float clipMask = 1.0;
    if (vClip > 0) {
        vec4 c = uClips[vClip - 1];
        if (vLayerPos.x < c.x || vLayerPos.y < c.y || vLayerPos.x >= c.z || vLayerPos.y >= c.w) {
            clipMask = 0.0; // discarded at the end, so derivatives stay valid for the whole 2x2 block
        }
    }

    vec4 color;
    if (vType == 0) {
        color = vColor;
        switch (vSlot) {
/*$textureSwitchCase*/
        }
    } else {
        float d;
        if (vType == 1) {
            d = sdRoundRect(vLocal, vTexCoord, vParams);
        } else if (vType == 2) {
            d = sdEllipse(vLocal, vTexCoord);
        } else {
            d = sdSegment(vLocal, vTexCoord, vParams.x);
        }
        float pixel = max(fwidth(d), 1e-4);

        float fillAlpha = vColor.a * coverage(d, pixel);
        vec4 result = vec4(vColor.rgb * fillAlpha, fillAlpha); // premultiplied
        if (vStyle > 0) {
            vec4 strokeColor = uStyles[(vStyle - 1) * 2];
            vec4 stroke = uStyles[(vStyle - 1) * 2 + 1];
            float strokeAlpha = strokeColor.a * coverage(abs(d - stroke.y) - 0.5 * stroke.x, pixel);
            result = vec4(strokeColor.rgb * strokeAlpha, strokeAlpha) + result * (1.0 - strokeAlpha);
        }
        color = result.a > 0.0 ? vec4(result.rgb / result.a, result.a) : vec4(0.0);
    }

    if (clipMask == 0.0 || color.a <= 0.0) {
        discard;
    }
    oFragColor = color;
}
