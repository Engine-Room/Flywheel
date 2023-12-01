// API
// ------------------------------------
#include "flywheel:api/material.glsl"

out vec4 flw_vertexPos;
out vec4 flw_vertexColor;
out vec2 flw_vertexTexCoord;
flat out ivec2 flw_vertexOverlay;
out vec2 flw_vertexLight;
out vec3 flw_vertexNormal;

out float flw_distance;

out vec4 flw_var0;
out vec4 flw_var1;
out vec4 flw_var2;
out vec4 flw_var3;

FlwMaterial flw_material;

void flw_layoutVertex();
void flw_initVertex();
void flw_instanceVertex(FlwInstance i);
void flw_materialVertex();
void flw_contextVertex();

// ------------------------------------
// INTERNAL
// ------------------------------------

uint _flw_materialVertexID;
uint _flw_materialFragmentID;

FlwInstance _flw_unpackInstance();

uniform uvec3 _flw_material_instancing;

// ------------------------------------
