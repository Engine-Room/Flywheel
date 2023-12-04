#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/indirect/object.glsl"

layout(local_size_x = FLW_SUBGROUP_SIZE) in;

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

layout(std430, binding = OBJECT_BINDING) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = TARGET_BINDING) restrict writeonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = MODEL_BINDING) restrict buffer ModelDescriptors {
    ModelDescriptor models[];
};

// Disgustingly vectorized sphere frustum intersection taking advantage of ahead of time packing.
// Only uses 6 fmas and some boolean ops.
// See also:
// flywheel:uniform/flywheel.glsl
// com.jozufozu.flywheel.lib.math.MatrixMath.writePackedFrustumPlanes
// org.joml.FrustumIntersection.testSphere
bool testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flywheel.planes.xyX, center.xxxx, fma(flywheel.planes.xyY, center.yyyy, fma(flywheel.planes.xyZ, center.zzzz, flywheel.planes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flywheel.planes.zX, center.xx, fma(flywheel.planes.zY, center.yy, fma(flywheel.planes.zZ, center.zz, flywheel.planes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool isVisible(uint objectID, uint modelID) {
    BoundingSphere sphere = models[modelID].boundingSphere;

    vec3 center;
    float radius;
    unpackBoundingSphere(sphere, center, radius);

    FlwInstance instance = _flw_unpackInstance(objects[objectID].instance);

    flw_transformBoundingSphere(instance, center, radius);

    return testSphere(center, radius);
}

void main() {
    uint objectID = gl_GlobalInvocationID.x;

    if (objectID >= objects.length()) {
        return;
    }

    uint modelID = objects[objectID].modelID;

    if (isVisible(objectID, modelID)) {
        uint batchIndex = atomicAdd(models[modelID].instanceCount, 1);
        uint globalIndex = models[modelID].baseInstance + batchIndex;

        objectIDs[globalIndex] = objectID;
    }
}
