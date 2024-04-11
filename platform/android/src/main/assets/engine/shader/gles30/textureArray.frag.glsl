#version 300 es

precision mediump float;

// A single sampler for the texture array
uniform mediump sampler2DArray uTextureArray;

// Inputs from the vertex shader
in highp vec2 vTextureCoordinates;
in mediump vec4 vColor;
flat in int vTextureIndex; // Index of the texture in the array

// Output color
out vec4 fragColor;

void main() {
    // Sample the texture array using the texture index
    // Note: vec3's third component is used to select the texture slice
    fragColor = texture(uTextureArray, vec3(vTextureCoordinates, float(vTextureIndex))) * vColor;

    // Optional: Discard the fragment if the alpha is too low
    if (fragColor.a <= 0.0) {
        discard;
    }
}