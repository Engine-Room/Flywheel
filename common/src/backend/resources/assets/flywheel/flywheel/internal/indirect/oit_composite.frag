#include "flywheel:internal/wavelet.glsl"

layout (location = 0) out vec4 frag;

layout (binding = 0) uniform sampler2DArray _flw_coefficients;
layout (binding = 1) uniform sampler2D _flw_accumulate;

void main() {
    vec4 texel = texelFetch(_flw_accumulate, ivec2(gl_FragCoord.xy), 0);

    if (texel.a < 1e-5) {
        discard;
    }

    float total_transmittance = total_transmittance(_flw_coefficients);

    frag = vec4(texel.rgb / texel.a, total_transmittance);
}
