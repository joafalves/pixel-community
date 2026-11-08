// Animated water for the URP materials demo. Edit this file while MaterialsDemo runs (from its project directory):
// it is reloaded live, and a compile error keeps the previous version running (the HUD shows the error).
uniform float uTime;

vec4 effect(vec4 color, vec2 uv, Fragment f) {
    vec2 p = f.position * 0.045;
    float wave = sin(p.x + uTime * 1.7 + sin(p.y * 0.8)) * cos(p.y * 1.3 + uTime) * 0.5 + 0.5;
    vec3 deep = vec3(0.07, 0.25, 0.52);
    vec3 shallow = vec3(0.30, 0.68, 0.88);
    vec3 water = mix(deep, shallow, wave * wave);
    float sparkle = smoothstep(0.93, 1.0, sin(p.x * 3.1 - uTime * 2.0) * sin(p.y * 2.7 + uTime * 1.3));
    return vec4(min(water + sparkle * 0.5, vec3(1.0)) * color.a, color.a); // premultiplied
}
