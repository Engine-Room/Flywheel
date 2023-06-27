#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

uniform vec2 uWindowSize;

#if defined(VERTEX_SHADER)

vec4 FLWVertex(inout Vertex v) {
    FragDistance = cylindrical_distance(v.pos, uCameraPos);

    return uViewProjection * vec4(v.pos, 1.);
}

#elif defined(FRAGMENT_SHADER)
#use "flywheel:core/lightutil.glsl"
// optimize discard usage
#if defined(ALPHA_DISCARD)
#if defined(GL_ARB_conservative_depth)
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
