#include "flywheel:internal/common.frag"

flat in uvec3 _flw_packedMaterial;

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.x;
    _flw_unpackUint2x16(_flw_packedMaterial.y, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.z, flw_material);

    _flw_main();
}
