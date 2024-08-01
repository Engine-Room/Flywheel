void flw_shaderLight() {
    #ifdef FLW_EMBEDDED
    vec3 light;
    if (flw_light(flw_vertexPos.xyz, flw_vertexNormal, light)) {
        flw_fragLight = max(flw_fragLight, light.xy);

        flw_fragColor *= light.z;
    }
    #endif
}
