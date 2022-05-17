#use "flywheel:api/vertex.glsl"
#use "flywheel:util/light.glsl"
#use "flywheel:util/quaternion.glsl"

struct Oriented {
    vec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
};

void flw_instanceVertex(Oriented oriented) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - oriented.pivot, oriented.rotation) + oriented.pivot + oriented.pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, oriented.rotation);
    flw_vertexColor = oriented.color;
    flw_vertexLight = shiftLight(oriented.light);
}
