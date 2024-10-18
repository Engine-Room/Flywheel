#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"
#include "flywheel:internal/indirect/light.glsl"
#include "flywheel:internal/indirect/matrices.glsl"

layout(std430, binding = _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING) restrict readonly buffer DrawIndexBuffer {
    uint _flw_drawIndices[];
};

layout(std430, binding = _FLW_DRAW_BUFFER_BINDING) restrict readonly buffer DrawBuffer {
    MeshDrawCommand _flw_drawCommands[];
};

#ifdef FLW_EMBEDDED
layout(std430, binding = _FLW_MATRIX_BUFFER_BINDING) restrict buffer MatrixBuffer {
    Matrices _flw_matrices[];
};
#endif

uniform uint _flw_baseDraw;

flat out uvec2 _flw_packedMaterial;

flat out uint _flw_instanceID;

#if __VERSION__ < 460
#define flw_baseInstance gl_BaseInstanceARB
#define flw_drawId gl_DrawIDARB
#else
#define flw_baseInstance gl_BaseInstance
#define flw_drawId gl_DrawID
#endif

void main() {
    uint drawIndex = flw_drawId + _flw_baseDraw;
    MeshDrawCommand draw = _flw_drawCommands[drawIndex];

    uint packedMaterialProperties = draw.packedMaterialProperties;
    _flw_unpackMaterialProperties(packedMaterialProperties, flw_material);
    _flw_packedMaterial = uvec2(draw.packedFogAndCutout, packedMaterialProperties);

    #ifdef FLW_EMBEDDED
    _flw_unpackMatrices(_flw_matrices[draw.matrixIndex], _flw_modelMatrix, _flw_normalMatrix);
    #endif

    #ifdef _FLW_CRUMBLING
    uint instanceIndex = flw_baseInstance;
    #else
    uint instanceIndex = _flw_drawIndices[flw_baseInstance + gl_InstanceID];
    #endif

    FlwInstance instance = _flw_unpackInstance(instanceIndex);

    _flw_main(instance);

    // Add 1 because a 0 instance id means null.
    _flw_instanceID = instanceIndex + 1;
}
