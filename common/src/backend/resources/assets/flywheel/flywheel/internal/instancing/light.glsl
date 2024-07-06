#include "flywheel:internal/light_lut.glsl"

uniform usamplerBuffer _flw_lightLut;
uniform usamplerBuffer _flw_lightSections;

uint _flw_indexLut(uint index) {
    return texelFetch(_flw_lightLut, int(index)).r;
}

uint _flw_indexLight(uint index) {
    return texelFetch(_flw_lightSections, int(index)).r;
}
