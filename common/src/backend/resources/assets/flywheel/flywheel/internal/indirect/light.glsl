#include "flywheel:internal/light_lut.glsl"

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
