#include "flywheel:internal/common.frag"
#include "flywheel:internal/light_lut.glsl"
#include "flywheel:internal/indirect/buffer_bindings.glsl"

flat in uvec3 _flw_packedMaterial;

layout(std430, binding = _FLW_LIGHT_LUT_BINDING) restrict readonly buffer LightLut {
    uint _flw_lightLut[];
};

layout(std430, binding = _FLW_LIGHT_SECTIONS_BINDING) restrict readonly buffer LightSections {
    uint _flw_lightSections[];
};

uint _flw_indexLut(uint index) {
    return _flw_lightLut[index];
}

uint _flw_indexLight(uint index) {
    return _flw_lightSections[index];
}

void main() {
    _flw_uberMaterialFragmentIndex = _flw_packedMaterial.x;
    _flw_unpackUint2x16(_flw_packedMaterial.y, _flw_uberCutoutIndex, _flw_uberFogIndex);
    _flw_unpackMaterialProperties(_flw_packedMaterial.z, flw_material);

    _flw_main();
}
