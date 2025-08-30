#include "flywheel:api/material.glsl"
#include "flywheel:api/common.glsl"

vec4 flw_vertexPos;
vec4 flw_vertexColor;
vec2 flw_vertexTexCoord;
ivec2 flw_vertexOverlay;
vec2 flw_vertexLight;
vec3 flw_vertexNormal;

/*const*/ FlwMaterial flw_material;

// To be implemented by the instance shader.
void flw_instanceVertex(FlwInstance i);

// To be implemented by the instance cull shader.
void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius);

// To be implemented by the material vertex shader.
void flw_materialVertex();
