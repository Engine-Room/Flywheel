#include "flywheel:api/material.glsl"

/*const*/ vec4 flw_vertexPos;
/*const*/ vec4 flw_vertexColor;
/*const*/ vec2 flw_vertexTexCoord;
/*const*/ ivec2 flw_vertexOverlay;
/*const*/ vec2 flw_vertexLight;
/*const*/ vec3 flw_vertexNormal;

/*const*/ FlwMaterial flw_material;

/*const*/ vec4 flw_sampleColor;

/*const*/ float flw_distance;

vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;

// To be implemented by the material fragment shader.
void flw_materialFragment();
// To be implement by fog shaders.
vec4 flw_fogFilter(vec4 color);
// To be implemented by discard shaders.
bool flw_discardPredicate(vec4 finalColor);

// To be implemented by the context shader.
void flw_beginFragment();
void flw_endFragment();
