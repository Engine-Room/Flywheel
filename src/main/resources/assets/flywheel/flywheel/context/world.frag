#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"
#use "flywheel:uniform/fog.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

out vec4 fragColor;

void flw_contextFragment() {
    vec4 texColor = texture(uBlockAtlas, flw_vertexTexCoord);
    vec4 lightColor = texture(uLightMap, flw_vertexLight);
    vec4 color = flw_vertexColor * vec4(texColor.rgb * lightColor.rgb, texColor.a);

    #ifdef ALPHA_DISCARD
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    #ifdef COLOR_FOG
    color = linear_fog(color, flw_distance, flw_fogRange.x, flw_fogRange.y, flw_fogColor);
    #elif defined(FADE_FOG)
    color = linear_fog_fade(color, flw_distance, flw_fogRange.x, flw_fogRange.y);
    #endif

    fragColor = color;
}
