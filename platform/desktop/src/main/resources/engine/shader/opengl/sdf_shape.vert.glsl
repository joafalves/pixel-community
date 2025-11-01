#version 330 core

// Vertex attributes
layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;

// Outputs to fragment shader
out vec2 vTexCoord;

// Uniforms
uniform mat4 uMatrix;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = uMatrix * vec4(aPosition, 0.0, 1.0);
}
