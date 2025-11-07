#version 330
#define parallaxIntensity 2.0

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;
uniform vec2 scrollOffset;
uniform vec2 scrollSize;
uniform float time;
uniform float zoom;

in vec2 texCoord0;
out vec4 fragColor;

float rand(vec2 co) { return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453); }

void main() {
    vec2 fragCoord = texCoord0 * size;
    vec2 center = size * 0.5;
    float minDim = min(size.x, size.y);
    float safeZoom = max(zoom / 16.0, 0.0001);
    float invZoom = 1.0 / safeZoom;

    vec2 p = (fragCoord - center) / minDim * invZoom; // нормализация
    vec2 scrollPos = scrollOffset / max(scrollSize, vec2(1.0));
    p += scrollPos * 0.02 * parallaxIntensity;

    vec3 col = vec3(0.02, 0.05, 0.03); // тёмный фон
    for (float i = 1.0; i < 25.0; i++) {
        vec2 pos = vec2(rand(vec2(i, i * 1.3)), rand(vec2(i * 1.7, i)));
        float speed = 0.2 + rand(pos) * 0.8;
        vec2 ppos = pos + vec2(sin(time * speed + i), cos(time * 0.3 + i)) * 0.1;
        float d = length(p - (ppos - 0.5));
        float glow = exp(-30.0 * d) * (0.3 + rand(pos) * 0.7);
        col += glow * vec3(0.2 + 0.8 * rand(pos), 1.0, 0.5 + 0.5 * rand(pos));
    }
    fragColor = vec4(col, 0.75);
}
