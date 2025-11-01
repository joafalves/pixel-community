#version 330 core

in vec4 vColor;
in vec2 vTextureCoord;

uniform sampler2D uTextureImage;
uniform float u_intensity;

out vec4 FragColor;

void main() {
    vec4 texColor = texture(uTextureImage, vTextureCoord);
    float grayscaleValue = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    vec4 grayscaleColor = vec4(grayscaleValue, grayscaleValue, grayscaleValue, texColor.a);

    // Mix between original color and grayscale color based on intensity
    FragColor = mix(texColor, grayscaleColor, u_intensity);
}

