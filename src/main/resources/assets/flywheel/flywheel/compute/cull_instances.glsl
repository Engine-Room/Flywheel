#define FLW_SUBGROUP_SIZE 32
layout(local_size_x = FLW_SUBGROUP_SIZE) in;
#use "flywheel:api/cull.glsl"
#use "flywheel:util/quaternion.glsl"
#use "flywheel:uniform/frustum.glsl"
#use "flywheel:instance/oriented_indirect.glsl"

struct MeshDrawCommand {
    uint indexCount;
    uint instanceCount;
    uint firstIndex;
    uint vertexOffset;
    uint baseInstance;

    vec4 boundingSphere;
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

layout(std430, binding = 4) restrict writeonly buffer DebugVisibility {
    uint objectVisibilityBits[];
};

// 83 - 27 = 56 spirv instruction results
bool testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flw_planes.xyX, center.xxxx, fma(flw_planes.xyY, center.yyyy, fma(flw_planes.xyZ, center.zzzz, flw_planes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flw_planes.zX, center.xx, fma(flw_planes.zY, center.yy, fma(flw_planes.zZ, center.zz, flw_planes.zW))), -radius.xx);

    uint debug = uint(xyInside.x);
    debug |= uint(xyInside.y) << 1;
    debug |= uint(xyInside.z) << 2;
    debug |= uint(xyInside.w) << 3;
    debug |= uint(zInside.x) << 4;
    debug |= uint(zInside.y) << 5;

    objectVisibilityBits[flw_objectID] = debug;

    return all(xyInside) && all(zInside);
}

bool isVisible() {
    vec4 sphere = drawCommands[flw_batchID].boundingSphere;

    vec3 center = sphere.xyz;
    float radius = sphere.r;
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
