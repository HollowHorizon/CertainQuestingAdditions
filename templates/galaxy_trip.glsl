#version 330

// Настройки качества
#define PASS_COUNT 3   // Количество проходов для звезд (уменьши до 2 или 1, если будет лагать)

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;          // Screen resolution
uniform vec2 scrollOffset;  // Current scroll X/Y
uniform vec2 scrollSize;    // Total scrollable area
uniform float time;         // Game time
uniform float zoom;         // Current zoom level

in vec2 texCoord0;
out vec4 fragColor;

// --- Galaxy Trip Constants & Variables ---
float fBrightness = 2.5;
float fSteps = 121.0;
float fParticleSize = 0.015;
float fParticleLength = 0.5 / 60.0;
float fMinDist = 0.8;
float fMaxDist = 5.0;
float fRepeatMin = 1.0;
float fRepeatMax = 2.0;
float fDepthFade = 0.8;

// --- Helper Functions from Original Shader ---

float Random(float x) {
    return fract(sin(x * 123.456) * 23.4567 + sin(x * 345.678) * 45.6789 + sin(x * 456.789) * 56.789);
}

vec3 GetParticleColour(const in vec3 vParticlePos, const in float fParticleSize, const in vec3 vRayDir) {
    vec2 vNormDir = normalize(vRayDir.xy);
    float d1 = dot(vParticlePos.xy, vNormDir.xy) / length(vRayDir.xy);
    vec3 vClosest2d = vRayDir * d1;

    vec3 vClampedPos = vParticlePos;
    vClampedPos.z = clamp(vClosest2d.z, vParticlePos.z - fParticleLength, vParticlePos.z + fParticleLength);

    float d = dot(vClampedPos, vRayDir);
    vec3 vClosestPos = vRayDir * d;
    vec3 vDeltaPos = vClampedPos - vClosestPos;

    float fClosestDist = length(vDeltaPos) / fParticleSize;
    float fShade = clamp(1.0 - fClosestDist, 0.0, 1.0);

    if (d < 3.0) {
        fClosestDist = max(abs(vDeltaPos.x), abs(vDeltaPos.y)) / fParticleSize;
        float f = clamp(1.0 - 0.8 * fClosestDist, 0.0, 1.0);
        fShade += f * f * f * f;
        fShade *= fShade;
    }

    fShade = fShade * exp2(-d * fDepthFade) * fBrightness;
    return vec3(fShade);
}

vec3 GetParticlePos(const in vec3 vRayDir, const in float fZPos, const in float fSeed) {
    float fAngle = atan(vRayDir.x, vRayDir.y);
    float fAngleFraction = fract(fAngle / (3.14 * 2.0));

    float fSegment = floor(fAngleFraction * fSteps + fSeed) + 0.5 - fSeed;
    float fParticleAngle = fSegment / fSteps * (3.14 * 2.0);

    float fSegmentPos = fSegment / fSteps;
    float fRadius = fMinDist + Random(fSegmentPos + fSeed) * (fMaxDist - fMinDist);

    float tunnelZ = vRayDir.z / length(vRayDir.xy / fRadius);
    tunnelZ += fZPos;

    float fRepeat = fRepeatMin + Random(fSegmentPos + 0.1 + fSeed) * (fRepeatMax - fRepeatMin);
    float fParticleZ = (ceil(tunnelZ / fRepeat) - 0.5) * fRepeat - fZPos;

    return vec3(sin(fParticleAngle) * fRadius, cos(fParticleAngle) * fRadius, fParticleZ);
}

vec3 Starfield(const in vec3 vRayDir, const in float fZPos, const in float fSeed) {
    vec3 vParticlePos = GetParticlePos(vRayDir, fZPos, fSeed);
    return GetParticleColour(vParticlePos, fParticleSize, vRayDir);
}

vec3 RotateX(const in vec3 vPos, const in float fAngle) {
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3(vPos.x, c * vPos.y + s * vPos.z, -s * vPos.y + c * vPos.z);
}

vec3 RotateY(const in vec3 vPos, const in float fAngle) {
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3(c * vPos.x + s * vPos.z, vPos.y, -s * vPos.x + c * vPos.z);
}

vec3 RotateZ(const in vec3 vPos, const in float fAngle) {
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3(c * vPos.x + s * vPos.y, -s * vPos.x + c * vPos.y, vPos.z);
}

// --- Noise Functions ---
vec2 hash(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(in vec2 p) {
    const float K1 = 0.366025404;
    const float K2 = 0.211324865;

    vec2 i = floor(p + (p.x + p.y) * K1);
    vec2 a = p - i + (i.x + i.y) * K2;
    vec2 o = (a.x > a.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec2 b = a - o + K2;
    vec2 c = a - 1.0 + 2.0 * K2;

    vec3 h = max(0.5 - vec3(dot(a, a), dot(b, b), dot(c, c)), 0.0);
    vec3 n = h * h * h * h * vec3(dot(a, hash(i + 0.0)), dot(b, hash(i + o)), dot(c, hash(i + 1.0)));

    return dot(n, vec3(70.0));
}

const mat2 m = mat2(0.80, 0.60, -0.60, 0.80);

float fbm4(in vec2 p) {
    float f = 0.0;
    f += 0.5000 * noise(p); p = m * p * 2.02;
    f += 0.2500 * noise(p); p = m * p * 2.03;
    f += 0.1250 * noise(p); p = m * p * 2.01;
    f += 0.0625 * noise(p);
    return f;
}

float marble(in vec2 p) {
    return cos(p.x + fbm4(p));
}

float dowarp(in vec2 q, out vec2 a, out vec2 b) {
    float ang = 0.;
    ang = 1.2345 * sin(33.33);
    mat2 m1 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));
    ang = 0.2345 * sin(66.66);
    mat2 m2 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));

    a = vec2(marble(m1 * q), marble(m2 * q + vec2(1.12, 0.654)));

    ang = 0.543 * cos(13.33);
    m1 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));
    ang = 1.128 * cos(53.33);
    m2 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));

    b = vec2(marble(m2 * (q + a)), marble(m1 * (q + a)));

    return marble(q + b + vec2(0.32, 1.654));
}

// --- MAIN ---
void main() {
    // 1. Coordinates Setup (Aspect Ratio Fix)
    // Преобразуем UV (0..1) в центрированные координаты (-1..1) с учетом соотношения сторон
    vec2 q = (texCoord0 - 0.5) * 2.0;
    q.x *= size.x / size.y;

    // 2. Zoom handling
    // Чем больше zoom, тем меньше область видимости (эффект приближения)
    float scale = zoom / 16.0;
    // Защита от деления на ноль, если зум очень маленький
    if(scale < 0.001) scale = 0.001;

    // В оригинале координаты q.x, q.y напрямую влияют на ray direction.
    // Чтобы приблизить, делим q на масштаб.
    vec3 rd = normalize(vec3(q.x / scale, q.y / scale, 1.0));

    // 3. Camera Rotation based on Scroll (Parallax) & Time
    vec3 euler = vec3(
    sin(time * 0.2) * 0.625,
    cos(time * 0.1) * 0.625,
    time * 0.1 + sin(time * 0.3) * 0.5
    );

    // Добавляем влияние скроллинга на поворот камеры (имитация полета)
    if(scrollSize.x > 0.0 && scrollSize.y > 0.0) {
        float scrollXRatio = scrollOffset.x / scrollSize.x;
        float scrollYRatio = scrollOffset.y / scrollSize.y;

        // Маппим скролл на углы (-1.0 .. 1.0 * множитель)
        euler.x -= (scrollYRatio * 2.0 - 1.0) * 1.5; // Pitch
        euler.y -= (scrollXRatio * 2.0 - 1.0) * 1.5; // Yaw
    }

    rd = RotateX(rd, euler.x);
    rd = RotateY(rd, euler.y);
    rd = RotateZ(rd, euler.z);

    // 4. Nebulae Background
    float pi = 3.141592654;
    vec2 warp_uv = vec2(0.0);
    warp_uv.x = 0.5 + atan(rd.z, rd.x) / (2. * pi);
    warp_uv.y = 0.5 - asin(rd.y) / pi + 0.512 + 0.001 * time;
    warp_uv *= 2.34;

    vec2 wa = vec2(0.);
    vec2 wb = vec2(0.);
    float f = dowarp(warp_uv, wa, wb);
    f = 0.5 + 0.5 * f;

    vec3 col = vec3(f);
    float wc = f;
    col = vec3(wc, wc * wc, wc * wc * wc);
    wc = abs(wa.x);
    col -= vec3(wc * wc, wc, wc * wc * wc);
    wc = abs(wb.x);
    col += vec3(wc * wc * wc, wc * wc, wc);
    col *= 0.7;
    col.x = pow(col.x, 2.18);
    col.z = pow(col.z, 1.88);
    col = smoothstep(0., 1., col);
    col = 0.5 - (1.4 * col - 0.7) * (1.4 * col - 0.7);
    col = 0.75 * sqrt(col);
    col *= 1. - 0.5 * fbm4(8. * warp_uv);
    col = clamp(col, 0., 1.);

    // 5. StarField
    float fZPos = 5.0; // Можно добавить движение вперед: + time * 0.5;
    float fSpeed = 0.;

    // fParticleLength зависит от скорости, но здесь она 0, так что используем константу или минимум
    // Чтобы звезды не были точками при нулевой скорости, оставим базовое значение
    fParticleLength = 0.5 / 60.0;

    float fSeed = 0.0;
    vec3 vResult = vec3(0.);
    vec3 red = vec3(0.7, 0.4, 0.3);
    vec3 blue = vec3(0.3, 0.4, 0.7);
    vec3 tint = vec3(0.);

    float ti = 1.0 / float(PASS_COUNT - 1);
    float t = 0.;

    // Временная копия rd для цикла, чтобы не портить основной вектор
    vec3 starRd = rd;

    for(int i = 0; i < PASS_COUNT; i++) {
        tint = mix(red, blue, t);
        vResult += 1.1 * tint * Starfield(starRd, fZPos, fSeed);
        t += ti;
        fSeed += 1.234;
        starRd = RotateX(starRd, 0.25 * euler.x);
    }

    col += sqrt(vResult);

    // 6. Vignetting (Optional)
    // Используем исходные координаты q (aspect fixed, normalized)
    // Но для виньетки лучше взять сырые UV (0..1) преобразованные в (-1..1) без aspect fix для круглой виньетки по центру экрана
    vec2 r = (texCoord0 - 0.5) * 2.0;
    float vb = max(abs(r.x), abs(r.y));
    col *= (0.15 + 0.85 * (1.0 - exp(-(1.0 - vb) * 30.0)));

    fragColor = vec4(col, 1.0);
}