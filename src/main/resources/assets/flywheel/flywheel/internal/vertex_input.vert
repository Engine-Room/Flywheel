in vec3 _flw_a_pos;
in vec4 _flw_a_color;
in vec2 _flw_a_texCoord;
in ivec2 _flw_a_overlay;
in vec2 _flw_a_light;
in vec3 _flw_a_normal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_a_pos, 1.0);
    flw_vertexColor = _flw_a_color;
    flw_vertexTexCoord = _flw_a_texCoord;
    flw_vertexOverlay = _flw_a_overlay;
    flw_vertexLight = _flw_a_light / 256.0;
    flw_vertexNormal = _flw_a_normal;
}
