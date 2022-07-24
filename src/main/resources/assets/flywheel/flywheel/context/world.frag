#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"
#use "flywheel:uniform/fog.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform sampler2D flw_diffuseTex;
uniform sampler2D flw_overlayTex;
uniform sampler2D flw_lightTex;

out vec4 fragColor;

void flw_initFragment() {
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;
}

void flw_contextFragment() {
    vec4 overlayColor = texelFetch(flw_overlayTex, flw_fragOverlay, 0);
    vec4 lightColor = texture(flw_lightTex, (flw_fragLight * 15.0 + 0.5) / 16.0);

    vec4 color = flw_fragColor;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightColor;

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
