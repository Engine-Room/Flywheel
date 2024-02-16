layout(location = 0) in vec3 _flw_a_pos;
layout(location = 1) in vec4 _flw_a_color;
layout(location = 2) in vec2 _flw_a_texCoord;
layout(location = 3) in ivec4 _flw_a_overlay_light;
layout(location = 4) in vec3 _flw_a_normal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_a_pos, 1.0);
    flw_vertexColor = _flw_a_color;
    flw_vertexTexCoord = _flw_a_texCoord;
    flw_vertexOverlay = _flw_a_overlay_light.xy;
    flw_vertexLight = (_flw_a_overlay_light.zw >> 4) / 15.0;
    flw_vertexNormal = _flw_a_normal;
}
