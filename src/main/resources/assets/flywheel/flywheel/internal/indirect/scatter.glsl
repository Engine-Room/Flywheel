layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

struct Copy {
    uint srcOffset;
    uint dstOffset;
    uint byteSize;
};

layout(std430, binding = 0) restrict readonly buffer Copies {
    Copy copies[];
};

layout(std430, binding = 1) restrict readonly buffer Src {
    uint src[];
};

layout(std430, binding = 2) restrict writeonly buffer Dst {
    uint dst[];
};

void main() {
    uint copy = gl_GlobalInvocationID.x;

    if (copy >= copies.length()) {
        return;
    }

    uint srcOffset = copies[copy].srcOffset >> 2;
    uint dstOffset = copies[copy].dstOffset >> 2;
    uint size = copies[copy].byteSize >> 2;

    for (uint i = 0; i < size; i++) {
        dst[dstOffset + i] = src[srcOffset + i];
    }
}
