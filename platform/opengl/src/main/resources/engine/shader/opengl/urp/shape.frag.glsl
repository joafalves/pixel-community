// URP v2 shape fragment shader (VertexLayout.FULL). No #version: the backend prepends the GL / GLES preamble.
// Output colors are premultiplied (rgb * a); the backend blends with (ONE, ONE_MINUS_SRC_ALPHA).
// Shapes are signed distance fields (negative inside) in shape-local units; coverage uses fwidth, so edges stay
// one pixel soft at any scale or rotation. Shape type codes match org.pixel.graphics.draw.ShapeType; the style
// record layout matches org.pixel.graphics.draw.StyleTable.
// Effect materials are compiled from this file with PIXEL_MATERIAL defined and their effect() function inserted.
/*$defines*/

in vec2 vTexCoord;   // texture coordinates (TEXTURED, GLYPH) or shape dimensions (SDF shapes)
in vec4 vColor;      // fill color, gradient start color, or tint
in vec2 vLocal;      // shape-local position
in vec4 vParams;     // shape parameters
in vec2 vLayerPos;   // position in layer space (for clipping)
flat in int vType;
flat in int vSlot;
flat in int vClip;   // 1-based index into uClips, 0 = none
flat in int vStyle;  // 1-based style index (8 vec4 each), 0 = plain fill

uniform sampler2D uTextures[/*$numTextures*/];

layout(std140) uniform ClipBlock {
    vec4 uClips[/*$maxClips*/];      // x0, y0, x1, y1
};

layout(std140) uniform StyleBlock {
    vec4 uStyles[/*$maxStyleVec4*/]; // 8 vec4 per style (see StyleTable)
};

out vec4 oFragColor;

const int TEXTURED = 0;
const int RECT = 1;
const int ELLIPSE = 2;
const int SEGMENT = 3;
const int PATH_FILL = 4;   // local x = coverage, uv = path coordinates
const int PATH_STROKE = 5; // local y = distance across the line, uv.x = half width
const int GLYPH = 6;       // MTSDF atlas glyph: params = (range, weight, layer, has true distance)

uniform int uPremultiplied; // bit per slot: textures already holding premultiplied colors (render targets)

// the texel as stored (glyph atlases hold distances, not colors)
vec4 sampleRaw(int slot, vec2 uv) {
    vec4 texel = vec4(1.0);
    switch (slot) {
/*$sampleSwitchCase*/
    }
    return texel;
}

// the texel as a premultiplied color
vec4 sampleSlot(int slot, vec2 uv) {
    vec4 texel = sampleRaw(slot, uv);
    if (((uPremultiplied >> slot) & 1) == 0) {
        texel.rgb *= texel.a; // straight-alpha image
    }
    return texel;
}

vec4 premultiply(vec4 c) {
    return vec4(c.rgb * c.a, c.a);
}

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
// fill and stroke

// Premultiplied fill plus stroke band. The interior inside the band, the band over the shape and the band outside
// the shape are disjoint regions, so their coverages add up exactly where they meet: an outside stroke meets the
// fill without a seam (plain "stroke over fill" leaves that edge pixel partly transparent) and nothing blends twice.
//   cf     fill coverage (shape edge at d = 0)
//   d      signed distance to the shape edge; dOuter: the distance used for the band's outer edge
//   inner  band start relative to the edge (<= 0); outer: band end (>= 0)
//   mask   dash mask of the stroke (1 = solid)
vec4 fillAndStroke(vec4 fill, vec4 stroke, float cf, float d, float dOuter, float inner, float outer, float mask,
                   float pixel) {
    vec4 f = vec4(fill.rgb * fill.a, fill.a);
    if (stroke.a <= 0.0) {
        return f * cf;
    }
    vec4 s = vec4(stroke.rgb * stroke.a, stroke.a) * mask;
    float ci = min(coverage(d - inner, pixel), cf);      // inside the band's inner edge
    float co = max(coverage(dOuter - outer, pixel), cf); // inside the band's outer edge
    return f * ci + (s + f * (1.0 - s.a)) * (cf - ci) + s * (co - cf);
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
// distance-field text (msdfgen MTSDF atlases)

float median(vec3 v) {
    return max(min(v.r, v.g), min(max(v.r, v.g), v.b));
}

// One layer of a glyph. Text draws all shadows, then all strokes, then all fills, so a glyph's outline or shadow
// never covers a neighbouring glyph. Distances are in text units (negative inside), like the other shapes.
vec4 glyphColor() {
    vec4 field = sampleRaw(vSlot, vTexCoord);
    float range = vParams.x;
    float weight = vParams.y;
    int layer = int(vParams.z + 0.5);
    // text units per screen pixel (rotation invariant); at least one pixel's worth of range, as msdfgen advises
    vec2 dx = dFdx(vLocal), dy = dFdy(vLocal);
    float pixel = clamp(sqrt(0.5 * (dot(dx, dx) + dot(dy, dy))), 1e-5, range);
    float dEdge = (0.5 - median(field.rgb)) * range - weight; // MSDF: sharp corners, exact near the edge
    float dTrue = vParams.w > 0.5 ? (0.5 - field.a) * range - weight : dEdge; // true SDF: valid farther out

    vec4 fill = vColor;
    vec4 strokeColor = vec4(0.0);
    float strokeInner = 0.0, strokeOuter = 0.0;
    if (vStyle > 0) {
        int base = (vStyle - 1) * 8;
        vec4 stroke = uStyles[base + 1];
        vec4 flags = uStyles[base + 6];
        if (flags.x > 0.5) {
            vec4 gradient = uStyles[base + 2];
            float t;
            if (flags.x < 1.5) {
                vec2 axis = gradient.zw - gradient.xy;
                t = dot(vLocal - gradient.xy, axis) / max(dot(axis, axis), 1e-6);
            } else {
                t = length(vLocal - gradient.xy) / gradient.z;
            }
            fill = mix(vColor, uStyles[base + 3], clamp(t, 0.0, 1.0));
        }
        strokeColor = uStyles[base];
        strokeInner = stroke.y - 0.5 * stroke.x;
        strokeOuter = stroke.y + 0.5 * stroke.x;

        if (layer == 2) {
            vec4 shadowColor = uStyles[base + 4];
            vec4 shadow = uStyles[base + 5]; // offset (applied to the quad), blur, spread
            float d = dTrue - (strokeColor.a > 0.0 ? max(strokeOuter, 0.0) : 0.0) - shadow.w;
            float sigma = shadow.z * 0.5;
            float a = sigma < 0.25 ? coverage(d, pixel) : 0.5 - 0.5 * erf2(vec2(d * 0.70710678 / sigma, 0.0)).x;
            return premultiply(vec4(shadowColor.rgb, shadowColor.a * a));
        }
    }

    // Both layers compute the same fill + outline; each pixel belongs to exactly one of them: the fill layer owns
    // the pixels the glyph covers (drawn after every outline, so neighbouring outlines stay below the fills), the
    // outline layer the pixels outside the glyph.
    float cf = coverage(dEdge, pixel);
    if (layer == 1 ? cf > 0.0 : cf <= 0.0) {
        return vec4(0.0);
    }
    return fillAndStroke(fill, strokeColor, cf, dEdge, strokeOuter > 0.0 ? dTrue : dEdge,
                         strokeInner, strokeOuter, 1.0, pixel);
}

// ---------------------------------------------------------------------------------------------------------------
// materials

// What an effect material's function gets besides the color (see org.pixel.graphics.material.Material).
struct Fragment {
    vec2 position; // layer space
    vec2 local;    // shape-local coordinates: from the center of shapes, images, nine-patches (text: text units)
    vec4 data;     // the command's material data
    int type;      // shape type (TEXTURED, RECT, ...)
};

// the command's own texture at uv, premultiplied (images, nine-patches)
vec4 sampleImage(vec2 uv) {
    return sampleSlot(vSlot, uv);
}

#ifdef PIXEL_MATERIAL
/*$material*/
#endif

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
        color = premultiply(vColor) * sampleSlot(vSlot, vTexCoord);
    } else if (vType == GLYPH) {
        color = glyphColor();
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
        float strokeInner = 0.0, strokeOuter = 0.0, strokeMask = 1.0;
        vec4 shadowResult = vec4(0.0);
        if (vStyle > 0) {
            int base = (vStyle - 1) * 8;
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

            // stroke band, relative to the edge
            strokeColor = uStyles[base];
            strokeInner = stroke.y - 0.5 * stroke.x;
            strokeOuter = stroke.y + 0.5 * stroke.x;

            // dashes: the line body for segments, the stroke for ellipses
            if (stroke.z > 0.0) {
                if (vType == SEGMENT) {
                    fill.a *= dashMask(vLocal.x + vTexCoord.x + flags.y, stroke.z, stroke.w, pixel);
                } else if (vType == ELLIPSE) {
                    float meanRadius = 0.5 * (vTexCoord.x + vTexCoord.y);
                    float s = (atan(vLocal.y, vLocal.x) + 3.14159265) * meanRadius + flags.y;
                    // stretch the pattern so a whole number of periods fits the outline: no seam where it wraps
                    float circumference = 6.28318531 * meanRadius;
                    float period = stroke.z + stroke.w;
                    float fit = circumference / (max(1.0, floor(circumference / period + 0.5)) * period);
                    strokeMask = dashMask(s, stroke.z * fit, stroke.w * fit, pixel);
                }
            }

            // shadow below everything
            if (shadowColor.a > 0.0) {
                float sa = shadowColor.a * shadowAlpha(vLocal - shadow.xy, shadow.z, shadow.w, pixel);
                shadowResult = vec4(shadowColor.rgb * sa, sa);
            }
        }

        // premultiplied: fill and stroke, over the shadow
        vec4 shape = fillAndStroke(fill, strokeColor, fillCoverage, d, d, strokeInner, strokeOuter, strokeMask, pixel);
        color = shape + shadowResult * (1.0 - shape.a);
    }

#ifdef PIXEL_MATERIAL
    Fragment f;
    f.position = vLayerPos;
    f.local = vLocal;
    f.data = vStyle > 0 ? uStyles[(vStyle - 1) * 8 + 7] : vec4(0.0);
    f.type = vType;
    color = effect(color, vTexCoord, f);
#endif

    if (clipMask == 0.0 || (color.a <= 0.0 && max(color.r, max(color.g, color.b)) <= 0.0)) {
        discard; // nothing to add (alpha 0 with color is light: premultiplied emission)
    }
    oFragColor = color;
}
