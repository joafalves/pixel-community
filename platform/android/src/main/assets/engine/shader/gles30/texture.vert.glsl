#version 300 es

in vec2 aVertexPosition;
in vec2 aTextureCoordinates;
in vec4 aVertexColor;

out vec2 vTextureCoordinates;
out vec4 vColor;

uniform mat4 uMatrix;

void main() {
    vColor = aVertexColor;
    vTextureCoordinates = aTextureCoordinates;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
