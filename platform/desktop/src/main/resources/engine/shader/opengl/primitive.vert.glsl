#version 330 core

out vec4 vColor;

in vec2 aVertexPosition;
in vec4 aVertexColor;

uniform mat4 uMatrix;

void main() {
    vColor = aVertexColor;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
