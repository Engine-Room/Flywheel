void flw_shaderLight() {
    vec3 light;
    if (flw_light(flw_vertexPos.xyz, flw_vertexNormal, light)) {
        flw_fragLight = max(flw_fragLight, light.xy);

        flw_fragColor.rgb *= light.z;
    }
}
