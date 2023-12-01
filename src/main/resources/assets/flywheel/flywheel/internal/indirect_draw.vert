#include "flywheel:internal/indirect_draw_command.glsl"

out vec4 flw_vertexPos;
out vec4 flw_vertexColor;
out vec2 flw_vertexTexCoord;
flat out ivec2 flw_vertexOverlay;
out vec2 flw_vertexLight;
out vec3 flw_vertexNormal;

out float flw_distance;

out vec4 flw_var0;
out vec4 flw_var1;
out vec4 flw_var2;
out vec4 flw_var3;

uint _flw_materialVertexID;
flat out uint _flw_materialFragmentID;
flat out uint _flw_packedMaterialProperties;

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

FlwInstance _flw_unpackInstance(FlwPackedInstance i);

void flw_layoutVertex();
void flw_initVertex();
void flw_instanceVertex(FlwInstance i);
void flw_materialVertex();
void flw_contextVertex();

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
