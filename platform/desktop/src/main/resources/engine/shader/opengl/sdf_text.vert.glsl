#version 330 core

// Vertex attributes
layout (location = 0) in vec2 aPosition;  // Pre-transformed world position
layout (location = 1) in vec2 aTexCoord;  // Texture coordinates

// Uniforms
uniform mat4 uViewMatrix;  // Camera view-projection matrix

// Outputs to fragment shader
out vec2 vTexCoord;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = uViewMatrix * vec4(aPosition, 0.0, 1.0);
}
