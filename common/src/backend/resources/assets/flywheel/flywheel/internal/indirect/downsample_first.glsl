#include "flywheel:internal/indirect/downsample.glsl"

layout(binding = 0) uniform sampler2D depth_tex;
layout(binding = 1, r32f) uniform restrict writeonly image2D mip_0;

float reduce_load_depth_tex(ivec2 tex) {
    // NOTE: depth_tex is the actual depth buffer, and mip_0 is the "base" of our depth pyramid and has the next
    // smallest Po2 dimensions to depth_tex's dimensions. We dispatch enough invocations to cover the entire mip_0
    // and will very likely oversample depth_tex, but that's okay because we need to ensure conservative coverage.
    // All following mip levels are proper halvings of their parents and will not waste any work.
    vec2 uv = (vec2(tex) + 0.5) / vec2(imageSize(mip_0)) * 0.5;
    return reduce_4(textureGather(depth_tex, uv));
}

void downsample_depth_tex(uint x, uint y, ivec2 workgroup_id) {
    vec4 v;

    ivec2 tex = workgroup_id * 64 + ivec2(x * 2u, y * 2u);
    ivec2 pix = workgroup_id * 32 + ivec2(x, y);
    v[0] = reduce_load_depth_tex(tex);
    imageStore(mip_0, pix, vec4(v[0]));

    tex = workgroup_id * 64 + ivec2(x * 2u + 32u, y * 2u);
    pix = workgroup_id * 32 + ivec2(x + 16u, y);
    v[1] = reduce_load_depth_tex(tex);
    imageStore(mip_0, pix, vec4(v[1]));

    tex = workgroup_id * 64 + ivec2(x * 2u, y * 2u + 32u);
    pix = workgroup_id * 32 + ivec2(x, y + 16u);
    v[2] = reduce_load_depth_tex(tex);
    imageStore(mip_0, pix, vec4(v[2]));

    tex = workgroup_id * 64 + ivec2(x * 2u + 32u, y * 2u + 32u);
    pix = workgroup_id * 32 + ivec2(x + 16u, y + 16u);
    v[3] = reduce_load_depth_tex(tex);
    imageStore(mip_0, pix, vec4(v[3]));
}

void main() {
    uvec2 xy = get_xy();
    downsample_depth_tex(xy.x, xy.y, ivec2(gl_WorkGroupID.xy));
}
