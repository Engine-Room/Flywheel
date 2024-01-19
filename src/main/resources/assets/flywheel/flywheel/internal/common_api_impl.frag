#include "flywheel:internal/material.glsl"
#include "flywheel:internal/uniforms/frame.glsl"
#include "flywheel:internal/uniforms/fog.glsl"

in vec4 flw_vertexPos;
in vec4 flw_vertexColor;
in vec2 flw_vertexTexCoord;
flat in ivec2 flw_vertexOverlay;
in vec2 flw_vertexLight;
in vec3 flw_vertexNormal;

in float flw_distance;

bool flw_vertexDiffuse;

vec4 flw_sampleColor;

FlwMaterial flw_material;

bool flw_fragDiffuse;
vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;
