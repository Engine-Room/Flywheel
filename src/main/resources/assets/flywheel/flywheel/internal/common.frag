#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"

// optimize discard usage
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif

out vec4 _flw_outputColor;

in vec4 _flw_debugColor;

void _flw_main() {
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
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
        // Need to clamp the overlay texture coords to sane coordinates because integer vertex attributes explode on
        // some drivers for some draw calls. This should only effect instances that don't write to overlay, but
        // the internal vertex format is unfortunately subject to these issues.
        vec4 overlayColor = texelFetch(flw_overlayTex, clamp(flw_fragOverlay, 0, 10), 0);
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    }

    if (flw_material.useLight) {
        vec4 lightColor = texture(flw_lightTex, (flw_fragLight * 15.0 + 0.5) / 16.0);
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
