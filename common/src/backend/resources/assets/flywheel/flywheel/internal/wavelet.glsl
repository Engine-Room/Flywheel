#define TRANSPARENCY_WAVELET_RANK 3
#define TRANSPARENCY_WAVELET_COEFFICIENT_COUNT 16

// -------------------------------------------------------------------------
// WRITING
// -------------------------------------------------------------------------

void add_to_index(inout vec4[4] coefficients, int index, float addend) {
    coefficients[index >> 2][index & 3] = addend;
}

void add_absorbance(inout vec4[4] coefficients, float signal, float depth) {
    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    int index = clamp(int(floor(depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    index += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;
        int new_index = (index - 1) >> 1;
        float k = float((new_index + 1) & ((1 << power) - 1));

        int wavelet_sign = ((index & 1) << 1) - 1;
        float wavelet_phase = ((index + 1) & 1) * exp2(-power);
        float addend = fma(fma(-exp2(-power), k, depth), wavelet_sign, wavelet_phase) * exp2(power * 0.5) * signal;
        add_to_index(coefficients, new_index, addend);

        index = new_index;
    }

    float addend = fma(signal, -depth, signal);
    add_to_index(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1, addend);
}

void add_transmittance(inout vec4[4] coefficients, float transmittance, float depth) {
    float absorbance = -log(max(transmittance, 0.00001));// transforming the signal from multiplicative transmittance to additive absorbance
    add_absorbance(coefficients, absorbance, depth);
}

// -------------------------------------------------------------------------
// READING
// -------------------------------------------------------------------------

// TODO: maybe we could reduce the number of texel fetches below?
float get_coefficients(in sampler2DArray coefficients, int index) {
    return texelFetch(coefficients, ivec3(gl_FragCoord.xy, index >> 2), 0)[index & 3];
}

/// Compute the total absorbance, as if at infinite depth.
float total_absorbance(in sampler2DArray coefficients) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
        return 0;
    }

    int index_b = TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;
    }

    return b;
}

/// Compute the absorbance at a given normalized depth.
float absorbance(in sampler2DArray coefficients, float depth) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
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

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a) {
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

/// Compute the absorbance at a given normalized depth,
/// correcting for self-occlusion by undoing the previously recorded absorbance event.
float signal_corrected_absorbance(in sampler2DArray coefficients, float depth, float signal) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
        return 0;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float scale_coefficient_addend = fma(signal, -depth, signal);
    scale_coefficient -= scale_coefficient_addend;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index_b = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    bool sample_a = index_b >= 1;
    int index_a = sample_a ? (index_b - 1) : index_b;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;
    index_a += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;
    float a = sample_a ? scale_coefficient : 0;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);

        float wavelet_phase_b = ((index_b + 1) & 1) * exp2(-power);
        float k = float((new_index_b + 1) & ((1 << power) - 1));
        float addend = fma(fma(-exp2(-power), k, depth), wavelet_sign_b, wavelet_phase_b) * exp2(power * 0.5) * signal;
        coeff_b -= addend;

        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a) {
            int new_index_a = (index_a - 1) >> 1;
            int wavelet_sign_a = ((index_a & 1) << 1) - 1;
            float coeff_a = (new_index_a == new_index_b) ? coeff_b : get_coefficients(coefficients, new_index_a);// No addend here on purpose, the original signal didn't contribute to this coefficient
            a -= exp2(float(power) * 0.5) * coeff_a * wavelet_sign_a;
            index_a = new_index_a;
        }
    }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);

    return mix(a, b, t);
}

// Helpers below to deal directly in transmittance.

#define ABSORBANCE_TO_TRANSMITTANCE(a) clamp(exp(-(a)), 0., 1.)

float total_transmittance(in sampler2DArray coefficients) {
    return ABSORBANCE_TO_TRANSMITTANCE(total_absorbance(coefficients));
}

float transmittance(in sampler2DArray coefficients, float depth) {
    return ABSORBANCE_TO_TRANSMITTANCE(absorbance(coefficients, depth));
}

float signal_corrected_transmittance(in sampler2DArray coefficients, float depth, float signal) {
    return ABSORBANCE_TO_TRANSMITTANCE(signal_corrected_absorbance(coefficients, depth, signal));
}
