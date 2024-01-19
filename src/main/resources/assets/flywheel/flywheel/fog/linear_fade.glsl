vec4 linearFogFade(vec4 color, float distance, float fogStart, float fogEnd) {
    if (distance <= fogStart) {
        return color;
    } else if (distance >= fogEnd) {
        return vec4(0.0);
    }

    return color * smoothstep(fogEnd, fogStart, distance);
}

vec4 flw_fogFilter(vec4 color) {
    return linearFogFade(color, flw_distance, flw_fogRange.x, flw_fogRange.y);
}
