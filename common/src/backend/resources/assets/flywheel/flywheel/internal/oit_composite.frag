#include "flywheel:internal/wavelet.glsl"
#include "flywheel:internal/depth.glsl"
#include "flywheel:internal/uniforms/frame.glsl"

out vec4 frag;

uniform sampler2D _flw_accumulate;
uniform sampler2D _flw_depthRange;
uniform sampler2DArray _flw_coefficients;

void main() {
    vec4 texel = texelFetch(_flw_accumulate, ivec2(gl_FragCoord.xy), 0);

    if (texel.a < 1e-5) {
        discard;
    }

    float total_transmittance = total_transmittance(_flw_coefficients);

    frag = vec4(texel.rgb / texel.a, 1. - total_transmittance);

    float minDepth = -texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0).r;

    gl_FragDepth = delinearize_depth(minDepth, _flw_cullData.znear, _flw_cullData.zfar);
}
