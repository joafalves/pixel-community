#version 330 core

out vec2 vTextureCoordinates;
out vec4 vColor;
flat out int vTextureIndex;

in vec2 aVertexPosition;
in vec2 aTextureCoordinates;
in vec4 aVertexColor;
in float aTextureIndex;

uniform mat4 uMatrix;

void main() {
    vColor = aVertexColor;
    vTextureCoordinates = aTextureCoordinates;
    vTextureIndex = int(aTextureIndex);
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
