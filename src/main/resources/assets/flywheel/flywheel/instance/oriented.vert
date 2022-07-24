#use "flywheel:api/vertex.glsl"
#use "flywheel:util/quaternion.glsl"

layout(location = 0) in ivec2 oriented_light;
layout(location = 1) in vec4 oriented_color;
layout(location = 2) in vec3 oriented_pos;
layout(location = 3) in vec3 oriented_pivot;
layout(location = 4) in vec4 oriented_rotation;

void flw_instanceVertex() {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - oriented_pivot, oriented_rotation) + oriented_pivot + oriented_pos, 1.0);
    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, oriented_rotation);
    flw_vertexColor = oriented_color;
    flw_vertexLight = oriented_light / 15.0;
}
