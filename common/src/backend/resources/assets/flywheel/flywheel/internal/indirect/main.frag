#include "flywheel:internal/common.frag"
#include "flywheel:internal/indirect/buffer_bindings.glsl"
#include "flywheel:internal/indirect/light.glsl"

flat in uvec3 _flw_packedMaterial;

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.x;
    _flw_unpackUint3x10(_flw_packedMaterial.y, _flw_uberFogIndex, _flw_uberCutoutIndex, _flw_uberLightIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.z, flw_material);

    _flw_main();
}
