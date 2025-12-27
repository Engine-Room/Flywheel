float sphericalDistance(vec3 worldPos, vec3 cameraPos) {
    return length(worldPos - cameraPos);
}

float cylindricalDistance(vec3 worldPos, vec3 cameraPos) {
    vec3 relativePos = worldPos - cameraPos;
    float distXZ = length(relativePos.xz);
    float distY = abs(relativePos.y);
    return max(distXZ, distY);
}
