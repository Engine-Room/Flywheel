layout(local_size_x = 64) in;

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
    // Each work group is responsible for one of the copies in the buffer.
    // We dispatch exactly as many work groups as there are copies, so no need to check bounds.
    uint copy = gl_WorkGroupID.x;

    // Each invocation in the work group is responsible for one uint in the copy.
    uint i = gl_LocalInvocationID.x;

    // Unpack the copy.
    uint sizeAndSrcOffset = copies[copy].sizeAndSrcOffset;
    uint dstOffset = copies[copy].dstOffset;
    uint srcOffset = sizeAndSrcOffset & SRC_OFFSET_MASK;
    uint size = sizeAndSrcOffset >> 24;

    // Fetch the uint to copy before exiting to make instruction reordering happy.
    // With 20mb going through a 24mb staging buffer, this made a 1ms/frame difference.
    // Should properly test with nsight at some point.
    uint toCopy = src[srcOffset + i];

    if (i >= size) {
        return;
    }

    dst[dstOffset + i] = toCopy;
}
