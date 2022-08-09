#use "flywheel:api/vertex.glsl"
#use "flywheel:util/quaternion.glsl"

#define FLW_INSTANCE_STRUCT Instance
struct Instance {
    vec4 rotation;
    vec3 pos;
    vec3 pivot;
    uint light;
    uint color;
};

void flw_transformBoundingSphere(in Instance i, inout vec3 center, inout float radius) {
    center = rotateVertexByQuat(center - i.pivot, i.rotation) + i.pivot + i.pos;
}

#ifdef VERTEX_SHADER
void flw_instanceVertex(Instance i) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - i.pivot, i.rotation) + i.pivot + i.pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, i.rotation);
    flw_vertexColor = unpackUnorm4x8(i.color);
    flw_vertexLight = vec2(float((i.light >> 16) & 0xFFFFu), float(i.light & 0xFFFFu)) / 15.0;
}
#endif
