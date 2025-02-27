#include "flywheel:internal/uniforms/frame.glsl"
#include "flywheel:internal/wavelet.glsl"
#include "flywheel:internal/depth.glsl"

uniform sampler2D _flw_depthRange;

uniform sampler2DArray _flw_coefficients;

float eye_depth_from_normalized_transparency_depth(float tDepth) {
    vec2 depthRange = texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0).rg;

    float delta = depthRange.x + depthRange.y;

    return tDepth * delta - depthRange.x;
}

void main() {
    float threshold = 0.0001;

    //
    // If transmittance an infinite depth is above the threshold, it doesn't ever become
    // zero, so we can bail out.
    //
    float transmittance_at_far_depth = total_transmittance(_flw_coefficients);
    if (transmittance_at_far_depth > threshold) {
        discard;
    }

    float normalized_depth_at_zero_transmittance = 1.0;
    float sample_depth = 0.5;
    float delta = 0.25;

    //
    // Quick & Dirty way to binary search through the transmittance function
    // looking for a value that's below the threshold.
    //
    int steps = 6;
    for (int i = 0; i < steps; ++i) {
        float transmittance = transmittance(_flw_coefficients, sample_depth);
        if (transmittance <= threshold) {
            normalized_depth_at_zero_transmittance = sample_depth;
            sample_depth -= delta;
        } else {
            sample_depth += delta;
        }
        delta *= 0.5;
    }

    //
    // Searching inside the transparency depth bounds, so have to transform that to
    // a world-space linear-depth and that into a device depth we can output into
    // the currently bound depth buffer.
    //
    float eyeDepth = eye_depth_from_normalized_transparency_depth(normalized_depth_at_zero_transmittance);
    gl_FragDepth = delinearize_depth(eyeDepth, _flw_cullData.znear, _flw_cullData.zfar);
}
