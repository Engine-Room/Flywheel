void flw_shaderLight() {
    vec2 embeddedLight;
    if (flw_light(flw_vertexPos.xyz, flw_vertexNormal, embeddedLight)) {
        flw_fragLight = max(flw_fragLight, embeddedLight);
    }
}
