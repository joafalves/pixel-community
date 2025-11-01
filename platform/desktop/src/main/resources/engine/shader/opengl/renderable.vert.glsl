#version 330 core

// This vertex position is already in world-space, calculated on the CPU
layout (location = 0) in vec2 aVertexPosition;
layout (location = 1) in vec2 aTextureCoord;

// This is the camera's View-Projection matrix
uniform mat4 uMatrix;

out vec2 vTextureCoord;

void main() {
    vTextureCoord = aTextureCoord;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
