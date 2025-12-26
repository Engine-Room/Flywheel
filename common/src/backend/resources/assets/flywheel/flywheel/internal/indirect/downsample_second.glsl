#include "flywheel:internal/indirect/downsample.glsl"

uniform uint mip_levels;
uniform uint base_mip_level;

layout(binding = 0, r32f) uniform restrict readonly image2D mip_0;
layout(binding = 1, r32f) uniform restrict writeonly image2D mip_1;
layout(binding = 2, r32f) uniform restrict writeonly image2D mip_2;
layout(binding = 3, r32f) uniform restrict writeonly image2D mip_3;
layout(binding = 4, r32f) uniform restrict writeonly image2D mip_4;
layout(binding = 5, r32f) uniform restrict writeonly image2D mip_5;
layout(binding = 6, r32f) uniform restrict writeonly image2D mip_6;

shared float[16][16] intermediate_memory;

float reduce_load_mip_0(ivec2 tex) {
    // NOTE: We could bind mip_0 as a sampler2D and use textureGather,
    // but it's already written to as an image in a previous pass so I think this is fine.
    return reduce_4(vec4(
    imageLoad(mip_0, tex + ivec2(0u, 0u)).r,
    imageLoad(mip_0, tex + ivec2(0u, 1u)).r,
    imageLoad(mip_0, tex + ivec2(1u, 0u)).r,
    imageLoad(mip_0, tex + ivec2(1u, 1u)).r
    ));
}

void downsample_mips_0_and_1(uint x, uint y, ivec2 workgroup_id) {
    vec4 v;

    ivec2 tex = workgroup_id * 64 + ivec2(x * 4u + 0u, y * 4u + 0u);
    ivec2 pix = workgroup_id * 32 + ivec2(x * 2u + 0u, y * 2u + 0u);
    v[0] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[0]));

    tex = workgroup_id * 64 + ivec2(x * 4u + 2u, y * 4u + 0u);
    pix = workgroup_id * 32 + ivec2(x * 2u + 1u, y * 2u + 0u);
    v[1] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[1]));

    tex = workgroup_id * 64 + ivec2(x * 4u + 0u, y * 4u + 2u);
    pix = workgroup_id * 32 + ivec2(x * 2u + 0u, y * 2u + 1u);
    v[2] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[2]));

    tex = workgroup_id * 64 + ivec2(x * 4u + 2u, y * 4u + 2u);
    pix = workgroup_id * 32 + ivec2(x * 2u + 1u, y * 2u + 1u);
    v[3] = reduce_load_mip_0(tex);
    imageStore(mip_1, pix, vec4(v[3]));

    if (mip_levels <= base_mip_level + 2u) { return; }

    float vr = reduce_4(v);
    imageStore(mip_2, workgroup_id * 16 + ivec2(x, y), vec4(vr));
    intermediate_memory[x][y] = vr;
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
    if (mip_levels <= base_mip_level + 3u) { return; }
    barrier();
    downsample_mip_2(x, y, workgroup_id, local_invocation_index);

    if (mip_levels <= base_mip_level + 4u) { return; }
    barrier();
    downsample_mip_3(x, y, workgroup_id, local_invocation_index);

    if (mip_levels <= base_mip_level + 5u) { return; }
    barrier();
    downsample_mip_4(x, y, workgroup_id, local_invocation_index);

    if (mip_levels <= base_mip_level + 6u) { return; }
    barrier();
    downsample_mip_5(workgroup_id, local_invocation_index);
}

void main() {
    uvec2 xy = get_xy();
    uint x = xy.x;
    uint y = xy.y;

    downsample_mips_0_and_1(x, y, ivec2(gl_WorkGroupID.xy));
    downsample_mips_2_to_5(x, y, ivec2(gl_WorkGroupID.xy), gl_LocalInvocationIndex);
}
