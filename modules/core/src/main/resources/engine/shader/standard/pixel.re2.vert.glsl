#version 330 core

layout(location=0) in vec2 aVertexPosition;

uniform mat4 uMatrix;

void main() {
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
