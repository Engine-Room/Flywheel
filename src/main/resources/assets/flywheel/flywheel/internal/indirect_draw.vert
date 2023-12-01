#include "flywheel:api/vertex.glsl"
#include "flywheel:internal/indirect_draw_command.glsl"

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
    _flw_packedMaterialProperties = drawCommands[batchID].packedMaterialProperties;

    flw_layoutVertex();
    flw_initVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
