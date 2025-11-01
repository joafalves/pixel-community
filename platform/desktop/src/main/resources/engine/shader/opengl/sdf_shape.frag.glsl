#version 330 core

// Inputs from vertex shader
in vec2 vTexCoord;

// Uniforms
uniform vec4 uColor;
uniform vec2 uSize;        // Actual shape size (width, height)
uniform vec2 uQuadSize;    // Quad size (expanded for stroke/AA)
uniform float uRadius;     // Corner radius OR circle radius
uniform float uSmoothness; // Edge smoothness for anti-aliasing
uniform float uStrokeWidth; // Stroke width (0 = filled)
uniform int uShapeType;    // 0 = rounded rect, 1 = circle

// Output
out vec4 fragColor;

// SDF for a rounded rectangle
float sdRoundedRect(vec2 p, vec2 size, float radius) {
    vec2 d = abs(p) - size + radius;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - radius;
}

// SDF for a circle
float sdCircle(vec2 p, float radius) {
    return length(p) - radius;
}

void main() {
    // Convert texture coordinates (0-1) to centered position
    // Use uQuadSize for the coordinate mapping, but uSize for the shape SDF
    vec2 pos = (vTexCoord - 0.5) * uQuadSize;
    
    // Calculate signed distance based on shape type using ORIGINAL shape size
    float dist;
    if (uShapeType == 1) {
        // Circle - centered in the expanded quad
        dist = sdCircle(pos, uRadius);
    } else {
        // Rounded rectangle - centered in the expanded quad
        dist = sdRoundedRect(pos, uSize * 0.5, uRadius);
    }
    
    // Handle stroke vs fill
    float alpha;
    if (uStrokeWidth > 0.0) {
        // Stroke: only render the outline
        float outerEdge = abs(dist) - uStrokeWidth * 0.5;
        alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, outerEdge);
    } else {
        // Fill: render inside the shape
        alpha = 1.0 - smoothstep(-uSmoothness, uSmoothness, dist);
    }
    
    fragColor = uColor * alpha;
    
    // Discard fully transparent pixels
    if (fragColor.a < 0.01) {
        discard;
    }
}
