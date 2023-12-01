#include "flywheel:api/vertex.glsl"

uniform uvec3 _flw_material_instancing;

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
