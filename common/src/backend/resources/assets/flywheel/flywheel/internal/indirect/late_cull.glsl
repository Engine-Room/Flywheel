#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/model_descriptor.glsl"
#include "flywheel:internal/uniforms/uniforms.glsl"
#include "flywheel:util/matrix.glsl"
#include "flywheel:internal/indirect/matrices.glsl"

layout(local_size_x = 32) in;

layout(std430, binding = _FLW_PASS_TWO_INSTANCE_INDEX_BUFFER_BINDING) restrict readonly buffer PassTwoIndexBuffer {
    uint _flw_passTwoIndicies[];
};

layout(std430, binding = _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING) restrict writeonly buffer DrawIndexBuffer {
    uint _flw_drawIndices[];
};

layout(std430, binding = _FLW_PAGE_FRAME_DESCRIPTOR_BUFFER_BINDING) restrict readonly buffer PageFrameDescriptorBuffer {
    uint _flw_pageFrameDescriptors[];
};

layout(std430, binding = _FLW_MODEL_BUFFER_BINDING) restrict buffer ModelBuffer {
    ModelDescriptor _flw_models[];
};

layout(std430, binding = _FLW_MATRIX_BUFFER_BINDING) restrict readonly buffer MatrixBuffer {
    Matrices _flw_matrices[];
};

layout(binding = 0) uniform sampler2D _flw_depthPyramid;

bool projectSphere(vec3 c, float r, float znear, float P00, float P11, out vec4 aabb) {
    // Closest point on the sphere is between the camera and the near plane, don't even attempt to cull.
    if (c.z + r > -znear) {
        return false;
    }

    vec3 cr = c * r;
    float czr2 = c.z * c.z - r * r;

    float vx = sqrt(c.x * c.x + czr2);
    float minx = (vx * c.x - cr.z) / (vx * c.z + cr.x);
    float maxx = (vx * c.x + cr.z) / (vx * c.z - cr.x);

    float vy = sqrt(c.y * c.y + czr2);
    float miny = (vy * c.y - cr.z) / (vy * c.z + cr.y);
    float maxy = (vy * c.y + cr.z) / (vy * c.z - cr.y);

    aabb = vec4(minx * P00, miny * P11, maxx * P00, maxy * P11);
    aabb = aabb.xwzy * vec4(-0.5f, -0.5f, -0.5f, -0.5f) + vec4(0.5f); // clip space -> uv space

    return true;
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

    transformBoundingSphere(flw_view, center, radius);

    vec4 aabb;
    if (projectSphere(center, radius, _flw_cullData.znear, _flw_cullData.P00, _flw_cullData.P11, aabb))
    {
        float width = (aabb.z - aabb.x) * _flw_cullData.pyramidWidth;
        float height = (aabb.w - aabb.y) * _flw_cullData.pyramidHeight;

        int level = clamp(int(ceil(log2(max(width, height)))), 0, _flw_cullData.pyramidLevels);

        ivec2 levelSize = textureSize(_flw_depthPyramid, level);

        ivec4 levelSizePair = ivec4(levelSize, levelSize);

        ivec4 bounds = ivec4(aabb * vec4(levelSizePair));

        float depth01 = texelFetch(_flw_depthPyramid, bounds.xw, level).r;
        float depth11 = texelFetch(_flw_depthPyramid, bounds.zw, level).r;
        float depth10 = texelFetch(_flw_depthPyramid, bounds.zy, level).r;
        float depth00 = texelFetch(_flw_depthPyramid, bounds.xy, level).r;

        float depth;
        if (_flw_cullData.useMin == 0) {
            depth = max(max(depth00, depth01), max(depth10, depth11));
        } else {
            depth = min(min(depth00, depth01), min(depth10, depth11));
        }

        float depthSphere = 1. + _flw_cullData.znear / (center.z + radius);

        return depthSphere <= depth;
    }

    return true;
}

void main() {
    if (gl_GlobalInvocationID.x >= _flw_passTwoIndicies.length()) {
        return;
    }

    uint instanceIndex = _flw_passTwoIndices[gl_GlobalInvocationID.x];

    uint pageIndex = instanceIndex >> 5;

    uint packedModelIndexAndCount = _flw_pageFrameDescriptors[pageIndex];

    uint modelIndex = packedModelIndexAndCount & _FLW_MODEL_INDEX_MASK;

    if (_flw_isVisible(instanceIndex, modelIndex)) {
        uint localIndex = atomicAdd(_flw_models[modelIndex].instanceCount, 1);
        uint targetIndex = _flw_models[modelIndex].baseInstance + localIndex;
        _flw_instanceIndices[targetIndex] = instanceIndex;
    }
}
