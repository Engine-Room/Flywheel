#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;
uniform int uConstantAmbientLight;

uniform vec2 uTextureScale;
uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;
uniform sampler2D uCrumbling;

uniform vec2 uWindowSize;

#ifdef VERTEX_SHADER

#use "flywheel:context/diffuse.glsl"

vec4 FLWVertex(inout Vertex v) {
    fragDistance = fog_distance(v.pos, uCameraPos);

    return uViewProjection * vec4(v.pos, 1.);
}

float FLWDiffuse(vec3 normal) {
    if (uConstantAmbientLight == 1) {
        return diffuseNether(normal);
    } else {
        return diffuse(normal);
    }
}

#elif defined(FRAGMENT_SHADER)

out vec4 fragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    vec4 cr = texture(uCrumbling, texCoords * uTextureScale);
    float diffuseAlpha = texture(uBlockAtlas, texCoords).a;
    cr.a = cr.a * diffuseAlpha;
    return cr;
}

vec4 FLWLight(vec2 lightCoords) {
    return vec4(1.);
}

void FLWFinalizeColor(vec4 color) {
    #ifdef ALPHA_DISCARD
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    #ifdef COLOR_FOG
    color = linear_fog(color);
    #elif defined(FADE_FOG)
    color = linear_fog_fade(color);
    #endif

    fragColor = color;
}

#endif
