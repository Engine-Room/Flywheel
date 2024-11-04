#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"
#include "flywheel:util/matrix.glsl"
#include "flywheel:internal/indirect/matrices.glsl"
#include "flywheel:internal/indirect/dispatch.glsl"

layout(local_size_x = 32) in;

layout(std430, binding = _FLW_BOUNDING_SPHERE_BINDING) restrict writeonly buffer BoundingSphereBuffer {
    vec4 _flw_boundingSpheres[];
};

layout(std430, binding = _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING) restrict writeonly buffer DrawIndexBuffer {
    uint _flw_drawIndices[];
};

layout(std430, binding = _FLW_PAGE_FRAME_DESCRIPTOR_BUFFER_BINDING) restrict readonly buffer PageFrameDescriptorBuffer {
    uint _flw_pageFrameDescriptors[];
};

layout(std430, binding = _FLW_LAST_FRAME_VISIBILITY_BUFFER_BINDING) restrict readonly buffer LastFrameVisibilityBuffer {
    uint _flw_visibility[];
};

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict buffer ModelBuffer {
    ModelDescriptor _flw_models[];
};

layout(std430, binding = _FLW_MATRIX_BUFFER_BINDING) restrict readonly buffer MatrixBuffer {
    Matrices _flw_matrices[];
};

// Disgustingly vectorized sphere frustum intersection taking advantage of ahead of time packing.
// Only uses 6 fmas and some boolean ops.
// See also:
// flywheel:uniform/flywheel.glsl
// dev.engine_room.flywheel.lib.math.MatrixMath.writePackedFrustumPlanes
// org.joml.FrustumIntersection.testSphere
bool _flw_testSphere(vec3 center, float radius) {
    bvec4 xyInside = greaterThanEqual(fma(flw_frustumPlanes.xyX, center.xxxx, fma(flw_frustumPlanes.xyY, center.yyyy, fma(flw_frustumPlanes.xyZ, center.zzzz, flw_frustumPlanes.xyW))), -radius.xxxx);
    bvec2 zInside = greaterThanEqual(fma(flw_frustumPlanes.zX, center.xx, fma(flw_frustumPlanes.zY, center.yy, fma(flw_frustumPlanes.zZ, center.zz, flw_frustumPlanes.zW))), -radius.xx);

    return all(xyInside) && all(zInside);
}

bool _flw_isVisible(uint instanceIndex, uint modelIndex) {
    uint matrixIndex = _flw_models[modelIndex].matrixIndex;
    BoundingSphere sphere = _flw_models[modelIndex].boundingSphere;

    vec3 center;
    float radius;
    _flw_unpackBoundingSphere(sphere, center, radius);

    FlwInstance instance = _flw_unpackInstance(instanceIndex);

    flw_transformBoundingSphere(instance, center, radius);

    if (matrixIndex > 0) {
        transformBoundingSphere(_flw_matrices[matrixIndex].pose, center, radius);
    }

    _flw_boundingSpheres[instanceIndex] = vec4(center, radius);

    return _flw_testSphere(center, radius);
}

void main() {
    uint pageIndex = gl_WorkGroupID.x << 1u;

    if (pageIndex >= _flw_pageFrameDescriptors.length()) {
        return;
    }

    uint modelIndex = _flw_pageFrameDescriptors[pageIndex];

    uint pageValidity = _flw_pageFrameDescriptors[pageIndex + 1];

    if (((1u << gl_LocalInvocationID.x) & pageValidity) == 0) {
        return;
    }

    uint instanceIndex = gl_GlobalInvocationID.x;

    if (!_flw_isVisible(instanceIndex, modelIndex)) {
        return;
    }

    uint pageVisibility = _flw_visibility[gl_WorkGroupID.x];
    bool visibleLastFrame = (_flw_visibility[gl_WorkGroupID.x] & (1u << gl_LocalInvocationID.x)) != 0u;

    if (visibleLastFrame) {
        // This instance was visibile last frame, it should be rendered early.
        uint localIndex = atomicAdd(_flw_models[modelIndex].instanceCount, 1);
        uint targetIndex = _flw_models[modelIndex].baseInstance + localIndex;
        _flw_drawIndices[targetIndex] = instanceIndex;
    }
}
