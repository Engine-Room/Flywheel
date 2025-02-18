#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"
#include "flywheel:internal/colorizer.glsl"

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

#define TRANSPARENCY_WAVELET_RANK 3
#define TRANSPARENCY_WAVELET_COEFFICIENT_COUNT 16
#define floatN float
#define all(e) (e)
#define mad fma
#define lerp mix
#define Coefficients_Out vec4[4]
#define Coefficients_In sampler2DArray

layout (binding = 7) uniform sampler2D _flw_depthRange;

layout (binding = 8) uniform sampler2DArray _flw_coefficients;

#define REMOVE_SIGNAL true

#ifdef _FLW_DEPTH_RANGE

layout (location = 0) out vec2 _flw_depthRange_out;

#endif


#ifdef _FLW_COLLECT_COEFFS


layout (location = 0) out vec4 _flw_coeffs0;
layout (location = 1) out vec4 _flw_coeffs1;
layout (location = 2) out vec4 _flw_coeffs2;
layout (location = 3) out vec4 _flw_coeffs3;

void add_to_index(inout Coefficients_Out coefficients, uint index, floatN addend) {
    coefficients[index >> 2][index & 3u] = addend;
}

void add_event_to_wavelets(inout Coefficients_Out coefficients, floatN signal, float depth)
{
    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    int index = clamp(int(floor(depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    index += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i)
    {
        int power = TRANSPARENCY_WAVELET_RANK - i;
        int new_index = (index - 1) >> 1;
        float k = float((new_index + 1) & ((1 << power) - 1));

        int wavelet_sign = ((index & 1) << 1) - 1;
        float wavelet_phase = ((index + 1) & 1) * exp2(-power);
        floatN addend = mad(mad(-exp2(-power), k, depth), wavelet_sign, wavelet_phase) * exp2(power * 0.5) * signal;
        add_to_index(coefficients, new_index, addend);

        index = new_index;
    }

        floatN addend = mad(signal, -depth, signal);
    add_to_index(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1, addend);
}

void add_transmittance_event_to_wavelets(inout Coefficients_Out coefficients, floatN transmittance, float depth)
{
    float absorbance = -log(max(transmittance, 0.00001));// transforming the signal from multiplicative transmittance to additive absorbance
    add_event_to_wavelets(coefficients, absorbance, depth);
}

#endif

#ifdef _FLW_EVALUATE

layout (location = 0) out vec4 _flw_accumulate;


floatN get_coefficients(in Coefficients_In coefficients, uint index) {
    return texelFetch(coefficients, ivec3(gl_FragCoord.xy, index >> 2), 0)[index & 3u];
}

    floatN evaluate_wavelets(in Coefficients_In coefficients, float depth, floatN signal)
{
    floatN scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (all(scale_coefficient == 0))
    {
        return 0;
    }
    if (REMOVE_SIGNAL)
    {
        floatN scale_coefficient_addend = mad(signal, -depth, signal);
        scale_coefficient -= scale_coefficient_addend;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index_b = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    bool sample_a = index_b >= 1;
    int index_a = sample_a ? (index_b - 1) : index_b;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;
    index_a += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    floatN b = scale_coefficient;
floatN a = sample_a ? scale_coefficient : 0;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i)
    {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        floatN coeff_b = get_coefficients(coefficients, new_index_b);
        if (REMOVE_SIGNAL)
        {
            float wavelet_phase_b = ((index_b + 1) & 1) * exp2(-power);
            float k = float((new_index_b + 1) & ((1 << power) - 1));
            floatN addend = mad(mad(-exp2(-power), k, depth), wavelet_sign_b, wavelet_phase_b) * exp2(power * 0.5) * signal;
            coeff_b -= addend;
        }
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a)
        {
            int new_index_a = (index_a - 1) >> 1;
            int wavelet_sign_a = ((index_a & 1) << 1) - 1;
            floatN coeff_a = (new_index_a == new_index_b) ? coeff_b : get_coefficients(coefficients, new_index_a);// No addend here on purpose, the original signal didn't contribute to this coefficient
            a -= exp2(float(power) * 0.5) * coeff_a * wavelet_sign_a;
            index_a = new_index_a;
        }
    }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);

    return lerp(a, b, t);
}

    floatN evaluate_transmittance_wavelets(in Coefficients_In coefficients, float depth, floatN signal)
{
    floatN absorbance = evaluate_wavelets(coefficients, depth, signal);
    return clamp(exp(-absorbance), 0., 1.);// undoing the transformation from absorbance back to transmittance
}

#endif

// TODO: blue noise texture
uint HilbertIndex(uvec2 p) {
    uint i = 0u;
    for (uint l = 0x4000u; l > 0u; l >>= 1u) {
        uvec2 r = min(p & l, 1u);

        i = (i << 2u) | ((r.x * 3u) ^ r.y);
        p = r.y == 0u ? (0x7FFFu * r.x) ^ p.yx : p;
    }
    return i;
}

uint ReverseBits(uint x) {
    x = ((x & 0xaaaaaaaau) >> 1) | ((x & 0x55555555u) << 1);
    x = ((x & 0xccccccccu) >> 2) | ((x & 0x33333333u) << 2);
    x = ((x & 0xf0f0f0f0u) >> 4) | ((x & 0x0f0f0f0fu) << 4);
    x = ((x & 0xff00ff00u) >> 8) | ((x & 0x00ff00ffu) << 8);
    return (x >> 16) | (x << 16);
}

// from: https://psychopath.io/post/2021_01_30_building_a_better_lk_hash
uint OwenHash(uint x, uint seed) { // seed is any random number
    x ^= x * 0x3d20adeau;
    x += seed;
    x *= (seed >> 16) | 1u;
    x ^= x * 0x05526c56u;
    x ^= x * 0x53a22864u;
    return x;
}

// https://www.shadertoy.com/view/ssBBW1
float blue() {
    uint m = HilbertIndex(uvec2(gl_FragCoord.xy));// map pixel coords to hilbert curve index
    m = OwenHash(ReverseBits(m), 0xe7843fbfu);// owen-scramble hilbert index
    m = OwenHash(ReverseBits(m), 0x8d8fb1e0u);// map hilbert index to sobol sequence and owen-scramble
    float mask = float(ReverseBits(m)) / 4294967296.0;// convert to float

    return mask;
}

uniform vec3 _flw_depthAdjust;

float adjust_depth(float normalizedDepth) {

    float tentIn = abs(normalizedDepth * 2. - 1);
    float tentIn2 = tentIn * tentIn;
    float tentIn4 = tentIn2 * tentIn2;
    float tent = 1 - (tentIn2 * tentIn4);

    float b = blue();

    return normalizedDepth - b * tent * 0.08;
}

float linearize_depth(float d, float zNear, float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}

float linear_depth() {
    return linearize_depth(gl_FragCoord.z, _flw_cullData.znear, _flw_cullData.zfar);
}

float depth() {
    float linearDepth = linear_depth();

    vec2 depthRange = texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0).rg;
    float depth = (linearDepth + depthRange.x) / (depthRange.x + depthRange.y);

    return adjust_depth(depth);
}


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

    #ifdef _FLW_DEPTH_RANGE
    float linearDepth = linear_depth();

    // Pad the depth by some unbalanced epsilons because minecraft has a lot of single-quad tranparency.
    // The unbalance means our fragment will be considered closer to the screen in the normalization,
    // which helps prevent unnecessary noise as it'll be closer to the edge of our tent function.
    _flw_depthRange_out = vec2(-linearDepth + 1e-5, linearDepth + 1e-2);
    #endif

    #ifdef _FLW_COLLECT_COEFFS

    Coefficients_Out result;
    result[0] = vec4(0.);
    result[1] = vec4(0.);
    result[2] = vec4(0.);
    result[3] = vec4(0.);

    add_transmittance_event_to_wavelets(result, 1. - color.a, depth());

    _flw_coeffs0 = result[0];
    _flw_coeffs1 = result[1];
    _flw_coeffs2 = result[2];
    _flw_coeffs3 = result[3];

    #endif

    #ifdef _FLW_EVALUATE

    floatN transmittance = evaluate_transmittance_wavelets(_flw_coefficients, depth(), 1. - color.a);

    _flw_accumulate = vec4(color.rgb * color.a, color.a) * transmittance;

    #endif

    #else

    _flw_outputColor = color;

    #endif
}
