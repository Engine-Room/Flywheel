#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"

// optimize discard usage
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif

#ifdef _FLW_CRUMBLING
uniform sampler2D _flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;
#endif

#ifdef _FLW_EMBEDDED
uniform sampler3D _flw_lightVolume;

in vec3 _flw_lightVolumeCoord;

#endif

in vec4 _flw_debugColor;

out vec4 _flw_outputColor;

void _flw_main() {
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    #ifdef _FLW_EMBEDDED
    flw_fragLight = max(flw_fragLight, texture(_flw_lightVolume, _flw_lightVolumeCoord).rg);
    #endif

    flw_materialFragment();

    #ifdef _FLW_CRUMBLING
    vec4 crumblingSampleColor = texture(_flw_crumblingTex, _flw_crumblingTexCoord);

    // Make the crumbling overlay transparent when the fragment color after the material shader is transparent.
    flw_fragColor.rgb = crumblingSampleColor.rgb;
    flw_fragColor.a *= crumblingSampleColor.a;
    #endif

    vec4 color = flw_fragColor;

    if (flw_material.diffuse) {
        float diffuseFactor;
        if (flw_constantAmbientLight == 1u) {
            diffuseFactor = diffuseNether(flw_vertexNormal);
        } else {
            diffuseFactor = diffuse(flw_vertexNormal);
        }
        color.rgb *= diffuseFactor;
    }

    if (flw_material.useOverlay) {
        vec4 overlayColor = texelFetch(flw_overlayTex, flw_fragOverlay, 0);
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    }

    if (flw_material.useLight) {
        vec4 lightColor = texture(flw_lightTex, clamp(flw_fragLight, 0.5 / 16.0, 15.5 / 16.0));
        color *= lightColor;
    }

    if (flw_discardPredicate(color)) {
        discard;
    }

    if (_flw_debugMode != 0u) {
        color = _flw_debugColor;
    }

    _flw_outputColor = flw_fogFilter(color);
}
