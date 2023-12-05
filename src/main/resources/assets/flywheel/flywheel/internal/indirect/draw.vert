#include "flywheel:internal/indirect/api/vertex.glsl"
#include "flywheel:internal/indirect/buffers.glsl"
#include "flywheel:internal/indirect/draw_command.glsl"
#include "flywheel:internal/indirect/object.glsl"
#include "flywheel:internal/material.glsl"
#include "flywheel:internal/vertex_input.glsl"
#include "flywheel:util/diffuse.glsl"

flat out uvec3 _flw_material;

layout(std430, binding = OBJECT_BINDING) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = TARGET_BINDING) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = DRAW_BINDING) restrict readonly buffer DrawCommands {
    MeshDrawCommand drawCommands[];
};

uniform uint _flw_baseDraw;

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    uint batchID = gl_DrawID + _flw_baseDraw;
    FlwInstance i = _flw_unpackInstance(objects[instanceIndex].instance);

    _flw_materialVertexID = drawCommands[batchID].vertexMaterialID;
    uint p = drawCommands[batchID].packedMaterialProperties;

    _flw_unpackMaterialProperties(p, flw_material);
    _flw_material = uvec3(drawCommands[batchID].fragmentMaterialID, drawCommands[batchID].packedFogAndCutout, p);

    _flw_layoutVertex();
    flw_beginVertex();
    flw_instanceVertex(i);
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

    flw_distance = fog_distance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
}
