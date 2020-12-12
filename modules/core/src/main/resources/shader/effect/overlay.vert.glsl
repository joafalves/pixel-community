#version 330

out vec2 oTextureCoordinates;

in vec4 aVertexPosition;// <vec2 position, vec2 texCoords>

void main(void) {
    gl_Position = vec4(aVertexPosition.xy, 0.0f, 1.0f);
    oTextureCoordinates = aVertexPosition.zw;
}
