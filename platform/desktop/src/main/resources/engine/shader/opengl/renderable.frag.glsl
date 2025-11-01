#version 330 core

in vec4 vColor;
in vec2 vTextureCoord;

uniform sampler2D uTextureImage;

out vec4 FragColor;

void main() {
    vec4 color = texture(uTextureImage, vTextureCoord);
    FragColor = color;
}
