#define FLW_SUBGROUP_SIZE 32
layout(local_size_x = FLW_SUBGROUP_SIZE) in;
#use "flywheel:api/cull.glsl"
#use "flywheel:uniform/frustum.glsl"
#use "flywheel:util/types.glsl"

struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;

    BoundingSphere boundingSphere;
};

// populated by instancers
layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FLW_INSTANCE_STRUCT objects[];
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

// 83 - 27 = 56 spirv instruction results
bool testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flw_planes.xyX, center.xxxx, fma(flw_planes.xyY, center.yyyy, fma(flw_planes.xyZ, center.zzzz, flw_planes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flw_planes.zX, center.xx, fma(flw_planes.zY, center.yy, fma(flw_planes.zZ, center.zz, flw_planes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool isVisible() {
    BoundingSphere sphere = drawCommands[flw_batchID].boundingSphere;

    vec3 center;
    float radius;
    unpackBoundingSphere(sphere, center, radius);
    flw_transformBoundingSphere(objects[flw_objectID], center, radius);

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
