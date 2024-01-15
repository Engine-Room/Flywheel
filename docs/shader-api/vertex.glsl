#include "flywheel:api/material.glsl"

vec4 flw_vertexPos;
vec4 flw_vertexColor;
vec2 flw_vertexTexCoord;
ivec2 flw_vertexOverlay;
vec2 flw_vertexLight;
vec3 flw_vertexNormal;

bool flw_vertexDiffuse;

/*const*/ FlwMaterial flw_material;

// To be implemented by the instance shader.
void flw_instanceVertex(FlwInstance i);

// To be implemented by the instance cull shader.
void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius);

// To be implemented by the material vertex shader.
void flw_materialVertex();

// To be implemented by the context shader.
void flw_beginVertex();
void flw_endVertex();
