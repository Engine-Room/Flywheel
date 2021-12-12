#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

uniform vec2 uTextureScale;
uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

uniform vec2 uWindowSize;

void FLWFinalizeNormal(inout vec3 normal) {
    // noop
}

#if defined(VERTEX_SHADER)
void FLWFinalizeWorldPos(inout vec4 worldPos) {
    FragDistance = cylindrical_distance(worldPos.xyz, uCameraPos);

    gl_Position = uViewProjection * worldPos;
}

#elif defined(FRAGMENT_SHADER)
#use "flywheel:core/lightutil.glsl"

#define ALPHA_DISCARD 0.1
// optimize discard usage
#if defined(ALPHA_DISCARD)
#if defined(GL_ARB_conservative_depth)
#extension GL_ARB_conservative_depth : enable
layout (depth_greater) out float gl_FragDepth;
#endif
#endif
out vec4 fragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture(uBlockAtlas, texCoords);
}

void FLWFinalizeColor(vec4 color) {
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;

    #if defined(ALPHA_DISCARD)
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    fragColor = color;
}

vec4 FLWLight(vec2 lightCoords) {
    return texture(uLightMap, shiftLight(lightCoords));
}
#endif
