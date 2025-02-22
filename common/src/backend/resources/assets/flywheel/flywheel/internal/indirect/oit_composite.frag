layout (location = 0) out vec4 frag;

layout (binding = 0) uniform sampler2DArray _flw_coefficients;
layout (binding = 1) uniform sampler2D _flw_accumulate;

#define TRANSPARENCY_WAVELET_RANK 3
#define TRANSPARENCY_WAVELET_COEFFICIENT_COUNT 16

float get_coefficients(in sampler2DArray coefficients, uint index) {
    return texelFetch(coefficients, ivec3(gl_FragCoord.xy, index >> 2), 0)[index & 3u];
}

float evaluate_wavelets(in sampler2DArray coefficients, float depth)
{
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0)
    {
        return 0;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index_b = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    bool sample_a = index_b >= 1;
    int index_a = sample_a ? (index_b - 1) : index_b;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;
    index_a += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;
    float a = sample_a ? scale_coefficient : 0;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i)
    {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a)
        {
            int new_index_a = (index_a - 1) >> 1;
            int wavelet_sign_a = ((index_a & 1) << 1) - 1;
            float coeff_a = (new_index_a == new_index_b) ? coeff_b : get_coefficients(coefficients, new_index_a);
            a -= exp2(float(power) * 0.5) * coeff_a * wavelet_sign_a;
            index_a = new_index_a;
        }
    }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);

    return mix(a, b, t);
}

float evaluate_transmittance_wavelets(in sampler2DArray coefficients, float depth)
{
    float absorbance = evaluate_wavelets(coefficients, depth);
    return clamp(exp(-absorbance), 0., 1.);// undoing the transformation from absorbance back to transmittance
}

const float infinity = 1. / 0.;

void main() {
    vec4 texel = texelFetch(_flw_accumulate, ivec2(gl_FragCoord.xy), 0);

    if (texel.a < 1e-5) {
        discard;
    }

    float total_transmittance = evaluate_transmittance_wavelets(_flw_coefficients, infinity);

    frag = vec4(texel.rgb / texel.a, total_transmittance);
}
