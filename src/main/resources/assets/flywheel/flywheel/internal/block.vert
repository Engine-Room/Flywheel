#include "flywheel:api/vertex.glsl"

layout(location = 0) in vec3 v_pos;
layout(location = 1) in vec4 v_color;
layout(location = 2) in vec2 v_texCoord;
layout(location = 3) in ivec2 v_light;
layout(location = 4) in vec3 v_normal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(v_pos, 1.0);
    flw_vertexColor = v_color;
    flw_vertexTexCoord = v_texCoord;
    flw_vertexOverlay = ivec2(0, 10);
    flw_vertexLight = v_light / 15.0;
    flw_vertexNormal = v_normal;
}
