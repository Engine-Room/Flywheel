#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"
#include "flywheel:internal/indirect/light.glsl"
#include "flywheel:internal/indirect/matrices.glsl"

layout(std430, binding = _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING) restrict readonly buffer TargetBuffer {
    uint _flw_instanceIndices[];
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

flat out uvec3 _flw_packedMaterial;

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

    _flw_uberMaterialVertexIndex = draw.materialVertexIndex;
    uint packedMaterialProperties = draw.packedMaterialProperties;
    _flw_unpackMaterialProperties(packedMaterialProperties, flw_material);
    _flw_packedMaterial = uvec3(draw.materialFragmentIndex, draw.packedFogAndCutout, packedMaterialProperties);

    #ifdef FLW_EMBEDDED
    _flw_unpackMatrices(_flw_matrices[draw.matrixIndex], _flw_modelMatrix, _flw_normalMatrix);
    //    _flw_modelMatrix = mat4(1.);
    //    _flw_normalMatrix = mat3(1.);
    #endif

    #ifdef _FLW_CRUMBLING
    uint instanceIndex = flw_baseInstance;
    #else
    uint instanceIndex = _flw_instanceIndices[flw_baseInstance + gl_InstanceID];
    #endif

    FlwInstance instance = _flw_unpackInstance(instanceIndex);

    _flw_main(instance, instanceIndex);
}
