// URP v2 sprite fragment shader. No #version: the backend prepends the GL / GLES preamble.
// Samplers are selected with a switch over constant indices: indexing a sampler array with a non-constant value is
// invalid GLSL (and fails on Intel/AMD drivers and GLES).

in vec2 vTexCoord;
in vec4 vColor;
flat in int vSlot;

uniform sampler2D uTextures[/*$numTextures*/];

out vec4 oFragColor;

void main() {
    vec4 color = vColor;
    switch (vSlot) {
/*$textureSwitchCase*/
    }
    if (color.a <= 0.0) {
        discard;
    }
    oFragColor = color;
}
