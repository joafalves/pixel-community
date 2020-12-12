#version 330

out vec4 vColor;

in vec2 aVertexPosition;
in vec4 aVertexColorPosition;

uniform mat4 uMatrix;
uniform float uPointSize;

void main(void) {
    vColor = aVertexColorPosition;
    gl_PointSize = uPointSize;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
