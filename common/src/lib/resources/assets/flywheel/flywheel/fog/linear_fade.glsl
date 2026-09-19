vec4 linearFogFade(vec4 color, float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd) {
    float sphericalValue = smoothstep(environmentalEnd, environmentalStart, sphericalVertexDistance);
    float cylindricalValue = smoothstep(renderDistanceEnd, renderDistanceStart, cylindricalVertexDistance);
    float fadeValue = min(sphericalValue, cylindricalValue);

    return color * fadeValue;
}

vec4 flw_fogFilter(vec4 color) {
    return linearFogFade(color, flw_sphericalDistance, flw_cylindricalDistance, flw_fogEnvironmentalStart, flw_fogEnvironmentalEnd, flw_fogRenderDistanceStart, flw_fogRenderDistanceEnd);
}
