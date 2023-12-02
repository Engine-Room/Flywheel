#include "flywheel:internal/indirect/api/fragment.glsl"
#include "flywheel:internal/material.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform sampler2D flw_diffuseTex;
uniform sampler2D flw_overlayTex;
uniform sampler2D flw_lightTex;

flat in uvec3 _flw_material;

out vec4 fragColor;

void main() {
    _flw_materialFragmentID = _flw_material.x;

    _flw_unpackUint2x16(_flw_material.y, _flw_cutoutID, _flw_fogID);
    _flw_unpackMaterial(_flw_material.z, flw_material);

    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    flw_beginFragment();
    flw_materialFragment();
    flw_endFragment();

    vec4 overlayColor = texelFetch(flw_overlayTex, flw_fragOverlay, 0);
    vec4 lightColor = texture(flw_lightTex, (flw_fragLight * 15.0 + 0.5) / 16.0);

    vec4 color = flw_fragColor;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);

    if (flw_material.lighting) {
        color *= lightColor;
    }

    if (flw_material.lighting) {
        color *= lightColor;
    }

    if (flw_discardPredicate(color)) {
        discard;
    }

    fragColor = flw_fogFilter(color);
}
