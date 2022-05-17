float spherical_distance(vec3 relativePos) {
    return length(relativePos);
}

float cylindrical_distance(vec3 relativePos) {
    float distXZ = length(relativePos.xz);
    float distY = abs(relativePos.y);
    return max(distXZ, distY);
}

float fog_distance(vec3 relativePos, int fogShape) {
    if (fogShape == 0) {
        return spherical_distance(relativePos);
    } else {
        return cylindrical_distance(relativePos);
    }
}

float fog_distance(vec3 worldPos, vec3 cameraPos, int fogShape) {
    return fog_distance(worldPos - cameraPos, fogShape);
}

vec4 linear_fog(vec4 color, float distance, float fogStart, float fogEnd, vec4 fogColor) {
    if (distance <= fogStart) {
        return color;
    }

    float fogValue = distance < fogEnd ? smoothstep(fogStart, fogEnd, distance) : 1.0;
    return vec4(mix(color.rgb, fogColor.rgb, fogValue * fogColor.a), color.a);
}

vec4 linear_fog_fade(vec4 color, float distance, float fogStart, float fogEnd) {
    if (distance <= fogStart) {
        return color;
    } else if (distance >= fogEnd) {
        return vec4(0.0);
    }

    return color * smoothstep(fogEnd, fogStart, distance);
}
