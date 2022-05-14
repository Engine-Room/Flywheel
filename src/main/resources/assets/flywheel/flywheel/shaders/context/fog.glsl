uniform vec4 uFogColor;
uniform vec2 uFogRange;
uniform int uFogShape;

#ifdef VERTEX_SHADER
out float fragDistance;
#elif defined(FRAGMENT_SHADER)
in float fragDistance;
#endif

float spherical_distance(vec3 relativePos) {
    return length(relativePos);
}

float cylindrical_distance(vec3 relativePos) {
    float distXZ = length(relativePos.xz);
    float distY = abs(relativePos.y);
    return max(distXZ, distY);
}

float fog_distance(vec3 relativePos) {
    if (uFogShape == 0) {
        return spherical_distance(relativePos);
    } else {
        return cylindrical_distance(relativePos);
    }
}

float fog_distance(vec3 worldPos, vec3 cameraPos) {
    return fog_distance(worldPos - cameraPos);
}

vec4 linear_fog(vec4 color) {
    if (fragDistance <= uFogRange.x) {
        return color;
    }

    float fogValue = fragDistance < uFogRange.y ? smoothstep(uFogRange.x, uFogRange.y, fragDistance) : 1.0;
    return vec4(mix(color.rgb, uFogColor.rgb, fogValue * uFogColor.a), color.a);
}

vec4 linear_fog_fade(vec4 color) {
    if (fragDistance <= uFogRange.x) {
        return color;
    } else if (fragDistance >= uFogRange.y) {
        return vec4(0.0);
    }

    return color * smoothstep(uFogRange.y, uFogRange.x, fragDistance);
}
