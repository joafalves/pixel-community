#version 330 core

// Vertex attributes
in vec2 aPosition;        // Vertex position
in vec2 aTexCoord;        // Texture coordinate (for text glyphs)
in vec4 aColor;           // Vertex color
in vec4 aShapeData;       // Shape-specific data part 1: (width, height, radiusTL, radiusTR) for rounded rects
in vec3 aShapeDataExtra;  // Shape-specific data part 2: (radiusBR, radiusBL, strokeWidth) for rounded rects
in vec2 aQuadSize;        // Quad size (width, height) for coordinate transformation
in float aShapeType;      // Shape type discriminator
in float aTextureId;      // Texture atlas ID (-1 for non-text shapes)

// Outputs to fragment shader
out vec2 vTexCoord;
out vec4 vColor;
out vec4 vShapeData;
out vec3 vShapeDataExtra;
out vec2 vQuadSize;
flat out int vShapeType;
flat out int vTextureId;

// Uniforms
uniform mat4 uViewMatrix;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    vShapeData = aShapeData;
    vShapeDataExtra = aShapeDataExtra;
    vQuadSize = aQuadSize;
    vShapeType = int(aShapeType);
    vTextureId = int(aTextureId);

    gl_Position = uViewMatrix * vec4(aPosition, 0.0, 1.0);
}
