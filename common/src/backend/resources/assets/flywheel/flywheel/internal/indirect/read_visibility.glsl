layout(local_size_x = 8, local_size_y = 8) in;

layout(binding = 0) uniform usampler2D visBuffer;

layout(std430) restrict buffer VisibleFlagBuffer {
    uint _flw_visibleFlag[];
};

void main() {
    uint instanceID = texelFetch(visBuffer, ivec2(gl_GlobalInvocationID.xy), 0).r;

    // Null instance id.
    if (instanceID == 0) {
        return;
    }

    // Adjust for null to find the actual index.
    instanceID = instanceID - 1;

    uint index = instanceID >> 5;

    uint mask = 1u << (instanceID & 31u);

    atomicOr(_flw_visibleFlag[index], mask);
}
