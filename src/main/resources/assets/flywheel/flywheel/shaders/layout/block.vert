#use "flywheel:api/vertex.glsl"
#use "flywheel:util/light.glsl"

layout(location = 0) in vec3 _flw_v_pos;
layout(location = 1) in vec4 _flw_v_color;
layout(location = 2) in vec2 _flw_v_texCoord;
layout(location = 3) in vec2 _flw_v_light;
layout(location = 4) in vec3 _flw_v_normal;

void flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_v_pos, 1.0);
    flw_vertexColor = _flw_v_color;
    flw_vertexTexCoord = _flw_v_texCoord;
    flw_vertexLight = shiftLight(_flw_v_light);
    flw_vertexNormal = _flw_v_normal;
}
