#include "flywheel:api/material.glsl"

/*const*/ vec4 flw_vertexPos;
/*const*/ vec4 flw_vertexColor;
/*const*/ vec2 flw_vertexTexCoord;
/*const*/ ivec2 flw_vertexOverlay;
/*const*/ vec2 flw_vertexLight;
/*const*/ vec3 flw_vertexNormal;

/*const*/ float flw_distance;

/*const*/ vec4 flw_var0;
/*const*/ vec4 flw_var1;
/*const*/ vec4 flw_var2;
/*const*/ vec4 flw_var3;

/*const*/ vec4 flw_sampleColor;

/*const*/ FlwMaterial flw_material;

vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;

// To be implemented by material shaders.
vec4 flw_fogFilter(vec4 color);
bool flw_discardPredicate(vec4 finalColor);
void flw_materialFragment();

// To be implemented by the context shader.
void flw_initFragment();
void flw_contextFragment();
