#include "flywheel:api/vertex.glsl"
#include "flywheel:internal/indirect_draw_command.glsl"

layout(std430, binding = 0) restrict readonly buffer ObjectBuffer {
    FlwPackedInstance objects[];
};

layout(std430, binding = 1) restrict readonly buffer TargetBuffer {
    uint objectIDs[];
};

layout(std430, binding = 2) restrict readonly buffer BatchBuffer {
    uint batchIDs[];
};

layout(std430, binding = 3) restrict readonly buffer DrawCommands {
    MeshDrawCommand drawCommands[];
};

void main() {
    uint instanceIndex = objectIDs[gl_BaseInstance + gl_InstanceID];
    uint batchID = batchIDs[instanceIndex];
    FlwInstance i = _flw_unpackInstance(objects[instanceIndex]);

    _flw_materialVertexID = drawCommands[batchID].vertexMaterialID;
    _flw_materialFragmentID = drawCommands[batchID].fragmentMaterialID;

    flw_layoutVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
