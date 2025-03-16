#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"
#include "flywheel:internal/colorizer.glsl"
#include "flywheel:internal/wavelet.glsl"
#include "flywheel:internal/depth.glsl"

// optimize discard usage
#if defined(GL_ARB_conservative_depth) && defined(_FLW_USE_DISCARD)
layout (depth_greater) out float gl_FragDepth;
#endif

#ifdef _FLW_CRUMBLING
uniform sampler2D _flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;
#endif

#ifdef _FLW_DEBUG
flat in uvec2 _flw_ids;
#endif

#ifdef _FLW_OIT

uniform sampler2D _flw_depthRange;

uniform sampler2DArray _flw_coefficients;

uniform sampler2D _flw_blueNoise;

float tented_blue_noise(float normalizedDepth) {

    float tentIn = abs(normalizedDepth * 2. - 1);
    float tentIn2 = tentIn * tentIn;
    float tentIn4 = tentIn2 * tentIn2;
    float tent = 1 - (tentIn2 * tentIn4);

    float b = texture(_flw_blueNoise, gl_FragCoord.xy / vec2(64)).r;

    return b * tent;
}

float linear_depth() {
    return linearize_depth(gl_FragCoord.z, _flw_cullData.znear, _flw_cullData.zfar);
}

#ifdef _FLW_DEPTH_RANGE

out vec2 _flw_depthRange_out;

#endif

#ifdef _FLW_COLLECT_COEFFS

out vec4 _flw_coeffs0;
out vec4 _flw_coeffs1;
out vec4 _flw_coeffs2;
out vec4 _flw_coeffs3;

#endif

#ifdef _FLW_EVALUATE

out vec4 _flw_accumulate;

#endif

#else

out vec4 _flw_outputColor;

#endif

float _flw_diffuseFactor() {
    if (flw_material.cardinalLightingMode == 2u) {
        return diffuseFromLightDirections(flw_vertexNormal);
    } else if (flw_material.cardinalLightingMode == 1u) {
        if (flw_constantAmbientLight == 1u) {
            return diffuseNether(flw_vertexNormal);
        } else {
            return diffuse(flw_vertexNormal);
        }
    } else {
        return 1.;
    }
}

void _flw_main() {
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    flw_materialFragment();

    #ifdef _FLW_CRUMBLING
    vec4 crumblingSampleColor = texture(_flw_crumblingTex, _flw_crumblingTexCoord);

    // Make the crumbling overlay transparent when the fragment color after the material shader is transparent.
    flw_fragColor.rgb = crumblingSampleColor.rgb;
    flw_fragColor.a *= crumblingSampleColor.a;
    #endif

    flw_shaderLight();

    vec4 color = flw_fragColor;

    #ifdef _FLW_USE_DISCARD
    if (flw_discardPredicate(color)) {
        discard;
    }
    #endif

    float diffuseFactor = _flw_diffuseFactor();
    color.rgb *= diffuseFactor;

    if (flw_material.useOverlay) {
        vec4 overlayColor = texelFetch(flw_overlayTex, flw_fragOverlay, 0);
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    }

    vec4 lightColor = vec4(1.);
    if (flw_material.useLight) {
        lightColor = texture(flw_lightTex, clamp(flw_fragLight, 0.5 / 16.0, 15.5 / 16.0));
        color *= lightColor;
    }

    #ifdef _FLW_DEBUG
    switch (_flw_debugMode) {
        case 1u:
        color = vec4(flw_vertexNormal * .5 + .5, 1.);
        break;
        case 2u:
        color = _flw_id2Color(_flw_ids.x);
        break;
        case 3u:
        color = vec4(vec2((flw_fragLight * 15.0 + 0.5) / 16.), 0., 1.);
        break;
        case 4u:
        color = lightColor;
        break;
        case 5u:
        color = vec4(flw_fragOverlay / 16., 0., 1.);
        break;
        case 6u:
        color = vec4(vec3(diffuseFactor), 1.);
        break;
        case 7u:
        color = _flw_id2Color(_flw_ids.y);
        break;
    }
    #endif

    color = flw_fogFilter(color);

    #ifdef _FLW_OIT

    float linearDepth = linear_depth();
    #ifdef _FLW_DEPTH_RANGE

    // Pad the depth by some unbalanced epsilons because minecraft has a lot of single-quad tranparency.
    // The unbalance means our fragment will be considered closer to the screen in the normalization,
    // which helps prevent unnecessary noise as it'll be closer to the edge of our tent function.
    _flw_depthRange_out = vec2(-linearDepth + 1e-5, linearDepth + 1e-2);
    #else
    // This section is common to both other passes.

    vec2 depthRange = texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0).rg;
    float delta = depthRange.x + depthRange.y;
    float our_depth = (linearDepth + depthRange.x) / delta;

    float depth_adjustment = tented_blue_noise(our_depth) * _flw_oitNoise;

    float our_transmittance = 1. - color.a;
    // Don't do the depth adjustment if this fragment is opaque.
    if (our_transmittance > 1e-5) {
        our_depth -= depth_adjustment;
    }
    #endif

    #ifdef _FLW_COLLECT_COEFFS

    vec4[4] result;
    result[0] = vec4(0.);
    result[1] = vec4(0.);
    result[2] = vec4(0.);
    result[3] = vec4(0.);

    add_transmittance(result, our_transmittance, our_depth);

    _flw_coeffs0 = result[0];
    _flw_coeffs1 = result[1];
    _flw_coeffs2 = result[2];
    _flw_coeffs3 = result[3];

    #endif

    #ifdef _FLW_EVALUATE

    float transmittance = signal_corrected_transmittance(_flw_coefficients, our_depth, our_transmittance);

    _flw_accumulate = vec4(color.rgb * color.a, color.a) * transmittance;

    #endif

    #else

    _flw_outputColor = color;

    #endif
}
