#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"
#include "flywheel:util/matrix.glsl"
#include "flywheel:internal/indirect/matrices.glsl"
#include "flywheel:internal/indirect/dispatch.glsl"

layout(local_size_x = 32) in;

layout(std430, binding = _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING) restrict writeonly buffer DrawIndexBuffer {
    uint _flw_drawIndices[];
};

// High 6 bits for the number of instances in the page.
const uint _FLW_PAGE_COUNT_OFFSET = 26u;
// Bottom 26 bits for the model index.
const uint _FLW_MODEL_INDEX_MASK = 0x3FFFFFF;

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

    return _flw_testSphere(center, radius);
}

// TODO: There's an opportunity here to write out the transformed bounding spheres to a buffer and use them in pass 2,
//  instead of pulling the entire instance again. It would save a lot of memory bandwidth and matrix multiplications in
//  pass 2, but it would also be a good bit of writes in pass 1. It's worth investigating, but it would be nice to have
//  nsight trace working to be more sure.
void main() {
    uint pageIndex = gl_WorkGroupID.x;

    if (pageIndex >= _flw_pageFrameDescriptors.length()) {
        return;
    }

    uint packedModelIndexAndCount = _flw_pageFrameDescriptors[pageIndex];

    uint pageInstanceCount = packedModelIndexAndCount >> _FLW_PAGE_COUNT_OFFSET;

    if (gl_LocalInvocationID.x >= pageInstanceCount) {
        return;
    }

    uint instanceIndex = gl_GlobalInvocationID.x;

    uint modelIndex = packedModelIndexAndCount & _FLW_MODEL_INDEX_MASK;

    if (!_flw_isVisible(instanceIndex, modelIndex)) {
        return;
    }

    uint pageVisibility = _flw_visibility[pageIndex];

    if ((pageVisibility & (1u << gl_LocalInvocationID.x)) != 0u) {
        // This instance was visibile last frame, it should be rendered early.
        uint localIndex = atomicAdd(_flw_models[modelIndex].instanceCount, 1);
        uint targetIndex = _flw_models[modelIndex].baseInstance + localIndex;
        _flw_drawIndices[targetIndex] = instanceIndex;
    }
}
