// Shockwave (post effect): a ring that bends the scene behind it and glows faintly.
// uWave: center (uv, top-left origin), radius (in screen heights), strength (0 = off).
uniform vec4 uWave;
uniform float uThickness;

vec4 effect(vec2 uv) {
    if (uWave.w <= 0.0) {
        return texture(uSource, uv);
    }
    float aspect = uResolution.x / uResolution.y;
    vec2 d = (uv - uWave.xy) * vec2(aspect, 1.0);
    float distance = length(d);
    float x = (distance - uWave.z) / uThickness;
    float ring = exp(-x * x) * uWave.w;
    vec2 direction = distance > 0.0001 ? d / distance : vec2(0.0);
    vec2 offset = direction * ring * 0.025;
    offset.x /= aspect;
    vec4 c = texture(uSource, uv - offset);
    // a little light on the ring's front (premultiplied: scaled by coverage)
    c.rgb += vec3(0.55, 0.75, 1.0) * ring * 0.12 * c.a;
    return c;
}
