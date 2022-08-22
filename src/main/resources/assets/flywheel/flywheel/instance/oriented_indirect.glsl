#use "flywheel:api/vertex.glsl"
#use "flywheel:util/quaternion.glsl"
#use "flywheel:util/types.glsl"

#define FLW_INSTANCE_STRUCT Instance
struct Instance {
    Vec4F rotation;
    Vec3F pos;
    Vec3F pivot;
    uint light;
    uint color;
};

void flw_transformBoundingSphere(in Instance i, inout vec3 center, inout float radius) {
    vec4 rotation = unpackVec4F(i.rotation);
    vec3 pivot = unpackVec3F(i.pivot);
    vec3 pos = unpackVec3F(i.pos);

    center = rotateVertexByQuat(center - pivot, rotation) + pivot + pos;
}

#ifdef VERTEX_SHADER
void flw_instanceVertex(Instance i) {
    vec4 rotation = unpackVec4F(i.rotation);
    vec3 pivot = unpackVec3F(i.pivot);
    vec3 pos = unpackVec3F(i.pos);
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - pivot, rotation) + pivot + pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, rotation);
    flw_vertexColor = unpackUnorm4x8(i.color);
    flw_vertexLight = vec2(float((i.light >> 16) & 0xFFFFu), float(i.light & 0xFFFFu)) / 15.0;
}
    #endif
