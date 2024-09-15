#include "flywheel:internal/indirect/downsample.glsl"

layout(binding = 0, r32f) uniform restrict readonly image2D mip_6;
layout(binding = 1, r32f) uniform restrict writeonly image2D mip_7;
layout(binding = 2, r32f) uniform restrict writeonly image2D mip_8;
layout(binding = 3, r32f) uniform restrict writeonly image2D mip_9;
layout(binding = 4, r32f) uniform restrict writeonly image2D mip_10;
layout(binding = 5, r32f) uniform restrict writeonly image2D mip_11;
layout(binding = 6, r32f) uniform restrict writeonly image2D mip_12;

float reduce_load_mip_6(ivec2 tex) {
    // NOTE: We could bind mip_6 as a sampler2D and use textureGather,
    // but it's already written to as an image in the first pass so I think this is fine.
    return reduce_4(vec4(
    imageLoad(mip_6, tex + ivec2(0u, 0u)).r,
    imageLoad(mip_6, tex + ivec2(0u, 1u)).r,
    imageLoad(mip_6, tex + ivec2(1u, 0u)).r,
    imageLoad(mip_6, tex + ivec2(1u, 1u)).r
    ));
}

void downsample_mips_6_and_7(uint x, uint y) {
    vec4 v;

    ivec2 tex = ivec2(x * 4u + 0u, y * 4u + 0u);
    ivec2 pix = ivec2(x * 2u + 0u, y * 2u + 0u);
    v[0] = reduce_load_mip_6(tex);
    imageStore(mip_7, pix, vec4(v[0]));

    tex = ivec2(x * 4u + 2u, y * 4u + 0u);
    pix = ivec2(x * 2u + 1u, y * 2u + 0u);
    v[1] = reduce_load_mip_6(tex);
    imageStore(mip_7, pix, vec4(v[1]));

    tex = ivec2(x * 4u + 0u, y * 4u + 2u);
    pix = ivec2(x * 2u + 0u, y * 2u + 1u);
    v[2] = reduce_load_mip_6(tex);
    imageStore(mip_7, pix, vec4(v[2]));

    tex = ivec2(x * 4u + 2u, y * 4u + 2u);
    pix = ivec2(x * 2u + 1u, y * 2u + 1u);
    v[3] = reduce_load_mip_6(tex);
    imageStore(mip_7, pix, vec4(v[3]));

    if (max_mip_level <= 7u) { return; }

    float vr = reduce_4(v);
    imageStore(mip_8, ivec2(x, y), vec4(vr));
    intermediate_memory[x][y] = vr;
}


void downsample_mip_8(uint x, uint y, uint local_invocation_index) {
    if (local_invocation_index < 64u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 2u + 0u][y * 2u + 0u],
        intermediate_memory[x * 2u + 1u][y * 2u + 0u],
        intermediate_memory[x * 2u + 0u][y * 2u + 1u],
        intermediate_memory[x * 2u + 1u][y * 2u + 1u]
        ));
        imageStore(mip_9, ivec2(x, y), vec4(v));
        intermediate_memory[x * 2u + y % 2u][y * 2u] = v;
    }
}

void downsample_mip_9(uint x, uint y, uint local_invocation_index) {
    if (local_invocation_index < 16u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 4u + 0u + 0u][y * 4u + 0u],
        intermediate_memory[x * 4u + 2u + 0u][y * 4u + 0u],
        intermediate_memory[x * 4u + 0u + 1u][y * 4u + 2u],
        intermediate_memory[x * 4u + 2u + 1u][y * 4u + 2u]
        ));
        imageStore(mip_10, ivec2(x, y), vec4(v));
        intermediate_memory[x * 4u + y][y * 4u] = v;
    }
}

void downsample_mip_10(uint x, uint y, uint local_invocation_index) {
    if (local_invocation_index < 4u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 8u + 0u + 0u + y * 2u][y * 8u + 0u],
        intermediate_memory[x * 8u + 4u + 0u + y * 2u][y * 8u + 0u],
        intermediate_memory[x * 8u + 0u + 1u + y * 2u][y * 8u + 4u],
        intermediate_memory[x * 8u + 4u + 1u + y * 2u][y * 8u + 4u]
        ));
        imageStore(mip_11, ivec2(x, y), vec4(v));
        intermediate_memory[x + y * 2u][0u] = v;
    }
}

void downsample_mip_11(uint local_invocation_index) {
    if (local_invocation_index < 1u) {
        float v = reduce_4(vec4(
        intermediate_memory[0u][0u],
        intermediate_memory[1u][0u],
        intermediate_memory[2u][0u],
        intermediate_memory[3u][0u]
        ));

        imageStore(mip_12, ivec2(0u, 0u), vec4(v));
    }
}


void downsample_mips_8_to_11(uint x, uint y, uint local_invocation_index) {
    if (max_mip_level <= 8u) { return; }
    barrier();
    downsample_mip_8(x, y, local_invocation_index);

    if (max_mip_level <= 9u) { return; }
    barrier();
    downsample_mip_9(x, y, local_invocation_index);

    if (max_mip_level <= 10u) { return; }
    barrier();
    downsample_mip_10(x, y, local_invocation_index);

    if (max_mip_level <= 11u) { return; }
    barrier();
    downsample_mip_11(local_invocation_index);
}

void downsample_depth_second() {
    uvec2 sub_xy = remap_for_wave_reduction(gl_LocalInvocationIndex % 64u);
    uint x = sub_xy.x + 8u * ((gl_LocalInvocationIndex >> 6u) % 2u);
    uint y = sub_xy.y + 8u * (gl_LocalInvocationIndex >> 7u);

    downsample_mips_6_and_7(x, y);

    downsample_mips_8_to_11(x, y, gl_LocalInvocationIndex);
}

void main() {
    downsample_depth_second();
}
