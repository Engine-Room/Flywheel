layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

const uint SRC_OFFSET_MASK = 0xFFFFFF;

// Since StagingBuffer is 16MB, a source offset *into an array of uints* can be represented with 22 bits.
// We use 24 here for some wiggle room.
// The lower 24 bits are the offset into the Src buffer.
// The upper 8 bits are the size of the copy.
struct Copy {
    uint sizeAndSrcOffset;
    uint dstOffset;
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

    uint sizeAndSrcOffset = copies[copy].sizeAndSrcOffset;
    uint srcOffset = sizeAndSrcOffset & SRC_OFFSET_MASK;
    uint size = sizeAndSrcOffset >> 24;

    uint dstOffset = copies[copy].dstOffset;

    for (uint i = 0; i < size; i++) {
        dst[dstOffset + i] = src[srcOffset + i];
    }
}
