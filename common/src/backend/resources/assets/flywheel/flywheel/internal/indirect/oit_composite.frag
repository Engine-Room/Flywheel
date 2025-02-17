layout (location = 0) out vec4 frag;

layout (binding = 0) uniform sampler2D zerothMoment;
layout (binding = 1) uniform sampler2D accumulate;

void main() {
    ivec2 coords = ivec2(gl_FragCoord.xy);

    float b0 = texelFetch(zerothMoment, coords, 0).r;

    if (b0 < 1e-5) {
        discard;
    }

    vec4 accumulation = texelFetch(accumulate, coords, 0);

    vec3 normalizedAccumulation = accumulation.rgb / max(accumulation.a, 1e-5);

    frag = vec4(normalizedAccumulation, exp(-b0));
}
