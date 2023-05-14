#define FLW_SUBGROUP_SIZE 32
layout(local_size_x = FLW_SUBGROUP_SIZE) in;

#include "flywheel:util/types.glsl"
#include "flywheel:internal/indirect_draw_command.glsl"

// populated by instancers
layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FlwPackedInstance objects[];
};

layout(std430, binding = 1) restrict writeonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = 2) restrict readonly buffer BatchBuffer {
    uint batchIDs[];
};

layout(std430, binding = 3) restrict buffer DrawCommands {
    MeshDrawCommand drawCommands[];
};

uint flw_objectID;
uint flw_batchID;

// 83 - 27 = 56 spirv instruction results
bool testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flywheel.planes.xyX, center.xxxx, fma(flywheel.planes.xyY, center.yyyy, fma(flywheel.planes.xyZ, center.zzzz, flywheel.planes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flywheel.planes.zX, center.xx, fma(flywheel.planes.zY, center.yy, fma(flywheel.planes.zZ, center.zz, flywheel.planes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool isVisible() {
    BoundingSphere sphere = drawCommands[flw_batchID].boundingSphere;

    vec3 center;
    float radius;
    unpackBoundingSphere(sphere, center, radius);

    FlwInstance object = _flw_unpackInstance(objects[flw_objectID]);
    flw_transformBoundingSphere(object, center, radius);

    return testSphere(center, radius);
}

void main() {
    flw_objectID = gl_GlobalInvocationID.x;

    if (flw_objectID >= objects.length()) {
        return;
    }

    flw_batchID = batchIDs[flw_objectID];

    if (isVisible()) {
        uint batchIndex = atomicAdd(drawCommands[flw_batchID].instanceCount, 1);
        uint globalIndex = drawCommands[flw_batchID].baseInstance + batchIndex;

        objectIDs[globalIndex] = flw_objectID;
    }
}
