#version 330

out vec4 oFragColor;

in vec2 vTextureCoordinates;

uniform sampler2D uTextureImage;

void main(void) {
    oFragColor = texture(uTextureImage, vTextureCoordinates);
}
