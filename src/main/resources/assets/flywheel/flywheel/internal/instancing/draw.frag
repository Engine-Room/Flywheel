#include "flywheel:internal/instancing/api/fragment.glsl"
#include "flywheel:internal/material.glsl"

void main() {
    _flw_materialVertexID = _flw_material_instancing.x;
    _flw_materialFragmentID = _flw_material_instancing.y;

    _flw_unpackMaterial(_flw_material_instancing.z, flw_material);

    flw_initFragment();
    flw_materialFragment();
    flw_contextFragment();
}
