#version 330

out vec4 oFragColor;

uniform sampler2D uTextureImage;
uniform float uTime;
uniform float uBlockSizeH;
uniform float uBlockSizeV;
uniform int uAnimated;

in vec2 vTextureCoordinates;

void main(void) {
    float blockSizeH = 100.0 / uBlockSizeH;
    float blockSizeV = 100.0 / uBlockSizeV;
    vec2 clip;

    if (uAnimated == 0) {
        clip = vec2(floor(vTextureCoordinates.x * 100.0 / blockSizeH) * blockSizeH / 100.0,
        floor(vTextureCoordinates.y * 100.0 / blockSizeV) * blockSizeV / 100.0);

    } else {
        float sx = (cos(uTime) + 1) / 2.0 / 100.0;
        float sy = (sin(uTime) + 1) / 2.0 / 100.0;
        clip = vec2(floor((vTextureCoordinates.x) * 100.0 / blockSizeH) * blockSizeH / 100.0 + sx,
        floor((vTextureCoordinates.y) * 100.0 / blockSizeV) * blockSizeV / 100.0 + sy);
    }

    vec4 color = texture(uTextureImage, clip);
    oFragColor = color;
}
