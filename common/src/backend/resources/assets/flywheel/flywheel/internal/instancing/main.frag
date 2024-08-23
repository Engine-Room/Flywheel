#include "flywheel:internal/common.frag"
#include "flywheel:internal/instancing/light.glsl"

uniform uvec4 _flw_packedMaterial;

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.y;
    _flw_unpackUint2x16(_flw_packedMaterial.z, _flw_uberFogIndex, _flw_uberCutoutIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    _flw_main();
}
