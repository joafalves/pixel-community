// URP v2 shape vertex shader (VertexLayout.FULL). No #version: the backend prepends the GL / GLES preamble.

layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aLocal;
layout(location = 4) in vec4 aParams;
layout(location = 5) in int aType;
layout(location = 6) in int aSlot;
layout(location = 7) in int aClip;
layout(location = 8) in int aStyle;

uniform mat4 uMatrix;

out vec2 vTexCoord;
out vec4 vColor;
out vec2 vLocal;
out vec4 vParams;
out vec2 vLayerPos;
flat out int vType;
flat out int vSlot;
flat out int vClip;
flat out int vStyle;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    vLocal = aLocal;
    vParams = aParams;
    vLayerPos = aPosition;
    vType = aType;
    vSlot = aSlot;
    vClip = aClip;
    vStyle = aStyle;
    gl_Position = uMatrix * vec4(aPosition, 0.0, 1.0);
}
