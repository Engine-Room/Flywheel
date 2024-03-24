#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"
#include "flywheel:util/matrix.glsl"

layout(local_size_x = _FLW_SUBGROUP_SIZE) in;

layout(std430, binding = _FLW_TARGET_BUFFER_BINDING) restrict writeonly buffer TargetBuffer {
    uint _flw_instanceIndices[];
};

layout(std430, binding = _FLW_MODEL_INDEX_BUFFER_BINDING) restrict readonly buffer ModelIndexBuffer {
    uint _flw_modelIndices[];
};

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict buffer ModelBuffer {
    ModelDescriptor _flw_models[];
};

uniform mat4 _flw_embeddedModel;
uniform bool _flw_useEmbeddedModel = false;

// Disgustingly vectorized sphere frustum intersection taking advantage of ahead of time packing.
// Only uses 6 fmas and some boolean ops.
// See also:
// flywheel:uniform/flywheel.glsl
// com.jozufozu.flywheel.lib.math.MatrixMath.writePackedFrustumPlanes
// org.joml.FrustumIntersection.testSphere
bool _flw_testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flw_frustumPlanes.xyX, center.xxxx, fma(flw_frustumPlanes.xyY, center.yyyy, fma(flw_frustumPlanes.xyZ, center.zzzz, flw_frustumPlanes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flw_frustumPlanes.zX, center.xx, fma(flw_frustumPlanes.zY, center.yy, fma(flw_frustumPlanes.zZ, center.zz, flw_frustumPlanes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool _flw_isVisible(uint instanceIndex, uint modelIndex) {
    BoundingSphere sphere = _flw_models[modelIndex].boundingSphere;

    vec3 center;
    float radius;
    _flw_unpackBoundingSphere(sphere, center, radius);

    FlwInstance instance = _flw_unpackInstance(instanceIndex);

    flw_transformBoundingSphere(instance, center, radius);

    if (_flw_useEmbeddedModel) {
        transformBoundingSphere(_flw_embeddedModel, center, radius);
    }

    return _flw_testSphere(center, radius);
}

void main() {
    uint instanceIndex = gl_GlobalInvocationID.x;

    if (instanceIndex >= _flw_modelIndices.length()) {
        return;
    }

    uint modelIndex = _flw_modelIndices[instanceIndex];

    if (_flw_isVisible(instanceIndex, modelIndex)) {
        uint localIndex = atomicAdd(_flw_models[modelIndex].instanceCount, 1);
        uint targetIndex = _flw_models[modelIndex].baseInstance + localIndex;
        _flw_instanceIndices[targetIndex] = instanceIndex;
    }
}
