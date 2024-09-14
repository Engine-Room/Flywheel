#include "flywheel:internal/indirect/buffer_bindings.glsl"

layout(local_size_x = 256) in;

layout(binding = 0) uniform usampler2D visBuffer;

layout(std430, binding = _FLW_LAST_FRAME_VISIBILITY_BUFFER_BINDING) restrict buffer LastFrameVisibilityBuffer {
    uint _flw_lastFrameVisibility[];
};

uint extractBits(uint e, uint offset, uint count) {
    return (e >> offset) & ((1u << count) - 1u);
}

uint insertBits(uint e, uint newbits, uint offset, uint count) {
    uint countMask = ((1u << count) - 1u);
    // zero out the bits we're going to replace first
    return (e & ~(countMask << offset)) | ((newbits & countMask) << offset);
}

uvec2 remap_for_wave_reduction(uint a) {
    return uvec2(
    insertBits(extractBits(a, 2u, 3u), a, 0u, 1u),
    insertBits(extractBits(a, 3u, 3u), extractBits(a, 1u, 2u), 0u, 2u)
    );
}

void emit(uint instanceID) {
    // Null instance id.
    if (instanceID == 0) {
        return;
    }

    // Adjust for null to find the actual index.
    instanceID = instanceID - 1;

    uint index = instanceID >> 5;

    uint mask = 1u << (instanceID & 31u);

    atomicOr(_flw_lastFrameVisibility[index], mask);
}

void main() {
    uvec2 sub_xy = remap_for_wave_reduction(gl_LocalInvocationIndex % 64u);
    uint x = sub_xy.x + 8u * ((gl_LocalInvocationIndex >> 6u) % 2u);
    uint y = sub_xy.y + 8u * (gl_LocalInvocationIndex >> 7u);

    ivec2 tex = ivec2(gl_WorkGroupID.xy) * 32 + ivec2(x, y) * 2;

    uint instanceID01 = texelFetchOffset(visBuffer, tex, 0, ivec2(0, 1)).r;
    uint instanceID11 = texelFetchOffset(visBuffer, tex, 0, ivec2(1, 1)).r;
    uint instanceID10 = texelFetchOffset(visBuffer, tex, 0, ivec2(1, 0)).r;
    uint instanceID00 = texelFetch(visBuffer, tex, 0).r;

    if (instanceID00 == instanceID01 && instanceID01 == instanceID10 && instanceID10 == instanceID11) {
        emit(instanceID00);
    } else {
        emit(instanceID00);
        emit(instanceID01);
        emit(instanceID10);
        emit(instanceID11);
    }
}
