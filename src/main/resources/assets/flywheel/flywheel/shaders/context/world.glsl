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
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif

    gl_Position = uViewProjection * worldPos;
}

#elif defined(FRAGMENT_SHADER)
#use "flywheel:core/lightutil.glsl"

#define ALPHA_DISCARD 0.1
//
//#if defined(ALPHA_DISCARD)
//#if defined(GL_ARB_conservative_depth)
//layout (depth_greater) out float gl_FragDepth;
//#endif
//#endif

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture2D(uBlockAtlas, texCoords);
}

void FLWFinalizeColor(vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif

    #if defined(ALPHA_DISCARD)
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    gl_FragColor = color;
}

vec4 FLWLight(vec2 lightCoords) {
    return texture2D(uLightMap, shiftLight(lightCoords));
}
#endif
