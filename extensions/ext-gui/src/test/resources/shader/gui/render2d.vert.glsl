#version 330

out oTextureCoordinates;

in vec2 aVertexPosition;
in vec2 aTextureCoordinates;

uniform mat4 uMatrix;

void main() {
    aTextureCoordinates = aTextureCoordinates;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
