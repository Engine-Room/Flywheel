void flw_shaderLight() {
    vec2 embeddedLight;
    if (flw_lightFetch(ivec3(floor(flw_vertexPos.xyz)) + flw_renderOrigin, embeddedLight)) {
        flw_fragLight = max(flw_fragLight, embeddedLight);
    }
}
