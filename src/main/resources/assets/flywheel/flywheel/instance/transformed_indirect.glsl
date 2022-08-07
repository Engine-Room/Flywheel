#use "flywheel:api/vertex.glsl"
#define FLW_INSTANCE_STRUCT Instance

struct Instance {
    mat4 pose;
    mat3 normal;
    uint color;
    uint light;
};

void flw_transformBoundingSphere(in Instance i, inout vec3 center, inout float radius) {
    center = (i.pose * vec4(center, 1.0)).xyz;
}

#ifdef VERTEX_SHADER
void flw_instanceVertex(Instance i) {
    flw_vertexPos = i.pose * flw_vertexPos;
    flw_vertexNormal = i.normal * flw_vertexNormal;
    flw_vertexColor = unpackUnorm4x8(i.color);
    flw_vertexLight = vec2(float((i.light >> 16) & 0xFFFFu), float(i.light & 0xFFFFu)) / 15.0;
}
#endif
