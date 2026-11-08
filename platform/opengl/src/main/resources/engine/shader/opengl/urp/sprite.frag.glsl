// URP v2 sprite fragment shader. No #version: the backend prepends the GL / GLES preamble.
// Output colors are premultiplied (rgb * a); the backend blends with (ONE, ONE_MINUS_SRC_ALPHA).
// Samplers are selected with a switch over constant indices: indexing a sampler array with a non-constant value is
// invalid GLSL (and fails on Intel/AMD drivers and GLES).

in vec2 vTexCoord;
in vec4 vColor;
flat in int vSlot;

uniform sampler2D uTextures[/*$numTextures*/];
uniform int uPremultiplied; // bit per slot: textures already holding premultiplied colors (render targets)

out vec4 oFragColor;

vec4 sampleSlot(int slot, vec2 uv) {
    vec4 texel = vec4(1.0);
    switch (slot) {
/*$textureSwitchCase*/
    }
    if (((uPremultiplied >> slot) & 1) == 0) {
        texel.rgb *= texel.a; // straight-alpha image
    }
    return texel; // premultiplied
}

void main() {
    vec4 color = vec4(vColor.rgb * vColor.a, vColor.a) * sampleSlot(vSlot, vTexCoord);
    if (color.a <= 0.0 && max(color.r, max(color.g, color.b)) <= 0.0) {
        discard; // nothing to add (alpha 0 with color is light: premultiplied emission)
    }
    oFragColor = color;
}
