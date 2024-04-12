#version 330 core

out vec4 oFragColor;

in vec2 vTextureCoordinates;
in vec4 vColor;

uniform sampler2D uTextureImage;

void main() {
    vec4 color = texture(uTextureImage, vTextureCoordinates) * vColor;
    if (color.a <= 0) {
        discard;
    }
    oFragColor = vec4(color.rgb, 1.0);
}
