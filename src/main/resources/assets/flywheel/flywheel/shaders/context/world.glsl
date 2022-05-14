#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;
uniform int uConstantAmbientLight;

uniform vec2 uTextureScale;
uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

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

#use "flywheel:core/lightutil.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif
out vec4 fragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture(uBlockAtlas, texCoords);
}

vec4 FLWLight(vec2 lightCoords) {
    return texture(uLightMap, shiftLight(lightCoords));
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
