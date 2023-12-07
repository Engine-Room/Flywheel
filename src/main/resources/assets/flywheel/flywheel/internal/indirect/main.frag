#include "flywheel:internal/packed_material.glsl"

// optimize discard usage
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif

uniform sampler2D _flw_diffuseTex;
uniform sampler2D _flw_overlayTex;
uniform sampler2D _flw_lightTex;

flat in uvec3 _flw_packedMaterial;

out vec4 _flw_outputColor;

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.x;
    _flw_unpackUint2x16(_flw_packedMaterial.y, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.z, flw_material);

    flw_sampleColor = texture(_flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    flw_beginFragment();
    flw_materialFragment();
    flw_endFragment();

    vec4 color = flw_fragColor;

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

    _flw_outputColor = flw_fogFilter(color);
}
