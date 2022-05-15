#use "flywheel:api/vertex.glsl"

layout(location = 0) in vec3 _flw_v_pos;
layout(location = 1) in vec2 _flw_v_texCoord;
layout(location = 2) in vec3 _flw_v_normal;

void flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_v_pos, 1.0);
    flw_vertexColor = vec4(1.0);
    flw_vertexTexCoord = _flw_v_texCoord;
    flw_vertexLight = vec2(0.0);
    flw_vertexNormal = _flw_v_normal;
}
