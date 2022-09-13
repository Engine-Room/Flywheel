#use "flywheel:api/vertex.glsl"
#use "flywheel:util/quaternion.glsl"
#use "flywheel:util/types.glsl"

void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {
    vec4 rotation = i.rotation;
    vec3 pivot = i.pivot;
    vec3 pos = i.position;

    center = rotateVertexByQuat(center - pivot, rotation) + pivot + pos;
}

    #ifdef VERTEX_SHADER
void flw_instanceVertex(in FlwInstance i) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - i.pivot, i.rotation) + i.pivot + i.position, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, i.rotation);
    flw_vertexColor = i.color;
    flw_vertexLight = i.light / 15.0;
}
    #endif
