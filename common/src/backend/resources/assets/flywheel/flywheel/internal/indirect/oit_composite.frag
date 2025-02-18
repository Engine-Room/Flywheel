layout (location = 0) out vec4 frag;

layout (binding = 0) uniform sampler2DArray _flw_coefficients;
layout (binding = 1) uniform sampler2D _flw_accumulate;

#define TRANSPARENCY_WAVELET_RANK 3
#define TRANSPARENCY_WAVELET_COEFFICIENT_COUNT 16
#define floatN float
#define all(e) (e)
#define mad fma
#define lerp mix
#define Coefficients_Out vec4[4]
#define Coefficients_In sampler2DArray


floatN get_coefficients(in Coefficients_In coefficients, uint index) {
    return texelFetch(coefficients, ivec3(gl_FragCoord.xy, index >> 2), 0)[index & 3u];
}

    floatN evaluate_wavelet_index(in Coefficients_In coefficients, int index)
{
    floatN result = 0;

    index += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i)
    {
        int power = TRANSPARENCY_WAVELET_RANK - i;
        int new_index = (index - 1) >> 1;
        floatN coeff = get_coefficients(coefficients, new_index);
        int wavelet_sign = ((index & 1) << 1) - 1;
        result -= exp2(float(power) * 0.5) * coeff * wavelet_sign;
        index = new_index;
    }
    return result;
}


    floatN evaluate_wavelets(in Coefficients_In coefficients, float depth)
{
    floatN scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (all(scale_coefficient == 0))
    {
        return 0;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);

    floatN a = 0;
floatN b = scale_coefficient + evaluate_wavelet_index(coefficients, index);
    if (index > 0) { a = scale_coefficient + evaluate_wavelet_index(coefficients, index - 1); }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);
    floatN signal = lerp(a, b, t);// You can experiment here with different types of interpolation as well
    return signal;
}

    floatN evaluate_transmittance_wavelets(in Coefficients_In coefficients, float depth)
{
    floatN absorbance = evaluate_wavelets(coefficients, depth);
    return clamp(exp(-absorbance), 0., 1.);// undoing the transformation from absorbance back to transmittance
}

const float infinity = 1. / 0.;

void main() {
    vec4 texel = texelFetch(_flw_accumulate, ivec2(gl_FragCoord.xy), 0);

    if (texel.a < 1e-5) {
        discard;
    }

        floatN total_transmittance = evaluate_transmittance_wavelets(_flw_coefficients, infinity);

    frag = vec4(texel.rgb / texel.a, total_transmittance);
}
