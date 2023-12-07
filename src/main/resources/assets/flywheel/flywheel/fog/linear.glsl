vec4 linearFog(vec4 color, float distance, float fogStart, float fogEnd, vec4 fogColor) {
    if (distance <= fogStart) {
        return color;
    }

    float fogValue = distance < fogEnd ? smoothstep(fogStart, fogEnd, distance) : 1.0;
    return vec4(mix(color.rgb, fogColor.rgb, fogValue * fogColor.a), color.a);
}

vec4 flw_fogFilter(vec4 color) {
    return linearFog(color, flw_distance, flywheel.fogRange.x, flywheel.fogRange.y, flywheel.fogColor);
}
