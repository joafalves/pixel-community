#version 330

out vec4 oFragColor;

in vec2 vTextureCoordinates;

uniform vec4 uOverlayColor;
uniform float uAmount;
uniform sampler2D uTextureImage;

void main(void) {
    // color 'mix()' also exists but this seems to have better results
    vec4 color = texture(uTextureImage, vTextureCoordinates);
    float r = color.r;
    float g = color.g;
    float b = color.b;

    color.r = min(1.0, (r * (1.0 - (0.607 * uAmount))) + (g * (0.769 * uAmount)) + (b * (0.189 * uAmount)));
    color.g = min(1.0, (r * 0.349 * uAmount) + (g * (1.0 - (0.314 * uAmount))) + (b * 0.168 * uAmount));
    color.b = min(1.0, (r * 0.272 * uAmount) + (g * 0.534 * uAmount) + (b * (1.0 - (0.869 * uAmount))));

    oFragColor = color;
}
