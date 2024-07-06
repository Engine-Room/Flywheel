void flw_materialFragment() {
    #ifdef FLW_EMBEDDED
    vec2 embeddedLight;
    if (flw_light(flw_vertexPos.xyz, embeddedLight)) {
        flw_fragLight = max(flw_fragLight, embeddedLight);
    }
    #endif
}
