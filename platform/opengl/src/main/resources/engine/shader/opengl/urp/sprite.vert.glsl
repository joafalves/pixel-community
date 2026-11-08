// URP v2 sprite vertex shader. No #version: the backend prepends the GL / GLES preamble.
// Attribute locations match VertexLayout.SPRITE.

layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in int aSlot;

uniform mat4 uMatrix;

out vec2 vTexCoord;
out vec4 vColor;
flat out int vSlot;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    vSlot = aSlot;
    gl_Position = uMatrix * vec4(aPosition, 0.0, 1.0);
}
