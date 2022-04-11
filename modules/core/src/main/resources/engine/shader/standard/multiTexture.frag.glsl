#version 330 core

out vec4 oFragColor;

in vec2 vTextureCoordinates;
in vec4 vColor;
flat in int vTextureIndex;

uniform sampler2D uTextureImage[/*$numTextures*/];

void main() {
    vec4 color;
    // The following switch case exists because of the GLSL spec. More specifically this:
    // "sampler arrays indexed with non-constant expressions are not allowed"
    // This is enforced by a considerable amount of Intel iGPUs and AMD GPUs (the latter fails silently).
    // To workaround this issue, the switch case is used to apply constant expressions to the sampler array index.
    switch (vTextureIndex)
    {
    /*$textureSwitchCase*/
    }
    if (color.a <= 0) {
        discard;
    }
    oFragColor = color;
}
