#use "flywheel:api/vertex.glsl"
#use "flywheel:util/light.glsl"

struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

void flw_instanceVertex(Instance instance) {
    flw_vertexPos = instance.transform * flw_vertexPos;
    flw_vertexNormal = instance.normalMat * flw_vertexNormal;
    flw_vertexColor = instance.color;
    flw_vertexLight = shiftLight(instance.light);
}
