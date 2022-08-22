#ifdef FRAGMENT_SHADER
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

//

/*const*/ vec4 flw_sampleColor;

vec4 flw_fragColor;
ivec2 flw_fragOverlay;
vec2 flw_fragLight;

/*
 * Must be implemented by materials.
 */
vec4 flw_fogFilter(vec4 color);

/*
 * May be implemented by materials.
 * If implemented, a material must define FLW_DISCARD
 *
 * Guard calls with FLW_DISCARD
 */
bool flw_discardPredicate(vec4 finalColor);
#endif
