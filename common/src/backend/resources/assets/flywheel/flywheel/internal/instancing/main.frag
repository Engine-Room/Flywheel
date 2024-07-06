#include "flywheel:internal/common.frag"
#include "flywheel:internal/light_lut.glsl"

uniform uvec4 _flw_packedMaterial;

uniform usamplerBuffer _flw_lightLut;
uniform usamplerBuffer _flw_lightSections;

uint _flw_indexLut(uint index) {
    return texelFetch(_flw_lightLut, int(index)).r;
}

uint _flw_indexLight(uint index) {
    return texelFetch(_flw_lightSections, int(index)).r;
}

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.y;
    _flw_unpackUint2x16(_flw_packedMaterial.z, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.w, flw_material);

    _flw_main();
}
