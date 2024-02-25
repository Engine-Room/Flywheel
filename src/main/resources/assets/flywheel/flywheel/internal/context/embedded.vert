uniform vec3 _flw_oneOverLightBoxSize;
uniform vec3 _flw_lightVolumeMin;
uniform mat4 _flw_model;
uniform mat3 _flw_normal;

out vec3 _flw_lightVolumeCoord;

void flw_beginVertex() {
}

void flw_endVertex() {
    _flw_lightVolumeCoord = (flw_vertexPos.xyz - _flw_lightVolumeMin) * _flw_oneOverLightBoxSize;

    flw_vertexPos = _flw_model * flw_vertexPos;
    flw_vertexNormal = _flw_normal * flw_vertexNormal;
}
