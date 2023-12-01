vec4 flw_vertexPos;
vec4 flw_vertexColor;
vec2 flw_vertexTexCoord;
ivec2 flw_vertexOverlay;
vec2 flw_vertexLight;
vec3 flw_vertexNormal;

float flw_distance;

vec4 flw_var0;
vec4 flw_var1;
vec4 flw_var2;
vec4 flw_var3;

// To be implemented by the layout shader.
void flw_layoutVertex();

// To be implemented by the instance shader.
void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius);
void flw_instanceVertex(FlwInstance i);

// To be implemented by material shaders.
void flw_materialVertex();

// To be implemented by the context shader.
void flw_initVertex();
void flw_contextVertex();
