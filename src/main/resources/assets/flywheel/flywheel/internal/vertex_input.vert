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
    // Need to clamp the overlay texture coords to sane coordinates because integer vertex attributes explode on
    // some drivers for some draw calls. This should only effect instances that don't write to overlay, but
    // the internal vertex format is unfortunately subject to these issues.
    flw_vertexOverlay = clamp(_flw_a_overlay, 0, 15);
    flw_vertexLight = _flw_a_light / 256.0;
    flw_vertexNormal = _flw_a_normal;
}
