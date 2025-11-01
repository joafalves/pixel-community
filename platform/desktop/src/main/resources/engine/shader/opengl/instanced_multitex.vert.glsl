#version 330 core

// Per-vertex attributes (for a unit quad)
layout (location = 0) in vec2 aVertexPosition;

// Per-instance attributes
layout (location = 1) in vec2 iPosition;
layout (location = 2) in vec2 iSize;
layout (location = 3) in vec2 iAnchor;
layout (location = 4) in float iRotation;
layout (location = 5) in vec4 iColor;
layout (location = 6) in vec4 iSource;
layout (location = 7) in float iTexIndex;

uniform mat4 uMatrix;

out vec4 vColor;
out vec2 vTextureCoord;
flat out int vTextureIndex;

void main() {
    vColor = iColor;
    vTextureIndex = int(iTexIndex);

    // Calculate texture coordinates from source rectangle
    vTextureCoord = iSource.xy + aVertexPosition * iSource.zw;

    // --- Transform vertex position on the GPU ---
    vec2 pos = aVertexPosition;

    // 1. Scale the unit quad vertex to the sprite's size
    pos *= iSize;

    // 2. Subtract anchor offset to position the anchor point at the origin
    // This ensures the sprite is positioned such that its anchor point aligns with iPosition
    pos -= iAnchor * iSize;

    // 3. Apply rotation around the origin (which is now the anchor point)
    float c = cos(iRotation);
    float s = sin(iRotation);
    pos = vec2(pos.x * c - pos.y * s, pos.x * s + pos.y * c);

    // 4. Translate to final world position
    pos += iPosition;

    gl_Position = uMatrix * vec4(pos, 0.0, 1.0);
}
