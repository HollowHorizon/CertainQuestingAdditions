#version 330
#define parallaxIntensity 5.0

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;
uniform vec2 scrollOffset;
uniform vec2 scrollSize;
uniform float time;
uniform float zoom;

in vec2 texCoord0;
out vec4 fragColor;

float hash(vec2 p){ return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453123); }
float noise(vec2 p){
    vec2 i=floor(p);
    vec2 f=fract(p);
    float a=hash(i);
    float b=hash(i+vec2(1.,0.));
    float c=hash(i+vec2(0.,1.));
    float d=hash(i+vec2(1.,1.));
    vec2 u=f*f*(3.-2.*f);
    return mix(mix(a,b,u.x),mix(c,d,u.x),u.y);
}
float fbm(vec2 p){
    float v=0.;
    float a=0.5;
    for(int i=0;i<5;i++){
        v+=a*noise(p);
        p*=2.;
        a*=0.5;
    }
    return v;
}

void main(){
    vec2 fragCoord = texCoord0 * size;
    vec2 center = size * 0.5;
    float minDim = min(size.x, size.y);
    float safeZoom = max(zoom / 16.0, 0.0001);
    float invZoom = 1.0 / safeZoom;

    vec2 p = (fragCoord - center) / minDim * invZoom;
    vec2 scrollPos = scrollOffset / max(scrollSize, vec2(1.0));
    p += scrollPos * 0.05 * parallaxIntensity;
    p.y += time * 0.1;

    float f = fbm(p * 2.0 - vec2(0.0, time * 0.2));
    vec3 col = vec3(1.2 * f, 0.3 * f * f, 0.05);
    fragColor = vec4(col, 1.0);
}
