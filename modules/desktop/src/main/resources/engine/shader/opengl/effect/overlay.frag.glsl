#version 330

out vec4 oFragColor;

in vec2 vTextureCoordinates;

uniform vec4 uOverlayColor;
uniform sampler2D uTextureImage;

void main(void) {
    // color 'mix()' also exists but this seems to have better results
    vec4 color = texture(uTextureImage, vTextureCoordinates);
    color.r = color.r * uOverlayColor.x;
    color.g = color.g * uOverlayColor.y;
    color.b = color.b * uOverlayColor.z;
    color.a = color.a * uOverlayColor.w;

    oFragColor = color;
}
