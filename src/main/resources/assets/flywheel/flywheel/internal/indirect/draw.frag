#include "flywheel:internal/indirect/api/fragment.glsl"

flat in uvec2 _flw_material;

void main() {
    _flw_materialFragmentID = _flw_material.x;
    _flw_packedMaterialProperties = _flw_material.y;

    flw_initFragment();
    flw_materialFragment();
    flw_contextFragment();
}
