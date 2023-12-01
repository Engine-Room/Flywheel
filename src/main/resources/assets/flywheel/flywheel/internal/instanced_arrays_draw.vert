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


uniform uvec3 _flw_material_instancing;

void flw_layoutVertex();
void flw_initVertex();
void flw_instanceVertex(FlwInstance i);
void flw_materialVertex();
void flw_contextVertex();

void main() {
    _flw_materialVertexID = _flw_material_instancing.x;
    _flw_materialFragmentID = _flw_material_instancing.y;
    _flw_packedMaterialProperties = _flw_material_instancing.z;

    FlwInstance i = _flw_unpackInstance();

    flw_layoutVertex();
    flw_initVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
