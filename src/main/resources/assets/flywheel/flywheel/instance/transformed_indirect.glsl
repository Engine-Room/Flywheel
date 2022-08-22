#use "flywheel:api/vertex.glsl"
#use "flywheel:util/types.glsl"

#define FLW_INSTANCE_STRUCT Instance
struct Instance {
    Mat4F pose;
    Mat3F normal;
    uint color;
    uint light;
};

void flw_transformBoundingSphere(in Instance i, inout vec3 center, inout float radius) {
    mat4 pose = unpackMat4F(i.pose);
    center = (pose * vec4(center, 1.0)).xyz;

    float scale = max(length(pose[0].xyz), max(length(pose[1].xyz), length(pose[2].xyz)));
    radius *= scale;
}

    #ifdef VERTEX_SHADER
void flw_instanceVertex(Instance i) {
    flw_vertexPos = unpackMat4F(i.pose) * flw_vertexPos;
    flw_vertexNormal = unpackMat3F(i.normal) * flw_vertexNormal;
    flw_vertexColor = unpackUnorm4x8(i.color);
    flw_vertexLight = vec2(float((i.light >> 16) & 0xFFFFu), float(i.light & 0xFFFFu)) / 15.0;
}
    #endif
