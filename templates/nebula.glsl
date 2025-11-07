#version 330
#define parallaxIntensity 4.0

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;
uniform vec2 scrollOffset;
uniform vec2 scrollSize;
uniform float time;
uniform float zoom;

in vec2 texCoord0;
out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(23.43, 45.17))) * 54321.0); }

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 fragCoord = texCoord0 * size;
    vec2 center = size * 0.5;
    float minDim = min(size.x, size.y);
    float safeZoom = max(zoom / 16.0, 0.0001);
    float invZoom = 1.0 / safeZoom; // инвертированное поведение

    // нормализованные координаты (фиксуют растяжение)
    vec2 p = (fragCoord - center) / minDim * invZoom;
    vec2 scrollPos = scrollOffset / max(scrollSize, vec2(1.0));
    p += scrollPos * parallaxIntensity * 0.02;

    float t = time * 0.05;
    float n = fbm(p * 1.5 + t);
    float m = fbm(p * 3.0 - t * 0.7);
    vec3 col = vec3(0.3 + 0.7 * n, 0.2 + 0.5 * m, 0.5 + 0.5 * sin(n * 3.14159));

    fragColor = vec4(col * 1.2, 1.0);
}
