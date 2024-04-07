precision mediump float;

varying vec2 vTextureCoordinates;
varying vec4 vColor;

uniform sampler2D uTextureImage;

void main() {
    vec4 color = texture2D(uTextureImage, vTextureCoordinates) * vColor;
    if (color.a <= 0.0) {
        discard;
    }
    gl_FragColor = color;
}
