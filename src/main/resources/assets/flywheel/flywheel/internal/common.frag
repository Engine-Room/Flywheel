#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"

// optimize discard usage
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif

uniform sampler2D _flw_diffuseTex;
uniform sampler2D _flw_overlayTex;
uniform sampler2D _flw_lightTex;

out vec4 _flw_outputColor;

in vec4 _flw_debugColor;

void _flw_main() {
    flw_sampleColor = texture(_flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    flw_beginFragment();
    flw_materialFragment();
    flw_endFragment();

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
        vec4 overlayColor = texelFetch(_flw_overlayTex, flw_fragOverlay, 0);
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    }

    if (flw_material.useLight) {
        vec4 lightColor = texture(_flw_lightTex, (flw_fragLight * 15.0 + 0.5) / 16.0);
        color *= lightColor;
    }

    if (flw_discardPredicate(color)) {
        discard;
    }

    switch (_flw_debugOverlay) {
        case 1u:
            color *= _flw_debugColor;
            break;
        case 2u:
            color = _flw_debugColor;
            break;
    }

    _flw_outputColor = flw_fogFilter(color);
}
