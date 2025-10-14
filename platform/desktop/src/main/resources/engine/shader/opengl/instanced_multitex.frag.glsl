#version 330 core

in vec4 vColor;
in vec2 vTextureCoord;
flat in int vTextureIndex;

uniform sampler2D uTextureImage[/*$numTextures*/];

out vec4 FragColor;

void main() {
    vec4 color;

    switch (vTextureIndex) {
        /*$textureSwitchCase*/
    }

    FragColor = color * vColor;
}
