// Hit flash (effect material): blends a ship toward white. f.data.x is the amount (0 = none, 1 = white).
// Every ship is drawn with this material, so they all keep sharing one draw call whatever their flash.
// Colors are premultiplied: white at the texel's coverage is vec3(color.a).
vec4 effect(vec4 color, vec2 uv, Fragment f) {
    float amount = clamp(f.data.x, 0.0, 1.0);
    return vec4(mix(color.rgb, vec3(color.a), amount), color.a);
}
