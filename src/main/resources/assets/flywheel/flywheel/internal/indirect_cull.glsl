layout(local_size_x = FLW_SUBGROUP_SIZE) in;

#include "flywheel:internal/indirect_draw_command.glsl"

// need to add stubs so the instance shader compiles.
vec4 flw_vertexPos;
vec4 flw_vertexColor;
vec2 flw_vertexTexCoord;
ivec2 flw_vertexOverlay;
vec2 flw_vertexLight;
vec3 flw_vertexNormal;
float flw_distance;
vec4 flw_var0;
vec4 flw_var1;
vec4 flw_var2;
vec4 flw_var3;

void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius);

struct Object {
    uint batchID;
    FlwPackedInstance instance;
};

// populated by instancers
layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = 1) restrict writeonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = 2) restrict buffer DrawCommands {
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

    FlwInstance object = _flw_unpackInstance(objects[flw_objectID].instance);
    flw_transformBoundingSphere(object, center, radius);

    return testSphere(center, radius);
}

void main() {
    flw_objectID = gl_GlobalInvocationID.x;

    if (flw_objectID >= objects.length()) {
        return;
    }

    flw_batchID = objects[flw_objectID].batchID;

    if (isVisible()) {
        uint batchIndex = atomicAdd(drawCommands[flw_batchID].instanceCount, 1);
        uint globalIndex = drawCommands[flw_batchID].baseInstance + batchIndex;

        objectIDs[globalIndex] = flw_objectID;
    }
}
