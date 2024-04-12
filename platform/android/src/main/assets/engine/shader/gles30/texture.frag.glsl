#version 300 es

precision mediump float;  // This line is required in OpenGL ES for specifying default precision for float types

in vec2 vTextureCoordinates;
in vec4 vColor;

uniform sampler2D uTextureImage;

out vec4 oFragColor;

void main() {
    vec4 color = texture(uTextureImage, vTextureCoordinates) * vColor;
    if (color.a <= 0.0) {  // Ensure the comparison is to 0.0, not 0 to clarify the float nature
        discard;
    }
    oFragColor = color;
}
