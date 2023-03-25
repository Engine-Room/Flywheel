#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"

void flw_materialFragment() {
}

#define FLW_DISCARD
bool flw_discardPredicate(vec4 finalColor) {
    return finalColor.a < 0.1;
}

vec4 flw_fogFilter(vec4 color) {
    return linear_fog(color, flw_distance, flywheel.fogRange.x, flywheel.fogRange.y, flywheel.fogColor);
}
