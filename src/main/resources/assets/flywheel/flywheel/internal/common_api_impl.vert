#include "flywheel:internal/material.glsl"
#include "flywheel:internal/uniforms/frame.glsl"
#include "flywheel:internal/uniforms/fog.glsl"

// TODO: can we combine some of these internally to use fewer in/out slots?
out vec4 flw_vertexPos;
out vec4 flw_vertexColor;
out vec2 flw_vertexTexCoord;
flat out ivec2 flw_vertexOverlay;
out vec2 flw_vertexLight;
out vec3 flw_vertexNormal;

out float flw_distance;

bool flw_vertexDiffuse;

FlwMaterial flw_material;
