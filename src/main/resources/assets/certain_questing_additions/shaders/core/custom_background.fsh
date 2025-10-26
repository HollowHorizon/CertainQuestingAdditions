#version 330

uniform vec3 iResolution;
uniform float iTime;
uniform vec4 iMouse;

in vec2 texCoord0;
out vec4 fragColor;

uint hash(uint x) {
    x += (x << 10u);
    x ^= (x >> 6u);
    x += (x << 3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

uint hash(uvec2 v) { return hash(v.x ^ hash(v.y)); }

float floatConstruct(uint m) {
    const uint ieeeMantissa = 0x007FFFFFu;
    const uint ieeeOne = 0x3F800000u;
    m &= ieeeMantissa;
    m |= ieeeOne;
    float f = uintBitsToFloat(m);
    return f - 1.0;
}

float random(vec2 v) { return floatConstruct(hash(floatBitsToUint(v))); }
vec2 random2(vec2 v) {
    return vec2(
        floatConstruct(hash(floatBitsToUint(v))),
        floatConstruct(hash(floatBitsToUint(v * 2.0)))
    ) * 2.0 - 1.0;
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(dot(random2(i + vec2(0.0, 0.0)), f - vec2(0.0, 0.0)),
            dot(random2(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0)), u.x),
        mix(dot(random2(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0)),
            dot(random2(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0)), u.x),
        u.y
    ) * 0.40 + 0.02;
}

float clouds(vec2 uv) {
    vec2 pos = vec2(2.0 * uv);
    return (
        noise(pos) +
        0.5 * noise(pos * 2.0) +
        0.25 * noise(pos * 4.0) +
        0.125 * noise(pos * 8.0)
    ) / 1.875;
}

void main() {
    vec2 fragCoord = texCoord0 * iResolution.xy;
    vec2 uv = fragCoord / iResolution.xy;
    uv.x *= iResolution.x / iResolution.y;
    vec2 scrollPos = vec2(iMouse.x / iResolution.x, iMouse.y / iResolution.y);

    fragColor = vec4(0.07, 0.09, 0.21, 1.0);
    fragColor += clamp(vec4(0.29, 0.76, 1.00, 1.0) * clouds(uv + scrollPos * 0.01), 0.0, 1.0);

    vec4 stars = vec4(0.0);
    for (float starsize = 3.0; starsize > 0.0; starsize -= 0.5) {
        float cellsize = starsize * 100.0;
        float ratio = starsize / cellsize;
        float c1 = random(vec2(starsize)) * 500.0;
        float c2 = 50.0 * starsize;
        vec2 coord = fragCoord + vec2(c1, c1) + scrollPos * vec2(c2, c2);
        vec2 luv = mod(coord, cellsize) / cellsize;
        vec2 cell = floor(coord / cellsize);

        float r1 = random(cell + vec2(1.0));
        float r2 = random(cell);
        vec2 col = luv - clamp(vec2(r1, r2), ratio, 1.0 - ratio);
        col /= ratio * 0.7;

        float lensq = dot(col, col);
        float core = exp(-lensq);
        float glow = 1.0 / (1.0 + lensq);
        float intensity = core + 0.3 * glow;

        stars += vec4(vec3(intensity), 1.0);
    }

    fragColor += stars;
}
