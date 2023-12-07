#include "flywheel:internal/diffuse.glsl"
#include "flywheel:internal/fog_distance.glsl"
#include "flywheel:internal/vertex_input.glsl"
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

    _flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(instance);
    flw_materialVertex();
    flw_endVertex();

    flw_vertexNormal = normalize(flw_vertexNormal);

    if (flw_material.diffuse) {
        float diffuseFactor;
        if (flywheel.constantAmbientLight == 1) {
            diffuseFactor = diffuseNether(flw_vertexNormal);
        } else {
            diffuseFactor = diffuse(flw_vertexNormal);
        }
        flw_vertexColor = vec4(flw_vertexColor.rgb * diffuseFactor, flw_vertexColor.a);
    }

    flw_distance = fogDistance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
}
