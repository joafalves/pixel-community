#version 330 core

out vec4 oFragColor;

in vec2 vTextureCoordinates;
in vec4 vColor;
flat in int vTextureIndex;

uniform sampler2D uTextureImage[/*$numTextures*/];

void main() {
    vec4 color = texture(uTextureImage[vTextureIndex], vTextureCoordinates) * vColor;
    if (color.a <= 0) {
        discard;
    }
    oFragColor = color;
}
