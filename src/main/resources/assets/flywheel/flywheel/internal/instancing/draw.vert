#include "flywheel:internal/instancing/api/vertex.glsl"
#include "flywheel:internal/material.glsl"

void main() {
    _flw_materialVertexID = _flw_material_instancing.x;
    _flw_materialFragmentID = _flw_material_instancing.y;

    _flw_unpackMaterial(_flw_material_instancing.z, flw_material);

    FlwInstance i = _flw_unpackInstance();

    flw_layoutVertex();
    flw_initVertex();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
