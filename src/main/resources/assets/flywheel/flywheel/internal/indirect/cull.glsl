#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/indirect/object.glsl"

layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

layout(std430, binding = _FLW_OBJECT_BUFFER_BINDING) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = _FLW_TARGET_BUFFER_BINDING) restrict writeonly buffer TargetBuffer {
    uint objectIndices[];
};

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict buffer ModelBuffer {
    ModelDescriptor models[];
};

// Disgustingly vectorized sphere frustum intersection taking advantage of ahead of time packing.
// Only uses 6 fmas and some boolean ops.
// See also:
// flywheel:uniform/flywheel.glsl
// com.jozufozu.flywheel.lib.math.MatrixMath.writePackedFrustumPlanes
// org.joml.FrustumIntersection.testSphere
bool _flw_testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flywheel.planes.xyX, center.xxxx, fma(flywheel.planes.xyY, center.yyyy, fma(flywheel.planes.xyZ, center.zzzz, flywheel.planes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flywheel.planes.zX, center.xx, fma(flywheel.planes.zY, center.yy, fma(flywheel.planes.zZ, center.zz, flywheel.planes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool _flw_isVisible(uint objectIndex, uint modelIndex) {
    BoundingSphere sphere = models[modelIndex].boundingSphere;

    vec3 center;
    float radius;
    _flw_unpackBoundingSphere(sphere, center, radius);

    FlwInstance instance = _flw_unpackInstance(objects[objectIndex].instance);

    flw_transformBoundingSphere(instance, center, radius);

    return _flw_testSphere(center, radius);
}

void main() {
    uint objectIndex = gl_GlobalInvocationID.x;

    if (objectIndex >= objects.length()) {
        return;
    }

    uint modelIndex = objects[objectIndex].modelIndex;

    if (_flw_isVisible(objectIndex, modelIndex)) {
        uint localIndex = atomicAdd(models[modelIndex].instanceCount, 1);
        uint targetIndex = models[modelIndex].baseInstance + localIndex;
        objectIndices[targetIndex] = objectIndex;
    }
}
