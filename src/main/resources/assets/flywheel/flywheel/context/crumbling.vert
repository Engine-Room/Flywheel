#include "flywheel:api/vertex.glsl"
#include "flywheel:util/fog.glsl"

out vec2 flw_crumblingTexCoord;

vec3 tangent(vec3 normal) {
    float sinYRot = -normal.x;
    vec2 XZ = normal.xz;
    float sqLength = dot(XZ, XZ);
    if (sqLength > 0) {
        sinYRot *= inversesqrt(sqLength);
        sinYRot = clamp(sinYRot, -1, 1);
    }

    return vec3(sqrt(1 - sinYRot * sinYRot) * (normal.z < 0 ? -1 : 1), 0, sinYRot);
}

vec2 flattenedPos(vec3 pos, vec3 normal) {
    pos -= vec3(0.5);

    vec3 tangent = tangent(normal);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);

    // transpose is the same as inverse for orthonormal matrices
    return (transpose(tbn) * pos).xy + vec2(0.5);
}

void flw_initVertex() {
    flw_crumblingTexCoord = flattenedPos(flw_vertexPos.xyz, flw_vertexNormal);
}

void flw_contextVertex() {
    flw_distance = fog_distance(flw_vertexPos.xyz, flywheel.cameraPos.xyz, flywheel.fogShape);
    gl_Position = flywheel.viewProjection * flw_vertexPos;
    flw_vertexNormal = normalize(flw_vertexNormal);
}
