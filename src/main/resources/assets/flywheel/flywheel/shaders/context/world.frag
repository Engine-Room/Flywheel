#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform vec2 uFogRange;
uniform vec4 uFogColor;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

in float _flw_diffuse;

out vec4 fragColor;

void flw_contextFragment() {
    vec4 texColor = texture(uBlockAtlas, flw_vertexTexCoord);
    vec4 lightColor = texture(uLightMap, flw_vertexLight);
    vec4 color = flw_vertexColor * vec4(texColor.rgb * lightColor.rgb * _flw_diffuse, texColor.a);

    #ifdef ALPHA_DISCARD
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    #ifdef COLOR_FOG
    color = linear_fog(color, flw_distance, uFogRange.x, uFogRange.y, uFogColor);
    #elif defined(FADE_FOG)
    color = linear_fog_fade(color, flw_distance, uFogRange.x, uFogRange.y);
    #endif

    fragColor = color;
}
