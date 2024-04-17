float sphericalDistance(vec3 relativePos) {
    return length(relativePos);
}

float cylindricalDistance(vec3 relativePos) {
    float distXZ = length(relativePos.xz);
    float distY = abs(relativePos.y);
    return max(distXZ, distY);
}

float fogDistance(vec3 relativePos, int fogShape) {
    if (fogShape == 0) {
        return sphericalDistance(relativePos);
    } else {
        return cylindricalDistance(relativePos);
    }
}

float fogDistance(vec3 worldPos, vec3 cameraPos, int fogShape) {
    return fogDistance(worldPos - cameraPos, fogShape);
}
