#version 300 es

in vec2 aVertexPosition; // Vertex position
in vec2 aTextureCoordinates; // Texture coordinates
in float aTextureIndex; // Texture index for the sprite

uniform mat4 uMatrix; // Projection or transformation matrix

out highp vec2 vTextureCoordinates;
flat out int vTextureIndex; // Pass to fragment shader

void main() {
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
    vTextureCoordinates = aTextureCoordinates;
    vTextureIndex = int(aTextureIndex);
}