uniform sampler3D _flw_lightVolume;

in vec3 _flw_lightVolumeCoord;

void flw_beginFragment() {
    flw_fragLight = max(flw_fragLight, texture(_flw_lightVolume, _flw_lightVolumeCoord).rg);
}

void flw_endFragment() {
}
