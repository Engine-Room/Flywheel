#include "flywheel:api/vertex.glsl"

layout(location = 0) in vec3 v_pos;
layout(location = 1) in vec2 v_texCoord;
layout(location = 2) in vec3 v_normal;

void flw_layoutVertex() {
    flw_vertexPos = vec4(v_pos, 1.0);
    flw_vertexColor = vec4(1.0);
    flw_vertexTexCoord = v_texCoord;
    flw_vertexOverlay = ivec2(0, 10);
    flw_vertexLight = vec2(1.0);
    flw_vertexNormal = v_normal;
}
