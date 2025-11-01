#version 330 core

// Inputs from vertex shader
in vec2 vTexCoord;
in vec4 vColor;
in vec4 vShapeData;
in vec3 vShapeDataExtra;
in vec2 vQuadSize;
flat in int vShapeType;
flat in int vTextureId;

// Uniforms
uniform sampler2D uTextAtlas;              // Font texture atlas
uniform sampler2D uTextures[/*$numTextures*/];  // Texture array for image rendering
uniform float uSmoothness;                 // Global smoothness for anti-aliasing
uniform float uTextEdge;                   // SDF edge threshold for text rendering

// Output
out vec4 fragColor;

// Shape type constants (must match Java constants)
const int SHAPE_ROUNDED_RECT = 0;
const int SHAPE_CIRCLE = 1;
const int SHAPE_LINE = 2;
const int SHAPE_POINT = 3;
const int SHAPE_TEXT_GLYPH = 4;
const int SHAPE_TRIANGLE = 5;
const int SHAPE_TEXTURED_QUAD = 6;

// ============================================================================
// SDF Functions
// ============================================================================

// SDF for a rounded rectangle with uniform corner radius
// p: point position, size: half-size, radius: corner radius
float sdRoundedRect(vec2 p, vec2 size, float radius) {
    vec2 d = abs(p) - size + radius;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - radius;
}

// SDF for a rounded rectangle with individual corner radii
// p: point position, size: half-size, radii: (topLeft, topRight, bottomRight, bottomLeft)
float sdRoundedRectCorners(vec2 p, vec2 size, vec4 radii) {
    // Select radius based on which quadrant we're in
    // p.x < 0 means left side, p.y < 0 means top
    float r = (p.x < 0.0) ?
              (p.y < 0.0 ? radii.x : radii.w) :  // left side: TL or BL
              (p.y < 0.0 ? radii.y : radii.z);   // right side: TR or BR

    vec2 q = abs(p);
    vec2 d = q - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

// SDF for a circle
// p: point position, radius: circle radius
float sdCircle(vec2 p, float radius) {
    return length(p) - radius;
}

// SDF for a line segment
// p: point position, a: start point, b: end point, thickness: line half-width
float sdLine(vec2 p, vec2 a, vec2 b, float thickness) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h) - thickness;
}

// ============================================================================
// Main Fragment Shader
// ============================================================================

void main() {
    float dist = 0.0;
    float alpha = 0.0;
    vec4 outputColor = vColor;
    
    // Shape type discriminator - evaluate different SDF based on type
    if (vShapeType == SHAPE_ROUNDED_RECT) {
        // vShapeData: (width, height, radiusTL, radiusTR)
        // vShapeDataExtra: (radiusBR, radiusBL, strokeWidth)
        // These are the ORIGINAL shape dimensions (before padding)
        vec2 size = vShapeData.xy;
        vec4 radii = vec4(vShapeData.z, vShapeData.w, vShapeDataExtra.x, vShapeDataExtra.y);
        float strokeWidth = vShapeDataExtra.z;

        // Convert texture coordinates (0-1) to centered position
        // Use vQuadSize for coordinate mapping (expanded quad)
        // but size for SDF calculation (original shape)
        vec2 pos = (vTexCoord - 0.5) * vQuadSize;

        dist = sdRoundedRectCorners(pos, size * 0.5, radii);

        if (strokeWidth > 0.0) {
            // Stroke rendering
            float outerEdge = abs(dist) - strokeWidth * 0.5;
            alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, outerEdge);
        } else {
            // Fill rendering
            alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, dist);
        }

    } else if (vShapeType == SHAPE_CIRCLE) {
        // vShapeData: (radius, strokeWidth, unused, unused)
        float radius = vShapeData.x;
        float strokeWidth = vShapeData.y;
        
        // Convert texture coordinates to centered position
        // Use vQuadSize for mapping
        vec2 pos = (vTexCoord - 0.5) * vQuadSize;
        
        dist = sdCircle(pos, radius);
        
        if (strokeWidth > 0.0) {
            // Stroke rendering
            float outerEdge = abs(dist) - strokeWidth * 0.5;
            alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, outerEdge);
        } else {
            // Fill rendering
            alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, dist);
        }
        
    } else if (vShapeType == SHAPE_LINE) {
        // vShapeData: (lineWidth, unused, unused, unused)
        // Lines are rendered as oriented rectangles
        // The quad is oriented along the line, with texture coords:
        // - u (vTexCoord.x): 0 at start, 1 at end (along line direction)
        // - v (vTexCoord.y): 0 at one edge, 1 at other edge (perpendicular to line)
        
        float lineWidth = vShapeData.x;
        
        // Distance from the center line (v = 0.5 is the center)
        float distFromCenter = abs(vTexCoord.y - 0.5);
        
        // The quad spans the full line width in texture space (0 to 1)
        // distFromCenter ranges from 0 (center) to 0.5 (edge)
        // Apply anti-aliasing at the edges
        // Use minimal smoothing to avoid eating into thin lines
        float edgeSmoothness = 0.01; // Very tight smoothing for crisp lines
        alpha = 1.0 - smoothstep(0.5 - edgeSmoothness, 0.5 + edgeSmoothness, distFromCenter);
        
    } else if (vShapeType == SHAPE_POINT) {
        // vShapeData: (size, unused, unused, unused)
        float size = vShapeData.x;
        
        // Point is just a small circle
        // Use vQuadSize for coordinate mapping
        vec2 pos = (vTexCoord - 0.5) * vQuadSize;
        dist = sdCircle(pos, size);
        alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, dist);
        
    } else if (vShapeType == SHAPE_TEXT_GLYPH) {
        // Text rendering using SDF atlas texture
        // vShapeData: (strokeWidth, strokeR, strokeG, strokeB)
        float strokeWidth = vShapeData.x;
        vec3 strokeColor = vShapeData.yzw;
        
        float sdfValue = texture(uTextAtlas, vTexCoord).a;
        float onedge = uTextEdge; // Use configurable edge threshold from uniform
        
        // Use a much smaller smoothness value for crisp text (matching old renderer)
        float textSmoothness = 0.075;
        
        if (strokeWidth > 0.0) {
            // Text with stroke - render both fill and outline
            float fillAlpha = smoothstep(onedge - textSmoothness, onedge + textSmoothness, sdfValue);
            
            // Stroke edge is pushed inward from the fill edge
            float strokeEdge = onedge - strokeWidth;
            float strokeAlpha = smoothstep(strokeEdge - textSmoothness, strokeEdge + textSmoothness, sdfValue);
            
            // Combined alpha is the maximum (stroke encompasses fill)
            alpha = strokeAlpha;
            
            // Color mixing: stroke color at edges, fill color in center
            // fillAlpha controls the blend between stroke and fill
            outputColor.rgb = mix(strokeColor, outputColor.rgb, fillAlpha);
        } else {
            // Regular text fill
            alpha = smoothstep(onedge - textSmoothness, onedge + textSmoothness, sdfValue);
        }
    } else if (vShapeType == SHAPE_TRIANGLE) {
        // Raw triangle - no SDF, just flat color
        // Used for path API filled polygons
        alpha = 1.0;
    } else if (vShapeType == SHAPE_TEXTURED_QUAD) {
        // Textured quad - sample from texture array
        // vTextureId contains the texture slot index
        // vTexCoord contains the UV coordinates
        
        // Sample the appropriate texture using switch for GPU compatibility
        vec4 texColor;
        switch (vTextureId) {
/*$textureSwitchCase*/
        }
        
        // Apply tint color (vColor acts as a tint multiplier for both color and alpha)
        outputColor = texColor * vColor;
        alpha = outputColor.a;
    }
    
    fragColor = vec4(outputColor.rgb, outputColor.a * alpha);
    
    // Discard fully transparent pixels
    if (fragColor.a < 0.01) {
        discard;
    }
}
