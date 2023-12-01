#include "flywheel:internal/indirect/api/fragment.glsl"
#include "flywheel:internal/material.glsl"

flat in uvec2 _flw_material;

void main() {
    _flw_materialFragmentID = _flw_material.x;

    _flw_unpackMaterial(_flw_material.y, flw_material);

    flw_initFragment();
    flw_materialFragment();
    flw_contextFragment();
}
