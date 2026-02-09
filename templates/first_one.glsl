#version 330

#define parallaxIntensity 5.0

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

uniform vec2 size;          // Screen resolution
uniform vec2 scrollOffset;  // Scroll position
uniform vec2 scrollSize;    // Scrollable size
uniform float time;         // Game time
uniform float zoom;         // Zoom level

in vec2 texCoord0;
out vec4 fragColor;

/* =======================
   HASH / RANDOM
   ======================= */
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
    return uintBitsToFloat(m) - 1.0;
}

float random(vec2 v) {
    return floatConstruct(hash(floatBitsToUint(v)));
}

vec2 random2(vec2 v) {
    return vec2(
    floatConstruct(hash(floatBitsToUint(v))),
    floatConstruct(hash(floatBitsToUint(v * 2.0)))
    ) * 2.0 - 1.0;
}

/* =======================
   NOISE / CLOUDS
   ======================= */
float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(dot(random2(i), f),
            dot(random2(i + vec2(1,0)), f - vec2(1,0)), u.x),
        mix(dot(random2(i + vec2(0,1)), f - vec2(0,1)),
            dot(random2(i + vec2(1,1)), f - vec2(1,1)), u.x),
        u.y
    );
}

float clouds(vec2 uv) {
    float c = 0.0;
    c += noise(uv * 1.0);
    c += noise(uv * 2.0) * 0.5;
    c += noise(uv * 4.0) * 0.25;
    c += noise(uv * 8.0) * 0.125;
    return c / 1.875;
}

/* =======================
   MAIN
   ======================= */
void main() {

/* --- Coordinates --- */
    vec2 fragCoord = texCoord0 * size;
    vec2 center = size * 0.5;

    float scale = zoom / 16.0;
    vec2 pos = (fragCoord - center) / scale + center;
    vec2 uv = pos / size;

    uv.x *= size.x / size.y;

/* --- Scroll --- */
    vec2 scroll = vec2(0.0);
    if (scrollSize.x > 0.0) scroll.x = scrollOffset.x / scrollSize.x;
    if (scrollSize.y > 0.0) scroll.y = scrollOffset.y / scrollSize.y;

/* --- Background --- */
    vec3 color = vec3(0.07, 0.09, 0.21);

/* --- Clouds --- */
    vec2 cloudUV = uv;
    cloudUV += scroll * 0.01 * parallaxIntensity / scale;
    cloudUV += vec2(time * 0.005, time * 0.002);

    float cloud = clouds(cloudUV * 2.0);
    color += vec3(0.29, 0.76, 1.0) * cloud * 0.6;

/* --- Stars --- */
    vec3 stars = vec3(0.0);

    for (float s = 3.0; s > 0.0; s -= 0.5) {
        float cellSize = s * 100.0;
        float ratio = s / cellSize;

        vec2 coord = pos;
        coord += scroll * s * 50.0 * parallaxIntensity / scale;

        vec2 cell = floor(coord / cellSize);
        vec2 luv = fract(coord / cellSize);

        float r1 = random(cell);
        float r2 = random(cell + 1.0);

        vec2 d = luv - clamp(vec2(r1, r2), ratio, 1.0 - ratio);
        d /= ratio * 0.7;

        float dist = dot(d, d);
        float core = exp(-dist);
        float glow = 1.0 / (1.0 + dist);

        stars += vec3(core + glow * 0.3);
    }

    color += stars;

    fragColor = vec4(color, 1.0);
}