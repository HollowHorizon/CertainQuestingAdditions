#version 330
#define parallaxIntensity 3.0

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;
uniform vec2 scrollOffset;
uniform vec2 scrollSize;
uniform float time;
uniform float zoom;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 fragCoord = texCoord0 * size;
    vec2 center = size * 0.5;
    float minDim = min(size.x, size.y);
    float safeZoom = max(zoom / 16.0, 0.0001) / 4.0;
    float invZoom = 1.0 / safeZoom;

    vec2 p = (fragCoord - center) / minDim * invZoom;
    vec2 scrollPos = scrollOffset / max(scrollSize, vec2(1.0));
    p += scrollPos * parallaxIntensity * 0.05;
    p.y += time * 0.1;

    // grid и anti-aliasing через fwidth на нормализованной сетке
    vec2 gv = abs(fract(p) - 0.5) / max(fwidth(p), 1e-6);
    float line = min(min(gv.x, gv.y), 1.0);
    float intensity = 1.0 - smoothstep(0.0, 2.0, line);

    vec3 base = vec3(0.0, 0.05, 0.1);
    vec3 glow = vec3(0.1, 0.8, 1.0) * intensity;

    fragColor = vec4(base + glow, 1.0);
}
