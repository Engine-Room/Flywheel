#if defined(VERTEX_SHADER)
out float FragDistance;
#elif defined(FRAGMENT_SHADER)
in float FragDistance;
#endif
uniform vec4 uFogColor;
uniform vec2 uFogRange;

float cylindrical_distance(vec3 worldPos, vec3 cameraPos) {
    float distXZ = length(worldPos.xz - cameraPos.xz);
    float distY = abs(worldPos.y - cameraPos.y);
    return max(distXZ, distY);
}

float cylindrical_distance(vec3 worldPos) {
    float distXZ = length(worldPos.xz);
    float distY = abs(worldPos.y);
    return max(distXZ, distY);
}

float FLWFogFactor() {
    return (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
}
