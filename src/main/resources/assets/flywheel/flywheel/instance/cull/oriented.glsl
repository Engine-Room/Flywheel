#include "flywheel:util/quaternion.glsl"

void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {
    vec4 rotation = i.rotation;
    vec3 pivot = i.pivot;
    vec3 pos = i.position;

    center = rotateVertexByQuat(center - pivot, rotation) + pivot + pos;
}
