#use "flywheel:api/vertex.glsl"
#use "flywheel:util/types.glsl"

void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {
    mat4 pose = i.pose;
    center = (pose * vec4(center, 1.0)).xyz;

    float scale = max(length(pose[0].xyz), max(length(pose[1].xyz), length(pose[2].xyz)));
    radius *= scale;
}

    #ifdef VERTEX_SHADER
void flw_instanceVertex(in FlwInstance i) {
    flw_vertexPos = i.pose * flw_vertexPos;
    flw_vertexNormal = i.normal * flw_vertexNormal;
    flw_vertexColor = i.color;
    flw_vertexLight = i.light / 15.0;
}
    #endif
