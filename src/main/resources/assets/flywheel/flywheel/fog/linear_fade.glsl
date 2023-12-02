#include "flywheel:util/fog.glsl"

vec4 flw_fogFilter(vec4 color) {
    return linear_fog_fade(color, flw_distance, flywheel.fogRange.x, flywheel.fogRange.y);
}
