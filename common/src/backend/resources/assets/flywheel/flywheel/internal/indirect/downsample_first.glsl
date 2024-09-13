#include "flywheel:internal/indirect/downsample.glsl"

layout(binding = 0) uniform sampler2D mip_0;
layout(binding = 1, r32f) uniform writeonly image2D mip_1;
layout(binding = 2, r32f) uniform writeonly image2D mip_2;
layout(binding = 3, r32f) uniform writeonly image2D mip_3;
layout(binding = 4, r32f) uniform writeonly image2D mip_4;
layout(binding = 5, r32f) uniform writeonly image2D mip_5;
layout(binding = 6, r32f) uniform writeonly image2D mip_6;

float reduce_load_mip_0(uvec2 tex) {
    vec2 uv = (vec2(tex) + 0.5) / vec2(imageSize(mip_1)) * 0.5;
    return reduce_4(textureGather(mip_0, uv));
}

void downsample_mips_0_and_1(uint x, uint y, ivec2 workgroup_id, uint local_invocation_index) {
    vec4 v;

    ivec2 tex = workgroup_id * 64 + ivec2(x * 2u, y * 2u);
    ivec2 pix = workgroup_id * 32 + ivec2(x, y);
    v[0] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[0]));

    tex = workgroup_id * 64 + ivec2(x * 2u + 32u, y * 2u);
    pix = workgroup_id * 32 + ivec2(x + 16u, y);
    v[1] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[1]));

    tex = workgroup_id * 64 + ivec2(x * 2u, y * 2u + 32u);
    pix = workgroup_id * 32 + ivec2(x, y + 16u);
    v[2] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[2]));

    tex = workgroup_id * 64 + ivec2(x * 2u + 32u, y * 2u + 32u);
    pix = workgroup_id * 32 + ivec2(x + 16u, y + 16u);
    v[3] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[3]));

    if (max_mip_level <= 1u) { return; }

    for (uint i = 0u; i < 4u; i++) {
        intermediate_memory[x][y] = v[i];
        barrier();
        if (local_invocation_index < 64u) {
            v[i] = reduce_4(vec4(
            intermediate_memory[x * 2u + 0u][y * 2u + 0u],
            intermediate_memory[x * 2u + 1u][y * 2u + 0u],
            intermediate_memory[x * 2u + 0u][y * 2u + 1u],
            intermediate_memory[x * 2u + 1u][y * 2u + 1u]
            ));
            pix = (workgroup_id * 16) + ivec2(
            x + (i % 2u) * 8u,
            y + (i / 2u) * 8u
            );
            imageStore(mip_2, pix, vec4(v[i]));
        }
        barrier();
    }

    if (local_invocation_index < 64u) {
        intermediate_memory[x + 0u][y + 0u] = v[0];
        intermediate_memory[x + 8u][y + 0u] = v[1];
        intermediate_memory[x + 0u][y + 8u] = v[2];
        intermediate_memory[x + 8u][y + 8u] = v[3];
    }
}


void downsample_mip_2(uint x, uint y, ivec2 workgroup_id, uint local_invocation_index) {
    if (local_invocation_index < 64u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 2u + 0u][y * 2u + 0u],
        intermediate_memory[x * 2u + 1u][y * 2u + 0u],
        intermediate_memory[x * 2u + 0u][y * 2u + 1u],
        intermediate_memory[x * 2u + 1u][y * 2u + 1u]
        ));
        imageStore(mip_3, (workgroup_id * 8) + ivec2(x, y), vec4(v));
        intermediate_memory[x * 2u + y % 2u][y * 2u] = v;
    }
}

void downsample_mip_3(uint x, uint y, ivec2 workgroup_id, uint local_invocation_index) {
    if (local_invocation_index < 16u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 4u + 0u + 0u][y * 4u + 0u],
        intermediate_memory[x * 4u + 2u + 0u][y * 4u + 0u],
        intermediate_memory[x * 4u + 0u + 1u][y * 4u + 2u],
        intermediate_memory[x * 4u + 2u + 1u][y * 4u + 2u]
        ));
        imageStore(mip_4, (workgroup_id * 4) + ivec2(x, y), vec4(v));
        intermediate_memory[x * 4u + y][y * 4u] = v;
    }
}

void downsample_mip_4(uint x, uint y, ivec2 workgroup_id, uint local_invocation_index) {
    if (local_invocation_index < 4u) {
        float v = reduce_4(vec4(
        intermediate_memory[x * 8u + 0u + 0u + y * 2u][y * 8u + 0u],
        intermediate_memory[x * 8u + 4u + 0u + y * 2u][y * 8u + 0u],
        intermediate_memory[x * 8u + 0u + 1u + y * 2u][y * 8u + 4u],
        intermediate_memory[x * 8u + 4u + 1u + y * 2u][y * 8u + 4u]
        ));
        imageStore(mip_5, (workgroup_id * 2) + ivec2(x, y), vec4(v));
        intermediate_memory[x + y * 2u][0u] = v;
    }
}

void downsample_mip_5(ivec2 workgroup_id, uint local_invocation_index) {
    if (local_invocation_index < 1u) {
        float v = reduce_4(vec4(
        intermediate_memory[0u][0u],
        intermediate_memory[1u][0u],
        intermediate_memory[2u][0u],
        intermediate_memory[3u][0u]
        ));
        imageStore(mip_6, workgroup_id, vec4(v));
    }
}

void downsample_mips_2_to_5(uint x, uint y, ivec2 workgroup_id, uint local_invocation_index) {
    if (max_mip_level <= 2u) { return; }
    barrier();
    downsample_mip_2(x, y, workgroup_id, local_invocation_index);

    if (max_mip_level <= 3u) { return; }
    barrier();
    downsample_mip_3(x, y, workgroup_id, local_invocation_index);

    if (max_mip_level <= 4u) { return; }
    barrier();
    downsample_mip_4(x, y, workgroup_id, local_invocation_index);

    if (max_mip_level <= 5u) { return; }
    barrier();
    downsample_mip_5(workgroup_id, local_invocation_index);
}

void downsample_depth_first() {
    uvec2 sub_xy = remap_for_wave_reduction(gl_LocalInvocationIndex % 64u);
    uint x = sub_xy.x + 8u * ((gl_LocalInvocationIndex >> 6u) % 2u);
    uint y = sub_xy.y + 8u * (gl_LocalInvocationIndex >> 7u);

    downsample_mips_0_and_1(x, y, ivec2(gl_WorkGroupID.xy), gl_LocalInvocationIndex);

    downsample_mips_2_to_5(x, y, ivec2(gl_WorkGroupID.xy), gl_LocalInvocationIndex);
}

void main() {
    downsample_depth_first();
}
