#include "flywheel:internal/indirect/api/vertex.glsl"
#include "flywheel:internal/indirect/mesh.glsl"
#include "flywheel:internal/material.glsl"
#include "flywheel:util/diffuse.glsl"

flat out uvec2 _flw_material;

struct Object {
    uint batchID;
    FlwPackedInstance instance;
};

layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    Object objects[];
};

layout(std430, binding = 1) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = 2) restrict readonly buffer DrawCommands {
    MeshDrawCommand drawCommands[];
};

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    uint batchID = objects[instanceIndex].batchID;
    FlwInstance i = _flw_unpackInstance(objects[instanceIndex].instance);

    _flw_materialVertexID = drawCommands[batchID].vertexMaterialID;
    _flw_materialFragmentID = drawCommands[batchID].fragmentMaterialID;
    uint p = drawCommands[batchID].packedMaterialProperties;

    _flw_unpackMaterial(p, flw_material);
    _flw_material = uvec2(_flw_materialFragmentID, p);

    flw_layoutVertex();
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
