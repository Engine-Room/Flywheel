// API
// -----------------------------------------
#include "flywheel:api/material.glsl"

in vec4 flw_vertexPos;
in vec4 flw_vertexColor;
in vec2 flw_vertexTexCoord;
flat in ivec2 flw_vertexOverlay;
in vec2 flw_vertexLight;
in vec3 flw_vertexNormal;

in float flw_distance;

in vec4 flw_var0;
in vec4 flw_var1;
in vec4 flw_var2;
in vec4 flw_var3;

vec4 flw_sampleColor;

vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;

FlwMaterial flw_material;

vec4 flw_fogFilter(vec4 color);

bool flw_discardPredicate(vec4 finalColor);

void flw_beginFragment();
void flw_materialFragment();
void flw_endFragment();

// -----------------------------------------
// INTERNAL
// -----------------------------------------

uint _flw_materialVertexID;
uint _flw_materialFragmentID;

uniform uvec3 _flw_material_instancing;

// -----------------------------------------
