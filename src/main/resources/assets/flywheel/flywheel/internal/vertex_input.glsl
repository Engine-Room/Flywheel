#include "flywheel:api/vertex.glsl"

layout(location = 0) in vec3 _flw_pos;
layout(location = 1) in vec4 _flw_color;
layout(location = 2) in vec2 _flw_texCoord;
layout(location = 3) in ivec2 _flw_overlay;
layout(location = 4) in ivec2 _flw_light;
layout(location = 5) in vec3 _flw_normal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_pos, 1.0);
    flw_vertexColor = _flw_color;
    flw_vertexTexCoord = _flw_texCoord;
    flw_vertexOverlay = _flw_overlay;
    flw_vertexLight = _flw_light / 15.0;
    flw_vertexNormal = _flw_normal;
}
