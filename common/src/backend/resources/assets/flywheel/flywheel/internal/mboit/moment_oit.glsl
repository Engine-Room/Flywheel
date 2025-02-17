/*! \file
	This header provides the functionality to create the vectors of moments and
	to blend surfaces together with an appropriately reconstructed
	transmittance. It is needed for both additive passes of moment-based OIT.
*/

//cbuffer MomentOIT
//{
//	struct {
//		vec4 wrapping_zone_parameters;
//		float overestimation;
//		float moment_bias;
//	}MomentOIT;
//};

#include "flywheel:internal/mboit/moment_math.glsl"

const float moment_bias = 0.25;
const float overestimation = 0.25;
const vec4 wrapping_zone_parameters = vec4(0.);


void clip(float a) {
    if (a < 0.) {
        discard;
    }
}

// jozu: The trigonometric moments and higher order power moments rely on a second render target
//  which the java side is not set up to support. Trying to enable them as is will cause compile errors also.
#define NUM_MOMENTS 4

#define SINGLE_PRECISION 1

#ifdef _FLW_GENERATE_MOMENTS
/*! Generation of moments in case that rasterizer ordered views are used.
	This includes the case if moments are stored in 16 bits. */

/*! This functions relies on fixed function additive blending to compute the
	vector of moments.moment vector. The shader that calls this function must
	provide the required render targets.*/
#if NUM_MOMENTS == 4
void generateMoments(float depth, float transmittance, vec4 wrapping_zone_parameters, out float b_0, out vec4 b)
#elif NUM_MOMENTS == 6
#if USE_R_RG_RBBA_FOR_MBOIT6
void generateMoments(float depth, float transmittance, vec4 wrapping_zone_parameters, out float b_0, out vec2 b_12, out vec4 b_3456)
#else
void generateMoments(float depth, float transmittance, vec4 wrapping_zone_parameters, out float b_0, out vec2 b_12, out vec2 b_34, out vec2 b_56)
#endif
#elif NUM_MOMENTS == 8
void generateMoments(float depth, float transmittance, vec4 wrapping_zone_parameters, out float b_0, out vec4 b_even, out vec4 b_odd)
#endif
{
    transmittance = max(transmittance, 0.000001);
    float absorbance = -log(transmittance);

    b_0 = absorbance;
    #if TRIGONOMETRIC
    float phase = fma(depth, wrapping_zone_parameters.y, wrapping_zone_parameters.y);
    vec2 circle_point = vec2(sin(phas), cos(phase));

    vec2 circle_point_pow2 = Multiply(circle_point, circle_point);
    #if NUM_MOMENTS == 4
    b = vec4(circle_point, circle_point_pow2) * absorbance;
    #elif NUM_MOMENTS == 6
    b_12 = circle_point * absorbance;
    #if USE_R_RG_RBBA_FOR_MBOIT6
    b_3456 = vec4(circle_point_pow2, Multiply(circle_point, circle_point_pow2)) * absorbance;
    #else
    b_34 = circle_point_pow2 * absorbance;
    b_56 = Multiply(circle_point, circle_point_pow2) * absorbance;
    #endif
    #elif NUM_MOMENTS == 8
    b_even = vec4(circle_point_pow2, Multiply(circle_point_pow2, circle_point_pow2)) * absorbance;
    b_odd = vec4(circle_point, Multiply(circle_point, circle_point_pow2)) * absorbance;
    #endif
    #else
    float depth_pow2 = depth * depth;
    float depth_pow4 = depth_pow2 * depth_pow2;
    #if NUM_MOMENTS == 4
    b = vec4(depth, depth_pow2, depth_pow2 * depth, depth_pow4) * absorbance;
    #elif NUM_MOMENTS == 6
    b_12 = vec2(depth, depth_pow2) * absorbance;
    #if USE_R_RG_RBBA_FOR_MBOIT6
    b_3456 = vec4(depth_pow2 * depth, depth_pow4, depth_pow4 * depth, depth_pow4 * depth_pow2) * absorbance;
    #else
    b_34 = vec2(depth_pow2 * depth, depth_pow4) * absorbance;
    b_56 = vec2(depth_pow4 * depth, depth_pow4 * depth_pow2) * absorbance;
    #endif
    #elif NUM_MOMENTS == 8
    float depth_pow6 = depth_pow4 * depth_pow2;
    b_even = vec4(depth_pow2, depth_pow4, depth_pow6, depth_pow6 * depth_pow2) * absorbance;
    b_odd = vec4(depth, depth_pow2 * depth, depth_pow4 * depth, depth_pow6 * depth) * absorbance;
    #endif
    #endif
}

#else//MOMENT_GENERATION is disabled

layout (binding = 7) uniform sampler2D _flw_zeroth_moment_sampler;
layout (binding = 8) uniform sampler2D _flw_moments_sampler;
#if USE_R_RG_RBBA_FOR_MBOIT6
uniform sampler2D extra_moments;
#endif

/*! This function is to be called from the shader that composites the
	transparent fragments. It reads the moments and calls the appropriate
	function to reconstruct the transmittance at the specified depth.*/
void resolveMoments(out float transmittance_at_depth, out float total_transmittance, float depth, vec2 sv_pos)
{
    ivec2 idx0 = ivec2(sv_pos);
    ivec2 idx1 = idx0;

    transmittance_at_depth = 1;
    total_transmittance = 1;

    float b_0 = texelFetch(_flw_zeroth_moment_sampler, idx0, 0).x;
    clip(b_0 - 0.00100050033f);
    total_transmittance = exp(-b_0);

    #if NUM_MOMENTS == 4
    #if TRIGONOMETRIC
    vec4 b_tmp = texelFetch(_flw_moments_sampler, idx0, 0);
    vec2 trig_b[2];
    trig_b[0] = b_tmp.xy;
    trig_b[1] = b_tmp.zw;
    #if SINGLE_PRECISION
    trig_b[0] /= b_0;
    trig_b[1] /= b_0;
    #else
    trig_b[0] = fma(trig_b[0], 2.0, -1.0);
    trig_b[1] = fma(trig_b[1], 2.0, -1.0);
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom2TrigonometricMoments(b_0, trig_b, depth, moment_bias, overestimation, wrapping_zone_parameters);
    #else
    vec4 b_1234 = texelFetch(_flw_moments_sampler, idx0, 0).xyzw;
    #if SINGLE_PRECISION
    vec2 b_even = b_1234.yw;
    vec2 b_odd = b_1234.xz;

    b_even /= b_0;
    b_odd /= b_0;

    const vec4 bias_vector = vec4(0, 0.375, 0, 0.375);
    #else
    vec2 b_even_q = b_1234.yw;
    vec2 b_odd_q = b_1234.xz;

    // Dequantize the moments
    vec2 b_even;
    vec2 b_odd;
    offsetAndDequantizeMoments(b_even, b_odd, b_even_q, b_odd_q);
    const vec4 bias_vector = vec4(0, 0.628, 0, 0.628);
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom4PowerMoments(b_0, b_even, b_odd, depth, moment_bias, overestimation, bias_vector);
    #endif
    #elif NUM_MOMENTS == 6
    ivec2 idx2 = idx0;
    #if TRIGONOMETRIC
    vec2 trig_b[3];
    trig_b[0] = texelFetch(_flw_moments_sampler, idx0, 0).xy;
    #if USE_R_RG_RBBA_FOR_MBOIT6
    vec4 tmp = texelFetch(extra_moments, idx0, 0);
    trig_b[1] = tmp.xy;
    trig_b[2] = tmp.zw;
    #else
    trig_b[1] = texelFetch(_flw_moments_sampler, idx1, 0).xy;
    trig_b[2] = texelFetch(_flw_moments_sampler, idx2, 0).xy;
    #endif
    #if SINGLE_PRECISION
    trig_b[0] /= b_0;
    trig_b[1] /= b_0;
    trig_b[2] /= b_0;
    #else
    trig_b[0] = fma(trig_b[0], 2.0, -1.0);
    trig_b[1] = fma(trig_b[1], 2.0, -1.0);
    trig_b[2] = fma(trig_b[2], 2.0, -1.0);
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom3TrigonometricMoments(b_0, trig_b, depth, moment_bias, overestimation, wrapping_zone_parameters);
    #else
    vec2 b_12 = texelFetch(_flw_moments_sampler, idx0, 0).xy;
    #if USE_R_RG_RBBA_FOR_MBOIT6
    vec4 tmp = texelFetch(extra_moments, idx0, 0);
    vec2 b_34 = tmp.xy;
    vec2 b_56 = tmp.zw;
    #else
    vec2 b_34 = texelFetch(_flw_moments_sampler, idx1, 0).xy;
    vec2 b_56 = texelFetch(_flw_moments_sampler, idx2, 0).xy;
    #endif
    #if SINGLE_PRECISION
    vec3 b_even = vec3(b_12.y, b_34.y, b_56.y);
    vec3 b_odd = vec3(b_12.x, b_34.x, b_56.x);

    b_even /= b_0;
    b_odd /= b_0;

    const float bias_vector[6] = { 0, 0.48, 0, 0.451, 0, 0.45 };
    #else
    vec3 b_even_q = vec3(b_12.y, b_34.y, b_56.y);
    vec3 b_odd_q = vec3(b_12.x, b_34.x, b_56.x);
    // Dequantize b_0 and the other moments
    vec3 b_even;
    vec3 b_odd;
    offsetAndDequantizeMoments(b_even, b_odd, b_even_q, b_odd_q);

    const float bias_vector[6] = { 0, 0.5566, 0, 0.489, 0, 0.47869382 };
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom6PowerMoments(b_0, b_even, b_odd, depth, moment_bias, overestimation, bias_vector);
    #endif
    #elif NUM_MOMENTS == 8
    #if TRIGONOMETRIC
    vec4 b_tmp = texelFetch(_flw_moments_sampler, idx0, 0);
    vec4 b_tmp2 = texelFetch(_flw_moments_sampler, idx1, 0);
    #if SINGLE_PRECISION
    vec2 trig_b[4] = {
    b_tmp2.xy / b_0,
    b_tmp.xy / b_0,
    b_tmp2.zw / b_0,
    b_tmp.zw / b_0
    };
    #else
    vec2 trig_b[4] = {
    fma(b_tmp2.xy, 2.0, -1.0),
    fma(b_tmp.xy, 2.0, -1.0),
    fma(b_tmp2.zw, 2.0, -1.0),
    fma(b_tmp.zw, 2.0, -1.0)
    };
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom4TrigonometricMoments(b_0, trig_b, depth, moment_bias, overestimation, wrapping_zone_parameters);
    #else
    #if SINGLE_PRECISION
    vec4 b_even = texelFetch(_flw_moments_sampler, idx0, 0);
    vec4 b_odd = texelFetch(_flw_moments_sampler, idx1, 0);

    b_even /= b_0;
    b_odd /= b_0;
    const float bias_vector[8] = { 0, 0.75, 0, 0.67666666666666664, 0, 0.63, 0, 0.60030303030303034 };
    #else
    vec4 b_even_q = texelFetch(_flw_moments_sampler, idx0, 0);
    vec4 b_odd_q = texelFetch(_flw_moments_sampler, idx1, 0);

    // Dequantize the moments
    vec4 b_even;
    vec4 b_odd;
    offsetAndDequantizeMoments(b_even, b_odd, b_even_q, b_odd_q);
    const float bias_vector[8] = { 0, 0.42474916387959866, 0, 0.22407802675585284, 0, 0.15369230769230768, 0, 0.12900440529089119 };
    #endif
    transmittance_at_depth = computeTransmittanceAtDepthFrom8PowerMoments(b_0, b_even, b_odd, depth, moment_bias, overestimation, bias_vector);
    #endif
    #endif

}
#endif
