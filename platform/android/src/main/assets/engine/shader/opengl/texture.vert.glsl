attribute vec2 aVertexPosition;
attribute vec2 aTextureCoordinates;
attribute vec4 aVertexColor;

uniform mat4 uMatrix;

varying vec2 vTextureCoordinates;
varying vec4 vColor;

void main() {
    vColor = aVertexColor;
    vTextureCoordinates = aTextureCoordinates;
    gl_Position = uMatrix * vec4(aVertexPosition, 0.0, 1.0);
}
