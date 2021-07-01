#version 330

out oFragColor;

in vec2 vTextureCoordinates;

uniform float uHorizontal;
uniform float uVertical;
uniform vec4 uColor;
uniform float uTime;
uniform sampler2D uTextureImage;

void main() {
    vec4 color = texture(uTextureImage, vTextureCoordinates);
    vec2 st = vTextureCoordinates;

    if (st.y < uVertical || st.y > 1.0 - uVertical || st.x < uHorizontal || st.x > 1.0 - uHorizontal) {
        color = vec4(uColor.r, uColor.g, uColor.b, uColor.a);
    }

    oFragColor = color;
}
