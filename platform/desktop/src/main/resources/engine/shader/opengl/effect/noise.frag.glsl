#version 330

out vec4 oFragColor;

in vec2 vTextureCoordinates;

uniform float uAmount;
uniform float uDensity;
uniform vec2 uOffset;
uniform sampler2D uTextureImage;

vec2 hash(vec2 p)
{
    p = vec2(dot(p, vec2(127.1, 311.7)),
    dot(p, vec2(269.5, 183.3)));

    return -1.0 + 2.0 * fract(sin(p + 20.0) * 53758.5453123);
}

float lerp(float a, float b, float t)
{
    return a + t * (b - a);
}

float perlin_noise_2(in vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);

    //grid points
    vec2 p0 = vec2(0.0, 0.0);
    vec2 p1 = vec2(1.0, 0.0);
    vec2 p2 = vec2(0.0, 1.0);
    vec2 p3 = vec2(1.0, 1.0);

    //distance vectors to each grid point
    vec2 s0 = f - p0;
    vec2 s1 = f - p1;
    vec2 s2 = f - p2;
    vec2 s3 = f - p3;

    //random gradient vectors on each grid point
    vec2 g0 = hash(i + p0);
    vec2 g1 = hash(i + p1);
    vec2 g2 = hash(i + p2);
    vec2 g3 = hash(i + p3);

    //gradient values
    float q0 = dot(s0, g0);
    float q1 = dot(s1, g1);
    float q2 = dot(s2, g2);
    float q3 = dot(s3, g3);

    //interpolant weights
    vec2 u = f * f * (3.0 - 2.0 * f);

    //bilinear interpolation
    float l0 = lerp(q0, q1, u.x);
    float l1 = lerp(q2, q3, u.x);
    float l2 = lerp(l0, l1, u.y);

    return l2;
}

float perlin_fbm(vec2 uv, float persistence, int octaves)
{
    float total = 0.0;
    float maxValue = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;

    for (int i=0; i<octaves;++i)
    {
        total += perlin_noise_2(uv * frequency) * amplitude;
        maxValue += amplitude;
        amplitude *= persistence;
        frequency *= 2.0;
    }

    return total/maxValue;
}

vec4 render(vec2 uv)
{
    float n = perlin_fbm(uv, 0.7, 8);
    return vec4(n+0.5);
}

void main(void) {
    vec4 color = texture(uTextureImage, vTextureCoordinates);
    vec2 uv = ((vTextureCoordinates.xy * 2.0)) * uDensity * 0.5;
    uv.x += uOffset.x;// * (uTime * 0.05)
    uv.y -= uOffset.y;
    oFragColor = mix(color, render(uv), uAmount);
}
