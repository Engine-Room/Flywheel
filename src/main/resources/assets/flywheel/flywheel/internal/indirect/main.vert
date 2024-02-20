#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"
#include "flywheel:internal/indirect/object.glsl"

layout(std430, binding = _FLW_OBJECT_BUFFER_BINDING) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = _FLW_TARGET_BUFFER_BINDING) restrict readonly buffer TargetBuffer {
    uint objectIndices[];
};

layout(std430, binding = _FLW_DRAW_BUFFER_BINDING) restrict readonly buffer DrawBuffer {
    MeshDrawCommand drawCommands[];
};

uniform uint _flw_baseDraw;

flat out uvec3 _flw_packedMaterial;

void main() {
    uint drawIndex = gl_DrawID + _flw_baseDraw;
    MeshDrawCommand draw = drawCommands[drawIndex];

    _flw_uberMaterialVertexIndex = draw.materialVertexIndex;
    uint packedMaterialProperties = draw.packedMaterialProperties;
    _flw_unpackMaterialProperties(packedMaterialProperties, flw_material);
    _flw_packedMaterial = uvec3(draw.materialFragmentIndex, draw.packedFogAndCutout, packedMaterialProperties);

    uint objectIndex = objectIndices[gl_BaseInstance + gl_InstanceID];
    FlwInstance instance = _flw_unpackInstance(objects[objectIndex].instance);

    _flw_main(instance, objectIndex);
}
