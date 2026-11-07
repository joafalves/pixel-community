// URP v2 shape fragment shader (VertexLayout.FULL). No #version: the backend prepends the GL / GLES preamble.
// Shapes are signed distance fields (negative inside) in shape-local units; coverage uses fwidth, so edges stay
// one pixel soft at any scale or rotation. Shape type codes match org.pixel.graphics.draw.ShapeType; the style
// record layout matches org.pixel.graphics.draw.StyleTable.

in vec2 vTexCoord;   // texture coordinates (TEXTURED) or shape dimensions (SDF shapes)
in vec4 vColor;      // fill color, gradient start color, or tint
in vec2 vLocal;      // shape-local position
in vec4 vParams;     // shape parameters
in vec2 vLayerPos;   // position in layer space (for clipping)
flat in int vType;
flat in int vSlot;
flat in int vClip;   // 1-based index into uClips, 0 = none
flat in int vStyle;  // 1-based style index (7 vec4 each), 0 = plain fill

uniform sampler2D uTextures[/*$numTextures*/];

layout(std140) uniform ClipBlock {
    vec4 uClips[/*$maxClips*/];      // x0, y0, x1, y1
};

layout(std140) uniform StyleBlock {
    vec4 uStyles[/*$maxStyleVec4*/]; // 7 vec4 per style (see StyleTable)
};

out vec4 oFragColor;

const int TEXTURED = 0;
const int RECT = 1;
const int ELLIPSE = 2;
const int SEGMENT = 3;
const int PATH_FILL = 4;   // local x = coverage, uv = path coordinates
const int PATH_STROKE = 5; // local y = distance across the line, uv.x = half width

// ---------------------------------------------------------------------------------------------------------------
// distance functions

float sdBox(vec2 p, vec2 b) {
    vec2 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0);
}

// radius of the quadrant of p: x = top-left, y = top-right, z = bottom-right, w = bottom-left (y axis points down)
float cornerRadius(vec2 p, vec2 halfSize, vec4 radii) {
    float r = p.x < 0.0 ? (p.y < 0.0 ? radii.x : radii.w) : (p.y < 0.0 ? radii.y : radii.z);
    return min(r, min(halfSize.x, halfSize.y));
}

float sdRoundRect(vec2 p, vec2 halfSize, vec4 radii) {
    float r = cornerRadius(p, halfSize, radii);
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

float shapeDistance(vec2 p) {
    if (vType == RECT) {
        return sdRoundRect(p, vTexCoord, vParams);
    } else if (vType == ELLIPSE) {
        return sdEllipse(p, vTexCoord);
    }
    return sdSegment(p, vTexCoord, vParams.x);
}

float coverage(float distance, float pixel) {
    return clamp(0.5 - distance / pixel, 0.0, 1.0);
}

// ---------------------------------------------------------------------------------------------------------------
// soft shadows

vec2 erf2(vec2 x) { // error function approximation (Abramowitz & Stegun 7.1.27)
    vec2 s = sign(x), a = abs(x);
    x = 1.0 + (0.278393 + (0.230389 + 0.078108 * (a * a)) * a) * a;
    x *= x;
    return s - s / (x * x);
}

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (2.50662827463 * sigma);
}

// Gaussian-blurred rounded box (closed form along x, 4 samples along y). After Evan Wallace,
// "Fast Rounded Rectangle Shadows" (2016).
float roundedBoxShadowX(float x, float y, float sigma, float corner, vec2 halfSize) {
    float delta = min(halfSize.y - corner - abs(y), 0.0);
    float curved = halfSize.x - corner + sqrt(max(0.0, corner * corner - delta * delta));
    vec2 integral = 0.5 + 0.5 * erf2((x + vec2(-curved, curved)) * (0.70710678 / sigma));
    return integral.y - integral.x;
}

float roundedBoxShadow(vec2 p, vec2 halfSize, float sigma, float corner) {
    float low = p.y - halfSize.y;
    float high = p.y + halfSize.y;
    float start = clamp(-3.0 * sigma, low, high);
    float end = clamp(3.0 * sigma, low, high);
    float step = (end - start) / 4.0;
    float y = start + step * 0.5;
    float value = 0.0;
    for (int i = 0; i < 4; i++) {
        value += roundedBoxShadowX(p.x, p.y - y, sigma, corner, halfSize) * gaussian(y, sigma) * step;
        y += step;
    }
    return value;
}

float shadowAlpha(vec2 p, float blur, float spread, float pixel) {
    float sigma = blur * 0.5;
    if (sigma < 0.25) {
        return coverage(shapeDistance(p) - spread, pixel); // hard shadow
    }
    if (vType == RECT) {
        vec2 halfSize = max(vTexCoord + spread, vec2(0.0));
        float corner = min(max(cornerRadius(p, vTexCoord, vParams) + spread, 0.0), min(halfSize.x, halfSize.y));
        return roundedBoxShadow(p, halfSize, sigma, corner);
    }
    // other shapes: Gaussian across the edge distance (exact for straight edges, close for convex curves)
    float d = shapeDistance(p) - spread;
    return 0.5 - 0.5 * erf2(vec2(d * 0.70710678 / sigma, 0.0)).x;
}

// ---------------------------------------------------------------------------------------------------------------
// dashes

// 1 inside a dash, 0 in a gap, anti-aliased over one pixel; s is the arc length along the outline
float dashMask(float s, float dashLength, float gap, float pixel) {
    float period = dashLength + gap;
    float x = mod(s, period);
    float d = x <= dashLength ? min(x, dashLength - x) : -min(x - dashLength, period - x);
    return clamp(d / pixel + 0.5, 0.0, 1.0);
}

// ---------------------------------------------------------------------------------------------------------------

void main() {
    float clipMask = 1.0;
    if (vClip > 0) {
        vec4 c = uClips[vClip - 1];
        if (vLayerPos.x < c.x || vLayerPos.y < c.y || vLayerPos.x >= c.z || vLayerPos.y >= c.w) {
            clipMask = 0.0; // discarded at the end, so derivatives stay valid for the whole 2x2 block
        }
    }

    vec4 color;
    if (vType == TEXTURED) {
        color = vColor;
        switch (vSlot) {
/*$textureSwitchCase*/
        }
    } else {
        float d;
        float pixel;
        float fillCoverage;
        if (vType == PATH_FILL) {
            d = 0.0;
            pixel = 1.0;
            fillCoverage = clamp(vLocal.x, 0.0, 1.0); // interpolated across the anti-aliasing fringe
        } else if (vType == PATH_STROKE) {
            d = abs(vLocal.y) - vTexCoord.x;
            pixel = max(fwidth(d), 1e-4);
            fillCoverage = coverage(d, pixel);
        } else {
            d = shapeDistance(vLocal);
            pixel = max(fwidth(d), 1e-4);
            fillCoverage = coverage(d, pixel);
        }
        vec4 fill = vColor;

        vec4 strokeColor = vec4(0.0);
        float strokeCoverage = 0.0;
        vec4 result;
        if (vStyle > 0) {
            int base = (vStyle - 1) * 7;
            vec4 stroke = uStyles[base + 1];       // width, offset, dash length, dash gap
            vec4 gradient = uStyles[base + 2];     // linear (x0, y0, x1, y1) / radial (cx, cy, r, -)
            vec4 shadowColor = uStyles[base + 4];
            vec4 shadow = uStyles[base + 5];       // offset x, offset y, blur, spread
            vec4 flags = uStyles[base + 6];        // paint type, dash phase

            // paint
            if (flags.x > 0.5) {
                vec2 gp = vType == PATH_FILL ? vTexCoord : vLocal; // paths: gradients in path coordinates
                float t;
                if (flags.x < 1.5) {
                    vec2 axis = gradient.zw - gradient.xy;
                    t = dot(gp - gradient.xy, axis) / max(dot(axis, axis), 1e-6);
                } else {
                    t = length(gp - gradient.xy) / gradient.z;
                }
                fill = mix(vColor, uStyles[base + 3], clamp(t, 0.0, 1.0));
            }

            // stroke
            strokeColor = uStyles[base];
            if (strokeColor.a > 0.0) {
                strokeCoverage = coverage(abs(d - stroke.y) - 0.5 * stroke.x, pixel);
            }

            // dashes: the line body for segments, the stroke for ellipses
            if (stroke.z > 0.0) {
                if (vType == SEGMENT) {
                    fillCoverage *= dashMask(vLocal.x + vTexCoord.x + flags.y, stroke.z, stroke.w, pixel);
                } else if (vType == ELLIPSE) {
                    float meanRadius = 0.5 * (vTexCoord.x + vTexCoord.y);
                    float s = (atan(vLocal.y, vLocal.x) + 3.14159265) * meanRadius + flags.y;
                    // stretch the pattern so a whole number of periods fits the outline: no seam where it wraps
                    float circumference = 6.28318531 * meanRadius;
                    float period = stroke.z + stroke.w;
                    float fit = circumference / (max(1.0, floor(circumference / period + 0.5)) * period);
                    strokeCoverage *= dashMask(s, stroke.z * fit, stroke.w * fit, pixel);
                }
            }

            // shadow below everything
            result = vec4(0.0);
            if (shadowColor.a > 0.0) {
                float sa = shadowColor.a * shadowAlpha(vLocal - shadow.xy, shadow.z, shadow.w, pixel);
                result = vec4(shadowColor.rgb * sa, sa);
            }
        } else {
            result = vec4(0.0);
        }

        // premultiplied: shadow, then fill over it, then stroke over both
        float fa = fill.a * fillCoverage;
        result = vec4(fill.rgb * fa, fa) + result * (1.0 - fa);
        float sa = strokeColor.a * strokeCoverage;
        result = vec4(strokeColor.rgb * sa, sa) + result * (1.0 - sa);
        color = result.a > 0.0 ? vec4(result.rgb / result.a, result.a) : vec4(0.0);
    }

    if (clipMask == 0.0 || color.a <= 0.0) {
        discard;
    }
    oFragColor = color;
}
