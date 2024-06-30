#include "flywheel:internal/common.frag"

uniform uvec4 _flw_packedMaterial;

#ifdef _FLW_EMBEDDED
bool _flw_embeddedLight(vec3 worldPos, out vec2 lightCoord) {
    return true;
}
#endif

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.y;
    _flw_unpackUint2x16(_flw_packedMaterial.z, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    _flw_main();
}
